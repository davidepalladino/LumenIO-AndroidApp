package it.davidepalladino.lumenio.view.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.Scene;
import it.davidepalladino.lumenio.view.viewModel.SceneViewModel;

public class SearchProfileSceneDialog extends DialogFragment {
    private SceneViewModel sceneViewModel;

    private Observer<List<Profile>> profileGetAllObserver;

    private final int sceneID;
    private final View viewHost;

    public SearchProfileSceneDialog(View viewHost, int sceneID) {
        this.viewHost = viewHost;
        this.sceneID = sceneID;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = requireActivity().getLayoutInflater().inflate(R.layout.dialog_search_profile_scene, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity()).setView(view);
        AlertDialog dialog = builder.create();

        SearchView searchView = view.findViewById(R.id.search_dialog_search_profile_scene);
        ListView listView = view.findViewById(R.id.list_profiles_dialog_search_profile_scene);

        profileGetAllObserver = profiles -> {
            ArrayAdapter<Profile> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, profiles);

            listView.setAdapter(adapter);
            listView.setOnItemClickListener((parent, view1, position, id) -> new Thread(() -> {
                Profile profileSelected = adapter.getItem(position);
                Scene sceneSelected = new Scene(this.sceneID, profileSelected.id);

                String snackbarMessage = profileSelected.name + " ";

                try {
                    sceneViewModel.updateScene(sceneSelected);
                    snackbarMessage += getString(R.string.scene_saved) + " " + sceneSelected.id + ".";
                } catch (Exception e) {
                    snackbarMessage += getString(R.string.scene_not_saved_for_name);
                }

                SearchProfileSceneDialog.this.dismiss();

                SpannableString spannableSnackbarMessage = new SpannableString(snackbarMessage);
                spannableSnackbarMessage.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.BOLD_ITALIC)), 0, profileSelected.name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Snackbar.make(viewHost, spannableSnackbarMessage, 5000).show();

            }).start());

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    adapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return false;
                }
            });
        };

        sceneViewModel = new ViewModelProvider(requireActivity()).get(SceneViewModel.class);
        sceneViewModel.getProfilesAll().observe(requireActivity(), profileGetAllObserver);

        return dialog;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sceneViewModel.getProfilesAll().removeObserver(profileGetAllObserver);
    }
}
