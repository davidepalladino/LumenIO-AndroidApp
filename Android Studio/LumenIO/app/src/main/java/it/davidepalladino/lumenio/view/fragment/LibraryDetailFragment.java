package it.davidepalladino.lumenio.view.fragment;

import android.app.AlertDialog;
import android.bluetooth.BluetoothManager;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.TransitionInflater;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Scene;
import it.davidepalladino.lumenio.databinding.FragmentLibraryDetailBinding;
import it.davidepalladino.lumenio.util.BluetoothService;
import it.davidepalladino.lumenio.view.viewModel.ManualViewModel;
import it.davidepalladino.lumenio.view.viewModel.LibraryViewModel;
import it.davidepalladino.lumenio.view.viewModel.SceneViewModel;

public class LibraryDetailFragment extends Fragment {
    public static final String BUNDLE_PROFILE_ID = "PROFILE_ID";

    private FragmentLibraryDetailBinding fragmentLibraryDetailBinding;
    private LibraryViewModel libraryViewModel;
    private SceneViewModel sceneViewModel;
    private ManualViewModel manualViewModel;

    private BluetoothService bluetoothService;

    private Menu menu;
    private MenuInflater inflater;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        bluetoothService = BluetoothService.getInstance(requireActivity().getSystemService(BluetoothManager.class).getAdapter());

        TransitionInflater inflater = TransitionInflater.from(requireContext());
        setEnterTransition(inflater.inflateTransition(R.transition.slide_right));
        setExitTransition(inflater.inflateTransition(R.transition.slide_left));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        libraryViewModel = new ViewModelProvider(requireActivity()).get(LibraryViewModel.class);
        sceneViewModel = new ViewModelProvider(requireActivity()).get(SceneViewModel.class);
        manualViewModel = new ViewModelProvider(requireActivity()).get(ManualViewModel.class);

        fragmentLibraryDetailBinding = FragmentLibraryDetailBinding.inflate(inflater, container, false);
        fragmentLibraryDetailBinding.setLifecycleOwner(this);
        fragmentLibraryDetailBinding.setLibraryViewModel(libraryViewModel);
        fragmentLibraryDetailBinding.setButtonsHandler(new ButtonsHandler());

        new Thread(() -> libraryViewModel.loadByID(getArguments().getLong(BUNDLE_PROFILE_ID))).start();

        return fragmentLibraryDetailBinding.getRoot();
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
        fragmentLibraryDetailBinding = null;
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

    public void checkSyntaxName(CharSequence s, int start, int before, int count) {
        if (s.toString().matches(getString(R.string.sentence_incorrect_only_white_space))) {
            fragmentLibraryDetailBinding.messageName.setText(getString(R.string.empty_field));
            fragmentLibraryDetailBinding.messageName.setVisibility(View.VISIBLE);
        } else if (s.toString().matches(getString(R.string.sentence_incorrect_white_space_start))) {
            fragmentLibraryDetailBinding.messageName.setText(R.string.incorrect_start_field);
            fragmentLibraryDetailBinding.messageName.setVisibility(View.VISIBLE);
        } else if (s.toString().matches(getString(R.string.sentence_correct))) {
            fragmentLibraryDetailBinding.messageName.setText("");
            fragmentLibraryDetailBinding.messageName.setVisibility(View.GONE);
        }
    }

    private void showRemoveDialog() {
        String dialogMessage = getString(R.string.questionRemoveProfilePre) + " " + fragmentLibraryDetailBinding.nameTextView.getText().toString() + " " + getString(R.string.questionRemoveProfilePost);
        SpannableString spannableDialogMessage = new SpannableString(dialogMessage);
        spannableDialogMessage.setSpan(
                new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)),
                getString(R.string.questionRemoveProfilePre).length() + 1,
                getString(R.string.questionRemoveProfilePre).length() + 1 + fragmentLibraryDetailBinding.nameTextView.getText().toString().length(),
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
        if (bluetoothService.isConnected()) {
            String json = "";
            try {
                json = new JSONObject()
                        .put("Request", 1)
                        .put("Values", new JSONObject()
                                .put("Brightness", fragmentLibraryDetailBinding.seekbarBrightness.getProgress())
                                .put("Red", fragmentLibraryDetailBinding.seekbarRed.getProgress())
                                .put("Green", fragmentLibraryDetailBinding.seekbarGreen.getProgress())
                                .put("Blue", fragmentLibraryDetailBinding.seekbarBlue.getProgress())
                        )
                        .toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            bluetoothService.writeData(requireContext(), json);
        }
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
            Snackbar.make(fragmentLibraryDetailBinding.getRoot(), spannableSnackbarMessage, 5000).show();
        }).start();
    }

    private void deleteProfile() {
        new Thread(() -> {
            try {
                String snackbarMessage = "";

                libraryViewModel.delete();
                if (libraryViewModel.getSelectedID().getValue() == (manualViewModel.getSelectedID().getValue())) {
                    manualViewModel.loadByID(0);
                }

                snackbarMessage = libraryViewModel.getSelectedName().getValue() + " " + getString(R.string.profile_removed);
                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Snackbar.make(fragmentLibraryDetailBinding.getRoot(), spannableSnackbarMessage, 5000).show();
            } catch (SQLiteConstraintException ignored) { }
        }).start();

        /* Checking if this profile is used for some scene. If so, the scene will be removed. */
        sceneViewModel.getScenesAll().observe(requireActivity(), scenes -> {
            Iterator iteratorScenesAll = scenes.iterator();
            while (iteratorScenesAll.hasNext()) {
                Scene scene = (Scene) iteratorScenesAll.next();
                if (scene.profileId == libraryViewModel.getSelectedID().getValue()) {
                    new Thread(() -> {
                        sceneViewModel.deleteScene(scene);
                    }).start();
                    break;
                }
            }
        });
    }

    private void enableEditMode() {
        fragmentLibraryDetailBinding.controlCard.setVisibility(View.VISIBLE);
        fragmentLibraryDetailBinding.nameEditText.setVisibility(View.VISIBLE);
        fragmentLibraryDetailBinding.nameTextView.setVisibility(View.GONE);
        fragmentLibraryDetailBinding.messageName.setVisibility(View.VISIBLE);
    }

    public void disableEditMode() {
        fragmentLibraryDetailBinding.controlCard.setVisibility(View.GONE);
        fragmentLibraryDetailBinding.nameEditText.setVisibility(View.GONE);
        fragmentLibraryDetailBinding.nameTextView.setVisibility(View.VISIBLE);
        fragmentLibraryDetailBinding.messageName.setVisibility(View.GONE);

        /* Disabling the keyboard. */
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
    }

    public class ButtonsHandler {
        public void controlIt(View v) {
            new Thread(() -> {
                SharedPreferences.Editor sharedPreferencesEditor = requireActivity().getPreferences(Context.MODE_PRIVATE).edit();
                sharedPreferencesEditor.putLong(getString(R.string.latest_profile_selected), libraryViewModel.getSelectedID().getValue());
                sharedPreferencesEditor.apply();

                libraryViewModel.updateUse();
                libraryViewModel.reload();
                manualViewModel.loadByID(libraryViewModel.getSelectedID().getValue());

                updateDevice();

                Snackbar.make(fragmentLibraryDetailBinding.getRoot(), getString(R.string.profile_loaded), 5000).show();
            }).start();
        }

        public void setAsScene(View v) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(requireActivity());
            alertDialog
                .setTitle("Select a scene slot")
                .setItems(R.array.scenes_array, (dialog, which) -> {
                    AtomicReference<String> snackbarMessage = new AtomicReference<>("");

                    new Thread(() -> {
                        snackbarMessage.set(libraryViewModel.getSelectedName().getValue() + " ");

                        try {
                            Scene scene = new Scene(which + 1, libraryViewModel.getSelectedID().getValue());
                            sceneViewModel.updateScene(scene);

                            snackbarMessage.set(snackbarMessage.get() + getString(R.string.scene_saved) + " " + scene.id + ".");
                        } catch (Exception e) {
                            snackbarMessage.set(snackbarMessage.get() + getString(R.string.scene_not_saved_for_name));
                        }

                        SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage.toString());
                        spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, libraryViewModel.getSelectedName().getValue().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        Snackbar.make(fragmentLibraryDetailBinding.getRoot(), spannableSnackbarMessage, 5000).show();
                    }).start();
                })
                .create()
                .show();
        }
    }
}