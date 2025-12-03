package com.example.csci_310project2team26.ui.home;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.SessionManager;
import com.example.csci_310project2team26.data.repository.BookmarkManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    public interface OnPostDeletedListener {
        void onPostDeleted(String postId);
    }

    public interface OnBookmarkToggleListener {
        void onBookmarkToggle(Post post, boolean isBookmarked);
    }

    public interface OnPostVoteListener {
        void onVote(Post post, String type);
    }

    private final List<Post> items = new ArrayList<>();
    private final OnPostClickListener clickListener;
    private OnPostDeletedListener deleteListener;
    private OnBookmarkToggleListener bookmarkToggleListener;
    private OnPostVoteListener voteListener;
    private long sessionVersion = SessionManager.getSessionVersion();

    public PostsAdapter(OnPostClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnPostDeletedListener(OnPostDeletedListener listener) {
        this.deleteListener = listener;
    }

    public void setOnBookmarkToggleListener(OnBookmarkToggleListener listener) {
        this.bookmarkToggleListener = listener;
    }

    public void setOnPostVoteListener(OnPostVoteListener listener) {
        this.voteListener = listener;
    }

    public void submitList(List<Post> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        long latestSession = SessionManager.getSessionVersion();
        if (latestSession != sessionVersion) {
            sessionVersion = latestSession;
            clearLocalVoteSelections();
        }
        Post post = items.get(position);
        if (post == null) {
            return;
        }
        holder.bind(post, clickListener, deleteListener, bookmarkToggleListener, voteListener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    private void clearLocalVoteSelections() {
        for (Post post : items) {
            if (post != null) {
                post.setUser_vote_type(null);
            }
        }
        notifyDataSetChanged();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView tagTextView;
        private final TextView postTypeTextView;
        private final TextView authorTextView;
        private final TextView dateTextView;
        private final TextView contentTextView;
        private final TextView promptSectionTextView;
        private final TextView descriptionSectionTextView;
        private final View promptDivider;
        private final TextView upvoteTextView;
        private final TextView downvoteTextView;
        private final TextView commentCountTextView;
        private final ImageButton deleteButton;
        private final ImageButton bookmarkButton;
        private final ImageButton upvoteButton;
        private final ImageButton downvoteButton;
        private final NumberFormat numberFormat;
        private final SimpleDateFormat dateFormat;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            tagTextView = itemView.findViewById(R.id.tagTextView);
            postTypeTextView = itemView.findViewById(R.id.postTypeTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            promptSectionTextView = itemView.findViewById(R.id.promptSectionTextView);
            descriptionSectionTextView = itemView.findViewById(R.id.descriptionSectionTextView);
            promptDivider = itemView.findViewById(R.id.promptDivider);
            upvoteTextView = itemView.findViewById(R.id.upvoteTextView);
            downvoteTextView = itemView.findViewById(R.id.downvoteTextView);
            commentCountTextView = itemView.findViewById(R.id.commentCountTextView);
            deleteButton = itemView.findViewById(R.id.deletePostButton);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            upvoteButton = itemView.findViewById(R.id.upvoteButton);
            downvoteButton = itemView.findViewById(R.id.downvoteButton);
            numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        }

        public void bind(Post post,
                        OnPostClickListener clickListener,
                        OnPostDeletedListener deleteListener,
                        OnBookmarkToggleListener bookmarkToggleListener,
                        OnPostVoteListener voteListener) {
            if (post == null) {
                return;
            }
            Resources resources = itemView.getResources();
            titleTextView.setText(post.getTitle() != null ? post.getTitle() : "");

            String author = post.isAnonymous()
                    ? resources.getString(R.string.post_author_anonymous)
                    : (post.getAuthor_name() != null && !post.getAuthor_name().isEmpty()
                        ? post.getAuthor_name()
                        : resources.getString(R.string.post_meta_unknown_author));
            boolean hasTag = post.getLlm_tag() != null && !post.getLlm_tag().isEmpty();
            String tagLabel = hasTag
                    ? resources.getString(R.string.post_tag_format, post.getLlm_tag())
                    : resources.getString(R.string.post_tag_unknown);
            tagTextView.setText(tagLabel);
            if (postTypeTextView != null) {
                postTypeTextView.setText(post.isIs_prompt_post()
                        ? resources.getString(R.string.post_type_label_prompt)
                        : resources.getString(R.string.post_type_label_post));
            }
            authorTextView.setText(resources.getString(R.string.post_author_format, author));

            // Format and display date
            if (dateTextView != null) {
                String dateText = formatDate(post.getCreated_at(), resources);
                dateTextView.setText(dateText);
            }

            // For prompt posts, show prompt + description preview; for regular posts, show content
            if (post.isIs_prompt_post()) {
                contentTextView.setVisibility(View.GONE);
                if (promptSectionTextView != null) {
                    if (post.getPrompt_section() != null && !post.getPrompt_section().trim().isEmpty()) {
                        promptSectionTextView.setText(post.getPrompt_section().trim());
                        promptSectionTextView.setVisibility(View.VISIBLE);
                    } else {
                        promptSectionTextView.setVisibility(View.GONE);
                    }
                }
                if (descriptionSectionTextView != null) {
                    if (post.getDescription_section() != null && !post.getDescription_section().trim().isEmpty()) {
                        descriptionSectionTextView.setText(post.getDescription_section().trim());
                        descriptionSectionTextView.setVisibility(View.VISIBLE);
                    } else {
                        descriptionSectionTextView.setVisibility(View.GONE);
                    }
                }
                if (promptDivider != null) {
                    boolean showDivider = promptSectionTextView != null
                            && promptSectionTextView.getVisibility() == View.VISIBLE
                            && descriptionSectionTextView != null
                            && descriptionSectionTextView.getVisibility() == View.VISIBLE;
                    promptDivider.setVisibility(showDivider ? View.VISIBLE : View.GONE);
                }
            } else {
                contentTextView.setVisibility(View.VISIBLE);
                contentTextView.setText(post.getContent() != null ? post.getContent() : "");
                if (promptSectionTextView != null) {
                    promptSectionTextView.setVisibility(View.GONE);
                }
                if (descriptionSectionTextView != null) {
                    descriptionSectionTextView.setVisibility(View.GONE);
                }
                if (promptDivider != null) {
                    promptDivider.setVisibility(View.GONE);
                }
            }

            int upvotes = Math.max(post.getUpvotes(), 0);
            int downvotes = Math.max(post.getDownvotes(), 0);
            int commentCount = Math.max(post.getComment_count(), 0);
            String commentsText = resources.getQuantityString(
                    R.plurals.post_comments,
                    commentCount,
                    numberFormat.format(commentCount)
            );

            upvoteTextView.setText(numberFormat.format(upvotes));
            downvoteTextView.setText(numberFormat.format(downvotes));
            commentCountTextView.setText(commentsText);

            updateVoteIcons(post.getUser_vote_type());

            if (upvoteButton != null) {
                upvoteButton.setOnClickListener(v -> {
                    toggleVoteSelection(post, "up");
                    if (voteListener != null) {
                        voteListener.onVote(post, "up");
                    }
                });
            }
            if (downvoteButton != null) {
                downvoteButton.setOnClickListener(v -> {
                    toggleVoteSelection(post, "down");
                    if (voteListener != null) {
                        voteListener.onVote(post, "down");
                    }
                });
            }

            // Show delete button only for own posts
            String currentUserId = SessionManager.getUserId();
            boolean isOwnPost = currentUserId != null && post.getAuthor_id() != null 
                    && currentUserId.equals(post.getAuthor_id());
            
            if (deleteButton != null) {
                if (isOwnPost && deleteListener != null) {
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(v -> {
                        // Consume the click event to prevent post click
                        if (post.getId() != null) {
                            deleteListener.onPostDeleted(post.getId());
                        }
                    });
                } else {
                    deleteButton.setVisibility(View.GONE);
                    deleteButton.setOnClickListener(null);
                }
            }

            if (bookmarkButton != null) {
                boolean isBookmarked = BookmarkManager.isBookmarked(post);
                updateBookmarkIcon(isBookmarked);
                bookmarkButton.setOnClickListener(v -> {
                    boolean newState = BookmarkManager.toggleBookmark(post);
                    updateBookmarkIcon(newState);
                    if (bookmarkToggleListener != null) {
                        bookmarkToggleListener.onBookmarkToggle(post, newState);
                    }
                });
            }

            // Set click listener for post
            itemView.setOnClickListener(v -> {
                if (clickListener != null && post.getId() != null && !post.getId().isEmpty()) {
                    clickListener.onPostClick(post);
                }
            });
        }

        private void updateBookmarkIcon(boolean isBookmarked) {
            if (bookmarkButton == null) return;
            bookmarkButton.setImageResource(isBookmarked
                    ? R.drawable.ic_bookmark_filled_24dp
                    : R.drawable.ic_bookmark_border_24dp);
            bookmarkButton.setContentDescription(isBookmarked
                    ? itemView.getResources().getString(R.string.remove_bookmark)
                    : itemView.getResources().getString(R.string.add_bookmark));
        }

        private void updateVoteIcons(String userVoteType) {
            boolean isUpvoted = "up".equalsIgnoreCase(userVoteType);
            boolean isDownvoted = "down".equalsIgnoreCase(userVoteType);

            if (upvoteButton != null) {
                upvoteButton.setImageResource(isUpvoted
                        ? R.drawable.ic_arrow_up_filled_24dp
                        : R.drawable.ic_arrow_up_outline_24dp);
            }

            if (downvoteButton != null) {
                downvoteButton.setImageResource(isDownvoted
                        ? R.drawable.ic_arrow_down_filled_24dp
                        : R.drawable.ic_arrow_down_outline_24dp);
            }
        }

        private void toggleVoteSelection(Post post, String type) {
            if (post == null) return;

            String currentVote = post.getUser_vote_type();
            String newVote = type;

            if ("up".equalsIgnoreCase(type) && "up".equalsIgnoreCase(currentVote)) {
                newVote = null;
            } else if ("down".equalsIgnoreCase(type) && "down".equalsIgnoreCase(currentVote)) {
                newVote = null;
            }

            post.setUser_vote_type(newVote);
            updateVoteIcons(newVote);
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
    }
}