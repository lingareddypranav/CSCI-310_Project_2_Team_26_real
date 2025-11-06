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

        binding.publishButton.setOnClickListener(v -> onPublishClicked());
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

        viewModel.createPost(title, body, tag, isPrompt);
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
        binding.titleEditText.setText("");
        binding.bodyEditText.setText("");
        binding.tagEditText.setText("");
        binding.promptSwitch.setChecked(false);

        Bundle args = new Bundle();
        args.putString("postId", postId);
        Navigation.findNavController(binding.getRoot()).navigate(R.id.postDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
