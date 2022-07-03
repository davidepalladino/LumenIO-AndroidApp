package it.davidepalladino.lumenio.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.transition.TransitionInflater;

import java.util.List;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.databinding.FragmentLibraryListBinding;
import it.davidepalladino.lumenio.util.LibraryListAdapter;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;

public class LibraryListFragment extends Fragment {
    private FragmentLibraryListBinding fragmentLibraryListBinding;
    private LibraryViewModel libraryViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.fade));
        setExitTransition(inflater.inflateTransition(R.transition.fade));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LibraryListAdapter adapter = new LibraryListAdapter(new LibraryListAdapter.ProfileDiff());

        Observer<List<Profile>> profileGetAllObserver = list -> {
            if (fragmentLibraryListBinding != null) {
                if (list.isEmpty()) {
                    fragmentLibraryListBinding.noEmptyListContainer.setVisibility(View.GONE);
                    fragmentLibraryListBinding.emptyListContainer.setVisibility(View.VISIBLE);
                } else {
                    fragmentLibraryListBinding.noEmptyListContainer.setVisibility(View.VISIBLE);
                    fragmentLibraryListBinding.emptyListContainer.setVisibility(View.GONE);

                    adapter.submitList(list);
                }
            }
        };

        Observer<List<Profile>> profileGetAllByNameObserver = list -> {
            if (fragmentLibraryListBinding != null) {
                if (list.isEmpty()) {
                    fragmentLibraryListBinding.list.setVisibility(View.GONE);
                    fragmentLibraryListBinding.itemNoFound.setVisibility(View.VISIBLE);
                } else {
                    fragmentLibraryListBinding.list.setVisibility(View.VISIBLE);
                    fragmentLibraryListBinding.itemNoFound.setVisibility(View.GONE);

                    adapter.submitList(list);
                }
            }
        };

        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        fragmentLibraryListBinding = FragmentLibraryListBinding.inflate(inflater, container, false);
        fragmentLibraryListBinding.setLifecycleOwner(this);

        fragmentLibraryListBinding.list.setLayoutManager(new LinearLayoutManager(requireActivity()));
        fragmentLibraryListBinding.list.setAdapter(adapter);

        fragmentLibraryListBinding.search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() != 0) {
                    libraryViewModel.getAll().removeObserver(profileGetAllObserver);
                    libraryViewModel.getAllByName(newText).observe(requireActivity(), profileGetAllByNameObserver);
                } else {
                    libraryViewModel.getAll().observe(requireActivity(), profileGetAllObserver);

                    fragmentLibraryListBinding.list.setVisibility(View.VISIBLE);
                    fragmentLibraryListBinding.itemNoFound.setVisibility(View.GONE);
                }

                return true;
            }
        });

        libraryViewModel.getAll().observe(requireActivity(), profileGetAllObserver);

        return fragmentLibraryListBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        appCompatActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        appCompatActivity.getSupportActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentLibraryListBinding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_library_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}