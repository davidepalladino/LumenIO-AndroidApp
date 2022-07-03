package it.davidepalladino.lumenio.data;

import androidx.room.*;

import java.util.Objects;

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

    @Ignore
    public Scene(long id) { this.id = id; }

    public Scene(long id, long profileId) {
        this.id = id;
        this.profileId = profileId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Scene scene = (Scene) obj;

        return id == scene.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}