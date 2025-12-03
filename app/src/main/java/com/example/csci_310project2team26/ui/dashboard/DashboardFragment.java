package com.example.csci_310project2team26.ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.databinding.FragmentDashboardBinding;
import com.example.csci_310project2team26.ui.home.PostsAdapter;
import com.example.csci_310project2team26.viewmodel.PostsViewModel;

public class DashboardFragment extends Fragment {

    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_OFFSET = 0;

    private FragmentDashboardBinding binding;
    private PostsViewModel postsViewModel;
    private PostsAdapter postsAdapter;
    private TextWatcher searchWatcher;
    private AdapterView.OnItemSelectedListener sortSelectionListener;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        postsViewModel = new ViewModelProvider(this).get(PostsViewModel.class);
        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        postsAdapter = new PostsAdapter(this::onPostClicked);
        postsAdapter.setOnPostDeletedListener(postId -> {
            // Show confirmation dialog
            new android.app.AlertDialog.Builder(requireContext())
                    .setTitle(R.string.delete_post_confirm_title)
                    .setMessage(R.string.delete_post_confirm_message)
                    .setPositiveButton(R.string.delete_post, (dialog, which) -> {
                        PostRepository postRepository = new PostRepository();
                        postRepository.deletePost(postId, new PostRepository.Callback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                                        // Reload posts after deletion
                                        postsViewModel.loadPosts(
                                                postsViewModel.getCurrentSort(),
                                                postsViewModel.getCurrentQuery(),
                                                resolveLimit(),
                                                resolveOffset(),
                                                true  // Prompt posts
                                        );
                                    });
                                }
                            }

                            @Override
                            public void onError(String error) {
                                if (getActivity() != null) {
                                    getActivity().runOnUiThread(() -> {
                                        Toast.makeText(requireContext(), error != null ? error : getString(R.string.delete_post_error), Toast.LENGTH_LONG).show();
                                    });
                                }
                            }
                        });
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        });
        postsAdapter.setOnBookmarkToggleListener((post, isBookmarked) -> {
            if (getContext() != null) {
                int messageId = isBookmarked ? R.string.bookmark_added : R.string.bookmark_removed;
                Toast.makeText(getContext(), messageId, Toast.LENGTH_SHORT).show();
            }
        });
        binding.promptPostsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.promptPostsRecyclerView.setAdapter(postsAdapter);

        setupFilterControls();
        observeViewModel();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload posts when fragment becomes visible (e.g., returning from post detail or create post)
        // DashboardFragment shows prompt posts
        postsViewModel.loadPosts(
                postsViewModel.getCurrentSort(),
                postsViewModel.getCurrentQuery(),
                resolveLimit(),
                resolveOffset(),
                true  // Explicitly set to true for prompt posts
        );
    }

    private void setupFilterControls() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.post_sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.promptSortSpinner.setAdapter(sortAdapter);

        String currentQuery = postsViewModel.getCurrentQuery();
        if (currentQuery != null && binding.promptSearchEditText.getText() != null) {
            if (!currentQuery.equals(binding.promptSearchEditText.getText().toString())) {
                binding.promptSearchEditText.setText(currentQuery);
                binding.promptSearchEditText.setSelection(currentQuery.length());
            }
        }

        binding.promptSortSpinner.setSelection(positionForSort(postsViewModel.getCurrentSort()), false);

        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s != null ? s.toString() : "";
                postsViewModel.loadPosts(
                        postsViewModel.getCurrentSort(),
                        query,
                        resolveLimit(),
                        resolveOffset(),
                        true
                );
            }
        };
        binding.promptSearchEditText.addTextChangedListener(searchWatcher);

        sortSelectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = sortForPosition(position);
                String query = binding.promptSearchEditText.getText() != null
                        ? binding.promptSearchEditText.getText().toString()
                        : "";
                if (!selectedSort.equals(postsViewModel.getCurrentSort())) {
                    postsViewModel.loadPosts(
                            selectedSort,
                            query,
                            resolveLimit(),
                            resolveOffset(),
                            true
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        };
        binding.promptSortSpinner.setOnItemSelectedListener(sortSelectionListener);
    }

    private void observeViewModel() {
        postsViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postsAdapter.submitList(posts);
            boolean isEmpty = posts == null || posts.isEmpty();
            binding.promptEmptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.promptPostsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
        postsViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loadingVisible = Boolean.TRUE.equals(isLoading);
            binding.promptLoadingProgressBar.setVisibility(loadingVisible ? View.VISIBLE : View.GONE);
            if (loadingVisible) {
                binding.promptEmptyStateTextView.setVisibility(View.GONE);
            }
        });
        postsViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
        });
    }

    private void onPostClicked(Post post) {
        if (post == null || post.getId() == null || post.getId().isEmpty()) {
            Toast.makeText(requireContext(), "Unable to open post", Toast.LENGTH_SHORT).show();
            return;
        }
        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.postDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            if (searchWatcher != null) {
                binding.promptSearchEditText.removeTextChangedListener(searchWatcher);
            }
            if (sortSelectionListener != null) {
                binding.promptSortSpinner.setOnItemSelectedListener(null);
            }
        }
        super.onDestroyView();
        binding = null;
    }

    private String sortForPosition(int position) {
        if (position == 1) {
            return PostsViewModel.SORT_TOP;
        }
        return PostsViewModel.SORT_NEW;
    }

    private int positionForSort(String sortKey) {
        if (PostsViewModel.SORT_TOP.equals(sortKey)) {
            return 1;
        }
        return 0;
    }

    private int resolveLimit() {
        Integer limit = postsViewModel.getCurrentLimit();
        return limit != null ? limit : DEFAULT_LIMIT;
    }

    private int resolveOffset() {
        Integer offset = postsViewModel.getCurrentOffset();
        return offset != null ? offset : DEFAULT_OFFSET;
    }
}
