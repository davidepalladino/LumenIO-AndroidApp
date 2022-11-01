package it.davidepalladino.lumenio.view.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileRepository;

public class ManualViewModel extends AndroidViewModel {
    private final ProfileRepository profileRepository;

    private Profile profileSelected;

    private final MutableLiveData<Long> selectedID = new MutableLiveData<>((long) 0);
    private final MutableLiveData<String> selectedName = new MutableLiveData<>("");
    private final MutableLiveData<String> selectedHex = new MutableLiveData<>("FFFFFF");
    private final MutableLiveData<Integer> selectedRed = new MutableLiveData<>(255);
    private final MutableLiveData<Integer> selectedGreen = new MutableLiveData<>(255);
    private final MutableLiveData<Integer> selectedBlue = new MutableLiveData<>(255);

    public ManualViewModel(Application application) {
        super(application);
        profileRepository = new ProfileRepository(application);
    }

    public LiveData<Long> getSelectedID() { return selectedID; }

    public void setSelectedName(String selectedName) { this.selectedName.setValue(selectedName); }

    public MutableLiveData<String> getSelectedName() { return selectedName; }

    public void setSelectedHex(String selectedHex) { this.selectedHex.setValue(selectedHex); }

    public MutableLiveData<String> getSelectedHex() { return selectedHex; }

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
        profileSelected = new Profile(
                selectedName.getValue(),
                selectedRed.getValue(),
                selectedGreen.getValue(),
                selectedBlue.getValue()
        );
        profileSelected.id = profileRepository.insert(profileSelected);
        selectedID.postValue(profileSelected.id);

        return profileSelected.id;
    }

    public void loadByID(long id) {
        profileSelected = getOneByID(id);

        if (profileSelected == null) {
            profileSelected = new Profile("", 255, 255, 255);
        }

        selectedID.postValue(profileSelected.id);
        selectedName.postValue(profileSelected.name);

        String selectedRedHex = Integer.toHexString(profileSelected.red);
        selectedRedHex = selectedRedHex.length() == 1 ? "0" + selectedRedHex : selectedRedHex;
        String selectedGreenHex = Integer.toHexString(profileSelected.green);
        selectedGreenHex = selectedGreenHex.length() == 1 ? "0" + selectedGreenHex : selectedGreenHex;
        String selectedBlueHex = Integer.toHexString(profileSelected.blue);
        selectedBlueHex = selectedBlueHex.length() == 1 ? "0" + selectedBlueHex : selectedBlueHex;

        selectedHex.postValue(
                selectedRedHex.toUpperCase() +
                selectedGreenHex.toUpperCase() +
                selectedBlueHex.toUpperCase()
        );

        selectedRed.postValue(profileSelected.red);
        selectedGreen.postValue(profileSelected.green);
        selectedBlue.postValue(profileSelected.blue);
    }

    public void reload() {
        loadByID(profileSelected.id);
    }
}