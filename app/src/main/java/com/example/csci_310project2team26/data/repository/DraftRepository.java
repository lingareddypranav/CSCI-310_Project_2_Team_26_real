package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Draft;
import com.example.csci_310project2team26.data.network.ApiService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;

public class DraftRepository {

    private final ApiService apiService;
    private final ExecutorService executorService;

    public DraftRepository() {
        this.apiService = ApiService.getInstance();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void createDraft(String title,
                           String content,
                           String promptSection,
                           String descriptionSection,
                           String llmTag,
                           boolean isPromptPost,
                           boolean anonymous,
                           Callback<Draft> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.DraftResponse> call = apiService.createDraft(
                    "Bearer " + token,
                    title,
                    content,
                    promptSection,
                    descriptionSection,
                    llmTag,
                    isPromptPost,
                    anonymous
                );

                Response<ApiService.DraftResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DraftResponse draftResponse = response.body();
                    Draft draft = convertDraftResponse(draftResponse);
                    if (draft != null) {
                        callback.onSuccess(draft);
                    } else {
                        callback.onError("Invalid draft response");
                    }
                } else {
                    String errorMsg = "Failed to create draft";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 400) {
                        errorMsg = "Invalid draft data";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void getDrafts(Callback<List<Draft>> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.DraftsResponse> call = apiService.getDrafts("Bearer " + token);
                Response<ApiService.DraftsResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    List<Draft> drafts = response.body().drafts != null ? response.body().drafts : new ArrayList<>();
                    callback.onSuccess(drafts);
                } else {
                    String errorMsg = "Failed to load drafts";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void getDraftById(String draftId, Callback<Draft> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.DraftResponse> call = apiService.getDraftById("Bearer " + token, draftId);
                Response<ApiService.DraftResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DraftResponse draftResponse = response.body();
                    Draft draft = convertDraftResponse(draftResponse);
                    if (draft != null) {
                        callback.onSuccess(draft);
                    } else {
                        callback.onError("Invalid draft response");
                    }
                } else {
                    String errorMsg = "Failed to load draft";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMsg = "Draft not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    private Draft convertDraftResponse(ApiService.DraftResponse response) {
        if (response == null) {
            return null;
        }
        // Backend returns draft fields directly in response
        if (response.draft != null) {
            return response.draft;
        }
        // Fallback: construct from direct fields
        if (response.id == null) {
            return null;
        }
        Draft draft = new Draft();
        draft.setId(response.id);
        draft.setTitle(response.title);
        draft.setBody(response.content);
        draft.setTag(response.llm_tag);
        draft.setPrompt(response.is_prompt_post);
        draft.setPromptSection(response.prompt_section);
        draft.setDescriptionSection(response.description_section);
        draft.setAnonymous(response.anonymous);
        draft.setUpdatedAt(response.updated_at);
        return draft;
    }

    public void updateDraft(String draftId,
                           String title,
                           String content,
                           String promptSection,
                           String descriptionSection,
                           String llmTag,
                           Boolean isPromptPost,
                           Boolean anonymous,
                           Callback<Draft> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<ApiService.DraftResponse> call = apiService.updateDraft(
                    "Bearer " + token,
                    draftId,
                    title,
                    content,
                    promptSection,
                    descriptionSection,
                    llmTag,
                    isPromptPost,
                    anonymous
                );

                Response<ApiService.DraftResponse> response = call.execute();

                if (response.isSuccessful() && response.body() != null) {
                    ApiService.DraftResponse draftResponse = response.body();
                    Draft draft = convertDraftResponse(draftResponse);
                    if (draft != null) {
                        callback.onSuccess(draft);
                    } else {
                        callback.onError("Invalid draft response");
                    }
                } else {
                    String errorMsg = "Failed to update draft";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMsg = "Draft not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }

    public void deleteDraft(String draftId, Callback<Void> callback) {
        executorService.execute(() -> {
            try {
                String token = SessionManager.getToken();
                if (token == null) {
                    callback.onError("Authentication required");
                    return;
                }

                retrofit2.Call<Void> call = apiService.deleteDraft("Bearer " + token, draftId);
                Response<Void> response = call.execute();

                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to delete draft";
                    if (response.code() == 401) {
                        errorMsg = "Authentication required";
                    } else if (response.code() == 404) {
                        errorMsg = "Draft not found";
                    }
                    callback.onError(errorMsg);
                }
            } catch (Exception e) {
                callback.onError(e.getMessage() != null ? e.getMessage() : "Network error");
            }
        });
    }
}

