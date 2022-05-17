package it.davidepalladino.lumenio.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(tableName = "profiles")
public class Profile {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;
    public int brightness;
    public int red;
    public int green;
    public int blue;

    public Profile(@NonNull String name, int brightness, int red, int green, int blue) {
        this.name = name;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
