package it.davidepalladino.lumenio.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import it.davidepalladino.lumenio.databinding.FragmentControlBinding;

public class ControlFragment extends Fragment {
    private FragmentControlBinding binding;
    private ProfileViewModel profileViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(requireActivity()).get(ProfileViewModel.class);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences profileSelected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                profileViewModel.loadSelectedByID(profileSelected.getLong("SELECTED_PROFILE_ID", 0));
            }
        }).start();

        binding = FragmentControlBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        binding.setProfileViewModel(profileViewModel);

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        SharedPreferences.Editor profileSelectedPreference = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                        profileSelectedPreference.putLong("SELECTED_PROFILE_ID", profileViewModel.insert());
                        profileSelectedPreference.apply();

                        Snackbar.make(view, "Saved", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }).start();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}