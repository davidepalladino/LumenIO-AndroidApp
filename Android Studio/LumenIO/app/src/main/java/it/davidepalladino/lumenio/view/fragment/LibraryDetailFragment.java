package it.davidepalladino.lumenio.view.fragment;

import android.app.AlertDialog;
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
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.FragmentLibraryDetailBinding;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;

public class LibraryDetailFragment extends Fragment {
    public static final String BUNDLE_PROFILE_ID = "PROFILE_ID";

    private FragmentLibraryDetailBinding binding;
    private LibraryViewModel libraryViewModel;
    private ManualViewModel manualViewModel;

    private Menu menu;
    private MenuInflater inflater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        binding = FragmentLibraryDetailBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        binding.setLibraryViewModel(libraryViewModel);
        binding.setButtonsHandler(new ButtonsHandler());

        new Thread(() -> libraryViewModel.loadByID(getArguments().getLong(BUNDLE_PROFILE_ID))).start();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        Objects.requireNonNull(appCompatActivity.getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        appCompatActivity.getSupportActionBar().setTitle(R.string.detail);
    }

    @Override
    public void onPause() {
        super.onPause();

        NavHostFragment.findNavController(this).navigateUp();

//        disableEditMode();
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                libraryViewModel.reload();
//            }
//        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        this.menu = menu;
        this.inflater = inflater;

        this.menu.clear();
        this.inflater.inflate(R.menu.menu_library_detail_no_edit, this.menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            requireActivity().onBackPressed();
            return true;
        } else if (id == R.id.edit) {
            enableEditMode();

            this.menu.clear();
            this.inflater.inflate(R.menu.menu_library_detail_edit, this.menu);

            return true;
        } else if (id == R.id.discard) {
            disableEditMode();

            new Thread(() -> libraryViewModel.reload()).start();

            this.menu.clear();
            this.inflater.inflate(R.menu.menu_library_detail_no_edit, this.menu);

            return true;
        } else if (id == R.id.accept) {
            updateProfile();
            disableEditMode();

            this.menu.clear();
            this.inflater.inflate(R.menu.menu_library_detail_no_edit, this.menu);

            return true;
        } else if (id == R.id.remove) {
            showRemoveDialog();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void checkName(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            binding.messageName.setText(getString(R.string.empty_field));
            binding.messageName.setVisibility(View.VISIBLE);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start_end))) {
            binding.messageName.setText(R.string.incorrect_start_end_field);
            binding.messageName.setVisibility(View.VISIBLE);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            binding.messageName.setText("");
            binding.messageName.setVisibility(View.GONE);
        }
    }

    private void showRemoveDialog() {
        String dialogMessage = getString(R.string.questionRemoveProfilePre) + " " + binding.nameTextView.getText().toString() + " " + getString(R.string.questionRemoveProfilePost);

        SpannableString spannableDialogMessage = new SpannableString(dialogMessage);
        spannableDialogMessage.setSpan(
                new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)),
                getString(R.string.questionRemoveProfilePre).length() + 1,
                getString(R.string.questionRemoveProfilePre).length() + 1 + binding.nameTextView.getText().toString().length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


        AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
        alertDialog
                .setMessage(spannableDialogMessage)
                .setPositiveButton(getString(R.string.remove), (dialog, idDialog) -> {
                    deleteProfile();
                    requireActivity().onBackPressed();
                })
                .setNegativeButton(getString(R.string.discard), (dialog, idDialog) -> {
                })
                .create()
                .show();
    }

    public void updateDevice() {
        // TODO: Updating the BT device.
    }

    private void updateProfile() {
        new Thread(() -> {
            String snackbarMessage = "";

            try {
                libraryViewModel.updateValues();
                libraryViewModel.reload();

                if (Objects.requireNonNull(libraryViewModel.getSelectedID().getValue()).longValue() == Objects.requireNonNull(manualViewModel.getSelectedID().getValue()).longValue()) {
                    manualViewModel.reload();
                }

                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_updated);
            } catch (SQLiteConstraintException e) {
                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_not_updated_for_name);
            }

            SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
            spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            Snackbar.make(binding.getRoot(), spannableSnackbarMessage, 5000).show();
        }).start();
    }

    private void deleteProfile() {
        new Thread(() -> {
            String snackbarMessage = "";

            try {
                libraryViewModel.delete();
                if (libraryViewModel.getSelectedID().getValue() == (manualViewModel.getSelectedID().getValue())) {
                    manualViewModel.loadByID(0);
                }

                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_removed);
                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                Snackbar.make(binding.getRoot(), spannableSnackbarMessage, 5000).show();
            } catch (SQLiteConstraintException ignored) { }
        }).start();
    }

    private void enableEditMode() {
        binding.controlCard.setVisibility(View.VISIBLE);
        binding.nameEditText.setVisibility(View.VISIBLE);
        binding.nameTextView.setVisibility(View.GONE);
        binding.messageName.setVisibility(View.VISIBLE);
    }

    public void disableEditMode() {
        binding.controlCard.setVisibility(View.GONE);
        binding.nameEditText.setVisibility(View.GONE);
        binding.nameTextView.setVisibility(View.VISIBLE);
        binding.messageName.setVisibility(View.GONE);

        /* Disabling the keyboard. */
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
    }



    public class ButtonsHandler {
        public void controlIt(View v) {
            new Thread(() -> {
                SharedPreferences.Editor profileSelectedPreference = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                profileSelectedPreference.putLong(getString(R.string.latest_profile_selected), libraryViewModel.getSelectedID().getValue());
                profileSelectedPreference.apply();

                libraryViewModel.updateUse();
                libraryViewModel.reload();
                manualViewModel.loadByID(libraryViewModel.getSelectedID().getValue());

                Snackbar.make(binding.getRoot(), getString(R.string.profile_loaded), 5000).show();
            }).start();
        }
    }
}