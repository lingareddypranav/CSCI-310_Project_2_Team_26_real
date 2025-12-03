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

import java.util.ArrayList;
import java.util.List;

public class DraftsAdapter extends RecyclerView.Adapter<DraftsAdapter.DraftViewHolder> {

    private final List<Draft> drafts = new ArrayList<>();

    @NonNull
    @Override
    public DraftViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draft, parent, false);
        return new DraftViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftViewHolder holder, int position) {
        Draft draft = drafts.get(position);
        holder.bind(draft);
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

        DraftViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.draftTitle);
            meta = itemView.findViewById(R.id.draftMeta);
            preview = itemView.findViewById(R.id.draftPreview);
        }

        void bind(Draft draft) {
            title.setText(draft.getTitle());

            StringBuilder metaBuilder = new StringBuilder();
            metaBuilder.append(draft.isPrompt() ? itemView.getContext().getString(R.string.draft_meta_prompt)
                    : itemView.getContext().getString(R.string.draft_meta_post));
            if (!TextUtils.isEmpty(draft.getTag())) {
                metaBuilder.append(" • ").append(draft.getTag());
            }
            CharSequence relativeTime = DateUtils.getRelativeTimeSpanString(
                    draft.getUpdatedAt(),
                    System.currentTimeMillis(),
                    DateUtils.MINUTE_IN_MILLIS
            );
            metaBuilder.append(" • ").append(relativeTime);
            meta.setText(metaBuilder.toString());

            String previewText;
            if (draft.isPrompt()) {
                previewText = !TextUtils.isEmpty(draft.getPromptSection())
                        ? draft.getPromptSection()
                        : draft.getDescriptionSection();
            } else {
                previewText = draft.getBody();
            }
            preview.setText(previewText);
        }
    }
}
