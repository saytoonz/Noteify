package com.interstellarstudios.note_ify.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface RecentSearchesDAO {

    @Insert
    void insert(RecentSearches recentSearches);

    @Update
    void update(RecentSearches recentSearches);

    @Delete
    void delete(RecentSearches recentSearches);

    @Query("SELECT * FROM recent_searches_table ORDER BY timeStamp DESC")
    List<RecentSearches> getAll();

    @Query("DELETE FROM recent_searches_table")
    void deleteAll();

    @Query("SELECT * FROM recent_searches_table WHERE recent_searches_table.searchTerm LIKE :term COLLATE NOCASE")
    long getTimeStamp(String term);
}
