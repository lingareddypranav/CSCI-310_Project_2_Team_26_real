/**
 * Auto Migration System
 * Automatically runs database migrations on server startup
 * Safe to run multiple times - uses IF NOT EXISTS clauses
 */

const { query } = require('../config/database');
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
    
    if (tablesExist) {
      console.log('✅ Database schema already exists - skipping migration');
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

    // Split by semicolons and execute each statement
    const statements = schema
      .split(';')
      .map(s => s.trim())
      .filter(s => s.length > 0 && !s.startsWith('--'));

    let executed = 0;
    let skipped = 0;

    for (let i = 0; i < statements.length; i++) {
      const statement = statements[i];
      if (statement.trim()) {
        try {
          await query(statement);
          executed++;
        } catch (error) {
          // Ignore "already exists" errors (idempotent)
          if (error.message.includes('already exists') || 
              error.message.includes('duplicate') ||
              error.code === '42P07' || // PostgreSQL "relation already exists"
              error.code === '42710') { // PostgreSQL "duplicate object"
            skipped++;
          } else {
            console.error(`❌ Migration error on statement ${i + 1}:`, error.message);
            throw error;
          }
        }
      }
    }

    // Run seed data
    console.log('🌱 Running seed data...');
    const seedPath = path.join(__dirname, 'seed.sql');
    if (fs.existsSync(seedPath)) {
      const seed = fs.readFileSync(seedPath, 'utf8');
      const seedStatements = seed
        .split(';')
        .map(s => s.trim())
        .filter(s => s.length > 0 && !s.startsWith('--'));

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

    console.log(`✅ Migration completed! Created ${executed} statements, skipped ${skipped}`);
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

