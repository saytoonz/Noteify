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
    private String attachmentName;
    private String audioUrl;
    private String audioZipUrl;
    private String audioZipName;

    public Note() {
        //empty constructor needed
    }

    public Note(String title, String lowercasetitle, String description, int priority, String date, String fromEmailAddress, int revision, String attachmentUrl, String attachmentName, String audioUrl, String audioZipUrl, String audioZipName) {

        this.title = title;
        this.lowercasetitle = lowercasetitle;
        this.description = description;
        this.priority = priority;
        this.date = date;
        this.fromEmailAddress = fromEmailAddress;
        this.revision = revision;
        this.attachmentUrl = attachmentUrl;
        this.attachmentName = attachmentName;
        this.audioUrl = audioUrl;
        this.audioZipUrl = audioZipUrl;
        this.audioZipName = audioZipName;
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

    public String getAttachmentName() {
        return attachmentName;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public String getAudioZipUrl() {
        return audioZipUrl;
    }

    public String getAudioZipName() {
        return audioZipName;
    }
}
