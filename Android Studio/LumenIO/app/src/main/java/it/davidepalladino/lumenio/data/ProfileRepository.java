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

    public Profile getById(int id) { return this.profileDao.getById(id); }
//    public LiveData<Profile> 0getById(int id) { return this.profileDao.getById(id); }

    public void insert(Profile profile) {
        this.profileDao.insert(profile);
    }
}
