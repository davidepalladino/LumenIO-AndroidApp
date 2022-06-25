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

import com.google.android.material.snackbar.Snackbar;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentManualBinding;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;

public class ManualFragment extends Fragment {
    private FragmentManualBinding binding;
    private ManualViewModel manualViewModel;

    private boolean errorFieldName = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        new Thread(() -> {
            SharedPreferences profileSelected = requireActivity().getPreferences(Context.MODE_PRIVATE);
            manualViewModel.loadByID(profileSelected.getLong(getString(R.string.latest_profile_selected), 0));
        }).start();

        binding = FragmentManualBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);

        binding.setManualViewModel(manualViewModel);
        binding.setManualFragment(this);

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
        inflater.inflate(R.menu.menu_control, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void checkName(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            errorFieldName = true;

            binding.messageName.setText(getString(R.string.empty_field));
            binding.messageName.setVisibility(View.VISIBLE);
            binding.fabAdd.setClickable(false);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start_end))) {
            errorFieldName = true;

            binding.messageName.setText(R.string.incorrect_start_end_field);
            binding.messageName.setVisibility(View.VISIBLE);
            binding.fabAdd.setClickable(false);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            errorFieldName = false;

            binding.messageName.setText("");
            binding.messageName.setVisibility(View.GONE);
            binding.fabAdd.setClickable(true);
        }
    }

    public void saveToTheLibrary() {
        if (binding.name.getText().length() > 0 && !errorFieldName) {
            new Thread(() -> {
                String snackbarMessage = "";

                try {
                    SharedPreferences.Editor profileSelectedPreference = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                    profileSelectedPreference.putLong(getString(R.string.latest_profile_selected), manualViewModel.insert());
                    profileSelectedPreference.apply();

                    manualViewModel.reload();

                    snackbarMessage = manualViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_saved);
                } catch (SQLiteConstraintException e) {
                    snackbarMessage = manualViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_not_saved_for_name);
                }

                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, manualViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Snackbar.make(binding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(binding.fabAdd).show();
            }).start();
        } else if (binding.name.getText().length() == 0 && !errorFieldName) {
            binding.messageName.setText(getString(R.string.empty_field));
            binding.messageName.setVisibility(View.VISIBLE);
            binding.fabAdd.setClickable(false);
        }
    }

    public void updateDevice() {
        // TODO: Updating the BT device.
    }
}