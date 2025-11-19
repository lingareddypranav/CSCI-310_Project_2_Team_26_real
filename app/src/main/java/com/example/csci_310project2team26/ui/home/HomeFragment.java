package com.example.csci_310project2team26.ui.home;

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
import com.example.csci_310project2team26.databinding.FragmentHomeBinding;
import com.example.csci_310project2team26.viewmodel.PostsViewModel;

public class HomeFragment extends Fragment {

    private static final int DEFAULT_LIMIT = 50;
    private static final int DEFAULT_OFFSET = 0;

    private FragmentHomeBinding binding;
    private PostsViewModel postsViewModel;
    private PostsAdapter postsAdapter;
    private TextWatcher searchWatcher;
    private AdapterView.OnItemSelectedListener sortSelectionListener;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        postsViewModel = new ViewModelProvider(this).get(PostsViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

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
                                                false  // Normal posts
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
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setAdapter(postsAdapter);

        setupFilterControls();
        observeViewModel();

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload posts when fragment becomes visible (e.g., returning from post detail or create post)
        // HomeFragment shows normal posts (not prompt posts)
        postsViewModel.loadPosts(
                postsViewModel.getCurrentSort(),
                postsViewModel.getCurrentQuery(),
                resolveLimit(),
                resolveOffset(),
                false  // Explicitly set to false for normal posts
        );
    }

    private void setupFilterControls() {
        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.post_sort_options,
                android.R.layout.simple_spinner_item
        );
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.sortSpinner.setAdapter(sortAdapter);

        String currentQuery = postsViewModel.getCurrentQuery();
        if (currentQuery != null && binding.searchEditText.getText() != null) {
            if (!currentQuery.equals(binding.searchEditText.getText().toString())) {
                binding.searchEditText.setText(currentQuery);
                binding.searchEditText.setSelection(currentQuery.length());
            }
        }

        binding.sortSpinner.setSelection(positionForSort(postsViewModel.getCurrentSort()), false);

        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String query = s != null ? s.toString() : "";
                postsViewModel.loadPosts(
                        postsViewModel.getCurrentSort(),
                        query,
                        resolveLimit(),
                        resolveOffset(),
                        false  // Explicitly set to false for normal posts
                );
            }
        };
        binding.searchEditText.addTextChangedListener(searchWatcher);

        sortSelectionListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSort = sortForPosition(position);
                String query = binding.searchEditText.getText() != null
                        ? binding.searchEditText.getText().toString()
                        : "";
                if (!selectedSort.equals(postsViewModel.getCurrentSort())) {
                    postsViewModel.loadPosts(
                            selectedSort,
                            query,
                            resolveLimit(),
                            resolveOffset(),
                            false  // Explicitly set to false for normal posts
                    );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        binding.sortSpinner.setOnItemSelectedListener(sortSelectionListener);
    }

    private void observeViewModel() {
        postsViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postsAdapter.submitList(posts);
            boolean isEmpty = posts == null || posts.isEmpty();
            binding.emptyStateTextView.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.postsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });
        postsViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            boolean loadingVisible = Boolean.TRUE.equals(isLoading);
            binding.loadingProgressBar.setVisibility(loadingVisible ? View.VISIBLE : View.GONE);
            if (loadingVisible) {
                binding.emptyStateTextView.setVisibility(View.GONE);
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
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_postDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        if (binding != null) {
            if (searchWatcher != null) {
                binding.searchEditText.removeTextChangedListener(searchWatcher);
            }
            if (sortSelectionListener != null) {
                binding.sortSpinner.setOnItemSelectedListener(null);
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