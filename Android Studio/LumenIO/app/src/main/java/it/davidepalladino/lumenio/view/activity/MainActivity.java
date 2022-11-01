package it.davidepalladino.lumenio.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.databinding.ActivityMainBinding;
import it.davidepalladino.lumenio.view.fragment.LibraryFragment;
import it.davidepalladino.lumenio.view.fragment.ManualFragment;
import it.davidepalladino.lumenio.view.fragment.SceneFragment;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding activityMainBinding;

    private FragmentManager fragmentManager;
    private Fragment actualFragment;
    private ManualFragment manualFragment;
    private SceneFragment sceneFragment;
    private LibraryFragment libraryFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());

        setContentView(activityMainBinding.getRoot());
        setSupportActionBar(activityMainBinding.toolbar);


        activityMainBinding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemID = item.getItemId();
            switch (itemID) {
                case R.id.bottom_navigation_manual:
                    fragmentManager.beginTransaction().detach(actualFragment).attach(manualFragment).commit();
                    actualFragment = manualFragment;

                    return true;
                case R.id.bottom_navigation_scene:
                    fragmentManager.beginTransaction().detach(actualFragment).attach(sceneFragment).commit();
                    actualFragment = sceneFragment;

                    return true;
                case R.id.bottom_navigation_library:
                    fragmentManager.beginTransaction().detach(actualFragment).attach(libraryFragment).commit();
                    actualFragment = libraryFragment;

                    return true;
            }

            return false;
        });

        fragmentManager = getSupportFragmentManager();
        manualFragment = ManualFragment.newInstance();
        sceneFragment = SceneFragment.newInstance();
        libraryFragment = LibraryFragment.newInstance();
        actualFragment = manualFragment;

        fragmentManager.beginTransaction().add(activityMainBinding.frameLayout.getId(), libraryFragment).detach(libraryFragment).commit();
        fragmentManager.beginTransaction().add(activityMainBinding.frameLayout.getId(), sceneFragment).detach(sceneFragment).commit();
        fragmentManager.beginTransaction().add(activityMainBinding.frameLayout.getId(), manualFragment).commit();
    }
}