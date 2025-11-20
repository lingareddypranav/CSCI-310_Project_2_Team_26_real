package com.example.csci_310project2team26.ui.createpost;

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
import androidx.navigation.Navigation;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.databinding.FragmentCreatePostBinding;
import com.example.csci_310project2team26.viewmodel.CreatePostViewModel;

public class CreatePostFragment extends Fragment {

    private FragmentCreatePostBinding binding;
    private CreatePostViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);

        // CRITICAL: Initialize switch state BEFORE setting listener to avoid triggering listener
        // Ensure switch starts unchecked (regular post by default)
        if (binding.promptSwitch != null) {
            // Temporarily remove any existing listener to prevent triggering during initialization
            binding.promptSwitch.setOnCheckedChangeListener(null);
            // Set to unchecked (regular post mode)
            binding.promptSwitch.setChecked(false);
        }
        if (binding.promptSectionLayout != null) {
            binding.promptSectionLayout.setVisibility(View.GONE);
        }
        
        binding.publishButton.setOnClickListener(v -> onPublishClicked());
        
        // Show/hide prompt fields based on toggle
        // Set listener AFTER initializing state to avoid unwanted triggers
        if (binding.promptSwitch != null) {
            binding.promptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (binding.promptSectionLayout != null) {
                    binding.promptSectionLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                }
                // For prompt posts, body is optional
                if (binding.bodyEditText != null) {
                    binding.bodyEditText.setHint(isChecked ? "Content (optional for prompt posts)" : getString(R.string.create_post_body_hint));
                }
            });
        }
        
        observeViewModel();

        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean inFlight = Boolean.TRUE.equals(loading);
            binding.publishButton.setEnabled(!inFlight);
            binding.titleEditText.setEnabled(!inFlight);
            binding.bodyEditText.setEnabled(!inFlight);
            binding.tagEditText.setEnabled(!inFlight);
            binding.promptSwitch.setEnabled(!inFlight);
            if (binding.promptSectionEditText != null) {
                binding.promptSectionEditText.setEnabled(!inFlight);
            }
            if (binding.descriptionSectionEditText != null) {
                binding.descriptionSectionEditText.setEnabled(!inFlight);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (!TextUtils.isEmpty(error)) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getCreatedPost().observe(getViewLifecycleOwner(), this::handlePostCreated);
    }

    private void onPublishClicked() {
        String title = binding.titleEditText.getText() != null ? binding.titleEditText.getText().toString() : "";
        String body = binding.bodyEditText.getText() != null ? binding.bodyEditText.getText().toString() : "";
        String tag = binding.tagEditText.getText() != null ? binding.tagEditText.getText().toString() : "";
        
        // CRITICAL: Read switch state explicitly - default to false (regular post) if switch is null
        // This ensures we never accidentally treat a regular post as a prompt post
        boolean isPrompt = false;
        if (binding.promptSwitch != null) {
            isPrompt = binding.promptSwitch.isChecked();
        }
        
        // Initialize prompt sections as null - will only be set if isPrompt is true
        String promptSection = null;
        String descriptionSection = null;
        
        // ONLY get prompt section values if switch is EXPLICITLY checked
        // For regular posts, these MUST remain null
        if (isPrompt) {
            // This is a prompt post - get the prompt section values
            if (binding.promptSectionEditText != null) {
                String promptText = binding.promptSectionEditText.getText() != null ? 
                    binding.promptSectionEditText.getText().toString() : "";
                if (!promptText.trim().isEmpty()) {
                    promptSection = promptText.trim();
                }
            }
            if (binding.descriptionSectionEditText != null) {
                String descText = binding.descriptionSectionEditText.getText() != null ? 
                    binding.descriptionSectionEditText.getText().toString() : "";
                if (!descText.trim().isEmpty()) {
                    descriptionSection = descText.trim();
                }
            }
        }
        // IMPORTANT: For regular posts (isPrompt = false), promptSection and descriptionSection MUST remain null
        // The ViewModel will enforce this, but we ensure it here too

        viewModel.createPost(title, body, tag, isPrompt, promptSection, descriptionSection);
    }

    private void handlePostCreated(Post post) {
        if (post == null) {
            return;
        }
        String postId = post.getId();
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(requireContext(), "Post created but unable to navigate", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(requireContext(), R.string.create_post_success, Toast.LENGTH_SHORT).show();
        
        // Clear form to allow creating another post
        binding.titleEditText.setText("");
        binding.bodyEditText.setText("");
        binding.tagEditText.setText("");
        binding.promptSwitch.setChecked(false);
        
        // Clear and hide prompt/description fields
        if (binding.promptSectionEditText != null) {
            binding.promptSectionEditText.setText("");
        }
        if (binding.descriptionSectionEditText != null) {
            binding.descriptionSectionEditText.setText("");
        }
        if (binding.promptSectionLayout != null) {
            binding.promptSectionLayout.setVisibility(View.GONE);
        }
        
        // Stay on create post page instead of navigating away
        // User can create multiple posts without restarting the app
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
