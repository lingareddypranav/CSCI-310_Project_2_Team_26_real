package com.example.csci_310project2team26.ui.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.data.repository.SessionManager;
import com.example.csci_310project2team26.data.repository.VotePreferenceManager;
import com.example.csci_310project2team26.databinding.FragmentPostDetailBinding;
import com.example.csci_310project2team26.viewmodel.CommentsViewModel;
import com.example.csci_310project2team26.viewmodel.PostDetailViewModel;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PostDetailFragment extends Fragment {

    private FragmentPostDetailBinding binding;
    private CommentsViewModel commentsViewModel;
    private PostDetailViewModel postDetailViewModel;
    private PostRepository postRepository;
    private CommentsAdapter commentsAdapter;
    private String postId;
    private int displayedCommentCount = 0;
    private Post currentPost;

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
        postDetailViewModel = new ViewModelProvider(this).get(PostDetailViewModel.class);
        postRepository = new PostRepository();

        commentsAdapter = new CommentsAdapter();
        commentsAdapter.setOnCommentVoteListener((comment, type) -> {
            if (postId != null && comment != null && comment.getId() != null && !comment.getId().isEmpty()) {
                // Check if already voting to prevent rapid clicks
                Boolean isVoting = commentsViewModel.getLoading().getValue();
                if (Boolean.TRUE.equals(isVoting)) {
                    return; // Already processing a vote
                }
                commentsViewModel.voteOnComment(postId, comment.getId(), type);
            }
        });
        commentsAdapter.setOnCommentEditListener(comment -> {
            if (comment != null && comment.getId() != null && postId != null) {
                // Navigate to edit comment fragment
                Bundle args = new Bundle();
                args.putString("postId", postId);
                args.putString("commentId", comment.getId());
                Navigation.findNavController(binding.getRoot()).navigate(R.id.editCommentFragment, args);
            }
        });
        commentsAdapter.setOnCommentDeleteListener(comment -> {
            if (comment != null && comment.getId() != null && postId != null && getContext() != null) {
                // Show confirmation dialog
                new android.app.AlertDialog.Builder(getContext())
                        .setTitle("Delete Comment")
                        .setMessage("Are you sure you want to delete this comment?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            commentsViewModel.deleteComment(postId, comment.getId());
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        binding.commentsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.commentsRecyclerView.setAdapter(commentsAdapter);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        if (postId == null || postId.isEmpty()) {
            Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return;
        }

        postDetailViewModel.loadPost(postId);
        commentsViewModel.loadComments(postId);

        binding.upvoteButton.setOnClickListener(v -> vote("up"));
        binding.downvoteButton.setOnClickListener(v -> vote("down"));
        binding.commentButton.setOnClickListener(v -> focusOnCommentField());
        binding.addCommentButton.setOnClickListener(v -> addComment());

        observeViewModel();
    }

    private void observeViewModel() {
        // Observe post data (similar to how comments are observed)
        postDetailViewModel.getPost().observe(getViewLifecycleOwner(), post -> {
            if (binding == null || post == null) return;
            updatePostUI(post);
        });
        
        postDetailViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            // Optionally show/hide loading indicator
        });
        
        postDetailViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && binding != null && getContext() != null) {
                Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
            }
        });
        
        // Observe comments
        commentsViewModel.getComments().observe(getViewLifecycleOwner(), list -> {
            if (binding == null) return;
            commentsAdapter.submitList(list);
            displayedCommentCount = list != null ? list.size() : 0;
            updateCommentCountText(displayedCommentCount);
        });
        commentsViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null && binding != null && getContext() != null) {
                Toast.makeText(getContext(), err, Toast.LENGTH_LONG).show();
            }
        });
        commentsViewModel.isPostingComment().observe(getViewLifecycleOwner(), posting -> {
            if (binding == null) return;
            boolean inFlight = Boolean.TRUE.equals(posting);
            binding.addCommentButton.setEnabled(!inFlight);
            binding.commentEditText.setEnabled(!inFlight);
            if (binding.commentTitleEditText != null) {
                binding.commentTitleEditText.setEnabled(!inFlight);
            }
        });
        commentsViewModel.getLatestPostedComment().observe(getViewLifecycleOwner(), comment -> {
            if (comment == null || binding == null) return;
            binding.commentEditText.setText("");
            if (binding.commentTitleEditText != null) {
                binding.commentTitleEditText.setText("");
            }
            // Comments will be reloaded automatically by the ViewModel
            // Scroll to top after a brief delay to allow RecyclerView to update
            binding.commentsRecyclerView.post(() -> {
                if (binding != null) {
                    binding.commentsRecyclerView.scrollToPosition(0);
                }
            });
        });
    }
    
    private void updatePostUI(Post post) {
        if (binding == null || getContext() == null || post == null) return;

        currentPost = post;
        
        binding.titleTextView.setText(post.getTitle() != null ? post.getTitle() : "");
        
        // Display content or prompt sections based on post type
        boolean isPromptPost = post.isIs_prompt_post();
        commentsAdapter.setParentIsPrompt(isPromptPost);
        if (isPromptPost) {
            // For prompt posts, show prompt_section and description_section
            binding.contentTextView.setVisibility(View.GONE);
            
            if (post.getPrompt_section() != null && !post.getPrompt_section().trim().isEmpty()) {
                if (binding.promptSectionLabel != null) {
                    binding.promptSectionLabel.setVisibility(View.VISIBLE);
                }
                if (binding.promptSectionTextView != null) {
                    binding.promptSectionTextView.setText(post.getPrompt_section());
                    binding.promptSectionTextView.setVisibility(View.VISIBLE);
                }
            } else {
                if (binding.promptSectionLabel != null) {
                    binding.promptSectionLabel.setVisibility(View.GONE);
                }
                if (binding.promptSectionTextView != null) {
                    binding.promptSectionTextView.setVisibility(View.GONE);
                }
            }
            
            if (post.getDescription_section() != null && !post.getDescription_section().trim().isEmpty()) {
                if (binding.descriptionSectionLabel != null) {
                    binding.descriptionSectionLabel.setVisibility(View.VISIBLE);
                }
                if (binding.descriptionSectionTextView != null) {
                    binding.descriptionSectionTextView.setText(post.getDescription_section());
                    binding.descriptionSectionTextView.setVisibility(View.VISIBLE);
                }
            } else {
                if (binding.descriptionSectionLabel != null) {
                    binding.descriptionSectionLabel.setVisibility(View.GONE);
                }
                if (binding.descriptionSectionTextView != null) {
                    binding.descriptionSectionTextView.setVisibility(View.GONE);
                }
            }

        } else {
            // For regular posts, show content
            binding.contentTextView.setVisibility(View.VISIBLE);
            binding.contentTextView.setText(post.getContent() != null ? post.getContent() : "");

            // Hide prompt sections
            if (binding.promptSectionLabel != null) {
                binding.promptSectionLabel.setVisibility(View.GONE);
            }
            if (binding.promptSectionTextView != null) {
                binding.promptSectionTextView.setVisibility(View.GONE);
            }
            if (binding.descriptionSectionLabel != null) {
                binding.descriptionSectionLabel.setVisibility(View.GONE);
            }
            if (binding.descriptionSectionTextView != null) {
                binding.descriptionSectionTextView.setVisibility(View.GONE);
            }
            if (binding.promptDivider != null) {
                binding.promptDivider.setVisibility(View.GONE);
            }
        }

        Resources resources = getResources();
        String author = post.isAnonymous()
                ? resources.getString(R.string.post_author_anonymous)
                : (post.getAuthor_name() != null && !post.getAuthor_name().isEmpty()
                    ? post.getAuthor_name()
                    : resources.getString(R.string.post_meta_unknown_author));
        boolean hasTag = post.getLlm_tag() != null && !post.getLlm_tag().isEmpty();
        String tagLabel = hasTag
                ? resources.getString(R.string.post_tag_format, post.getLlm_tag())
                : resources.getString(R.string.post_tag_unknown);
        binding.tagTextView.setText(tagLabel);
        binding.authorTextView.setText(resources.getString(R.string.post_author_format, author));

        // Format and display date
        if (binding.dateTextView != null) {
            String dateText = formatDate(post.getCreated_at(), resources);
            binding.dateTextView.setText(dateText);
        }

        // Show delete button only for own posts
        String currentUserId = SessionManager.getUserId();
        boolean isOwnPost = currentUserId != null && post.getAuthor_id() != null 
                && currentUserId.equals(post.getAuthor_id());
        
        if (binding.deletePostButton != null) {
            if (isOwnPost) {
                binding.deletePostButton.setVisibility(View.VISIBLE);
                binding.deletePostButton.setOnClickListener(v -> deletePost());
            } else {
                binding.deletePostButton.setVisibility(View.GONE);
            }
        }

        String persistedVote = VotePreferenceManager.getPostVote(getContext(), post.getId());
        if (persistedVote != null && (post.getUser_vote_type() == null || post.getUser_vote_type().isEmpty())) {
            post.setUser_vote_type(persistedVote);
        }

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

        // Update vote counts - this will automatically update when post LiveData changes
        binding.upvoteCountTextView.setText(upvoteText);
        binding.downvoteCountTextView.setText(downvoteText);
        updateCommentCountText(displayedCommentCount);

        updateVoteButtons(post.getUser_vote_type());
    }

    private void vote(String type) {
        if (postId == null || binding == null) return;
        
        // Check buttons exist before accessing
        if (binding.upvoteButton == null || binding.downvoteButton == null) {
            return;
        }
        
        // Check if already voting to prevent rapid clicks (same pattern as comment voting)
        Boolean isVoting = postDetailViewModel.getLoading().getValue();
        if (Boolean.TRUE.equals(isVoting)) {
            return; // Already processing a vote
        }

        applyLocalVote(type);

        // Use ViewModel to vote (same pattern as comment voting)
        // This will automatically reload the post and update UI via LiveData
        postDetailViewModel.voteOnPost(postId, type);
    }

    private void applyLocalVote(String type) {
        if (currentPost == null || binding == null) return;

        String currentVote = currentPost.getUser_vote_type();
        String newVote = type;

        if ("up".equalsIgnoreCase(type) && "up".equalsIgnoreCase(currentVote)) {
            newVote = null;
        } else if ("down".equalsIgnoreCase(type) && "down".equalsIgnoreCase(currentVote)) {
            newVote = null;
        }

        currentPost.setUser_vote_type(newVote);
        VotePreferenceManager.setPostVote(getContext(), currentPost.getId(), newVote);
        updateVoteButtons(newVote);
    }

    private void updateVoteButtons(String userVoteType) {
        if (binding == null) return;
        boolean isUpvoted = "up".equalsIgnoreCase(userVoteType);
        boolean isDownvoted = "down".equalsIgnoreCase(userVoteType);

        if (binding.upvoteButton != null) {
            binding.upvoteButton.setImageResource(isUpvoted
                    ? R.drawable.ic_arrow_up_filled_24dp
                    : R.drawable.ic_arrow_up_outline_24dp);
        }
        if (binding.downvoteButton != null) {
            binding.downvoteButton.setImageResource(isDownvoted
                    ? R.drawable.ic_arrow_down_filled_24dp
                    : R.drawable.ic_arrow_down_outline_24dp);
        }
    }

    private void addComment() {
        if (binding == null || postId == null) return;
        String text = binding.commentEditText.getText() != null 
                ? binding.commentEditText.getText().toString().trim() 
                : "";
        if (TextUtils.isEmpty(text)) return;
        
        String title = null;
        if (binding.commentTitleEditText != null) {
            String titleText = binding.commentTitleEditText.getText() != null 
                    ? binding.commentTitleEditText.getText().toString().trim() 
                    : "";
            if (!TextUtils.isEmpty(titleText)) {
                title = titleText;
            }
        }
        
        commentsViewModel.addComment(postId, text, title);
    }

    private void focusOnCommentField() {
        if (binding == null || getContext() == null) return;
        binding.commentEditText.requestFocus();
        binding.postDetailScrollView.post(() -> {
            if (binding == null || getContext() == null) return;
            binding.postDetailScrollView.smoothScrollTo(0, binding.commentEditText.getBottom());
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.commentEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        });
    }

    private void updateCommentCountText(int commentCount) {
        if (binding == null || getContext() == null) return;
        Resources resources = getResources();
        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
        String commentsText = resources.getQuantityString(
                R.plurals.post_comments,
                commentCount,
                numberFormat.format(commentCount)
        );
        binding.commentCountTextView.setText(commentsText);
    }

    private String formatDate(String dateString, Resources resources) {
        if (dateString == null || dateString.isEmpty()) {
            return "";
        }

        // PostgreSQL TIMESTAMP returns ISO 8601 format
        // Try multiple formats: with milliseconds, without, with timezone, without
        String[] formats = {
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",  // With milliseconds and Z
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ",     // With milliseconds and timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSS",      // With milliseconds, no timezone
            "yyyy-MM-dd'T'HH:mm:ss'Z'",      // Without milliseconds, with Z
            "yyyy-MM-dd'T'HH:mm:ssZ",         // Without milliseconds, with timezone
            "yyyy-MM-dd'T'HH:mm:ss",          // Without milliseconds, no timezone
            "yyyy-MM-dd HH:mm:ss"             // Space separator (fallback)
        };

        for (String formatStr : formats) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
                // Set timezone to UTC for parsing
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = format.parse(dateString);
                if (date != null) {
                    return formatRelativeTime(date, resources);
                }
            } catch (ParseException e) {
                // Try next format
                continue;
            }
        }

        // If all parsing fails, return formatted original string
        return dateString.length() > 10 ? dateString.substring(0, 10) : dateString;
    }

    private String formatRelativeTime(Date date, Resources resources) {
        long now = System.currentTimeMillis();
        long diff = now - date.getTime();
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);

        if (days > 0) {
            if (days == 1) {
                return resources.getString(R.string.post_date_yesterday);
            } else if (days < 7) {
                return resources.getString(R.string.post_date_days_ago, (int)days);
            } else {
                SimpleDateFormat displayFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                return displayFormat.format(date);
            }
        } else if (hours > 0) {
            return resources.getString(R.string.post_date_hours_ago, (int)hours);
        } else if (minutes > 0) {
            return resources.getString(R.string.post_date_minutes_ago, (int)minutes);
        } else {
            return resources.getString(R.string.post_date_just_now);
        }
    }

    private void deletePost() {
        if (postId == null || binding == null || getContext() == null) return;

        // Show confirmation dialog
        new android.app.AlertDialog.Builder(getContext())
                .setTitle(R.string.delete_post_confirm_title)
                .setMessage(R.string.delete_post_confirm_message)
                .setPositiveButton(R.string.delete_post, (dialog, which) -> {
                    postRepository.deletePost(postId, new PostRepository.Callback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                                    }
                                    // Navigate back
                                    if (getActivity() != null) {
                                        getActivity().onBackPressed();
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(String error) {
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), error != null ? error : getString(R.string.delete_post_error), Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}