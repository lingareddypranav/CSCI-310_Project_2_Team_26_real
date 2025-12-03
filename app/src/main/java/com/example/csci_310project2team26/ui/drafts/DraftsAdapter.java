package com.example.csci_310project2team26.ui.drafts;

import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Draft;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftViewHolder> {

    private final List<Draft> drafts = new ArrayList<>();
    private final DraftActionListener actionListener;

    public DraftsAdapter(DraftActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draft, parent, false);
        return new DraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder holder, int position) {
        Draft draft = drafts.get(position);
        holder.bind(draft, actionListener);
    }

    @Override
    public int getItemCount() {
        return drafts.size();
    }

    public void submitList(List<Draft> newDrafts) {
        drafts.clear();
        if (newDrafts != null) {
            drafts.addAll(newDrafts);
        }
        notifyDataSetChanged();
    }

    static class DraftViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final TextView meta;
        private final TextView preview;
        private final MaterialButton useDraftButton;
        private final MaterialButton deleteDraftButton;

        DraftViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.draftTitle);
            meta = itemView.findViewById(R.id.draftMeta);
            preview = itemView.findViewById(R.id.draftPreview);
            useDraftButton = itemView.findViewById(R.id.useDraftButton);
            deleteDraftButton = itemView.findViewById(R.id.deleteDraftButton);
        }

        void bind(Draft draft, DraftActionListener actionListener) {
            title.setText(draft.getTitle());

            StringBuilder metaBuilder = new StringBuilder();
            metaBuilder.append(draft.isPrompt() ? itemView.getContext().getString(R.string.draft_meta_prompt)
                    : itemView.getContext().getString(R.string.draft_meta_post));
            if (!TextUtils.isEmpty(draft.getTag())) {
                metaBuilder.append(" • ").append(draft.getTag());
            }
            long updatedAtTimestamp = parseTimestamp(draft.getUpdatedAt());
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    updatedAtTimestamp > 0 ? updatedAtTimestamp : System.currentTimeMillis(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            metaBuilder.append(" • ").append(relativeTime);
            meta.setText(metaBuilder.toString());

            String previewText;
            if (draft.isPrompt()) {
                List<String> sections = new ArrayList<>();
                if (!TextUtils.isEmpty(draft.getPromptSection())) {
                    sections.add(itemView.getContext().getString(R.string.draft_prompt_label) + " " + draft.getPromptSection());
                }
                if (!TextUtils.isEmpty(draft.getDescriptionSection())) {
                    sections.add(itemView.getContext().getString(R.string.draft_description_label) + " " + draft.getDescriptionSection());
                }
                previewText = sections.isEmpty() ? draft.getBody() : TextUtils.join("\n", sections);
            } else {
                previewText = draft.getBody();
            }
            preview.setText(previewText);

            useDraftButton.setOnClickListener(v -> actionListener.onUseDraft(draft));
            deleteDraftButton.setOnClickListener(v -> actionListener.onDeleteDraft(draft));
        }

        private long parseTimestamp(String dateString) {
            if (TextUtils.isEmpty(dateString)) {
                return 0L;
            }
            try {
                // Try parsing as ISO 8601 date string
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault());
                format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                java.util.Date date = format.parse(dateString);
                if (date != null) {
                    return date.getTime();
                }
            } catch (Exception e) {
                // Try parsing as long timestamp
                try {
                    return Long.parseLong(dateString);
                } catch (NumberFormatException e2) {
                    return 0L;
                }
            }
            return 0L;
        }
    }

    public interface DraftActionListener {
        void onUseDraft(Draft draft);

        void onDeleteDraft(Draft draft);
    }
}
