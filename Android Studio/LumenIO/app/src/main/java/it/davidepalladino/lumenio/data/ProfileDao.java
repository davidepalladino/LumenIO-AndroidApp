package it.davidepalladino.lumenio.data;

import androidx.room.*;
import it.davidepalladino.lumenio.data.Profile;

@Dao
public interface ProfileDao {
    @Insert
    void insert(Profile profile);
}
