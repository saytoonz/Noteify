package com.interstellarstudios.note_ify.models;

public class UserDetailsModel {

    private String userId;

    public UserDetailsModel() {
        //empty constructor needed
    }

    public UserDetailsModel(String userId) {

        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }
}
