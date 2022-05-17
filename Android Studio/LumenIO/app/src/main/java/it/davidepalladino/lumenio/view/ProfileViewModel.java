package it.davidepalladino.lumenio.view;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.List;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileDao;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {
    public ProfileRepository profileRepository;
    public LiveData<List<Profile>> allProfiles;

//    private final MutableLiveData<String> name;
//    private final MutableLiveData<Integer> brightness;
//    private final MutableLiveData<Integer> red;
//    private final MutableLiveData<Integer> green;
//    private final MutableLiveData<Integer> blue;
//
//    public ProfileViewModel(Application application) {
//        super(application);
//
//        this.profileRepository = new ProfileRepository(application);
//        this.allProfiles = profileRepository.getAll();
//
//        Profile profileSelected = profileRepository.getById(1).getValue();
//        if (profileSelected == null) {
//            profileSelected = new Profile("Manual", 0,0,0,0);
//        }
//
//        this.name = new MutableLiveData<String>(profileSelected.name);
//        this.brightness = new MutableLiveData<Integer>(profileSelected.brightness);
//        this.red = new MutableLiveData<Integer>(profileSelected.red);
//        this.blue = new MutableLiveData<Integer>(profileSelected.green);
//        this.green = new MutableLiveData<Integer>(profileSelected.blue);
//    }
//
//    public void setName(String name) { this.name.setValue(name); }
//
//    public MutableLiveData<String> getName() { return this.name; }
//
//    public void setBrightness(int brightness) { this.brightness.setValue(brightness); }
//
//    public MutableLiveData<Integer> getBrightness() { return this.brightness; }
//
//    public void setRed(int red) { this.red.setValue(red); }
//
//    public MutableLiveData<Integer> getRed() { return this.red; }
//
//    public void setGreen(int green) { this.green.setValue(green); }
//
//    public MutableLiveData<Integer> getGreen() { return this.green; }
//
//    public void setBlue(int blue) { this.blue.setValue(blue); }
//
//    public MutableLiveData<Integer> getBlue() { return this.blue; }
//
//    public LiveData<List<Profile>> getAllProfiles() { return this.allProfiles; }
//
//    public void insert() {
//        profileRepository.insert(new Profile(this.name.getValue(), this.brightness.getValue(), this.red.getValue(), this.green.getValue(), this.blue.getValue()));
//    }









    public LiveData<Profile> selectedProfile;

    public ProfileViewModel(Application application) {
        super(application);

        this.profileRepository = new ProfileRepository(application);
        this.allProfiles = profileRepository.getAll();
        this.selectedProfile = profileRepository.getById(1);

//        Profile profileSelected = profileRepository.getById(1).getValue();
//        if (profileSelected == null) {
//            profileSelected = new Profile("Manual", 0,0,0,0);
//        }
//        this.selectedProfile = new MutableLiveData<Profile>(profileSelected);
    }

//    public void setBlue(int blue) { this.selectedProfile.getValue().blue = blue; }
//
//    public int getBlue() { return this.selectedProfile.getValue().blue; }

    public LiveData<List<Profile>> getAllProfiles() { return this.allProfiles; }

    public LiveData<Profile> getSelectedProfile() { return this.selectedProfile; }

    public void insert(Profile profile) { profileRepository.insert(profile); }
}
