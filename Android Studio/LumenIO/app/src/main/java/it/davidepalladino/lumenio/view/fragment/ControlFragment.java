package it.davidepalladino.lumenio.view.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteConstraintException;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentControlBinding;
import it.davidepalladino.lumenio.view.viewModel.ControlViewModel;

public class ControlFragment extends Fragment {
    private FragmentControlBinding binding;
    private ControlViewModel controlViewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controlViewModel = new ViewModelProvider(requireActivity()).get(ControlViewModel.class);

        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPreferences profileSelected = requireActivity().getPreferences(Context.MODE_PRIVATE);
                controlViewModel.loadByID(profileSelected.getLong("SELECTED_PROFILE_ID", 0));
            }
        }).start();

        binding = FragmentControlBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        binding.setControlViewModel(controlViewModel);
        binding.setControlFragment(this);

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
                        String snackbarMessage = "";

                        try {
                            long newID = controlViewModel.insert();
                            SharedPreferences.Editor profileSelectedPreference = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                            profileSelectedPreference.putLong("SELECTED_PROFILE_ID", newID);
                            profileSelectedPreference.apply();

                            snackbarMessage = controlViewModel.getSelectedName().getValue() + getString(R.string.snackbarProfileSaved);
                        } catch (SQLiteConstraintException e) {
                            snackbarMessage = controlViewModel.getSelectedName().getValue() + getString(R.string.snackbarProfileNotSavedForName);
                        }

                        SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                        spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, controlViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        Snackbar.make(binding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(binding.fabAdd).show();
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

    public void updateDevice() {
        // TODO: Updating the BT device.
    }
}