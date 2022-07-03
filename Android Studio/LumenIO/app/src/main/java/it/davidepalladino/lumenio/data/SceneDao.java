package it.davidepalladino.lumenio.data;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import java.util.List;

@Dao
public interface SceneDao {
    @Insert
    void insert(Scene scene);

    @Update
    void update(Scene scene);

    @Delete
    void delete(Scene scene);

    @Query("SELECT * FROM `scenes`")
    LiveData<List<Scene>> getAll();

    @Query("SELECT * FROM `scenes` WHERE `id` = :id")
    Scene getOneById(long id);
}