package com.example.csci_310project2team26.ui.home;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Comment;
import com.example.csci_310project2team26.data.repository.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final List<Comment> items = new ArrayList<>();
    private OnCommentVoteListener voteListener;
    private OnCommentEditListener editListener;
    private OnCommentDeleteListener deleteListener;
    private String currentUserId;
    private boolean parentIsPrompt;

    public CommentsAdapter() {
        this.currentUserId = SessionManager.getUserId();
    }

    public void submitList(List<Comment> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnCommentVoteListener(OnCommentVoteListener listener) {
        this.voteListener = listener;
    }

    public void setOnCommentEditListener(OnCommentEditListener listener) {
        this.editListener = listener;
    }

    public void setOnCommentDeleteListener(OnCommentDeleteListener listener) {
        this.deleteListener = listener;
    }

    public void setParentIsPrompt(boolean parentIsPrompt) {
        this.parentIsPrompt = parentIsPrompt;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(items.get(position), voteListener, editListener, deleteListener, currentUserId);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView authorTextView;
        private final TextView dateTextView;
        private final TextView contextTextView;
        private final TextView titleTextView;
        private final TextView textTextView;
        private final ImageButton upvoteButton;
        private final ImageButton downvoteButton;
        private final TextView upvoteCountTextView;
        private final TextView downvoteCountTextView;
        private final Button editCommentButton;
        private final Button deleteCommentButton;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            contextTextView = itemView.findViewById(R.id.contextTextView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            textTextView = itemView.findViewById(R.id.textTextView);
            upvoteButton = itemView.findViewById(R.id.btnCommentUpvote);
            downvoteButton = itemView.findViewById(R.id.btnCommentDownvote);
            upvoteCountTextView = itemView.findViewById(R.id.upvoteCountTextView);
            downvoteCountTextView = itemView.findViewById(R.id.downvoteCountTextView);
            editCommentButton = itemView.findViewById(R.id.editCommentButton);
            deleteCommentButton = itemView.findViewById(R.id.deleteCommentButton);
        }

        public void bind(Comment comment, OnCommentVoteListener voteListener, 
                        OnCommentEditListener editListener, OnCommentDeleteListener deleteListener,
                        String currentUserId) {
            if (comment == null) {
                return;
            }
            
            Resources resources = itemView.getContext().getResources();
            
            // Author and date
            authorTextView.setText(comment.getAuthor_name() != null ? comment.getAuthor_name() : "");
            String dateText = formatDate(comment.getCreated_at(), resources);
            dateTextView.setText(dateText);

            if (contextTextView != null) {
                contextTextView.setVisibility(View.VISIBLE);
                contextTextView.setText(parentIsPrompt
                        ? resources.getString(R.string.comment_context_prompt)
                        : resources.getString(R.string.comment_context_post));
            }
            
            // Title (show only if exists)
            if (comment.getTitle() != null && !comment.getTitle().trim().isEmpty()) {
                titleTextView.setText(comment.getTitle());
                titleTextView.setVisibility(View.VISIBLE);
            } else {
                titleTextView.setVisibility(View.GONE);
            }
            
            // Text
            textTextView.setText(comment.getText() != null ? comment.getText() : "");
            
            // Vote counts
            int upvotes = Math.max(comment.getUpvotes(), 0);
            int downvotes = Math.max(comment.getDownvotes(), 0);
            upvoteCountTextView.setText(String.valueOf(upvotes));
            downvoteCountTextView.setText(String.valueOf(downvotes));

            updateVoteIcons(comment.getUser_vote_type());

            // Vote buttons
            upvoteButton.setOnClickListener(v -> {
                if (voteListener != null && comment.getId() != null && !comment.getId().isEmpty()) {
                    voteListener.onVote(comment, "up");
                }
            });
            downvoteButton.setOnClickListener(v -> {
                if (voteListener != null && comment.getId() != null && !comment.getId().isEmpty()) {
                    voteListener.onVote(comment, "down");
                }
            });
            
            // Edit/Delete buttons (only show for own comments)
            boolean isOwnComment = currentUserId != null && comment.getAuthor_id() != null 
                    && currentUserId.equals(comment.getAuthor_id());
            if (isOwnComment) {
                editCommentButton.setVisibility(View.VISIBLE);
                deleteCommentButton.setVisibility(View.VISIBLE);
                
                editCommentButton.setOnClickListener(v -> {
                    if (editListener != null && comment.getId() != null && !comment.getId().isEmpty()) {
                        editListener.onEdit(comment);
                    }
                });
                
                deleteCommentButton.setOnClickListener(v -> {
                    if (deleteListener != null && comment.getId() != null && !comment.getId().isEmpty()) {
                        deleteListener.onDelete(comment);
                    }
                });
            } else {
                editCommentButton.setVisibility(View.GONE);
                deleteCommentButton.setVisibility(View.GONE);
            }
        }

        private void updateVoteIcons(String userVoteType) {
            if (upvoteButton != null) {
                boolean isUpvoted = "up".equalsIgnoreCase(userVoteType);
                upvoteButton.setImageResource(isUpvoted
                        ? R.drawable.ic_arrow_up_filled_24dp
                        : R.drawable.ic_arrow_up_outline_24dp);
            }
            if (downvoteButton != null) {
                boolean isDownvoted = "down".equalsIgnoreCase(userVoteType);
                downvoteButton.setImageResource(isDownvoted
                        ? R.drawable.ic_arrow_down_filled_24dp
                        : R.drawable.ic_arrow_down_outline_24dp);
            }
        }
        
        private String formatDate(String dateString, Resources resources) {
            if (dateString == null || dateString.isEmpty()) {
                return "";
            }

            String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
                "yyyy-MM-dd'T'HH:mm:ss.SSS",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ssZ",
                "yyyy-MM-dd'T'HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss"
            };

            for (String formatStr : formats) {
                try {
                    SimpleDateFormat format = new SimpleDateFormat(formatStr, Locale.getDefault());
                    format.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = format.parse(dateString);
                    if (date != null) {
                        return formatRelativeTime(date, resources);
                    }
                } catch (ParseException e) {
                    continue;
                }
            }

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

    public interface OnCommentVoteListener {
        void onVote(Comment comment, String type);
    }
    
    public interface OnCommentEditListener {
        void onEdit(Comment comment);
    }
    
    public interface OnCommentDeleteListener {
        void onDelete(Comment comment);
    }
}