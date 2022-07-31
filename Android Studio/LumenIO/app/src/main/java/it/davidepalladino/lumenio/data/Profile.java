package it.davidepalladino.lumenio.data;

import androidx.annotation.NonNull;
import androidx.room.*;

@Entity(
    tableName = "profiles",
    indices = {
        @Index(
            value = "name",
            unique = true
        )
    }
)
public class Profile {
    @PrimaryKey(autoGenerate = true) public long id;

    @NonNull public String name;
    public int brightness;
    public int red;
    public int green;
    public int blue;

    public long createdAt;

    public long updatedAt;
    public long usedAt;

    public Profile(@NonNull String name, int brightness, int red, int green, int blue) {
        this.name = name;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setValues(@NonNull String name, int brightness, int red, int green, int blue) {
        this.name = name;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    @Override
    public String toString() {
        return name;
    }
}