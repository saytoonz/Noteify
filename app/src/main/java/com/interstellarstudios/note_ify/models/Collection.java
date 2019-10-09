package com.interstellarstudios.note_ify.models;

public class Collection {

    private String folder;
    private String folderDate;

    public Collection() {
        //empty constructor needed
    }

    public Collection(String folder, String folderDate) {

        this.folder = folder;
        this.folderDate = folderDate;
    }

    public String getFolder() {
        return folder;
    }

    public String getFolderDate() {
        return folderDate;
    }
}
