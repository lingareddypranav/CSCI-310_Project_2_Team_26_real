/**
 * Auto Migration System
 * Automatically runs database migrations on server startup
 * Safe to run multiple times - uses IF NOT EXISTS clauses
 */

const { query, getClient } = require('../config/database');
const fs = require('fs');
const path = require('path');

let migrationRun = false;

/**
 * Check if migrations have already been run
 * Returns true if tables exist, false otherwise
 */
async function checkTablesExist() {
  try {
    const result = await query(`
      SELECT COUNT(*) as count
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      AND table_name IN ('users', 'posts', 'comments', 'votes', 'profiles', 'tags')
    `);
    return parseInt(result.rows[0].count) >= 6; // All 6 tables exist
  } catch (error) {
    // If query fails, tables probably don't exist
    return false;
  }
}

/**
 * Parse SQL file into individual statements
 * Handles multi-line statements, functions, and triggers correctly
 */
function parseSQLStatements(sqlContent) {
  const statements = [];
  let currentStatement = '';
  let inFunction = false;
  let dollarQuoteTag = null;
  
  const lines = sqlContent.split('\n');
  
  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    const trimmed = line.trim();
    
    // Skip empty lines and comments
    if (trimmed === '' || trimmed.startsWith('--')) {
      continue;
    }
    
    currentStatement += line + '\n';
    
    // Detect function blocks with $$ delimiters
    if (trimmed.includes('CREATE OR REPLACE FUNCTION')) {
      inFunction = true;
      // Extract dollar quote tag (e.g., $$ or $tag$)
      const dollarMatch = trimmed.match(/\$[^$]*\$/);
      if (dollarMatch) {
        dollarQuoteTag = dollarMatch[0];
      } else {
        // Default to $$ if not found
        dollarQuoteTag = '$$';
      }
    }
    
    // Check for end of function block (look for $$ language 'plpgsql')
    if (inFunction && dollarQuoteTag && trimmed.includes(dollarQuoteTag)) {
      // Check if this line contains both the closing tag and 'language'
      if (trimmed.includes('language')) {
        inFunction = false;
        dollarQuoteTag = null;
      }
    }
    
    // If we have a semicolon and we're not in a function block, it's a complete statement
    if (trimmed.endsWith(';') && !inFunction) {
      const stmt = currentStatement.trim();
      if (stmt.length > 0 && !stmt.startsWith('--')) {
        statements.push(stmt);
      }
      currentStatement = '';
    }
  }
  
  // Add any remaining statement
  if (currentStatement.trim().length > 0) {
    statements.push(currentStatement.trim());
  }
  
  return statements;
}

/**
 * Run database migrations automatically
 * Safe to run on every server start - will skip if already done
 */
async function autoMigrate() {
  // Only run once per process
  if (migrationRun) {
    return { success: true, skipped: true, message: 'Migration already run in this process' };
  }

  // Check if auto-migration is disabled
  if (process.env.DISABLE_AUTO_MIGRATE === 'true') {
    console.log('⚠️  Auto-migration is disabled (DISABLE_AUTO_MIGRATE=true)');
    return { success: true, skipped: true, message: 'Auto-migration disabled' };
  }

  try {
    console.log('🔄 Checking database schema...');

    // Check if tables already exist
    const tablesExist = await checkTablesExist();
    
    // Always run incremental migrations (they check for column existence)
    const migrationsDir = path.join(__dirname, 'migrations');
    if (fs.existsSync(migrationsDir)) {
      const migrationFiles = fs.readdirSync(migrationsDir)
        .filter(file => file.endsWith('.sql'))
        .sort();
      
      if (migrationFiles.length > 0) {
        console.log('🔄 Running incremental migrations...');
        for (const file of migrationFiles) {
          const migrationPath = path.join(migrationsDir, file);
          console.log(`📝 Running migration: ${file}`);
          const migrationSQL = fs.readFileSync(migrationPath, 'utf8');
          const migrationStatements = parseSQLStatements(migrationSQL);
          
          for (const statement of migrationStatements) {
            if (statement.trim()) {
              try {
                await query(statement);
              } catch (error) {
                // Ignore "already exists" or "column already exists" errors
                if (error.message.includes('already exists') || 
                    error.message.includes('duplicate') ||
                    (error.message.includes('column') && error.message.includes('already exists')) ||
                    error.code === '42P07' || // PostgreSQL "relation already exists"
                    error.code === '42710' || // PostgreSQL "duplicate object"
                    error.code === '42701') { // PostgreSQL "duplicate column"
                  // Silently skip - column already exists
                } else {
                  console.error(`  ❌ Migration error in ${file}:`, error.message);
                  // Continue with other migrations even if one fails
                }
              }
            }
          }
        }
        console.log('✅ Incremental migrations completed');
      }
    }
    
    if (tablesExist) {
      console.log('✅ Database schema already exists - skipping initial migration');
      migrationRun = true;
      return { success: true, skipped: true, message: 'Schema already exists' };
    }

    console.log('📝 Running database migrations...');

    // Read schema file
    const schemaPath = path.join(__dirname, 'schema.sql');
    if (!fs.existsSync(schemaPath)) {
      console.error('❌ Schema file not found:', schemaPath);
      return { success: false, error: 'Schema file not found' };
    }

    const schema = fs.readFileSync(schemaPath, 'utf8');

    // Parse SQL into individual statements
    const statements = parseSQLStatements(schema);
    
    console.log(`📋 Found ${statements.length} SQL statements to execute`);

    // Execute statements one by one in a transaction
    const client = await getClient();
    let executed = 0;
    let skipped = 0;
    
    try {
      await client.query('BEGIN');
      
      for (let i = 0; i < statements.length; i++) {
        const statement = statements[i];
        if (statement.trim()) {
          try {
            await client.query(statement);
            executed++;
          } catch (error) {
            // Ignore "already exists" errors (idempotent)
            if (error.message.includes('already exists') || 
                error.message.includes('duplicate') ||
                error.code === '42P07' || // PostgreSQL "relation already exists"
                error.code === '42710') { // PostgreSQL "duplicate object"
              skipped++;
            } else {
              console.error(`❌ Migration error on statement ${i + 1}/${statements.length}:`, error.message);
              console.error(`Statement preview: ${statement.substring(0, 150).replace(/\n/g, ' ')}...`);
              await client.query('ROLLBACK');
              throw error;
            }
          }
        }
      }
      
      await client.query('COMMIT');
      console.log(`✅ Migration completed! Executed ${executed} statements, skipped ${skipped}`);
    } catch (error) {
      await client.query('ROLLBACK');
      throw error;
    } finally {
      client.release();
    }

    // Run seed data
    console.log('🌱 Running seed data...');
    const seedPath = path.join(__dirname, 'seed.sql');
    if (fs.existsSync(seedPath)) {
      const seed = fs.readFileSync(seedPath, 'utf8');
      const seedStatements = parseSQLStatements(seed);

      for (const statement of seedStatements) {
        if (statement.trim()) {
          try {
            await query(statement);
          } catch (error) {
            // Ignore duplicate key errors for seed data
            if (!error.message.includes('duplicate key')) {
              console.log('⚠️  Seed warning:', error.message);
            }
          }
        }
      }
    }

    // Verify tables were created
    const tablesResult = await query(`
      SELECT table_name 
      FROM information_schema.tables 
      WHERE table_schema = 'public' 
      ORDER BY table_name
    `);

    console.log(`📊 Database tables: ${tablesResult.rows.map(r => r.table_name).join(', ')}`);

    migrationRun = true;
    return { 
      success: true, 
      executed, 
      skipped,
      tables: tablesResult.rows.map(r => r.table_name)
    };
  } catch (error) {
    console.error('❌ Auto-migration failed:', error.message);
    // Don't throw - allow server to start even if migration fails
    // (might be a connection issue that resolves later)
    return { 
      success: false, 
      error: error.message 
    };
  }
}

module.exports = {
  autoMigrate,
  checkTablesExist
};
