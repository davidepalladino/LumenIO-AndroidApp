package it.davidepalladino.lumenio.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.davidepalladino.lumenio.util.AppDatabase;

public class SceneRepository {
    private final SceneDao sceneDao;

    public SceneRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        sceneDao = database.sceneDao();
    }

    public LiveData<List<Scene>> getAll() { return sceneDao.getAll(); }

    public LiveData<Scene> getOneById(long id) { return sceneDao.getOneByIdLive(id); }

    public void update(Scene scene) {
        if (sceneDao.getOneById(scene.id) == null) {
            sceneDao.insert(scene);
        } else {
            sceneDao.update(scene);
        }
    }

    public void delete(Scene scene) { sceneDao.delete(scene); }
}
