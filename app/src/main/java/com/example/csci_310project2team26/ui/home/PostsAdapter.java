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
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.data.repository.SessionManager;

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

    private final List<Post> items = new ArrayList<>();
    private final OnPostClickListener clickListener;
    private OnPostDeletedListener deleteListener;

    public PostsAdapter(OnPostClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOnPostDeletedListener(OnPostDeletedListener listener) {
        this.deleteListener = listener;
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
        Post post = items.get(position);
        if (post == null) {
            return;
        }
        holder.bind(post, clickListener, deleteListener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView tagTextView;
        private final TextView authorTextView;
        private final TextView dateTextView;
        private final TextView contentTextView;
        private final TextView upvoteTextView;
        private final TextView downvoteTextView;
        private final TextView commentCountTextView;
        private final ImageButton deleteButton;
        private final NumberFormat numberFormat;
        private final SimpleDateFormat dateFormat;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            tagTextView = itemView.findViewById(R.id.tagTextView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            upvoteTextView = itemView.findViewById(R.id.upvoteTextView);
            downvoteTextView = itemView.findViewById(R.id.downvoteTextView);
            commentCountTextView = itemView.findViewById(R.id.commentCountTextView);
            deleteButton = itemView.findViewById(R.id.deletePostButton);
            numberFormat = NumberFormat.getIntegerInstance(Locale.getDefault());
            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        }

        public void bind(Post post, OnPostClickListener clickListener, OnPostDeletedListener deleteListener) {
            if (post == null) {
                return;
            }
            Resources resources = itemView.getResources();
            titleTextView.setText(post.getTitle() != null ? post.getTitle() : "");

            String author = post.getAuthor_name() != null && !post.getAuthor_name().isEmpty()
                    ? post.getAuthor_name()
                    : resources.getString(R.string.post_meta_unknown_author);
            boolean hasTag = post.getLlm_tag() != null && !post.getLlm_tag().isEmpty();
            String tagLabel = hasTag
                    ? resources.getString(R.string.post_tag_format, post.getLlm_tag())
                    : resources.getString(R.string.post_tag_unknown);
            tagTextView.setText(tagLabel);
            authorTextView.setText(resources.getString(R.string.post_author_format, author));

            // Format and display date
            if (dateTextView != null) {
                String dateText = formatDate(post.getCreated_at(), resources);
                dateTextView.setText(dateText);
            }

            // For prompt posts, show prompt section preview; for regular posts, show content
            if (post.isIs_prompt_post() && post.getPrompt_section() != null && !post.getPrompt_section().trim().isEmpty()) {
                contentTextView.setText(post.getPrompt_section());
            } else {
                contentTextView.setText(post.getContent() != null ? post.getContent() : "");
            }

            int upvotes = Math.max(post.getUpvotes(), 0);
            int downvotes = Math.max(post.getDownvotes(), 0);
            int commentCount = Math.max(post.getComment_count(), 0);

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
            String commentsText = resources.getQuantityString(
                    R.plurals.post_comments,
                    commentCount,
                    numberFormat.format(commentCount)
            );

            upvoteTextView.setText(upvoteText);
            downvoteTextView.setText(downvoteText);
            commentCountTextView.setText(commentsText);

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

            // Set click listener for post
            itemView.setOnClickListener(v -> {
                if (clickListener != null && post.getId() != null && !post.getId().isEmpty()) {
                    clickListener.onPostClick(post);
                }
            });
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