package com.example.csci_310project2team26.ui.editcomment;

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
import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.databinding.FragmentEditCommentBinding;
import com.example.csci_310project2team26.viewmodel.EditCommentViewModel;

public class EditCommentFragment extends Fragment {

    private FragmentEditCommentBinding binding;
    private EditCommentViewModel viewModel;
    private String postId;
    private String commentId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEditCommentBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(EditCommentViewModel.class);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
            commentId = getArguments().getString("commentId");
        }

        binding.saveCommentButton.setOnClickListener(v -> onSaveClicked());
        observeViewModel();

        if (TextUtils.isEmpty(postId) || TextUtils.isEmpty(commentId)) {
            Toast.makeText(requireContext(), "Post ID or Comment ID is missing", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return binding.getRoot();
        }

        viewModel.loadComment(postId, commentId);
        return binding.getRoot();
    }

    private void observeViewModel() {
        viewModel.getComment().observe(getViewLifecycleOwner(), this::populateComment);
        viewModel.getUpdatedComment().observe(getViewLifecycleOwner(), updated -> {
            if (updated != null) {
                Toast.makeText(requireContext(), R.string.edit_comment_success, Toast.LENGTH_SHORT).show();
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
            binding.saveCommentButton.setEnabled(!inFlight);
            binding.editCommentEditText.setEnabled(!inFlight);
            if (binding.editCommentTitleEditText != null) {
                binding.editCommentTitleEditText.setEnabled(!inFlight);
            }
        });
    }

    private void populateComment(Comment comment) {
        if (comment == null) {
            return;
        }
        if (binding.editCommentTitleEditText != null) {
            binding.editCommentTitleEditText.setText(comment.getTitle() != null ? comment.getTitle() : "");
        }
        binding.editCommentEditText.setText(comment.getText() != null ? comment.getText() : "");
    }

    private void onSaveClicked() {
        if (TextUtils.isEmpty(postId) || TextUtils.isEmpty(commentId)) {
            Toast.makeText(requireContext(), R.string.edit_comment_error, Toast.LENGTH_LONG).show();
            return;
        }
        String text = binding.editCommentEditText.getText() != null
                ? binding.editCommentEditText.getText().toString()
                : "";
        if (TextUtils.isEmpty(text)) {
            Toast.makeText(requireContext(), R.string.edit_comment_error, Toast.LENGTH_LONG).show();
            return;
        }
        
        String title = null;
        if (binding.editCommentTitleEditText != null) {
            String titleText = binding.editCommentTitleEditText.getText() != null
                    ? binding.editCommentTitleEditText.getText().toString().trim()
                    : "";
            if (!TextUtils.isEmpty(titleText)) {
                title = titleText;
            }
        }
        
        viewModel.updateComment(postId, commentId, text, title);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
