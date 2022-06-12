package it.davidepalladino.lumenio.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.TransitionInflater;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentLibraryDetailBinding;
import it.davidepalladino.lumenio.view.viewModel.ControlViewModel;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;

public class LibraryDetailFragment extends Fragment {
    private FragmentLibraryDetailBinding binding;
    private LibraryViewModel libraryViewModel;
    private ControlViewModel controlViewModel;

    private Thread threadGetOneByID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        controlViewModel = new ViewModelProvider(requireActivity()).get(ControlViewModel.class);

        threadGetOneByID = new Thread(new Runnable() {
            @Override
            public void run() {
                libraryViewModel.loadSelectedByID(getArguments().getLong("profileID"));
                binding.setLibraryViewModel(libraryViewModel);
            }
        });

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryDetailBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        threadGetOneByID.start();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void updateDevice() {
        // TODO: Updating the BT device.
    }
}