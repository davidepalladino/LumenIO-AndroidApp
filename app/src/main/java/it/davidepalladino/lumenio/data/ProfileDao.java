package it.davidepalladino.lumenio.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import java.util.List;

@Dao
public interface ProfileDao {
    @Insert
    long insert(Profile profile);

    @Update
    void update(Profile profile);

    @Delete
    void delete(Profile profile);

    @Query("DELETE FROM profiles")
    void deleteAll();

    @Query("SELECT * FROM `profiles` ORDER BY `name` ASC")
    List<Profile> getAll();

    @Query("SELECT * FROM `profiles` ORDER BY `name` ASC")
    LiveData<List<Profile>> getAllLive();

    @Query("SELECT * FROM `profiles` WHERE `name` LIKE :name ORDER BY `name` ASC")
    LiveData<List<Profile>> getAllByName(String name);

    @Query("SELECT * FROM `profiles` WHERE `id` = :id")
    Profile getOneById(long id);

    @Query("SELECT * FROM `profiles` WHERE `id` = :id")
    LiveData<Profile> getOneByIdLive(long id);
}