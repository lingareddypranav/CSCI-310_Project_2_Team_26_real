package com.example.csci_310project2team26.ui.editpost;

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
import com.example.csci_310project2team26.databinding.FragmentEditPostBinding;
import com.example.csci_310project2team26.viewmodel.EditPostViewModel;

public class EditPostFragment extends Fragment {

    private FragmentEditPostBinding binding;
    private EditPostViewModel viewModel;
    private String postId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPostBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(EditPostViewModel.class);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        binding.savePostButton.setOnClickListener(v -> onSaveClicked());

        // Show/hide prompt fields based on toggle
        binding.editPromptSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            togglePromptFields(isChecked);
        });
        
        observeViewModel();

        if (TextUtils.isEmpty(postId)) {
            Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return binding.getRoot();
        }

        viewModel.loadPost(postId);
        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getPost().observe(getViewLifecycleOwner(), this::populatePost);
        viewModel.getUpdatedPost().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null) {
                Toast.makeText(requireContext(), R.string.edit_post_success, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        });
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (!TextUtils.isEmpty(error)) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean inFlight = Boolean.TRUE.equals(loading);
            binding.savePostButton.setEnabled(!inFlight);
            binding.editTitleEditText.setEnabled(!inFlight);
            binding.editBodyEditText.setEnabled(!inFlight);
            binding.editTagEditText.setEnabled(!inFlight);
            binding.editPromptSwitch.setEnabled(!inFlight);
            if (binding.editPromptSectionEditText != null) {
                binding.editPromptSectionEditText.setEnabled(!inFlight);
            }
            if (binding.editDescriptionSectionEditText != null) {
                binding.editDescriptionSectionEditText.setEnabled(!inFlight);
            }
        });
    }

    private void populatePost(Post post) {
        if (post == null) {
            return;
        }
        binding.editTitleEditText.setText(post.getTitle());
        binding.editBodyEditText.setText(post.getContent() != null ? post.getContent() : "");
        binding.editTagEditText.setText(post.getLlm_tag() != null ? post.getLlm_tag() : "");
        boolean isPrompt = post.isIs_prompt_post();
        binding.editPromptSwitch.setChecked(isPrompt);
        binding.editPromptSwitch.setEnabled(!isPrompt);
        togglePromptFields(isPrompt);

        // Populate prompt sections if they exist
        if (isPrompt) {
            if (binding.editPromptSectionEditText != null) {
                binding.editPromptSectionEditText.setText(post.getPrompt_section() != null ? post.getPrompt_section() : "");
            }
            if (binding.editDescriptionSectionEditText != null) {
                binding.editDescriptionSectionEditText.setText(post.getDescription_section() != null ? post.getDescription_section() : "");
            }
        }
    }

    private void onSaveClicked() {
        if (TextUtils.isEmpty(postId)) {
            Toast.makeText(requireContext(), R.string.edit_post_error, Toast.LENGTH_LONG).show();
            return;
        }
        String title = binding.editTitleEditText.getText() != null ? binding.editTitleEditText.getText().toString() : "";
        String body = binding.editBodyEditText.getText() != null ? binding.editBodyEditText.getText().toString() : "";
        String tag = binding.editTagEditText.getText() != null ? binding.editTagEditText.getText().toString() : "";
        boolean isPrompt = binding.editPromptSwitch.isChecked();
        
        String promptSection = null;
        String descriptionSection = null;
        if (isPrompt && binding.editPromptSectionEditText != null && binding.editDescriptionSectionEditText != null) {
            promptSection = binding.editPromptSectionEditText.getText() != null ? 
                binding.editPromptSectionEditText.getText().toString().trim() : "";
            descriptionSection = binding.editDescriptionSectionEditText.getText() != null ? 
                binding.editDescriptionSectionEditText.getText().toString().trim() : "";
            if (promptSection.isEmpty()) promptSection = null;
            if (descriptionSection.isEmpty()) descriptionSection = null;
        }
        
        // Validation
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_LONG).show();
            return;
        }
        
        if (isPrompt) {
            if ((promptSection == null || promptSection.isEmpty()) && 
                (descriptionSection == null || descriptionSection.isEmpty())) {
                Toast.makeText(requireContext(), "Prompt posts require either prompt section or description section", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            if (TextUtils.isEmpty(body)) {
                Toast.makeText(requireContext(), "Content is required", Toast.LENGTH_LONG).show();
                return;
            }
        }
        
        viewModel.updatePost(postId, title, body, tag, isPrompt, promptSection, descriptionSection);
    }

    private void togglePromptFields(boolean isPrompt) {
        if (binding.editPromptSectionLayout != null) {
            binding.editPromptSectionLayout.setVisibility(isPrompt ? View.VISIBLE : View.GONE);
        }
        if (binding.editBodyLayout != null) {
            binding.editBodyLayout.setVisibility(isPrompt ? View.GONE : View.VISIBLE);
        }
        if (binding.editBodyEditText != null) {
            binding.editBodyEditText.setHint(isPrompt ? getString(R.string.create_post_body_hint_optional_prompt) : getString(R.string.create_post_body_hint));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
