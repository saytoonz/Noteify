package com.interstellarstudios.note_ify;

public class Collection {

    private String folder;
    private String lowerCaseFolder;
    private String folderDate;

    public Collection() {
        //empty constructor needed
    }

    public Collection(String folder, String lowerCaseFolder, String folderDate) {

        this.folder = folder;
        this.folderDate = folderDate;
        this.lowerCaseFolder = lowerCaseFolder;
    }

    //These method naming conventions are important for Firebase. Always 'get' and then the name of the field,
    //NEED THE GET METHOD WHEN ADDING A FIELD
    public String getFolder() {
        return folder;
    }

    public String getLowerCaseFolder() {
        return lowerCaseFolder;
    }

    public String getFolderDate() {
        return folderDate;
    }
}
