package com.example.csci_310project2team26.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;

import java.util.ArrayList;
import java.util.List;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostViewHolder> {

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private final List<Post> items = new ArrayList<>();
    private final OnPostClickListener clickListener;

    public PostsAdapter(OnPostClickListener clickListener) {
        this.clickListener = clickListener;
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
        holder.bind(post);
        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onPostClick(post);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView metaTextView;
        private final TextView contentTextView;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            metaTextView = itemView.findViewById(R.id.metaTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }

        public void bind(Post post) {
            titleTextView.setText(post.getTitle());
            String meta = (post.getAuthor_name() != null ? post.getAuthor_name() : "")
                    + "  •  " + (post.getLlm_tag() != null ? post.getLlm_tag() : "")
                    + "  •  "+ post.getComment_count() + " comments";
            metaTextView.setText(meta);
            contentTextView.setText(post.getContent());
        }
    }
}


