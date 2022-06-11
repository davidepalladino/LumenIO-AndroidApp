package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class LibraryViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;
    private LiveData<List<Profile>> allProfiles;

    public LibraryViewModel(Application application) {
        super(application);
        this.profileRepository = new ProfileRepository(application);

        this.allProfiles = profileRepository.getAll();
    }

    public LiveData<List<Profile>> getAll() {
        return this.allProfiles;
    }

//    public void loadSelectedByID(long id) {
//        Profile selectedProfile = profileRepository.getById(id);
//
//        if (selectedProfile == null) {
//            selectedProfile = new Profile("", 0, 0, 0, 0);
//            selectedProfile.id = 0;
//        }
//
//        this.selectedID.postValue(selectedProfile.id);
//        this.selectedName.postValue(selectedProfile.name);
//        this.selectedBrightness.postValue(selectedProfile.brightness);
//        this.selectedRed.postValue(selectedProfile.red);
//        this.selectedGreen.postValue(selectedProfile.green);
//        this.selectedBlue.postValue(selectedProfile.blue);
//    }

//    public int update() {
//        Profile selectedProfile = new Profile(this.selectedName.getValue(), this.selectedBrightness.getValue(), this.selectedRed.getValue(), this.selectedGreen.getValue(), this.selectedBlue.getValue());
//        selectedProfile.id = selectedID.getValue();
//        return profileRepository.update(selectedProfile);
//    }
}