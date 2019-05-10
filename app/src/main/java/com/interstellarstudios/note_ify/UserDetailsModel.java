package com.interstellarstudios.note_ify;

public class UserDetailsModel {

    private String userId;

    public UserDetailsModel() {
        //empty constructor needed
    }

    public UserDetailsModel(String userId) {

        this.userId = userId;
    }

    //These method naming conventions are important for Firebase. Always 'get' and then the name of the field,
    //NEED THE GET METHOD WHEN ADDING A FIELD

    public String getUserId() {
        return userId;
    }
}
