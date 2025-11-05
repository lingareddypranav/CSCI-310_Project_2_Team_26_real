package com.example.csci_310project2team26.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.Post;
import com.example.csci_310project2team26.databinding.FragmentHomeBinding;
import com.example.csci_310project2team26.viewmodel.PostsViewModel;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private PostsViewModel postsViewModel;
    private PostsAdapter postsAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        postsViewModel = new ViewModelProvider(this).get(PostsViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        postsAdapter = new PostsAdapter(this::onPostClicked);
        binding.postsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.postsRecyclerView.setAdapter(postsAdapter);

        observeViewModel();
        postsViewModel.loadPosts("new", 50, 0, null);

        return root;
    }

    private void observeViewModel() {
        postsViewModel.getPosts().observe(getViewLifecycleOwner(), posts -> {
            postsAdapter.submitList(posts);
        });
        postsViewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
        });
    }

    private void onPostClicked(Post post) {
        Bundle args = new Bundle();
        args.putString("postId", post.getId());
        Navigation.findNavController(binding.getRoot()).navigate(R.id.action_navigation_home_to_postDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}