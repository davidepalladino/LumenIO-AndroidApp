package it.davidepalladino.lumenio.data;

import android.app.Application;

import java.util.List;

import it.davidepalladino.lumenio.util.AppDatabase;

public class SceneRepository {
    private final SceneDao sceneDao;

    public SceneRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);

        this.sceneDao = database.sceneDao();
    }

    public List<Scene> getProfilesAndScenes() { return this.sceneDao.getProfilesAndScenes(); }

    public void update(Scene scene) {
        if (sceneDao.getOneById(scene.id) == null) {
            sceneDao.insert(scene);
        } else {
            sceneDao.update(scene);
        }
    }
}
