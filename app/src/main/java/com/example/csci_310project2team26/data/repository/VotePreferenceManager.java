package com.example.csci_310project2team26.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

/**
 * Small helper to persist a user's vote choice locally so it survives
 * fragment swaps and process restarts even when the backend response
 * omits the user_vote_type field.
 */
public class VotePreferenceManager {

    private static final String PREF_NAME = "vote_preferences";
    private static final String KEY_PREFIX_POST_VOTE = "post_vote_";

    private static SharedPreferences getPrefs(Context context) {
        if (context == null) {
            return null;
        }
        return context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void setPostVote(Context context, String postId, String voteType) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null || TextUtils.isEmpty(postId)) {
            return;
        }

        String key = KEY_PREFIX_POST_VOTE + postId;
        SharedPreferences.Editor editor = prefs.edit();
        if (voteType == null || voteType.trim().isEmpty()) {
            editor.remove(key);
        } else {
            editor.putString(key, voteType.trim());
        }
        editor.apply();
    }

    public static String getPostVote(Context context, String postId) {
        SharedPreferences prefs = getPrefs(context);
        if (prefs == null || TextUtils.isEmpty(postId)) {
            return null;
        }

        return prefs.getString(KEY_PREFIX_POST_VOTE + postId, null);
    }
}
