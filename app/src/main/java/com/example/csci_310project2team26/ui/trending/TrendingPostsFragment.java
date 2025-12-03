package com.example.csci_310project2team26.ui.trending;

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
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.databinding.FragmentTrendingPostsBinding;
import com.example.csci_310project2team26.ui.home.PostsAdapter;
import com.example.csci_310project2team26.viewmodel.TrendingPostsViewModel;

public class TrendingPostsFragment extends Fragment {

    private FragmentTrendingPostsBinding binding;
    private TrendingPostsViewModel trendingViewModel;
    private PostsAdapter postsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTrendingPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        trendingViewModel = new ViewModelProvider(this).get(TrendingPostsViewModel.class);
        postsAdapter = new PostsAdapter(post -> {
            if (post != null && post.getId() != null && binding != null) {
                Bundle args = new Bundle();
                args.putString("postId", post.getId());
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_trendingPostsFragment_to_postDetailFragment, args);
            }
        });
        postsAdapter.setOnPostDeletedListener(postId -> {
            // Show confirmation dialog
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_post_confirm_title)
                    .setMessage(R.string.delete_post_confirm_message)
                    .setPositiveButton(R.string.delete_post, (dialog, which) -> {
                        PostRepository postRepository = new PostRepository();
                        postRepository.deletePost(postId, new PostRepository.Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                                        // Reload trending posts after deletion
                                        trendingViewModel.loadTrendingPosts(10);
                                    });
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), error != null ? error : getString(R.string.delete_post_error), Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        postsAdapter.setOnBookmarkToggleListener((post, isBookmarked) -> {
            if (getContext() != null) {
                int messageId = isBookmarked ? R.string.bookmark_added : R.string.bookmark_removed;
                Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
            }
        });

        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setAdapter(postsAdapter);

        // Load trending posts
        trendingViewModel.loadTrendingPosts(10);

        // Observe ViewModel
        trendingViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (binding == null) return;
            postsAdapter.submitList(posts != null ? posts : new java.util.ArrayList<>());
        });

        trendingViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            // Optionally show/hide loading indicator
        });

        trendingViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && binding != null && getContext() != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Refresh button
        binding.refreshButton.setOnClickListener(v -> {
            trendingViewModel.loadTrendingPosts(10);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

