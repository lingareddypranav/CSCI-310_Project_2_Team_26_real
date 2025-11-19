package com.example.csci_310project2team26.ui.auth;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.csci_310project2team26.MainActivity;
import com.example.csci_310project2team26.databinding.ActivityLoginBinding;
import com.example.csci_310project2team26.viewmodel.AuthViewModel;

/**
 * LoginActivity - Handles USC user authentication
 * Requirements: UR-1 Registration & Sign-In (USC-only)
 * 
 * Features:
 * - USC email (@usc.edu) and password authentication
 * - Remember me functionality
 * - Forgot password option
 * - Navigation to registration
 */
public class LoginActivity extends AppCompatActivity {
    
    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        setupUI();
        observeViewModel();
        checkSavedCredentials();
    }
    
    private void setupUI() {
        // Email validation
        binding.emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String email = s.toString();
                if (!email.isEmpty() && !email.endsWith("@usc.edu")) {
                    binding.emailInputLayout.setError("Must be a USC email (@usc.edu)");
                } else {
                    binding.emailInputLayout.setError(null);
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        
        // Login button
        binding.loginButton.setOnClickListener(v -> {


//            TEMPORARY CHANGE TO GET TO MAIN PAGE
//            navigateToMainApp();



            if (validateFields()) {
                login();
            }
        });
        
        // Register link
        binding.registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
        });
        
        // Forgot password link
        // Forgot password link - TODO: Implement later
        binding.forgotPasswordLink.setOnClickListener(v -> {
            Toast.makeText(this, "Forgot Password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }
    
    private boolean validateFields() {
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        
        boolean isValid = true;
        
        if (email.isEmpty()) {
            binding.emailInputLayout.setError("Email is required");
            isValid = false;
        } else if (!email.endsWith("@usc.edu")) {
            binding.emailInputLayout.setError("Must be a USC email (@usc.edu)");
            isValid = false;
        } else {
            binding.emailInputLayout.setError(null);
        }
        
        if (password.isEmpty()) {
            binding.passwordInputLayout.setError("Password is required");
            isValid = false;
        } else {
            binding.passwordInputLayout.setError(null);
        }
        
        return isValid;
    }
    
    private void login() {
        binding.loginButton.setEnabled(false);
        binding.progressBar.setVisibility(View.VISIBLE);
        
        String email = binding.emailEditText.getText().toString();
        String password = binding.passwordEditText.getText().toString();
        boolean rememberMe = binding.rememberMeCheckbox.isChecked();
        
        authViewModel.login(email, password, rememberMe);
    }
    
    private void checkSavedCredentials() {
        // Check if user credentials are saved
        authViewModel.checkSavedSession();
    }
    
    private void observeViewModel() {
        authViewModel.getLoginState().observe(this, state -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.loginButton.setEnabled(true);
            
            if (state instanceof AuthViewModel.LoginState.Success) {
                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show();
                navigateToMainApp();
            } else if (state instanceof AuthViewModel.LoginState.Error) {
                AuthViewModel.LoginState.Error errorState = 
                    (AuthViewModel.LoginState.Error) state;
                Toast.makeText(this, "Login failed: " + errorState.getMessage(), 
                    Toast.LENGTH_LONG).show();
            } else if (state instanceof AuthViewModel.LoginState.SessionExists) {
                // User already logged in, navigate to main app
                navigateToMainApp();
            }
        });
    }
    
    private void navigateToMainApp() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
