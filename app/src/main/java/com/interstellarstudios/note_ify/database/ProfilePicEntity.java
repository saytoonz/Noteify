package com.interstellarstudios.note_ify.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "profile_pic_table")
public class ProfilePicEntity {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String profilePicUrl;

    public ProfilePicEntity(String profilePicUrl) {

        this.profilePicUrl = profilePicUrl;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }
}
