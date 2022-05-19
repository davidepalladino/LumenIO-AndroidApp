package it.davidepalladino.lumenio.view;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileDao;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {
    private static final String TAG = "ProfileViewModel";
    public ProfileRepository profileRepository;
    private final MutableLiveData<Profile> selectedProfile = new MutableLiveData<>(null);

    public ProfileViewModel(Application application) {
        super(application);

        this.profileRepository = new ProfileRepository(application);

        profileRepository.getById(1).observeForever(profile -> selectedProfile.postValue(profile != null ? profile : new Profile("Manual", 10, 10, 10, 10)));
    }

    public MutableLiveData<Profile> getSelectedProfile() {
        return selectedProfile;
    }

    public void setSelectedProfile(Profile profile) {
        Log.d(TAG, String.valueOf(profile.red));
        this.selectedProfile.setValue(profile);
    }

    public LiveData<List<Profile>> getAll() {
        return profileRepository.getAll();
    }

    public LiveData<Profile> getById(int id) {
        return profileRepository.getById(1);
    }

    public void insert(Profile profile) {
        profileRepository.insert(profile);
    }









//    public LiveData<Profile> selectedProfile;

//    public ProfileViewModel(Application application) {
//        super(application);
//
//        this.profileRepository = new ProfileRepository(application);
//        this.allProfiles = profileRepository.getAll();
//        this.selectedProfile = profileRepository.getById(1);
//    }
//
////    public void setBlue(int blue) { this.selectedProfile.getValue().blue = blue; }
////
////    public int getBlue() { return this.selectedProfile.getValue().blue; }
//
//    public LiveData<List<Profile>> getAllProfiles() { return this.allProfiles; }
//
//    public LiveData<Profile> getSelectedProfile() { return this.selectedProfile; }
//
//    public void insert(Profile profile) { profileRepository.insert(profile); }
}
