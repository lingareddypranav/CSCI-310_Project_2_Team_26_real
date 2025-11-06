package com.example.csci_310project2team26.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Comment;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private final List<Comment> items = new ArrayList<>();
    private OnCommentVoteListener voteListener;

    public void submitList(List<Comment> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    public void setOnCommentVoteListener(OnCommentVoteListener listener) {
        this.voteListener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(items.get(position), voteListener);
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final TextView authorTextView;
        private final TextView textTextView;
        private final Button upvoteButton;
        private final Button downvoteButton;
        private final TextView upvoteCountTextView;
        private final TextView downvoteCountTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.authorTextView);
            textTextView = itemView.findViewById(R.id.textTextView);
            upvoteButton = itemView.findViewById(R.id.btnCommentUpvote);
            downvoteButton = itemView.findViewById(R.id.btnCommentDownvote);
            upvoteCountTextView = itemView.findViewById(R.id.upvoteCountTextView);
            downvoteCountTextView = itemView.findViewById(R.id.downvoteCountTextView);
        }

        public void bind(Comment comment, OnCommentVoteListener listener) {
            authorTextView.setText(comment.getAuthor_name());
            textTextView.setText(comment.getText());
            upvoteCountTextView.setText(String.valueOf(Math.max(comment.getUpvotes(), 0)));
            downvoteCountTextView.setText(String.valueOf(Math.max(comment.getDownvotes(), 0)));

            upvoteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVote(comment, "up");
                }
            });
            downvoteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onVote(comment, "down");
                }
            });
        }
    }

    public interface OnCommentVoteListener {
        void onVote(Comment comment, String type);
    }
}