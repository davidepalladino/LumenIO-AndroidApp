package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ManualViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;
    private Profile selectedProfile;

    private final MutableLiveData<Long> selectedID = new MutableLiveData<>((long) 0);
    private final MutableLiveData<String> selectedName = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedBrightness = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedRed = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedGreen = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedBlue = new MutableLiveData<>(0);

    public ManualViewModel(Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);
    }

    public LiveData<Long> getSelectedID() { return selectedID; }

    public void setSelectedName(String selectedName) { this.selectedName.setValue(selectedName); }

    public MutableLiveData<String> getSelectedName() { return selectedName; }

    public void setSelectedBrightness(int selectedBrightness) { this.selectedBrightness.setValue(selectedBrightness); }

    public MutableLiveData<Integer> getSelectedBrightness() { return selectedBrightness; }

    public void setSelectedRed(int selectedRed) { this.selectedRed.setValue(selectedRed); }

    public MutableLiveData<Integer> getSelectedRed() { return selectedRed; }

    public void setSelectedGreen(int selectedGreen) { this.selectedGreen.setValue(selectedGreen); }

    public MutableLiveData<Integer> getSelectedGreen() { return selectedGreen; }

    public void setSelectedBlue(int selectedBlue) { this.selectedBlue.setValue(selectedBlue); }

    public MutableLiveData<Integer> getSelectedBlue() { return selectedBlue; }

    public Profile getOneByID(long id) {
        return profileRepository.getOneById(id);
    }

    public long insert() {
        selectedProfile = new Profile(selectedName.getValue(), selectedBrightness.getValue(), selectedRed.getValue(), selectedGreen.getValue(), selectedBlue.getValue());
        selectedProfile.id = profileRepository.insert(selectedProfile);
        selectedID.postValue(selectedProfile.id);
        return selectedProfile.id;
    }

    public void loadByID(long id) {
        selectedProfile = getOneByID(id);

        if (selectedProfile == null) {
            selectedProfile = new Profile("", 0, 0, 0, 0);
        }

        selectedID.postValue(selectedProfile.id);
        selectedName.postValue(selectedProfile.name);
        selectedBrightness.postValue(selectedProfile.brightness);
        selectedRed.postValue(selectedProfile.red);
        selectedGreen.postValue(selectedProfile.green);
        selectedBlue.postValue(selectedProfile.blue);
    }

    public void reload() {
        loadByID(selectedProfile.id);
    }
}