package com.example.csci_310project2team26.ui.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.repository.SessionManager;
import com.example.csci_310project2team26.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private NotificationsViewModel viewModel;
    private UserActivityAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        setupRecyclerView();
        observeViewModel();

        String userId = SessionManager.getUserId();
        viewModel.loadUserActivity(userId);

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new UserActivityAdapter(new UserActivityAdapter.OnActivityClickListener() {
            @Override
            public void onActivityClicked(UserActivityItem item) {
                NotificationsFragment.this.onActivityClicked(item);
            }

            @Override
            public void onActivityDeleteClicked(UserActivityItem item) {
                NotificationsFragment.this.onActivityDelete(item);
            }

            @Override
            public void onVersionHistoryClicked(UserActivityItem item) {
                NotificationsFragment.this.onVersionHistoryClicked(item);
            }
        });
        binding.activityRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.activityRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getActivityItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            boolean isEmpty = items == null || items.isEmpty();
            binding.activityEmptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.activityRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean visible = Boolean.TRUE.equals(loading);
            binding.activityProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                binding.activityEmptyStateTextView.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });
    }

    private void onActivityClicked(UserActivityItem item) {
        if (item == null) {
            Toast.makeText(requireContext(), "Unable to open activity", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        switch (item.getType()) {
            case POST:
                String postId = item.getPostId();
                if (postId == null || postId.isEmpty()) {
                    Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                args.putString("postId", postId);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.editPostFragment, args);
                break;
            case COMMENT:
                String commentPostId = item.getPostId();
                String commentId = item.getId();
                if (commentPostId == null || commentPostId.isEmpty() || commentId == null || commentId.isEmpty()) {
                    Toast.makeText(requireContext(), "Post ID or Comment ID is missing", Toast.LENGTH_SHORT).show();
                    return;
                }
                args.putString("postId", commentPostId);
                args.putString("commentId", commentId);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.editCommentFragment, args);
                break;
        }
    }

    private void onActivityDelete(UserActivityItem item) {
        if (item == null) {
            Toast.makeText(requireContext(), "Unable to delete item", Toast.LENGTH_SHORT).show();
            return;
        }

        switch (item.getType()) {
            case POST:
                if (item.getId() != null && !item.getId().isEmpty()) {
                    viewModel.deletePost(item.getId());
                } else {
                    Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_SHORT).show();
                }
                break;
            case COMMENT:
                if (item.getId() != null && !item.getId().isEmpty()) {
                    viewModel.deleteComment(item.getId());
                } else {
                    Toast.makeText(requireContext(), "Comment ID is missing", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void onVersionHistoryClicked(UserActivityItem item) {
        if (item == null || item.getType() != UserActivityItem.Type.POST) {
            return;
        }
        String postId = item.getId();
        if (postId == null || postId.isEmpty()) {
            Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("postId", postId);
        Navigation.findNavController(binding.getRoot()).navigate(R.id.postVersionsFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
