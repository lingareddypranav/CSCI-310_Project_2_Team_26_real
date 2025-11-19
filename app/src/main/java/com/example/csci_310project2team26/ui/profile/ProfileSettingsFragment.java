package com.example.csci_310project2team26.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Profile;
import com.example.csci_310project2team26.data.repository.SessionManager;
import com.example.csci_310project2team26.databinding.FragmentProfileSettingsBinding;
import com.example.csci_310project2team26.viewmodel.ProfileViewModel;

public class ProfileSettingsFragment extends Fragment {

    private FragmentProfileSettingsBinding binding;
    private ProfileViewModel profileViewModel;
    private String userId;
    private Action lastAction = Action.NONE;

    private enum Action {
        NONE,
        UPDATE,
        RESET
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileSettingsBinding.inflate(inflater, container, false);
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        userId = SessionManager.getUserId();

        binding.saveButton.setOnClickListener(v -> onSaveClicked());
        binding.resetPasswordButton.setOnClickListener(v -> onResetPasswordClicked());

        observeViewModel();
        if (!TextUtils.isEmpty(userId)) {
            profileViewModel.loadProfile(userId);
        }

        return binding.getRoot();
    }

    private void observeViewModel() {
        profileViewModel.getCurrentProfile().observe(getViewLifecycleOwner(), this::bindProfile);
        profileViewModel.getProfileUpdateState().observe(getViewLifecycleOwner(), state -> {
            boolean loading = state instanceof ProfileViewModel.ProfileUpdateState.Loading;
            setLoading(loading);

            if (state instanceof ProfileViewModel.ProfileUpdateState.Success) {
                handleSuccess();
            } else if (state instanceof ProfileViewModel.ProfileUpdateState.Error) {
                String message = ((ProfileViewModel.ProfileUpdateState.Error) state).getMessage();
                handleError(message);
            }
        });
    }

    private void bindProfile(Profile profile) {
        if (profile == null || binding == null) {
            return;
        }
        binding.nameEditText.setText(profile.getName());
        binding.emailEditText.setText(profile.getEmail());
        binding.affiliationEditText.setText(profile.getAffiliation());
        binding.birthDateEditText.setText(profile.getBirthDate() != null ? profile.getBirthDate() : "");
        binding.bioEditText.setText(profile.getBio() != null ? profile.getBio() : "");
        binding.interestsEditText.setText(profile.getInterests() != null ? profile.getInterests() : "");
    }

    private void onSaveClicked() {
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(requireContext(), R.string.profile_update_failed, Toast.LENGTH_LONG).show();
            return;
        }
        lastAction = Action.UPDATE;
        String birthDate = binding.birthDateEditText.getText() != null ? binding.birthDateEditText.getText().toString() : null;
        String bio = binding.bioEditText.getText() != null ? binding.bioEditText.getText().toString() : null;
        String interests = binding.interestsEditText.getText() != null ? binding.interestsEditText.getText().toString() : null;
        profileViewModel.updateProfile(userId, birthDate, bio, interests, null);
    }

    private void onResetPasswordClicked() {
        if (TextUtils.isEmpty(userId)) {
            Toast.makeText(requireContext(), R.string.profile_settings_reset_error, Toast.LENGTH_LONG).show();
            return;
        }
        String currentPassword = binding.currentPasswordEditText.getText() != null
                ? binding.currentPasswordEditText.getText().toString()
                : "";
        String newPassword = binding.newPasswordEditText.getText() != null
                ? binding.newPasswordEditText.getText().toString()
                : "";
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword)) {
            Toast.makeText(requireContext(), R.string.profile_settings_reset_error, Toast.LENGTH_LONG).show();
            return;
        }
        lastAction = Action.RESET;
        profileViewModel.resetPassword(userId, currentPassword, newPassword);
    }

    private void setLoading(boolean loading) {
        binding.saveButton.setEnabled(!loading);
        binding.resetPasswordButton.setEnabled(!loading);
        binding.birthDateEditText.setEnabled(!loading);
        binding.bioEditText.setEnabled(!loading);
        binding.interestsEditText.setEnabled(!loading);
        binding.currentPasswordEditText.setEnabled(!loading);
        binding.newPasswordEditText.setEnabled(!loading);
    }

    private void handleSuccess() {
        if (lastAction == Action.UPDATE) {
            Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
        } else if (lastAction == Action.RESET) {
            Toast.makeText(requireContext(), R.string.profile_settings_reset_success, Toast.LENGTH_SHORT).show();
            binding.currentPasswordEditText.setText("");
            binding.newPasswordEditText.setText("");
        }
        lastAction = Action.NONE;
    }

    private void handleError(String message) {
        if (lastAction == Action.RESET) {
            Toast.makeText(requireContext(), R.string.profile_settings_reset_error, Toast.LENGTH_LONG).show();
        } else {
            String displayMessage = !TextUtils.isEmpty(message) ? message : getString(R.string.profile_update_failed);
            Toast.makeText(requireContext(), displayMessage, Toast.LENGTH_LONG).show();
        }
        lastAction = Action.NONE;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
