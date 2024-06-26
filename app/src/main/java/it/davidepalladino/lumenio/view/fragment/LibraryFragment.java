package it.davidepalladino.lumenio.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import it.davidepalladino.lumenio.databinding.FragmentLibraryBinding;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;

public class LibraryFragment extends Fragment {
    private FragmentLibraryBinding fragmentLibraryBinding;
    private LibraryViewModel libraryViewModel;

    public static LibraryFragment newInstance() {
        return new LibraryFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentLibraryBinding = FragmentLibraryBinding.inflate(inflater, container, false);
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        return fragmentLibraryBinding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        /* Setting this Fragment as primary to manage the back stack of navigation. */
        this.getParentFragmentManager().beginTransaction().setPrimaryNavigationFragment(this).commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentLibraryBinding = null;
    }
}