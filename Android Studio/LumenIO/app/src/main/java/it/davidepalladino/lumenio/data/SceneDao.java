package it.davidepalladino.lumenio.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SceneDao {
//    @Transaction
    @Query("SELECT * FROM `scenes`")
    public List<Scene> getProfilesAndScenes();

    @Update
    public void update(Scene scene);

    @Insert
    public long insert(Scene scene);

    @Query("SELECT * FROM `scenes` WHERE `id` = :id")
    public Scene getOneById(long id);
}