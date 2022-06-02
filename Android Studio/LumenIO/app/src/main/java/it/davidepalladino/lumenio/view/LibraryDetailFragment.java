package it.davidepalladino.lumenio.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.TransitionInflater;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentLibraryDetailBinding;

public class LibraryDetailFragment extends Fragment {
    private FragmentLibraryDetailBinding binding;
    private String transitionForName;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        transitionForName = getArguments().getString("transitionForName");

//        postponeEnterTransition();
        TransitionInflater inflater = TransitionInflater.from(getContext());
        setSharedElementEnterTransition(inflater.inflateTransition(R.transition.change_bounds));
//        setSharedElementReturnTransition(inflater.inflateTransition(R.transition.change_bounds));
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.name.setTransitionName(transitionForName);
//        startPostponedEnterTransition();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}