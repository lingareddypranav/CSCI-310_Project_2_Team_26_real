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
        });
    }

    private void populatePost(Post post) {
        if (post == null) {
            return;
        }
        binding.editTitleEditText.setText(post.getTitle());
        binding.editBodyEditText.setText(post.getContent());
        binding.editTagEditText.setText(post.getLlm_tag() != null ? post.getLlm_tag() : "");
        binding.editPromptSwitch.setChecked(post.isIs_prompt_post());
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
        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(body)) {
            Toast.makeText(requireContext(), R.string.edit_post_error, Toast.LENGTH_LONG).show();
            return;
        }
        viewModel.updatePost(postId, title, body, tag, isPrompt);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
