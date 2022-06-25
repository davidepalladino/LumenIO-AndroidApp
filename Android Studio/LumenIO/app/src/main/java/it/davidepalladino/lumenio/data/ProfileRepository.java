package it.davidepalladino.lumenio.data;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import it.davidepalladino.lumenio.util.AppDatabase;

public class ProfileRepository {
    private final ProfileDao profileDao;
    private LiveData<List<Profile>> allProfiles;

    public ProfileRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);

        this.profileDao = database.profileDao();
        this.allProfiles = profileDao.getAll();
    }

    public LiveData<List<Profile>> getAll() { return this.profileDao.getAll(); }

    public Profile getOneById(long id) {
        return this.profileDao.getOneById(id);
    }

    public Profile getOneByName(String name) {
        return this.profileDao.getOneByName(name);
    }

    public long insert(Profile profile) {
        cleanName(profile);

        long actualTime = System.currentTimeMillis() / 1000;
        profile.createdAt = actualTime;
        profile.updatedAt = 0;
        profile.usedAt = actualTime;

        return this.profileDao.insert(profile);
    }

    public int updateValues(Profile profile) {
        cleanName(profile);

        profile.updatedAt = System.currentTimeMillis() / 1000;
        return this.profileDao.update(profile);
    }

    public int updateUse(Profile profile) {
        profile.usedAt = System.currentTimeMillis() / 1000;
        return this.profileDao.update(profile);
    }

    public void delete(Profile profile) {
        this.profileDao.delete(profile);
    }

    private void cleanName(Profile profile) {
        if (profile.name.charAt(0) == ' ') {
            profile.name = profile.name.substring(1);
        }

        if (profile.name.charAt(profile.name.length() - 1) == ' ') {
            profile.name = profile.name.substring(0, profile.name.length() - 1);
        }
    }
}
