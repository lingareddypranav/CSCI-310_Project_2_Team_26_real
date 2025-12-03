package com.example.csci_310project2team26.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.BookmarkManager;

import java.util.ArrayList;
import java.util.List;

public class BookmarksViewModel extends ViewModel {

    public static final String FILTER_ALL = "all";
    public static final String FILTER_NORMAL = "normal";
    public static final String FILTER_PROMPT = "prompt";

    private final MutableLiveData<List<Post>> bookmarks = new MutableLiveData<>(new ArrayList<>());
    private String currentFilter = FILTER_ALL;

    public LiveData<List<Post>> getBookmarks() {
        return bookmarks;
    }

    public void setFilter(String filter) {
        currentFilter = filter != null ? filter : FILTER_ALL;
        refreshBookmarks();
    }

    public void refreshBookmarks() {
        Boolean isPromptPost = null;
        if (FILTER_PROMPT.equals(currentFilter)) {
            isPromptPost = true;
        } else if (FILTER_NORMAL.equals(currentFilter)) {
            isPromptPost = false;
        }
        bookmarks.postValue(BookmarkManager.getBookmarkedPosts(isPromptPost));
    }

    public void onBookmarkToggled() {
        refreshBookmarks();
    }
}
