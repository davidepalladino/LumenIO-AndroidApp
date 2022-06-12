package it.davidepalladino.lumenio.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionInflater;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentLibraryListBinding;
import it.davidepalladino.lumenio.util.LibraryListAdapter;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;

public class LibraryListFragment extends Fragment {
    private FragmentLibraryListBinding binding;
    private LibraryViewModel libraryViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        binding = FragmentLibraryListBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LibraryListAdapter adapter = new LibraryListAdapter(new LibraryListAdapter.ProfileDiff());
        binding.listProfiles.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.listProfiles.setAdapter(adapter);

        libraryViewModel.getAll().observe(requireActivity(), adapter::submitList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}