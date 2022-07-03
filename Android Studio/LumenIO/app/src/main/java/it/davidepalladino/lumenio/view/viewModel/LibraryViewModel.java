package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import it.davidepalladino.lumenio.R;
import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class LibraryViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;

    private final LiveData<List<Profile>> profilesAll;
    private Profile profileSelected;

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
        profilesAll = profileRepository.getAll();
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
        return profilesAll;
    }

    public LiveData<List<Profile>> getAllByName(String name) {
        return profileRepository.getAllByName(name);
    }

    public void updateValues() {
        profileSelected.setValues(
                selectedName.getValue(),
                selectedBrightness.getValue(),
                selectedRed.getValue(),
                selectedGreen.getValue(),
                selectedBlue.getValue());
        profileRepository.updateValues(profileSelected);
    }

    public void updateUse() {
        profileRepository.updateUse(profileSelected);
    }

    public void delete() {
        profileRepository.delete(profileSelected);
    }

    public void loadByID(long id) {
        profileSelected = getOneByID(id);

        selectedID.postValue(profileSelected.id);
        selectedName.postValue(profileSelected.name);
        selectedBrightness.postValue(profileSelected.brightness);
        selectedRed.postValue(profileSelected.red);
        selectedGreen.postValue(profileSelected.green);
        selectedBlue.postValue(profileSelected.blue);
        selectedCreatedAt.postValue(profileSelected.createdAt != 0 ? new SimpleDateFormat(getApplication().getString(R.string.datetime_format)).format(new Date(profileSelected.createdAt * 1000)) : "");
        selectedUpdatedAt.postValue(profileSelected.updatedAt != 0 ? new SimpleDateFormat(getApplication().getString(R.string.datetime_format)).format(new Date(profileSelected.updatedAt * 1000)) : "");
        selectedUsedAt.postValue(profileSelected.usedAt != 0 ? new SimpleDateFormat(getApplication().getString(R.string.datetime_format)).format(new Date(profileSelected.usedAt * 1000)) : "");
    }

    public void reload() {
        loadByID(profileSelected.id);
    }
}