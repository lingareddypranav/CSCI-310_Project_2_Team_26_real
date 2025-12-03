package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Draft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class DraftsViewModel extends ViewModel {
    private final MutableLiveData<List<Draft>> drafts = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Draft>> getDrafts() {
        return drafts;
    }

    public void saveDraft(String title,
                          String body,
                          String tag,
                          boolean prompt,
                          String promptSection,
                          String descriptionSection) {
        if (title == null || title.trim().isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        Draft newDraft = new Draft(
                UUID.randomUUID().toString(),
                title.trim(),
                body != null ? body.trim() : "",
                tag != null ? tag.trim() : "",
                prompt,
                promptSection != null ? promptSection.trim() : "",
                descriptionSection != null ? descriptionSection.trim() : "",
                now
        );

        List<Draft> current = drafts.getValue();
        if (current == null) {
            current = new ArrayList<>();
        } else {
            current = new ArrayList<>(current);
        }
        current.add(newDraft);
        current.sort(Comparator.comparingLong(Draft::getUpdatedAt).reversed());
        drafts.postValue(Collections.unmodifiableList(current));
    }
}
