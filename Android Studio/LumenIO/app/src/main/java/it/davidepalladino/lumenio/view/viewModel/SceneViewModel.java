package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.List;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;
import it.davidepalladino.lumenio.data.Scene;
import it.davidepalladino.lumenio.data.SceneRepository;

public class SceneViewModel extends AndroidViewModel {
    private final SceneRepository sceneRepository;
    private final ProfileRepository profileRepository;

    private LiveData<List<Scene>> scenesAll;
    private LiveData<List<Profile>> profilesAll;

    public SceneViewModel(Application application) {
        super(application);

        sceneRepository = new SceneRepository(application);
        profileRepository = new ProfileRepository(application);

        scenesAll = sceneRepository.getAll();
        profilesAll = profileRepository.getAll();
    }

    public LiveData<Scene> getSceneById(long id) { return sceneRepository.getOneById(id); }

    public LiveData<List<Scene>> getScenesAll() { return scenesAll; }

    public LiveData<List<Profile>> getProfilesAll() { return profilesAll; }

    public LiveData<Profile> getProfileById(long id) { return profileRepository.getOneByIdLive(id); }

    public void updateScene(Scene scene) {
        sceneRepository.update(scene);
    }

    public void deleteScene(Scene scene) {
        sceneRepository.delete(scene);
    }
}