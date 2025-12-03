package com.example.csci_310project2team26.ui.versions;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.PostVersion;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostVersionsAdapter extends ListAdapter<PostVersion, PostVersionsAdapter.VersionViewHolder> {

    public interface OnVersionClickListener {
        void onRevertClicked(PostVersion version);
    }

    private final OnVersionClickListener listener;

    public PostVersionsAdapter(OnVersionClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public VersionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_version, parent, false);
        return new VersionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VersionViewHolder holder, int position) {
        PostVersion version = getItem(position);
        holder.bind(version, listener);
    }

    static class VersionViewHolder extends RecyclerView.ViewHolder {
        private final TextView versionNumberTextView;
        private final TextView versionDateTextView;
        private final TextView versionTitleTextView;
        private final TextView versionContentTextView;
        private final MaterialButton revertButton;

        VersionViewHolder(@NonNull View itemView) {
            super(itemView);
            versionNumberTextView = itemView.findViewById(R.id.versionNumberTextView);
            versionDateTextView = itemView.findViewById(R.id.versionDateTextView);
            versionTitleTextView = itemView.findViewById(R.id.versionTitleTextView);
            versionContentTextView = itemView.findViewById(R.id.versionContentTextView);
            revertButton = itemView.findViewById(R.id.revertButton);
        }

        void bind(PostVersion version, OnVersionClickListener listener) {
            if (version == null) {
                return;
            }

            versionNumberTextView.setText("Version " + version.getVersion_number());
            versionTitleTextView.setText(version.getTitle() != null ? version.getTitle() : "");

            // Format date
            String dateText = formatDate(version.getCreated_at());
            versionDateTextView.setText(dateText);

            // Show content preview
            String contentPreview = "";
            if (version.isIs_prompt_post()) {
                if (!TextUtils.isEmpty(version.getPrompt_section())) {
                    contentPreview = version.getPrompt_section();
                } else if (!TextUtils.isEmpty(version.getDescription_section())) {
                    contentPreview = version.getDescription_section();
                }
            } else {
                contentPreview = version.getContent() != null ? version.getContent() : "";
            }

            if (contentPreview.length() > 150) {
                contentPreview = contentPreview.substring(0, 150) + "...";
            }
            versionContentTextView.setText(contentPreview);
            versionContentTextView.setVisibility(TextUtils.isEmpty(contentPreview) ? View.GONE : View.VISIBLE);

            revertButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRevertClicked(version);
                }
            });
        }

        private String formatDate(String dateString) {
            if (TextUtils.isEmpty(dateString)) {
                return "";
            }

            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                Date date = format.parse(dateString);
                if (date != null) {
                    return DateUtils.getRelativeTimeSpanString(
                        date.getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString();
                }
            } catch (Exception e) {
                // Try parsing as long
                try {
                    long timestamp = Long.parseLong(dateString);
                    return DateUtils.getRelativeTimeSpanString(
                        timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString();
                } catch (NumberFormatException e2) {
                    return dateString;
                }
            }
            return dateString;
        }
    }

    private static final DiffUtil.ItemCallback<PostVersion> DIFF_CALLBACK = new DiffUtil.ItemCallback<PostVersion>() {
        @Override
        public boolean areItemsTheSame(@NonNull PostVersion oldItem, @NonNull PostVersion newItem) {
            String oldId = oldItem != null ? oldItem.getId() : null;
            String newId = newItem != null ? newItem.getId() : null;
            return oldId != null && oldId.equals(newId);
        }

        @Override
        public boolean areContentsTheSame(@NonNull PostVersion oldItem, @NonNull PostVersion newItem) {
            if (oldItem == null || newItem == null) {
                return false;
            }
            return oldItem.getVersion_number() == newItem.getVersion_number()
                && TextUtils.equals(oldItem.getTitle(), newItem.getTitle());
        }
    };
}

