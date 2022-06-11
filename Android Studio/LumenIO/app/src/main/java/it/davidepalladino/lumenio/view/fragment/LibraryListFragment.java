package it.davidepalladino.lumenio.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

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
////        setSharedElementEnterTransition(inflater.inflateTransition(R.transition.change_bounds));
////        setSharedElementReturnTransition(inflater.inflateTransition(R.transition.change_bounds));
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        binding = FragmentLibraryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        postponeEnterTransition();

        LibraryListAdapter adapter = new LibraryListAdapter(new LibraryListAdapter.ProfileDiff());
        binding.recycleView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.recycleView.setAdapter(adapter);

        final ViewGroup parentView = (ViewGroup) view.getParent(); // Can use `binding` instead.
        libraryViewModel.getAll().observe(requireActivity(), profiles -> {
            adapter.submitList(profiles);
            parentView.getViewTreeObserver()
                    .addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw(){
                            parentView.getViewTreeObserver().removeOnPreDrawListener(this);
                            startPostponedEnterTransition();
                            return true;
                        }
                    });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}