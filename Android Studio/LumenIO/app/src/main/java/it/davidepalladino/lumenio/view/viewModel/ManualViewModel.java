package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ManualViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;

    private final MutableLiveData<Long> selectedID = new MutableLiveData<>((long) 0);
    private final MutableLiveData<String> selectedName = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedBrightness = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedRed = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedGreen = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedBlue = new MutableLiveData<>(0);

    public ManualViewModel(Application application) {
        super(application);
        this.profileRepository = new ProfileRepository(application);
    }

    public LiveData<Long> getSelectedID() { return this.selectedID; }

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

    public Profile getOneByID(long id) {
        return this.profileRepository.getOneById(id);
    }

    public Profile getOneByName(String name) {
        return this.profileRepository.getOneByName(name);
    }

    public long insert() {
        return profileRepository.insert(new Profile(this.selectedName.getValue(), this.selectedBrightness.getValue(), this.selectedRed.getValue(), this.selectedGreen.getValue(), this.selectedBlue.getValue()));
    }

    public void loadByID(long id) {
        Profile selectedProfile = this.getOneByID(id);

        if (selectedProfile == null) {
            selectedProfile = new Profile("", 0, 0, 0, 0);
        }

        this.selectedID.postValue(selectedProfile.id);
        this.selectedName.postValue(selectedProfile.name);
        this.selectedBrightness.postValue(selectedProfile.brightness);
        this.selectedRed.postValue(selectedProfile.red);
        this.selectedGreen.postValue(selectedProfile.green);
        this.selectedBlue.postValue(selectedProfile.blue);
    }

    public void reload() { this.loadByID(this.selectedID.getValue().longValue()); }
}