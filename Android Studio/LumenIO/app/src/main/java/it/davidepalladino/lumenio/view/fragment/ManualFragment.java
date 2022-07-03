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
    private FragmentManualBinding fragmentManualBinding;
    private ManualViewModel manualViewModel;

    private boolean errorSyntaxFieldName = false;

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

        fragmentManualBinding = FragmentManualBinding.inflate(inflater, container, false);
        fragmentManualBinding.setLifecycleOwner(this);

        fragmentManualBinding.setManualViewModel(manualViewModel);
        fragmentManualBinding.setManualFragment(this);

        return fragmentManualBinding.getRoot();
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
        fragmentManualBinding = null;
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

    public void checkSyntaxName(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            errorSyntaxFieldName = true;

            fragmentManualBinding.messageName.setText(getString(R.string.empty_field));
            fragmentManualBinding.messageName.setVisibility(View.VISIBLE);
            fragmentManualBinding.fabAdd.setClickable(false);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start))) {
            errorSyntaxFieldName = true;

            fragmentManualBinding.messageName.setText(R.string.incorrect_start_field);
            fragmentManualBinding.messageName.setVisibility(View.VISIBLE);
            fragmentManualBinding.fabAdd.setClickable(false);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            errorSyntaxFieldName = false;

            fragmentManualBinding.messageName.setText("");
            fragmentManualBinding.messageName.setVisibility(View.GONE);
            fragmentManualBinding.fabAdd.setClickable(true);
        }
    }

    public void saveToTheLibrary() {
        if (fragmentManualBinding.name.getText().length() > 0 && !errorSyntaxFieldName) {
            new Thread(() -> {
                String snackbarMessage =  manualViewModel.getSelectedName().getValue();

                try {
                    SharedPreferences.Editor profileSelectedPreference = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                    profileSelectedPreference.putLong(getString(R.string.latest_profile_selected), manualViewModel.insert());
                    profileSelectedPreference.apply();

                    manualViewModel.reload();

                    snackbarMessage += " " + getString(R.string.profile_saved);
                } catch (SQLiteConstraintException e) {
                    snackbarMessage += " " + getString(R.string.profile_not_saved_for_name);
                }

                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, manualViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Snackbar.make(fragmentManualBinding.getRoot(), spannableSnackbarMessage, 5000).setAnchorView(fragmentManualBinding.fabAdd).show();
            }).start();
        } else if (fragmentManualBinding.name.getText().length() == 0 && !errorSyntaxFieldName) {
            fragmentManualBinding.messageName.setText(getString(R.string.empty_field));
            fragmentManualBinding.messageName.setVisibility(View.VISIBLE);
            fragmentManualBinding.fabAdd.setClickable(false);
        }
    }

    public void updateDevice() {
        // TODO: Updating the BT device.
    }
}