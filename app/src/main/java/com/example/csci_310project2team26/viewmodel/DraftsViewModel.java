package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Draft;
import com.example.csci_310project2team26.data.repository.DraftRepository;

import java.util.ArrayList;
import java.util.List;

public class DraftsViewModel extends ViewModel {
    private final DraftRepository draftRepository = new DraftRepository();
    
    private final MutableLiveData<List<Draft>> drafts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Draft> draftToResume = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    public LiveData<List<Draft>> getDrafts() {
        return drafts;
    }

    public LiveData<Draft> getDraftToResume() {
        return draftToResume;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadDrafts() {
        loading.postValue(true);
        error.postValue(null);

        draftRepository.getDrafts(new DraftRepository.Callback<List<Draft>>() {
            @Override
            public void onSuccess(List<Draft> result) {
                loading.postValue(false);
                drafts.postValue(result != null ? result : new ArrayList<>());
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
                drafts.postValue(new ArrayList<>());
            }
        });
    }

    public void saveDraft(String title,
                          String body,
                          String tag,
                          boolean prompt,
                          String promptSection,
                          String descriptionSection,
                          boolean anonymous) {
        if (title == null || title.trim().isEmpty()) {
            error.postValue("Title is required");
            return;
        }

        loading.postValue(true);
        error.postValue(null);

        draftRepository.createDraft(
            title.trim(),
            body != null ? body.trim() : null,
            promptSection != null ? promptSection.trim() : null,
            descriptionSection != null ? descriptionSection.trim() : null,
            tag != null ? tag.trim() : null,
            prompt,
            anonymous,
            new DraftRepository.Callback<Draft>() {
                @Override
                public void onSuccess(Draft result) {
                    loading.postValue(false);
                    loadDrafts(); // Reload to get updated list
                }

                @Override
                public void onError(String err) {
                    loading.postValue(false);
                    error.postValue(err);
                }
            }
        );
    }

    public void selectDraft(Draft draft) {
        draftToResume.postValue(draft);
    }

    public void clearDraftToResume() {
        draftToResume.postValue(null);
    }

    public void deleteDraft(String draftId) {
        loading.postValue(true);
        error.postValue(null);

        draftRepository.deleteDraft(draftId, new DraftRepository.Callback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loading.postValue(false);
                loadDrafts(); // Reload to get updated list
            }

            @Override
            public void onError(String err) {
                loading.postValue(false);
                error.postValue(err);
            }
        });
    }
}
