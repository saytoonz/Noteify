package com.interstellarstudios.note_ify;

public class Details {

    private String firstName;
    private String lastName;
    private String profilePic;

    public Details() {
        //empty constructor needed
    }

    public Details(String firstName, String lastName, String profilePic) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.profilePic = profilePic;
    }

    //These method naming conventions are important for Firebase. Always 'get' and then the name of the field,
    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getProfilePic() {
        return profilePic;
    }
}
