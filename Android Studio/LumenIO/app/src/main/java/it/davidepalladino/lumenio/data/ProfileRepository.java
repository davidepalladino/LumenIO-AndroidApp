package it.davidepalladino.lumenio.data;

import android.app.Application;

import it.davidepalladino.lumenio.util.AppDatabase;

public class ProfileRepository {
    private final ProfileDao profileDao;

    public ProfileRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        profileDao = database.profileDao();
    }

    public void insert(Profile profile) {
        profileDao.insert(profile);
    }
}
