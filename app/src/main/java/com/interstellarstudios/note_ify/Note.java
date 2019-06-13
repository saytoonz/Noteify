package com.interstellarstudios.note_ify;

public class Note {

    private String title;
    private String description;
    private int priority;
    private String date;
    private String lowercasetitle;
    private String fromEmailAddress;
    private int revision;
    private String attachmentUrl;

    public Note() {
        //empty constructor needed
    }

    public Note(String title, String lowercasetitle, String description, int priority, String date, String fromEmailAddress, int revision, String attachmentUrl) {

        this.title = title;
        this.lowercasetitle = lowercasetitle;
        this.description = description;
        this.priority = priority;
        this.date = date;
        this.fromEmailAddress = fromEmailAddress;
        this.revision = revision;
        this.attachmentUrl = attachmentUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getLowerCaseTitle() {
        return lowercasetitle;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }

    public String getDate() {
        return date;
    }

    public String getFromEmailAddress() {
        return fromEmailAddress;
    }

    public int getRevision() {
        return revision;
    }

    public String getAttachmentUrl() {
        return attachmentUrl;
    }
}
