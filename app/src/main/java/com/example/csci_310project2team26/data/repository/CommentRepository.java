package com.example.csci_310project2team26.data.repository;

import com.example.csci_310project2team26.data.model.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CommentRepository - In-memory data source that mimics the behaviour of the backend API.
 *
 * This repository keeps a seeded collection of comments for the demo posts so the
 * PostDetail screen can be exercised without a network connection. The API mirrors the
 * original Retrofit-based implementation to minimise changes in the ViewModel.
 */
public class CommentRepository {

    public static class CommentsResult {
        private final List<Comment> comments;
        private final int count;

        public CommentsResult(List<Comment> comments, int count) {
            this.comments = comments;
            this.count = count;
        }

        public List<Comment> getComments() {
            return comments;
        }

        public int getCount() {
            return count;
        }
    }

    public static class VoteResult {
        private final String message;
        private final String action;
        private final String type;
        private final Comment comment;

        public VoteResult(String message, String action, String type, Comment comment) {
            this.message = message;
            this.action = action;
            this.type = type;
            this.comment = comment;
        }

        public String getMessage() {
            return message;
        }

        public String getAction() {
            return action;
        }

        public String getType() {
            return type;
        }

        public Comment getComment() {
            return comment;
        }
    }

    private final ExecutorService executorService;
    private final Map<String, List<Comment>> commentsByPost;

    public CommentRepository() {
        this.executorService = Executors.newSingleThreadExecutor();
        this.commentsByPost = Collections.synchronizedMap(new HashMap<>());
        seedDummyComments();
    }

    public interface Callback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public void fetchComments(String postId, Callback<CommentsResult> callback) {
        executorService.execute(() -> {
            List<Comment> comments = copyCommentsForPost(postId);
            callback.onSuccess(new CommentsResult(comments, comments.size()));
        });
    }

    public void createComment(String postId, String text, Callback<Comment> callback) {
        executorService.execute(() -> {
            List<Comment> target = getOrCreateComments(postId);
            Comment comment = new Comment();
            comment.setId(UUID.randomUUID().toString());
            comment.setPost_id(postId);

            String authorId = SessionManager.getUserId();
            comment.setAuthor_id(authorId != null ? authorId : "demo-user");
            comment.setAuthor_name(authorId != null ? authorId : "Demo User");

            long now = System.currentTimeMillis();
            comment.setText(text);
            comment.setCreated_at(Long.toString(now));
            comment.setUpdated_at(Long.toString(now));
            comment.setUpvotes(0);
            comment.setDownvotes(0);

            synchronized (target) {
                target.add(0, comment);
            }

            callback.onSuccess(copyComment(comment));
        });
    }

    public void voteOnComment(String postId,
                              String commentId,
                              String type,
                              Callback<VoteResult> callback) {
        executorService.execute(() -> {
            List<Comment> comments = getOrCreateComments(postId);
            String normalized = type != null ? type.toLowerCase(Locale.US) : "";

            synchronized (comments) {
                for (Comment comment : comments) {
                    if (comment.getId().equals(commentId)) {
                        switch (normalized) {
                            case "up":
                                comment.setUpvotes(comment.getUpvotes() + 1);
                                callback.onSuccess(new VoteResult("Vote recorded", "added", "up", copyComment(comment)));
                                return;
                            case "down":
                                comment.setDownvotes(comment.getDownvotes() + 1);
                                callback.onSuccess(new VoteResult("Vote recorded", "added", "down", copyComment(comment)));
                                return;
                            default:
                                callback.onError("Invalid vote type");
                                return;
                        }
                    }
                }
            }

            callback.onError("Comment not found");
        });
    }

    private List<Comment> getOrCreateComments(String postId) {
        List<Comment> list = commentsByPost.get(postId);
        if (list == null) {
            list = Collections.synchronizedList(new ArrayList<>());
            commentsByPost.put(postId, list);
        }
        return list;
    }

    private List<Comment> copyCommentsForPost(String postId) {
        List<Comment> source = getOrCreateComments(postId);
        List<Comment> copy = new ArrayList<>();
        synchronized (source) {
            for (Comment comment : source) {
                copy.add(copyComment(comment));
            }
        }
        return copy;
    }

    private Comment copyComment(Comment original) {
        return new Comment(
                original.getId(),
                original.getPost_id(),
                original.getAuthor_id(),
                original.getAuthor_name(),
                original.getText(),
                original.getCreated_at(),
                original.getUpdated_at(),
                original.getUpvotes(),
                original.getDownvotes()
        );
    }

    private void seedDummyComments() {
        long now = System.currentTimeMillis();
        addDummyComment("1", "c1", "olivia", "Olivia Perez",
                "Love the structure you suggested for summarising. The bullet hierarchy is clutch!",
                now - hoursToMillis(6), 18, 1);
        addDummyComment("1", "c2", "mason", "Mason Wright",
                "I usually run an extra pass that asks for supporting quotes. Helps keep things grounded.",
                now - hoursToMillis(3), 11, 0);
        addDummyComment("2", "c3", "neha", "Neha Kapoor",
                "Claude's inline suggestions feel more natural to me, but Gemini is catching up fast.",
                now - hoursToMillis(9), 9, 2);
        addDummyComment("2", "c4", "sam", "Sam Chen",
                "Biggest win with Gemini has been the code tracing view. Makes diffing super easy.",
                now - hoursToMillis(4), 7, 1);
        addDummyComment("3", "c5", "taylor", "Taylor Brooks",
                "Try prompting it to throw curveball questions back at you. Keeps the session lively!",
                now - hoursToMillis(20), 13, 0);
        addDummyComment("4", "c6", "rui", "Rui Zhang",
                "We've had luck freezing the embedding layer and fine-tuning only adapters.",
                now - hoursToMillis(12), 15, 3);
        addDummyComment("4", "c7", "jordan", "Jordan Miles",
                "+1 on adapters. Also consider low-rank updates if you're memory bound.",
                now - hoursToMillis(7), 8, 1);
        addDummyComment("5", "c8", "chloe", "Chloe Sanders",
                "I pair it with spaced repetition prompts—students say it feels like a tutor.",
                now - hoursToMillis(30), 6, 0);
        addDummyComment("6", "c9", "diego", "Diego Ramirez",
                "Retrieval results improve tons if you normalise the docs with a short TL;DR first.",
                now - hoursToMillis(16), 10, 2);
        addDummyComment("7", "c10", "ashley", "Ashley Moore",
                "We ask it to quote style guide sections whenever it flags an issue—helps new devs learn.",
                now - hoursToMillis(5), 14, 1);
        addDummyComment("8", "c11", "ian", "Ian Gallagher",
                "We're running BigBench Hard plus a few internal math sets to stress reasoning.",
                now - hoursToMillis(18), 5, 2);
        addDummyComment("9", "c12", "marie", "Marie Dubois",
                "I added a weekly reflection variant where it nudges you to compare entries. Super helpful.",
                now - hoursToMillis(6), 12, 0);
        addDummyComment("10", "c13", "leo", "Leo Grant",
                "Streaming + function calling was flaky until we buffered the partial tool calls.",
                now - hoursToMillis(2), 9, 1);
        addDummyComment("11", "c14", "grace", "Grace Lin",
                "Frame each debate point as a perspective. The model is better at building on those.",
                now - hoursToMillis(40), 7, 1);
        addDummyComment("12", "c15", "aaron", "Aaron Blake",
                "Quantisation post-training went smoother once we calibrated on representative logs.",
                now - hoursToMillis(24), 8, 3);
    }

    private void addDummyComment(String postId,
                                 String commentId,
                                 String authorId,
                                 String authorName,
                                 String text,
                                 long createdAtMillis,
                                 int upvotes,
                                 int downvotes) {
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost_id(postId);
        comment.setAuthor_id(authorId);
        comment.setAuthor_name(authorName);
        comment.setText(text);
        comment.setCreated_at(Long.toString(createdAtMillis));
        comment.setUpdated_at(Long.toString(createdAtMillis));
        comment.setUpvotes(upvotes);
        comment.setDownvotes(downvotes);

        List<Comment> comments = getOrCreateComments(postId);
        synchronized (comments) {
            comments.add(comment);
        }
    }

    private long hoursToMillis(int hours) {
        return hours * 60L * 60L * 1000L;
    }
}