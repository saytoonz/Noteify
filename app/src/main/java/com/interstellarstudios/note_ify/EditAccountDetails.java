package com.interstellarstudios.note_ify;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import es.dmoral.toasty.Toasty;

public class EditAccountDetails extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView mImageView;
    private StorageReference mStorageRef;
    private Uri mImageUri;
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_account_details);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        TextView changeProfilePic = findViewById(R.id.change_profile_pic_text);

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveDetails();
                finish();
            }
        });

        mImageView = findViewById(R.id.profile_pic);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        Button buttonDelete = findViewById(R.id.confirm_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(EditAccountDetails.this, DeleteAccount.class);
                startActivity(i);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(EditAccountDetails.this, R.color.colorPrimaryDarkTheme));
            changeProfilePic.setTextColor(ContextCompat.getColor(EditAccountDetails.this, R.color.colorDarkThemeText));
            buttonSave.setTextColor(ContextCompat.getColor(EditAccountDetails.this, R.color.colorDarkThemeText));
            buttonDelete.setTextColor(ContextCompat.getColor(EditAccountDetails.this, R.color.colorDarkThemeText));
        }

        mStorageRef = FirebaseStorage.getInstance().getReference("Users/" + mCurrentUserId + "/Profile_Pic");

        DocumentReference detailsRef = FirebaseFirestore.getInstance()
                .collection("Users").document(mCurrentUserId).collection("User_Details").document("This User");

        detailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Details details = document.toObject(Details.class);
                        String profilePicURL = details.getProfilePic();

                        if (!profilePicURL.equals("Profile Pic Not Uploaded")) {
                            Picasso.get().load(profilePicURL).into(mImageView);
                        }
                    }
                }
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            mImageUri = data.getData();
            Picasso.get().load(mImageUri).into(mImageView);
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void saveDetails() {

        final DocumentReference detailsRef = FirebaseFirestore.getInstance()
                .collection("Users").document(mCurrentUserId).collection("User_Details").document("This User");

        detailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Details details = document.toObject(Details.class);
                        String profilePicURL = details.getProfilePic();

                        detailsRef.set(new Details(profilePicURL));

                    } else {
                        detailsRef.set(new Details("Profile Pic Not Uploaded"));
                    }
                }
            }
        });

        if (mImageUri != null) {
            final StorageReference fileReference = mStorageRef.child("profile_pic." + getFileExtension(mImageUri));

            UploadTask uploadTask = fileReference.putFile(mImageUri);

            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return fileReference.getDownloadUrl();
                }
            })
                    .addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {

                                Uri downloadUri = task.getResult();
                                String downloadURL = downloadUri.toString();
                                detailsRef.set(new Details(downloadURL));
                            }
                        }
                    });
        }
        Toasty.success(EditAccountDetails.this, "Profile Updated", Toast.LENGTH_LONG, true).show();
    }
}
