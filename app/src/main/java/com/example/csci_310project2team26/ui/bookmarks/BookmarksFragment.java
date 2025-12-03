package com.example.csci_310project2team26.ui.bookmarks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.databinding.FragmentBookmarksBinding;
import com.example.csci_310project2team26.ui.home.PostsAdapter;
import com.example.csci_310project2team26.viewmodel.BookmarksViewModel;

public class BookmarksFragment extends Fragment {

    private FragmentBookmarksBinding binding;
    private PostsAdapter postsAdapter;
    private BookmarksViewModel bookmarksViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBookmarksBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bookmarksViewModel = new ViewModelProvider(this).get(BookmarksViewModel.class);

        postsAdapter = new PostsAdapter(this::onPostClicked);
        postsAdapter.setOnBookmarkToggleListener((post, isBookmarked) -> {
            if (getContext() != null) {
                int messageId = isBookmarked ? R.string.bookmark_added : R.string.bookmark_removed;
                Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
            }
            bookmarksViewModel.onBookmarkToggled();
        });
        postsAdapter.setOnPostVoteListener((post, type) -> bookmarksViewModel.voteOnPost(post.getId(), type));

        binding.bookmarkedPostsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.bookmarkedPostsRecyclerView.setAdapter(postsAdapter);

        setupFilterSpinner();
        observeViewModel();
        bookmarksViewModel.refreshBookmarks();
    }

    private void setupFilterSpinner() {
        ArrayAdapter<CharSequence> postTypeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.post_type_filter,
                android.R.layout.simple_spinner_item
        );
        postTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.bookmarkPostTypeSpinner.setAdapter(postTypeAdapter);
        binding.bookmarkPostTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                bookmarksViewModel.setFilter(filterKeyForPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void observeViewModel() {
        bookmarksViewModel.getBookmarks().observe(getViewLifecycleOwner(), posts -> {
            postsAdapter.submitList(posts);
            boolean isEmpty = posts == null || posts.isEmpty();
            binding.bookmarksEmptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.bookmarkedPostsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
    }

    private void onPostClicked(Post post) {
        if (post == null || post.getId() == null || post.getId().isEmpty()) {
            return;
        }
        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.postDetailFragment, args);
    }

    private String filterKeyForPosition(int position) {
        if (position == 1) {
            return BookmarksViewModel.FILTER_NORMAL;
        } else if (position == 2) {
            return BookmarksViewModel.FILTER_PROMPT;
        }
        return BookmarksViewModel.FILTER_ALL;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
