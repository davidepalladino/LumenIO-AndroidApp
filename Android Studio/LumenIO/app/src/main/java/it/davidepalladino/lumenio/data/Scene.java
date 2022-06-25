package it.davidepalladino.lumenio.data;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "scenes",
        indices = {
            @Index(
                    value = "profileId",
                    unique = true
            )
        }
)
public class Scene {
    @PrimaryKey public long id;
    public long profileId;

    public Scene(long id, long profileId) {
        this.id = id;
        this.profileId = profileId;
    }
}