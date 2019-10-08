package com.interstellarstudios.note_ify.repository;

import android.app.Application;
import com.interstellarstudios.note_ify.database.NoteDAO;
import com.interstellarstudios.note_ify.database.NoteDatabase;
import com.interstellarstudios.note_ify.database.NoteEntity;
import com.interstellarstudios.note_ify.database.ProfilePicDAO;
import com.interstellarstudios.note_ify.database.ProfilePicEntity;
import com.interstellarstudios.note_ify.database.RecentSearches;
import com.interstellarstudios.note_ify.database.RecentSearchesDAO;
import java.util.List;

public class Repository {

    private NoteDAO noteDAO;
    private ProfilePicDAO profilePicDAO;
    private RecentSearchesDAO recentSearchesDAO;

    public Repository(Application application) {

        NoteDatabase noteDatabase = NoteDatabase.getInstance(application);
        noteDAO = noteDatabase.noteDAO();
        profilePicDAO = noteDatabase.profilePicDAO();
        recentSearchesDAO = noteDatabase.recentSearchesDAO();
    }

    public void insert(NoteEntity noteEntity) {
        noteDAO.insert(noteEntity);
    }

    public void update(NoteEntity noteEntity) {
        noteDAO.update(noteEntity);
    }

    public void delete(NoteEntity noteEntity) {
        noteDAO.delete(noteEntity);
    }

    public void deleteAllNotes() {
        noteDAO.deleteAll();
    }

    public List<NoteEntity> getAllNotes() {
        return noteDAO.getAll();
    }

    public List<NoteEntity> searchNotes(String term) {
        return noteDAO.search(term);
    }

    public void insert(ProfilePicEntity profilePicEntity) {
        profilePicDAO.insert(profilePicEntity);
    }

    public void update(ProfilePicEntity profilePicEntity) {
        profilePicDAO.update(profilePicEntity);
    }

    public void delete(ProfilePicEntity profilePicEntity) {
        profilePicDAO.delete(profilePicEntity);
    }

    public void deleteProfilePicUrl() {
        profilePicDAO.deleteAll();
    }

    public List<ProfilePicEntity> getProfilePicUrl() {
        return profilePicDAO.getAll();
    }

    public void insert(RecentSearches recentSearches) {
        recentSearchesDAO.insert(recentSearches);
    }

    public void update(RecentSearches recentSearches) {
        recentSearchesDAO.update(recentSearches);
    }

    public void delete(RecentSearches recentSearches) {
        recentSearchesDAO.delete(recentSearches);
    }

    public void deleteAllRecentSearches() {
        recentSearchesDAO.deleteAll();
    }

    public List<RecentSearches> getRecentSearches() {
        return recentSearchesDAO.getAll();
    }

    public long getTimeStamp(String term) {
        return recentSearchesDAO.getTimeStamp(term);
    }
}
