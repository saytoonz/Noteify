package com.interstellarstudios.note_ify.firestore;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.database.NoteEntity;
import com.interstellarstudios.note_ify.database.ProfilePicEntity;
import com.interstellarstudios.note_ify.models.Collection;
import com.interstellarstudios.note_ify.models.Details;
import com.interstellarstudios.note_ify.models.Note;
import com.interstellarstudios.note_ify.repository.Repository;

public class GetData {

    public static void allNotes(FirebaseFirestore mFireBaseFireStore, String mCurrentUserId, Repository repository) {

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            repository.deleteAllNotes();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Note note = document.toObject(Note.class);
                                String noteId = document.getId();
                                String title = note.getTitle();
                                String description = note.getDescription();
                                String date = note.getDate();
                                String fromEmailAddress = note.getFromEmailAddress();
                                int priority = note.getPriority();
                                int revision = note.getRevision();
                                String attachmentUrl = note.getAttachmentUrl();
                                String attachmentName = note.getAttachmentName();
                                String audioDownloadUrl = note.getAudioUrl();
                                String audioZipDownloadUrl = note.getAudioZipUrl();
                                String audioZipFileName = note.getAudioZipName();

                                NoteEntity noteEntity = new NoteEntity(noteId, "Bin", title, description, priority, date, fromEmailAddress, revision, attachmentUrl, attachmentName, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName);
                                repository.insert(noteEntity);
                            }
                        }
                    }
                });

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Public").document("Shared").collection("Shared")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Note note = document.toObject(Note.class);
                                String noteId = document.getId();
                                String title = note.getTitle();
                                String description = note.getDescription();
                                String date = note.getDate();
                                String fromEmailAddress = note.getFromEmailAddress();
                                int priority = note.getPriority();
                                int revision = note.getRevision();
                                String attachmentUrl = note.getAttachmentUrl();
                                String attachmentName = note.getAttachmentName();
                                String audioDownloadUrl = note.getAudioUrl();
                                String audioZipDownloadUrl = note.getAudioZipUrl();
                                String audioZipFileName = note.getAudioZipName();

                                NoteEntity noteEntity = new NoteEntity(noteId, "Shared", title, description, priority, date, fromEmailAddress, revision, attachmentUrl, attachmentName, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName);
                                repository.insert(noteEntity);
                            }
                        }
                    }
                });

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Collection collection = document.toObject(Collection.class);
                                final String folderName = collection.getFolder();

                                mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folderName).collection(folderName)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        Note note = document.toObject(Note.class);
                                                        String noteId = document.getId();
                                                        String title = note.getTitle();
                                                        String description = note.getDescription();
                                                        String date = note.getDate();
                                                        String fromEmailAddress = note.getFromEmailAddress();
                                                        int priority = note.getPriority();
                                                        int revision = note.getRevision();
                                                        String attachmentUrl = note.getAttachmentUrl();
                                                        String attachmentName = note.getAttachmentName();
                                                        String audioDownloadUrl = note.getAudioUrl();
                                                        String audioZipDownloadUrl = note.getAudioZipUrl();
                                                        String audioZipFileName = note.getAudioZipName();

                                                        NoteEntity noteEntity = new NoteEntity(noteId, folderName, title, description, priority, date, fromEmailAddress, revision, attachmentUrl, attachmentName, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName);
                                                        repository.insert(noteEntity);
                                                    }
                                                }
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    public static void profilePic(FirebaseFirestore mFireBaseFireStore, String mCurrentUserId, Repository repository) {

        DocumentReference detailsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("This User");
        detailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Details details = document.toObject(Details.class);
                        String profilePicURL = details.getProfilePic();

                        ProfilePicEntity profilePicEntity = new ProfilePicEntity(profilePicURL);
                        repository.deleteProfilePicUrl();
                        repository.insert(profilePicEntity);
                    }
                }
            }
        });
    }
}
