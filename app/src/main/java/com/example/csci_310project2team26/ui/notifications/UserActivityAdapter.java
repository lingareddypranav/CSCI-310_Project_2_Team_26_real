package com.example.csci_310project2team26.ui.notifications;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.google.android.material.button.MaterialButton;

public class UserActivityAdapter extends ListAdapter<UserActivityItem, UserActivityAdapter.ActivityViewHolder> {

    public interface OnActivityClickListener {
        void onActivityClicked(UserActivityItem item);
        void onActivityDeleteClicked(UserActivityItem item);
        void onVersionHistoryClicked(UserActivityItem item);
    }

    private final OnActivityClickListener listener;

    public UserActivityAdapter(OnActivityClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        UserActivityItem item = getItem(position);
        holder.bind(item, listener);
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        private final ImageView typeIconImageView;
        private final TextView titleTextView;
        private final TextView subtitleTextView;
        private final MaterialButton versionHistoryButton;
        private final MaterialButton deleteButton;

        ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            typeIconImageView = itemView.findViewById(R.id.typeIconImageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            subtitleTextView = itemView.findViewById(R.id.subtitleTextView);
            versionHistoryButton = itemView.findViewById(R.id.versionHistoryButton);
            deleteButton = itemView.findViewById(R.id.deleteActivityButton);
        }

        void bind(UserActivityItem item, OnActivityClickListener listener) {
            titleTextView.setText(item.getTitle());
            int iconRes;
            if (item.getType() == UserActivityItem.Type.POST) {
                iconRes = item.isPromptPost()
                        ? R.drawable.ic_dashboard_black_24dp
                        : R.drawable.ic_home_black_24dp;
            } else {
                iconRes = R.drawable.ic_notifications_black_24dp;
            }
            typeIconImageView.setImageResource(iconRes);

            String label = item.getType() == UserActivityItem.Type.POST
                    ? itemView.getContext().getString(R.string.user_activity_post_label)
                    : itemView.getContext().getString(R.string.user_activity_comment_label);
            CharSequence relative = DateUtils.getRelativeTimeSpanString(
                    item.getTimestamp(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            String detail = item.getSubtitle();
            StringBuilder subtitleBuilder = new StringBuilder();
            subtitleBuilder.append(label).append(" • ").append(relative);
            if (!TextUtils.isEmpty(detail)) {
                if (item.getType() == UserActivityItem.Type.COMMENT) {
                    String parentLabel = item.isPromptPost()
                            ? itemView.getContext().getString(R.string.post_type_label_prompt)
                            : itemView.getContext().getString(R.string.post_type_label_post);
                    subtitleBuilder.append('\n').append(detail);
                } else {
                    subtitleBuilder.append(" • ").append(detail);
                }
            }
            subtitleTextView.setText(subtitleBuilder.toString());
            
            // Show version history button only for posts
            if (item.getType() == UserActivityItem.Type.POST) {
                versionHistoryButton.setVisibility(View.VISIBLE);
                versionHistoryButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onVersionHistoryClicked(item);
                    }
                });
            } else {
                versionHistoryButton.setVisibility(View.GONE);
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityClicked(item);
                }
            });
            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActivityDeleteClicked(item);
                }
            });
        }
    }

    private static final DiffUtil.ItemCallback<UserActivityItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<UserActivityItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull UserActivityItem oldItem, @NonNull UserActivityItem newItem) {
            String oldId = oldItem != null ? oldItem.getId() : null;
            String newId = newItem != null ? newItem.getId() : null;
            if (oldId == null || newId == null) {
                return false;
            }
            return oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull UserActivityItem oldItem, @NonNull UserActivityItem newItem) {
            if (oldItem == null || newItem == null) {
                return false;
            }
            String oldTitle = oldItem.getTitle() != null ? oldItem.getTitle() : "";
            String newTitle = newItem.getTitle() != null ? newItem.getTitle() : "";
            String oldSubtitle = oldItem.getSubtitle() != null ? oldItem.getSubtitle() : "";
            String newSubtitle = newItem.getSubtitle() != null ? newItem.getSubtitle() : "";
            return oldItem.getTimestamp() == newItem.getTimestamp()
                    && oldTitle.equals(newTitle)
                    && oldSubtitle.equals(newSubtitle)
                    && oldItem.getType() == newItem.getType()
                    && oldItem.isPromptPost() == newItem.isPromptPost();
        }
    };
}
