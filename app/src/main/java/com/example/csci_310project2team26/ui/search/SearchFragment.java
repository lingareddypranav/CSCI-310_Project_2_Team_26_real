package com.example.csci_310project2team26.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
import com.example.csci_310project2team26.data.repository.PostRepository;
import com.example.csci_310project2team26.databinding.FragmentSearchBinding;
import com.example.csci_310project2team26.ui.home.PostsAdapter;
import com.example.csci_310project2team26.viewmodel.PostsViewModel;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private PostsViewModel postsViewModel;
    private PostsAdapter postsAdapter;
    private TextWatcher searchWatcher;

    // Search type constants
    public static final String SEARCH_TYPE_TAG = "tag";
    public static final String SEARCH_TYPE_AUTHOR = "author";
    public static final String SEARCH_TYPE_TITLE = "title";
    public static final String SEARCH_TYPE_FULL_TEXT = "full_text";
    public static final String SEARCH_TYPE_PROMPT_TAG = "prompt_tag";

    // Post type filter constants
    public static final String POST_TYPE_ALL = "all";
    public static final String POST_TYPE_NORMAL = "normal";
    public static final String POST_TYPE_PROMPT = "prompt";

    private String currentSearchType = SEARCH_TYPE_FULL_TEXT;
    private String currentPostType = POST_TYPE_ALL;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postsViewModel = new ViewModelProvider(this).get(PostsViewModel.class);
        postsAdapter = new PostsAdapter(post -> {
            if (post != null && post.getId() != null && binding != null) {
                Bundle args = new Bundle();
                args.putString("postId", post.getId());
                Navigation.findNavController(binding.getRoot()).navigate(R.id.action_searchFragment_to_postDetailFragment, args);
            }
        });
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
                                        // Reload search after deletion
                                        performSearch();
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

        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setAdapter(postsAdapter);

        // Setup search type spinner
        ArrayAdapter<CharSequence> searchTypeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.search_types,
                android.R.layout.simple_spinner_item
        );
        searchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.searchTypeSpinner.setAdapter(searchTypeAdapter);

        binding.searchTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] searchTypes = getResources().getStringArray(R.array.search_types);
                String selectedType = searchTypes[position];
                currentSearchType = getSearchTypeFromString(selectedType);
                performSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup post type filter spinner
        ArrayAdapter<CharSequence> postTypeAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.post_type_filter,
                android.R.layout.simple_spinner_item
        );
        postTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.postTypeSpinner.setAdapter(postTypeAdapter);

        binding.postTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String[] postTypes = getResources().getStringArray(R.array.post_type_filter);
                String selectedType = postTypes[position];
                if ("All Posts".equals(selectedType)) {
                    currentPostType = POST_TYPE_ALL;
                } else if ("Normal Posts".equals(selectedType)) {
                    currentPostType = POST_TYPE_NORMAL;
                } else if ("Prompt Posts".equals(selectedType)) {
                    currentPostType = POST_TYPE_PROMPT;
                }
                performSearch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Setup search text watcher
        searchWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                performSearch();
            }
        };
        binding.searchEditText.addTextChangedListener(searchWatcher);

        // Observe ViewModel
        postsViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            if (binding == null) return;
            postsAdapter.submitList(posts != null ? posts : new java.util.ArrayList<>());
        });

        postsViewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            // Optionally show/hide loading indicator
        });

        postsViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && binding != null && getContext() != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void performSearch() {
        if (binding == null) return;

        String query = binding.searchEditText.getText() != null
                ? binding.searchEditText.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(query)) {
            // Clear results if query is empty
            if (postsAdapter != null) {
                postsAdapter.submitList(new java.util.ArrayList<>());
            }
            return;
        }

        // Determine post type filter
        Boolean isPromptPost = null;
        if (POST_TYPE_PROMPT.equals(currentPostType)) {
            isPromptPost = true;
        } else if (POST_TYPE_NORMAL.equals(currentPostType)) {
            isPromptPost = false;
        }
        // If POST_TYPE_ALL, isPromptPost remains null (search all)

        postsViewModel.searchPosts(
                query,
                currentSearchType,
                PostsViewModel.SORT_NEW,
                50,
                0,
                isPromptPost
        );
    }

    private String getSearchTypeFromString(String displayName) {
        if (getString(R.string.search_type_tag).equals(displayName)) {
            return SEARCH_TYPE_TAG;
        } else if (getString(R.string.search_type_author).equals(displayName)) {
            return SEARCH_TYPE_AUTHOR;
        } else if (getString(R.string.search_type_title).equals(displayName)) {
            return SEARCH_TYPE_TITLE;
        } else if (getString(R.string.search_type_full_text).equals(displayName)) {
            return SEARCH_TYPE_FULL_TEXT;
        } else if (getString(R.string.search_type_prompt_tag).equals(displayName)) {
            return SEARCH_TYPE_PROMPT_TAG;
        }
        return SEARCH_TYPE_FULL_TEXT;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null && searchWatcher != null) {
            binding.searchEditText.removeTextChangedListener(searchWatcher);
        }
        binding = null;
    }
}

