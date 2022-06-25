package it.davidepalladino.lumenio.data;

import androidx.room.Embedded;
import androidx.room.Relation;

public class ProfileAndScene {
    @Embedded public Profile profile;
    @Relation(
            parentColumn = "id",
            entityColumn = "profileId"
    ) public Scene scene;
}
