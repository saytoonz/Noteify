package com.interstellarstudios.note_ify.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface NoteDAO {

    @Insert
    void insert(NoteEntity noteEntity);

    @Update
    void update(NoteEntity noteEntity);

    @Delete
    void delete(NoteEntity noteEntity);

    @Query("SELECT * FROM note_table")
    List<NoteEntity> getAll();

    @Query("DELETE FROM note_table")
    void deleteAll();

    @Query("SELECT * FROM note_table WHERE note_table.title LIKE '%' || :term || '%' OR note_table.description LIKE '%' || :term || '%' COLLATE NOCASE")
    List<NoteEntity> search(String term);
}
