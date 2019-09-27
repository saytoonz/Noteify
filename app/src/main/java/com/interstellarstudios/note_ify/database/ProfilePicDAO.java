package com.interstellarstudios.note_ify.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ProfilePicDAO {

    @Insert
    void insert(ProfilePicEntity profilePicEntity);

    @Update
    void update(ProfilePicEntity profilePicEntity);

    @Delete
    void delete(ProfilePicEntity profilePicEntity);

    @Query("SELECT * FROM profile_pic_table")
    List<ProfilePicEntity> getAll();

    @Query("DELETE FROM profile_pic_table")
    void deleteAll();
}
