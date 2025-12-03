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
import com.example.csci_310project2team26.viewmodel.DraftsViewModel;

public class CreatePostFragment extends Fragment {

    private FragmentCreatePostBinding binding;
    private CreatePostViewModel viewModel;
    private DraftsViewModel draftsViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePostBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(CreatePostViewModel.class);
        draftsViewModel = new ViewModelProvider(requireActivity()).get(DraftsViewModel.class);

        // Always start with prompt mode off to avoid leaking prior state when navigating back to
        // this screen.
        binding.promptSwitch.setChecked(false);
        binding.anonymousSwitch.setChecked(false);
        if (binding.promptSectionLayout != null) {
            binding.promptSectionLayout.setVisibility(View.GONE);
        }
        if (binding.bodyEditText != null && binding.bodyEditText.getParent() instanceof View) {
            ((View) binding.bodyEditText.getParent()).setVisibility(View.VISIBLE);
        }

        binding.publishButton.setOnClickListener(v -> onPublishClicked());
        binding.saveDraftButton.setOnClickListener(v -> onSaveDraftClicked());
        binding.viewDraftsButton.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_navigation_create_post_to_savedDraftsFragment));

        // Show/hide prompt fields based on toggle
        binding.promptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (binding.promptSectionLayout != null) {
                binding.promptSectionLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            }

            // When turning prompt mode off, clear any lingering prompt content so it isn't
            // accidentally submitted or used to infer a prompt post type on the backend.
            if (!isChecked) {
                if (binding.promptSectionEditText != null) {
                    binding.promptSectionEditText.setText("");
                }
                if (binding.descriptionSectionEditText != null) {
                    binding.descriptionSectionEditText.setText("");
                }
            }

            // Hide the normal body field for prompt posts and show it for regular posts
            if (binding.bodyEditText != null && binding.bodyEditText.getParent() instanceof View) {
                View bodyContainer = (View) binding.bodyEditText.getParent();
                bodyContainer.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });
        
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
            binding.anonymousSwitch.setEnabled(!inFlight);
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
        boolean isPrompt = binding.promptSwitch.isChecked();
        boolean isAnonymous = binding.anonymousSwitch.isChecked();
        
        String promptSection = null;
        String descriptionSection = null;
        if (isPrompt && binding.promptSectionEditText != null && binding.descriptionSectionEditText != null) {
            promptSection = binding.promptSectionEditText.getText() != null ? 
                binding.promptSectionEditText.getText().toString() : "";
            descriptionSection = binding.descriptionSectionEditText.getText() != null ? 
                binding.descriptionSectionEditText.getText().toString() : "";
        }

        viewModel.createPost(title, body, tag, isPrompt, promptSection, descriptionSection, isAnonymous);
    }

    private void onSaveDraftClicked() {
        String title = binding.titleEditText.getText() != null ? binding.titleEditText.getText().toString() : "";
        String body = binding.bodyEditText.getText() != null ? binding.bodyEditText.getText().toString() : "";
        String tag = binding.tagEditText.getText() != null ? binding.tagEditText.getText().toString() : "";
        boolean isPrompt = binding.promptSwitch.isChecked();

        String promptSection = null;
        String descriptionSection = null;
        if (isPrompt && binding.promptSectionEditText != null && binding.descriptionSectionEditText != null) {
            promptSection = binding.promptSectionEditText.getText() != null ?
                    binding.promptSectionEditText.getText().toString() : "";
            descriptionSection = binding.descriptionSectionEditText.getText() != null ?
                    binding.descriptionSectionEditText.getText().toString() : "";
        }

        if (title == null || title.trim().isEmpty()) {
            Toast.makeText(requireContext(), R.string.create_post_draft_error, Toast.LENGTH_SHORT).show();
            return;
        }

        draftsViewModel.saveDraft(title, body, tag, isPrompt, promptSection, descriptionSection);
        Toast.makeText(requireContext(), R.string.create_post_draft_saved, Toast.LENGTH_SHORT).show();
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
        binding.anonymousSwitch.setChecked(false);
        
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
