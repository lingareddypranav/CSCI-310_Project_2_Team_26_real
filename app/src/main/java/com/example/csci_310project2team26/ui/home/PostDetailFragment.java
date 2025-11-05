package com.example.csci_310project2team26.ui.home;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.databinding.FragmentPostDetailBinding;
import com.example.csci_310project2team26.viewmodel.CommentsViewModel;

public class PostDetailFragment extends Fragment {

    private FragmentPostDetailBinding binding;
    private CommentsViewModel commentsViewModel;
    private CommentsAdapter commentsAdapter;
    private PostRepository postRepository;
    private String postId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        commentsViewModel = new ViewModelProvider(this).get(CommentsViewModel.class);
        postRepository = new PostRepository();

        commentsAdapter = new CommentsAdapter();
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.commentsRecyclerView.setAdapter(commentsAdapter);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        if (postId != null) {
            loadPost(postId);
            commentsViewModel.loadComments(postId);
        }

        binding.upvoteButton.setOnClickListener(v -> vote("up"));
        binding.downvoteButton.setOnClickListener(v -> vote("down"));
        binding.addCommentButton.setOnClickListener(v -> addComment());

        observeViewModel();
    }

    private void observeViewModel() {
        commentsViewModel.getComments().observe(getViewLifecycleOwner(), list -> {
            commentsAdapter.submitList(list);
        });
        commentsViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
        });
    }

    private void loadPost(String id) {
        postRepository.getPostById(id, new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post post) {
                binding.titleTextView.setText(post.getTitle());
                binding.contentTextView.setText(post.getContent());
                binding.metaTextView.setText(post.getAuthor_name());
                binding.voteCountsTextView.setText((post.getUpvotes() - post.getDownvotes()) + "");
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void vote(String type) {
        if (postId == null) return;
        postRepository.votePost(postId, type, new PostRepository.Callback<com.example.csci_310project2team26.data.network.ApiService.VoteActionResponse>() {
            @Override
            public void onSuccess(com.example.csci_310project2team26.data.network.ApiService.VoteActionResponse result) {
                Toast.makeText(requireContext(), "Voted " + result.type, Toast.LENGTH_SHORT).show();
                loadPost(postId);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addComment() {
        String text = binding.commentEditText.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;
        if (postId == null) return;
        binding.addCommentButton.setEnabled(false);
        commentsViewModel.addComment(postId, text);
        binding.addCommentButton.setEnabled(true);
        binding.commentEditText.setText("");
    }
}


