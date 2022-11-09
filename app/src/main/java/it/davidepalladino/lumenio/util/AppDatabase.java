package it.davidepalladino.lumenio.util;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileDao;
import it.davidepalladino.lumenio.data.Scene;
import it.davidepalladino.lumenio.data.SceneDao;

@Database(
    version = 1,
    entities = {Profile.class, Scene.class}
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "lumenio")
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ProfileDao profileDao();

    public abstract SceneDao sceneDao();
}
