package it.davidepalladino.lumenio.view;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {
    public ProfileRepository profileRepository;

    private final MutableLiveData<String> name = new MutableLiveData<String>("A");
    private final MutableLiveData<Integer> brightness = new MutableLiveData<Integer>(255);
    private final MutableLiveData<Integer> red = new MutableLiveData<Integer>(255);
    private final MutableLiveData<Integer> green = new MutableLiveData<Integer>(97);
    private final MutableLiveData<Integer> blue = new MutableLiveData<Integer>(92);

    public ProfileViewModel(Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);
    }

//    public void setProfileRepository(Application application) {
//        profileRepository = new ProfileRepository(application);
//    }

    public void setName(String name) { this.name.setValue(name); }

    public LiveData<String> getName() { return this.name; }

    public void setBrightness(int brightness) { this.brightness.setValue(brightness); }

    public LiveData<Integer> getBrightness() { return this.brightness; }

    public void setRed(int red) { this.red.setValue(red); }

    public LiveData<Integer> getRed() { return this.red; }

    public void setGreen(int green) { this.green.setValue(green); }

    public LiveData<Integer> getGreen() { return this.green; }

    public void setBlue(int blue) { this.blue.setValue(blue); }

    public LiveData<Integer> getBlue() { return this.blue; }

    public void insert() {
        profileRepository.insert(new Profile(this.name.getValue(), this.brightness.getValue(), this.red.getValue(), this.green.getValue(), this.blue.getValue()));
    }
}
