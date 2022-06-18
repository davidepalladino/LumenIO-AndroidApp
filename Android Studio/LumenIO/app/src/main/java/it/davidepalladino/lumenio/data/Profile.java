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
    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String name;
    public int brightness;
    public int red;
    public int green;
    public int blue;

    @NonNull
    public long createdAt;

    @NonNull
    public long updatedAt;
    public long usedAt;

    public Profile(@NonNull String name, int brightness, int red, int green, int blue) {
        this.name = name;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public void setEditableValues(@NonNull String name, int brightness, int red, int green, int blue) {
        this.name = name;
        this.brightness = brightness;
        this.red = red;
        this.green = green;
        this.blue = blue;
    }
}
