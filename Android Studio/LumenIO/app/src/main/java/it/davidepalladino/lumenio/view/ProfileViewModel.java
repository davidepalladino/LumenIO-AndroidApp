package it.davidepalladino.lumenio.view;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ProfileViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;
    private LiveData<List<Profile>> allProfiles;

    private final MutableLiveData<Long> selectedID = new MutableLiveData<>((long) 0);
    private final MutableLiveData<String> selectedName = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedBrightness = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedRed = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedGreen = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedBlue = new MutableLiveData<>(0);

    public ProfileViewModel(Application application) {
        super(application);
        this.profileRepository = new ProfileRepository(application);

        this.allProfiles = profileRepository.getAll();
    }

    public LiveData<List<Profile>> getAll() {
        return this.allProfiles;
    }

    public void loadSelectedByID(long id) {
        Profile selectedProfile = profileRepository.getById(id);

        if (selectedProfile == null) {
            selectedProfile = new Profile("", 0, 0, 0, 0);
            selectedProfile.id = 0;
        }

        this.selectedID.postValue(selectedProfile.id);
        this.selectedName.postValue(selectedProfile.name);
        this.selectedBrightness.postValue(selectedProfile.brightness);
        this.selectedRed.postValue(selectedProfile.red);
        this.selectedGreen.postValue(selectedProfile.green);
        this.selectedBlue.postValue(selectedProfile.blue);
    }

    public long insert() {
        long newID = profileRepository.insert(new Profile(this.selectedName.getValue(), this.selectedBrightness.getValue(), this.selectedRed.getValue(), this.selectedGreen.getValue(), this.selectedBlue.getValue()));
        this.selectedID.postValue(newID);
        return newID;
    }

    public void update() {
        Profile selectedProfile = new Profile(this.selectedName.getValue(), this.selectedBrightness.getValue(), this.selectedRed.getValue(), this.selectedGreen.getValue(), this.selectedBlue.getValue());
        selectedProfile.id = selectedID.getValue();
        profileRepository.update(selectedProfile);
    }

    public void setSelectedName(String selectedName) { this.selectedName.setValue(selectedName); }

    public MutableLiveData<String> getSelectedName() { return this.selectedName; }

    public void setSelectedBrightness(int selectedBrightness) { this.selectedBrightness.setValue(selectedBrightness); }

    public MutableLiveData<Integer> getSelectedBrightness() { return this.selectedBrightness; }

    public void setSelectedRed(int selectedRed) { this.selectedRed.setValue(selectedRed); }

    public MutableLiveData<Integer> getSelectedRed() { return this.selectedRed; }

    public void setSelectedGreen(int selectedGreen) { this.selectedGreen.setValue(selectedGreen); }

    public MutableLiveData<Integer> getSelectedGreen() { return this.selectedGreen; }

    public void setSelectedBlue(int selectedBlue) { this.selectedBlue.setValue(selectedBlue); }

    public MutableLiveData<Integer> getSelectedBlue() { return this.selectedBlue; }
}