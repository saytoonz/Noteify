package com.interstellarstudios.note_ify.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {NoteEntity.class, RecentSearches.class}, version = 7, exportSchema = false)

public abstract class NoteDatabase extends RoomDatabase {

    private static  NoteDatabase instance;
    public abstract NoteDAO noteDAO();
    public abstract RecentSearchesDAO recentSearchesDAO();

    public static synchronized NoteDatabase getInstance(Context context) {

        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), NoteDatabase.class, "note_database")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }
}
