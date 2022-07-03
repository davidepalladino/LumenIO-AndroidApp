package it.davidepalladino.lumenio.view.fragment;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.Scene;
import it.davidepalladino.lumenio.databinding.FragmentSceneBinding;
import it.davidepalladino.lumenio.view.dialog.SearchProfileSceneDialog;
import it.davidepalladino.lumenio.view.viewModel.SceneViewModel;

public class SceneFragment extends Fragment {
    private FragmentSceneBinding fragmentSceneBinding;
    private SceneViewModel sceneViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    // FIXME: Change the logic.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sceneViewModel = new ViewModelProvider(requireActivity()).get(SceneViewModel.class);
        sceneViewModel.getScenesAll().observe(requireActivity(), scenes -> {
            AtomicBoolean isSetSceneOne = new AtomicBoolean(false);
            AtomicBoolean isSetSceneTwo = new AtomicBoolean(false);
            AtomicBoolean isSetSceneThree = new AtomicBoolean(false);

            Iterator<Scene> iteratorScenes = scenes.iterator();
            while (iteratorScenes.hasNext()) {
                Scene sceneTemp = iteratorScenes.next();
                switch ((int) sceneTemp.id) {
                    case 1:
                        sceneViewModel.getProfileById(sceneTemp.profileId).observe(requireActivity(), profile -> {
                            if (profile != null) {
                                isSetSceneOne.set(true);
                            } else {
                                sceneViewModel.getProfileById(sceneTemp.profileId).removeObservers(requireActivity());
                                isSetSceneOne.set(false);
                            }

                            setSceneCard(profile, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne);
                        });
                        break;
                    case 2:
                        sceneViewModel.getProfileById(sceneTemp.profileId).observe(requireActivity(), profile -> {
                            if (profile != null) {
                                isSetSceneTwo.set(true);
                            } else {
                                sceneViewModel.getProfileById(sceneTemp.profileId).removeObservers(requireActivity());
                                isSetSceneTwo.set(false);
                            }

                            setSceneCard(profile, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo);
                        });
                        break;
                    case 3:
                        sceneViewModel.getProfileById(sceneTemp.profileId).observe(requireActivity(), profile -> {
                            if (profile != null) {
                                isSetSceneThree.set(true);
                            } else {
                                sceneViewModel.getProfileById(sceneTemp.profileId).removeObservers(requireActivity());
                                isSetSceneThree.set(false);
                            }

                            setSceneCard(profile, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree);
                        });
                        break;
                }
            }

            if (!isSetSceneOne.get()) {
                setSceneCard(null, fragmentSceneBinding.nameOne, fragmentSceneBinding.previewOne, fragmentSceneBinding.valuesOne);
            }

            if (!isSetSceneTwo.get()) {
                setSceneCard(null, fragmentSceneBinding.nameTwo, fragmentSceneBinding.previewTwo, fragmentSceneBinding.valuesTwo);
            }

            if (!isSetSceneThree.get()) {
                setSceneCard(null, fragmentSceneBinding.nameThree, fragmentSceneBinding.previewThree, fragmentSceneBinding.valuesThree);
            }
        });

        fragmentSceneBinding = FragmentSceneBinding.inflate(inflater, container, false);
        fragmentSceneBinding.setLifecycleOwner(this);

        fragmentSceneBinding.setSceneViewModel(sceneViewModel);
        fragmentSceneBinding.setSceneFragment(this);

        fragmentSceneBinding.nameOne.setOnClickListener(v -> {
            SearchProfileSceneDialog searchProfilesDialog = new SearchProfileSceneDialog(getView(), 1);
            searchProfilesDialog.show(getParentFragmentManager(), SceneFragment.class.getSimpleName());
        });

        fragmentSceneBinding.nameTwo.setOnClickListener(v -> {
            SearchProfileSceneDialog searchProfilesDialog = new SearchProfileSceneDialog(getView(), 2);
            searchProfilesDialog.show(getParentFragmentManager(), SceneFragment.class.getSimpleName());
        });

        fragmentSceneBinding.nameThree.setOnClickListener(v -> {
            SearchProfileSceneDialog searchProfilesDialog = new SearchProfileSceneDialog(getView(), 3);
            searchProfilesDialog.show(getParentFragmentManager(), SceneFragment.class.getSimpleName());
        });

        return fragmentSceneBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        fragmentSceneBinding = null;
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

    private void setSceneCard(Profile profile, TextView name, View preview, TextView values) {
        if (profile != null)  {
            name.setText(profile.name);

            values.setText(
                    String.format("%03d", profile.brightness) + " " +
                    String.format("%03d", profile.red) + " " +
                    String.format("%03d", profile.green) + " " +
                    String.format("%03d", profile.blue)
            );
            values.setVisibility(View.VISIBLE);

            preview.setBackground(new ColorDrawable(Color.argb(profile.brightness, profile.red, profile.green, profile.blue)));
            preview.setVisibility(View.VISIBLE);
        } else {
            String notSet = getString(R.string.not_set);
            SpannableString spannableNameNotSet = new SpannableString(notSet);
            spannableNameNotSet.setSpan(new TypefaceSpan(Typeface.create((String) null, Typeface.ITALIC)), 0, notSet.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            name.setText(notSet);
            values.setVisibility(View.GONE);

            preview.setBackground(new ColorDrawable(Color.argb(0, 0, 0,0)));
            preview.setVisibility(View.INVISIBLE);
        }
    }

    public void updateDevice() {
        // TODO: Updating the BT device.
    }
}