package it.davidepalladino.lumenio.data;

import androidx.room.*;

@Entity(tableName = "profiles")
public class Profile {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public int brightness;
    public int red;
    public int green;
    public int blue;

    public Profile(String name, int brightness, int red, int green, int blue) {
        this.name = name;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
