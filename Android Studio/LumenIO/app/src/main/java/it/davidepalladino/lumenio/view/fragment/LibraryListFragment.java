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
import androidx.fragment.app.Fragment;
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
    private FragmentLibraryListBinding binding;
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
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);

        binding = FragmentLibraryListBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        LibraryListAdapter adapter = new LibraryListAdapter(new LibraryListAdapter.ProfileDiff());
        binding.listProfiles.setLayoutManager(new LinearLayoutManager(requireActivity()));
        binding.listProfiles.setAdapter(adapter);

        libraryViewModel.getAll().observe(requireActivity(), (List<Profile> list) -> {
            if (binding != null) {
                if (list.isEmpty()) {
                    binding.listProfiles.setVisibility(View.GONE);
                    binding.emptyListMessage.setVisibility(View.VISIBLE);
                } else {
                    binding.listProfiles.setVisibility(View.VISIBLE);
                    binding.emptyListMessage.setVisibility(View.GONE);
                    adapter.submitList(list);
                }
            }
        });

        return binding.getRoot();
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
        binding = null;
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