package it.davidepalladino.lumenio.util;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.davidepalladino.lumenio.data.Profile;
import it.davidepalladino.lumenio.data.ProfileDao;

@Database(
    version = 2,
    entities = {Profile.class}
)
public abstract class AppDatabase extends RoomDatabase {
    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `profiles` ADD COLUMN `createdAt` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `profiles` ADD COLUMN `updatedAt` INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE `profiles` ADD COLUMN `usedAt` INTEGER NOT NULL DEFAULT 0");

            database.execSQL("UPDATE `profiles` SET `createdAt` = strftime('%s', 'now')");
            database.execSQL("UPDATE `profiles` SET `updatedAt` = strftime('%s', 'now')");
        }
    };

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room
                        .databaseBuilder(context.getApplicationContext(), AppDatabase.class, "lumenio")
                        .addMigrations(MIGRATION_1_2)
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ProfileDao profileDao();
}
