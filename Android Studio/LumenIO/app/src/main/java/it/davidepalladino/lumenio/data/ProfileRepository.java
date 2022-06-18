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

    public LiveData<List<Profile>> getAll() { return this.allProfiles; }

    public Profile getOneById(long id) {
        return this.profileDao.getOneById(id);
    }

    public Profile getOneByName(String name) {
        return this.profileDao.getOneByName(name);
    }

    public long insert(Profile profile) {
        profile.createdAt = System.currentTimeMillis() / 1000;
        profile.updatedAt = 0;
        profile.usedAt = 0;

        return this.profileDao.insert(profile);
    }

    public int update(Profile profile) {
        profile.updatedAt = System.currentTimeMillis() / 1000;
        return this.profileDao.update(profile);
    }

    public void delete(Profile profile) {
        this.profileDao.delete(profile);
    }
}
