package com.example.csci_310project2team26.ui.drafts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.navigation.fragment.NavHostFragment;

import com.example.csci_310project2team26.data.model.Draft;
import com.example.csci_310project2team26.databinding.FragmentSavedDraftsBinding;
import com.example.csci_310project2team26.viewmodel.DraftsViewModel;

public class SavedDraftsFragment extends Fragment {

    private FragmentSavedDraftsBinding binding;
    private DraftsViewModel draftsViewModel;
    private DraftsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSavedDraftsBinding.inflate(inflater, container, false);
        draftsViewModel = new ViewModelProvider(requireActivity()).get(DraftsViewModel.class);
        adapter = new DraftsAdapter(new DraftsAdapter.DraftActionListener() {
            @Override
            public void onUseDraft(@NonNull Draft draft) {
                draftsViewModel.selectDraft(draft);
                NavHostFragment.findNavController(SavedDraftsFragment.this).navigateUp();
            }

            @Override
            public void onDeleteDraft(@NonNull Draft draft) {
                draftsViewModel.deleteDraft(draft.getId());
            }
        });

        binding.draftsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.draftsRecyclerView.setAdapter(adapter);

        binding.savedDraftsToolbar.setNavigationOnClickListener(v -> requireActivity()
                .getOnBackPressedDispatcher()
                .onBackPressed());

        draftsViewModel.getDrafts().observe(getViewLifecycleOwner(), drafts -> {
            adapter.submitList(drafts);
            boolean isEmpty = drafts == null || drafts.isEmpty();
            binding.emptyDraftsText.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            binding.draftsRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
