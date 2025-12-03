package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Post;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BookmarkManager - Lightweight in-memory store for bookmarked posts.
 * This is a front-end only implementation; persistence would require
 * back-end or local storage support.
 */
public final class BookmarkManager {

    private static final Map<String, Post> bookmarkedPosts = new LinkedHashMap<>();

    private BookmarkManager() { }

    /**
     * Toggle bookmark state for a post.
     *
     * @param post Post to toggle
     * @return true if the post is now bookmarked, false if removed or invalid
     */
    public static synchronized boolean toggleBookmark(Post post) {
        if (post == null || post.getId() == null || post.getId().isEmpty()) {
            return false;
        }

        if (bookmarkedPosts.containsKey(post.getId())) {
            bookmarkedPosts.remove(post.getId());
            return false;
        } else {
            bookmarkedPosts.put(post.getId(), post);
            return true;
        }
    }

    public static synchronized boolean isBookmarked(Post post) {
        if (post == null) return false;
        return isBookmarked(post.getId());
    }

    public static synchronized boolean isBookmarked(String postId) {
        return postId != null && bookmarkedPosts.containsKey(postId);
    }

    public static synchronized List<Post> getBookmarkedPosts() {
        return new ArrayList<>(bookmarkedPosts.values());
    }

    public static synchronized List<Post> getBookmarkedPosts(Boolean isPromptPost) {
        if (isPromptPost == null) {
            return getBookmarkedPosts();
        }
        List<Post> filtered = new ArrayList<>();
        for (Post post : bookmarkedPosts.values()) {
            if (post != null && post.isIs_prompt_post() == isPromptPost) {
                filtered.add(post);
            }
        }
        return filtered;
    }
}

