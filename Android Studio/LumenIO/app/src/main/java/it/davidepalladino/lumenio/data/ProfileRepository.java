package it.davidepalladino.lumenio.data;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

import it.davidepalladino.lumenio.util.AppDatabase;

public class ProfileRepository {
    private final ProfileDao profileDao;

    public ProfileRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        profileDao = database.profileDao();
    }

    public LiveData<List<Profile>> getAll() { return profileDao.getAll(); }

    public LiveData<List<Profile>> getAllByName(String name) { return profileDao.getAllByName("%" + name + "%"); }

    public Profile getOneById(long id) {
        return profileDao.getOneById(id);
    }

    public LiveData<Profile> getOneByIdLive(long id) {
        return profileDao.getOneByIdLive(id);
    }

    public long insert(Profile profile) {
        cleanName(profile);

        long actualTime = System.currentTimeMillis() / 1000;
        profile.createdAt = actualTime;
        profile.updatedAt = 0;
        profile.usedAt = actualTime;

        return profileDao.insert(profile);
    }

    public void updateValues(Profile profile) {
        cleanName(profile);

        profile.updatedAt = System.currentTimeMillis() / 1000;
        profileDao.update(profile);
    }

    public void updateUse(Profile profile) {
        profile.usedAt = System.currentTimeMillis() / 1000;
        profileDao.update(profile);
    }

    public void delete(Profile profile) {
        profileDao.delete(profile);
    }

    private void cleanName(Profile profile) {
        if (profile.name.charAt(0) == ' ') {
            profile.name = profile.name.substring(1);
        }

        /* Checking and clean empty space at the end of name. */
        boolean noEmptySpace = false;
        while(!noEmptySpace) {
            if (profile.name.charAt(profile.name.length() - 1) == ' ') {
                profile.name = profile.name.substring(0, profile.name.length() - 1);
                continue;
            }

            noEmptySpace = true;
        }
    }
}
