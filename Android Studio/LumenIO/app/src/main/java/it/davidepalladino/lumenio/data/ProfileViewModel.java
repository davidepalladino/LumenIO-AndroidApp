package it.davidepalladino.lumenio.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfileViewModel extends ViewModel {
    private final MutableLiveData<String> name = new MutableLiveData<String>("");
    private final MutableLiveData<Integer> brightness = new MutableLiveData<Integer>(255);
    private final MutableLiveData<Integer> red = new MutableLiveData<Integer>(255);
    private final MutableLiveData<Integer> green = new MutableLiveData<Integer>(97);
    private final MutableLiveData<Integer> blue = new MutableLiveData<Integer>(92);

    public void setBrightness(int brightness) { this.brightness.setValue(brightness); }

    public LiveData<Integer> getBrightness() { return this.brightness; }

    public void setRed(int red) { this.red.setValue(red); }

    public LiveData<Integer> getRed() { return this.red; }

    public void setGreen(int green) { this.green.setValue(green); }

    public LiveData<Integer> getGreen() { return this.green; }

    public void setBlue(int blue) { this.blue.setValue(blue); }

    public LiveData<Integer> getBlue() { return this.blue; }
}
