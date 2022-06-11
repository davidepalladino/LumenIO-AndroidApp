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

    public Profile getById(long id) {
        return this.profileDao.getById(id);
    }

    public long insert(Profile profile) {
        return this.profileDao.insert(profile);
    }

    public int update(Profile profile) { return this.profileDao.update(profile); }
}
