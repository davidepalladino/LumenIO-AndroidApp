package it.davidepalladino.lumenio.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface ProfileDao {
    @Insert
    long insert(Profile profile);

    @Delete
    void delete(Profile profile);

    @Update
    int update(Profile profile);

    @Query("SELECT * FROM profiles ORDER BY name ASC")
    LiveData<List<Profile>> getAll();

    @Query("SELECT * FROM profiles WHERE id = :id")
    Profile getById(long id);
}