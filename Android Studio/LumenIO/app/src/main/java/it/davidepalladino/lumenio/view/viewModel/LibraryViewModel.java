package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class LibraryViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;

    private LiveData<List<Profile>> allProfiles;
    private Profile selectedProfile;

    private final MutableLiveData<Long> selectedID = new MutableLiveData<>((long) 0);
    private final MutableLiveData<String> selectedName = new MutableLiveData<>("");
    private final MutableLiveData<Integer> selectedBrightness = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedRed = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedGreen = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> selectedBlue = new MutableLiveData<>(0);
    private final MutableLiveData<String> selectedCreatedAt = new MutableLiveData<>( "");
    private final MutableLiveData<String> selectedUpdatedAt = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedUsedAt = new MutableLiveData<>("");

    public LibraryViewModel(Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);

        allProfiles = profileRepository.getAll();
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

    public LiveData<String> getSelectedCreatedAt() { return selectedCreatedAt; }

    public LiveData<String> getSelectedUpdatedAt() { return selectedUpdatedAt; }

    public LiveData<String> getSelectedUsedAt() { return selectedUsedAt; }

    public Profile getOneByID(long id) { return profileRepository.getOneById(id); }

    public LiveData<List<Profile>> getAll() {
        return allProfiles;
    }

    public int updateValues() {
        selectedProfile.setValues(selectedName.getValue(), selectedBrightness.getValue(), selectedRed.getValue(), selectedGreen.getValue(), selectedBlue.getValue());
        return profileRepository.updateValues(selectedProfile);
    }

    public int updateUse() {
        return profileRepository.updateUse(selectedProfile);
    }

    public void delete() {
        profileRepository.delete(selectedProfile);
    }

    public void loadByID(long id) {
        selectedProfile = getOneByID(id);

        selectedID.postValue(selectedProfile.id);
        selectedName.postValue(selectedProfile.name);
        selectedBrightness.postValue(selectedProfile.brightness);
        selectedRed.postValue(selectedProfile.red);
        selectedGreen.postValue(selectedProfile.green);
        selectedBlue.postValue(selectedProfile.blue);
        selectedCreatedAt.postValue(selectedProfile.createdAt != 0 ? new SimpleDateFormat(getApplication().getString(R.string.datetime_format)).format(new Date(selectedProfile.createdAt * 1000)) : "");
        selectedUpdatedAt.postValue(selectedProfile.updatedAt != 0 ? new SimpleDateFormat(getApplication().getString(R.string.datetime_format)).format(new Date(selectedProfile.updatedAt * 1000)) : "");
        selectedUsedAt.postValue(selectedProfile.usedAt != 0 ? new SimpleDateFormat(getApplication().getString(R.string.datetime_format)).format(new Date(selectedProfile.usedAt * 1000)) : "");
    }

    public void reload() {
        loadByID(selectedProfile.id);
    }
}