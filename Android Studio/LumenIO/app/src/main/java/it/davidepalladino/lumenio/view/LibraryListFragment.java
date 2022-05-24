package it.davidepalladino.lumenio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import it.davidepalladino.lumenio.databinding.FragmentLibraryListBinding;
import it.davidepalladino.lumenio.util.LibraryListAdapter;

public class LibraryListFragment extends Fragment {
    private FragmentLibraryListBinding binding;
    private ProfileViewModel profileViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        binding = FragmentLibraryListBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LibraryListAdapter adapter = new LibraryListAdapter(new LibraryListAdapter.ProfileDiff());
        adapter.fragmentHost = this;
        binding.recycleView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recycleView.setAdapter(adapter);

        profileViewModel.getAll().observe(requireActivity(), profiles -> {
            adapter.submitList(profiles);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}