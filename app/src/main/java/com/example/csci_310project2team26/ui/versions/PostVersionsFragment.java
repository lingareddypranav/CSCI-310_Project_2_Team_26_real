package com.example.csci_310project2team26.ui.versions;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.csci_310project2team26.R;
import com.example.csci_310project2team26.data.model.PostVersion;
import com.example.csci_310project2team26.databinding.FragmentPostVersionsBinding;
import com.example.csci_310project2team26.viewmodel.PostVersionsViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostVersionsFragment extends Fragment {

    private FragmentPostVersionsBinding binding;
    private PostVersionsViewModel viewModel;
    private PostVersionsAdapter adapter;
    private String postId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentPostVersionsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(PostVersionsViewModel.class);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        if (TextUtils.isEmpty(postId)) {
            Toast.makeText(requireContext(), "Post ID is missing", Toast.LENGTH_LONG).show();
            requireActivity().onBackPressed();
            return binding.getRoot();
        }

        setupRecyclerView();
        observeViewModel();

        binding.versionsToolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        viewModel.loadVersions(postId);

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new PostVersionsAdapter(new PostVersionsAdapter.OnVersionClickListener() {
            @Override
            public void onRevertClicked(PostVersion version) {
                if (version != null && !TextUtils.isEmpty(version.getId())) {
                    viewModel.revertToVersion(postId, version.getId());
                }
            }
        });
        binding.versionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.versionsRecyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getVersions().observe(getViewLifecycleOwner(), versions -> {
            adapter.submitList(versions);
            boolean isEmpty = versions == null || versions.isEmpty();
            binding.emptyVersionsText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.versionsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), loading -> {
            boolean visible = Boolean.TRUE.equals(loading);
            binding.versionsProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
            if (visible) {
                binding.emptyVersionsText.setVisibility(View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getRevertedPost().observe(getViewLifecycleOwner(), post -> {
            if (post != null) {
                Toast.makeText(requireContext(), "Post reverted successfully", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(binding.getRoot()).popBackStack();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

