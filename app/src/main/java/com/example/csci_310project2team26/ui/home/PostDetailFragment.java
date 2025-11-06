package com.example.csci_310project2team26.ui.home;

import android.content.res.Resources;
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

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.databinding.FragmentPostDetailBinding;
import com.example.csci_310project2team26.viewmodel.CommentsViewModel;

import java.text.NumberFormat;
import java.util.Locale;

public class PostDetailFragment extends Fragment {

    private FragmentPostDetailBinding binding;
    private CommentsViewModel commentsViewModel;
    private CommentsAdapter commentsAdapter;
    private PostRepository postRepository;
    private String postId;
    private int displayedCommentCount = 0;

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
        commentsAdapter.setOnCommentVoteListener((comment, type) -> {
            if (postId != null && comment != null) {
                commentsViewModel.voteOnComment(postId, comment.getId(), type);
            }
        });
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
            displayedCommentCount = list != null ? list.size() : 0;
            updateCommentCountText(displayedCommentCount);
        });
        commentsViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
        });
        commentsViewModel.isPostingComment().observe(getViewLifecycleOwner(), posting -> {
            boolean inFlight = Boolean.TRUE.equals(posting);
            binding.addCommentButton.setEnabled(!inFlight);
            binding.commentEditText.setEnabled(!inFlight);
        });
        commentsViewModel.getLatestPostedComment().observe(getViewLifecycleOwner(), comment -> {
            if (comment == null) return;
            binding.commentEditText.setText("");
            binding.commentsRecyclerView.scrollToPosition(0);
        });
    }

    private void loadPost(String id) {
        postRepository.getPostById(id, new PostRepository.Callback<Post>() {
            @Override
            public void onSuccess(Post post) {
                binding.titleTextView.setText(post.getTitle());
                binding.contentTextView.setText(post.getContent());

                Resources resources = requireContext().getResources();
                String author = post.getAuthor_name() != null && !post.getAuthor_name().isEmpty()
                        ? post.getAuthor_name()
                        : resources.getString(R.string.post_meta_unknown_author);
                boolean hasTag = post.getLlm_tag() != null && !post.getLlm_tag().isEmpty();
                String tagLabel = hasTag
                        ? resources.getString(R.string.post_tag_format, post.getLlm_tag())
                        : resources.getString(R.string.post_tag_unknown);
                binding.tagTextView.setText(tagLabel);
                binding.authorTextView.setText(resources.getString(R.string.post_author_format, author));

                NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
                int upvotes = Math.max(post.getUpvotes(), 0);
                int downvotes = Math.max(post.getDownvotes(), 0);
                displayedCommentCount = Math.max(post.getComment_count(), 0);

                String upvoteText = resources.getQuantityString(
                        R.plurals.post_upvotes,
                        upvotes,
                        numberFormat.format(upvotes)
                );
                String downvoteText = resources.getQuantityString(
                        R.plurals.post_downvotes,
                        downvotes,
                        numberFormat.format(downvotes)
                );

                binding.upvoteCountTextView.setText(upvoteText);
                binding.downvoteCountTextView.setText(downvoteText);
                updateCommentCountText(displayedCommentCount);
            }

            @Override
            public void onError(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void vote(String type) {
        if (postId == null) return;
        postRepository.votePost(postId, type, new PostRepository.Callback<PostRepository.VoteActionResult>() {
            @Override
            public void onSuccess(PostRepository.VoteActionResult result) {
                Toast.makeText(requireContext(), "Voted " + result.getType(), Toast.LENGTH_SHORT).show();
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
        commentsViewModel.addComment(postId, text);
    }

    private void updateCommentCountText(int commentCount) {
        Resources resources = requireContext().getResources();
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
        String commentsText = resources.getQuantityString(
                R.plurals.post_comments,
                commentCount,
                numberFormat.format(commentCount)
        );
        binding.commentCountTextView.setText(commentsText);
    }
}