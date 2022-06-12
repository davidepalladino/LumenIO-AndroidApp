package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class LibraryViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;
    private LiveData<List<Profile>> allProfiles;

    private final MutableLiveData<Long> selectedID = new MutableLiveData<>((long) 0);
    private final MutableLiveData<String> selectedName = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedBrightness = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedRed = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedGreen = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedBlue = new MutableLiveData<>(0);
    private final MutableLiveData<String> selectedCreatedAt = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedUpdatedAt = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedUsedAt = new MutableLiveData<>("");

    public LibraryViewModel(Application application) {
        super(application);
        this.profileRepository = new ProfileRepository(application);

        this.allProfiles = profileRepository.getAll();
    }

    public LiveData<List<Profile>> getAll() {
        return this.allProfiles;
    }

    public void loadSelectedByID(long id) {
        Profile selectedProfile = profileRepository.getOneById(id);

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
        this.selectedCreatedAt.postValue(selectedProfile.createdAt != 0 ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(selectedProfile.createdAt * 1000)) : "");
        this.selectedUpdatedAt.postValue(selectedProfile.updatedAt != 0 ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(selectedProfile.updatedAt * 1000)) : "");
        this.selectedUsedAt.postValue(selectedProfile.usedAt != 0 ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(selectedProfile.usedAt * 1000)) : "");
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

    public LiveData<String> getSelectedCreatedAt() { return this.selectedCreatedAt; }

    public LiveData<String> getSelectedUpdatedAt() { return this.selectedUpdatedAt; }

    public LiveData<String> getSelectedUsedAt() { return this.selectedUsedAt; }

//    public int update() {
//        Profile selectedProfile = new Profile(this.selectedName.getValue(), this.selectedBrightness.getValue(), this.selectedRed.getValue(), this.selectedGreen.getValue(), this.selectedBlue.getValue());
//        selectedProfile.id = selectedID.getValue();
//        return profileRepository.update(selectedProfile);
//    }
}