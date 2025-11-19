package com.example.csci_310project2team26.ui.auth;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.csci_310project2team26.databinding.ActivityRegistrationBinding;
import com.example.csci_310project2team26.viewmodel.AuthViewModel;
import com.example.csci_310project2team26.ui.create_profile.ProfileCreationActivity;
/**
 * RegistrationActivity - Handles USC-only user registration
 * Requirements: UR-1 Registration & Sign-In (USC-only)
 * 
 * Validates:
 * - Name (required)
 * - 10-digit USC Student ID
 * - @usc.edu email address
 * - Password (minimum 8 characters)
 * - Password confirmation match
 */
public class RegistrationActivity extends AppCompatActivity {
    
    private ActivityRegistrationBinding binding;
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegistrationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        setupUI();
        observeViewModel();
    }
    
    private void setupUI() {
        // Real-time validation for USC email
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateEmail(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        // Real-time validation for Student ID
        binding.studentIdEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateStudentId(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        // Password validation
        binding.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validatePassword(s.toString());
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        // Confirm password validation
        binding.confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                validateConfirmPassword();
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        // Register button
        binding.registerButton.setOnClickListener(v -> {
            if (validateAllFields()) {
                registerUser();
            }
        });
        
        // Already have account link
        binding.loginLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
    
    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            binding.emailInputLayout.setError("Email is required");
            return false;
        } else if (!email.endsWith("@usc.edu")) {
            binding.emailInputLayout.setError("Must be a USC email (@usc.edu)");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailInputLayout.setError("Invalid email format");
            return false;
        } else {
            binding.emailInputLayout.setError(null);
            return true;
        }
    }
    
    private boolean validateStudentId(String id) {
        if (id.isEmpty()) {
            binding.studentIdInputLayout.setError("Student ID is required");
            return false;
        } else if (id.length() != 10) {
            binding.studentIdInputLayout.setError("Must be exactly 10 digits");
            return false;
        } else if (!id.matches("\\d+")) {
            binding.studentIdInputLayout.setError("Must contain only digits");
            return false;
        } else {
            binding.studentIdInputLayout.setError(null);
            return true;
        }
    }
    
    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            binding.passwordInputLayout.setError("Password is required");
            return false;
        } else if (password.length() < 8) {
            binding.passwordInputLayout.setError("Password must be at least 8 characters");
            return false;
        } else {
            binding.passwordInputLayout.setError(null);
            return true;
        }
    }
    
    private boolean validateConfirmPassword() {
        String password = binding.passwordEditText.getText().toString();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString();
        
        if (confirmPassword.isEmpty()) {
            binding.confirmPasswordInputLayout.setError("Please confirm password");
            return false;
        } else if (!password.equals(confirmPassword)) {
            binding.confirmPasswordInputLayout.setError("Passwords do not match");
            return false;
        } else {
            binding.confirmPasswordInputLayout.setError(null);
            return true;
        }
    }
    
    private boolean validateAllFields() {
        String name = binding.nameEditText.getText().toString();
        String email = binding.emailEditText.getText().toString();
        String studentId = binding.studentIdEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        
        boolean isValid = true;
        
        if (name.isEmpty()) {
            binding.nameInputLayout.setError("Name is required");
            isValid = false;
        } else {
            binding.nameInputLayout.setError(null);
        }
        
        if (!validateEmail(email)) isValid = false;
        if (!validateStudentId(studentId)) isValid = false;
        if (!validatePassword(password)) isValid = false;
        if (!validateConfirmPassword()) isValid = false;
        
        return isValid;
    }
    
    private void registerUser() {
        binding.registerButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        
        String name = binding.nameEditText.getText().toString();
        String email = binding.emailEditText.getText().toString();
        String studentId = binding.studentIdEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        
        authViewModel.register(name, email, studentId, password);
    }
    
    private void observeViewModel() {
        authViewModel.getRegistrationState().observe(this, state -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.registerButton.setEnabled(true);
            
            if (state instanceof AuthViewModel.RegistrationState.Success) {
                // After successful registration, automatically log in to obtain token
                String email = binding.emailEditText.getText().toString();
                String password = binding.passwordEditText.getText().toString();

                binding.progressBar.setVisibility(View.VISIBLE);
                binding.registerButton.setEnabled(false);

                authViewModel.login(email, password, true);
            } else if (state instanceof AuthViewModel.RegistrationState.Error) {
                AuthViewModel.RegistrationState.Error errorState = 
                    (AuthViewModel.RegistrationState.Error) state;
                Toast.makeText(this, "Registration failed: " + errorState.getMessage(), 
                    Toast.LENGTH_LONG).show();
            }
        });

        // Observe login state to proceed to profile creation after auto-login
        authViewModel.getLoginState().observe(this, state -> {
            if (state instanceof AuthViewModel.LoginState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                binding.registerButton.setEnabled(true);

                Toast.makeText(this, "Registration successful! Please complete your profile.",
                        Toast.LENGTH_LONG).show();

                // We don't have userId from login here; use the Registration success ID from repo if needed.
                // For now, navigate with known name/email; server can derive user by token on profile create.
                Intent intent = new Intent(this, ProfileCreationActivity.class);
                intent.putExtra("USER_NAME", binding.nameEditText.getText().toString());
                intent.putExtra("USER_EMAIL", binding.emailEditText.getText().toString());
                startActivity(intent);
                finish();
            } else if (state instanceof AuthViewModel.LoginState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.registerButton.setEnabled(true);
                AuthViewModel.LoginState.Error errorState = (AuthViewModel.LoginState.Error) state;
                Toast.makeText(this, "Auto-login failed: " + errorState.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
