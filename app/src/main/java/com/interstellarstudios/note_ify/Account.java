package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class Account extends AppCompatActivity {

    private Context context = this;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseUser mUser;
    private String mCurrentUserId;
    private ImageView mProfilePic;
    private FirebaseFirestore mFireBaseFireStore;
    private static final int PICK_IMAGE_REQUEST = 1;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            mUser = mFireBaseAuth.getCurrentUser();
        }

        TextView textViewUserEmail = findViewById(R.id.textViewUserEmail);

        mProfilePic = findViewById(R.id.profile_pic);
        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
        if (acct != null) {
            Uri personPhoto = acct.getPhotoUrl();
            if (personPhoto != null) {
                Picasso.get().load(personPhoto).into(mProfilePic);
            }
        } else {
            String profilePicURL = sharedPreferences.getString("profilePicUrl", null);

            if (profilePicURL != null) {
                Picasso.get().load(profilePicURL).into(mProfilePic);
            }
        }

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        boolean guestAccountOn = sharedPreferences.getBoolean("guestAccount", false);

        if (guestAccountOn) {
            String guestAccount = "Guest Account";
            textViewUserEmail.setText(guestAccount);
        } else {
            textViewUserEmail.setText(mUser.getEmail());
        }

        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logOut();
            }
        });

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            buttonLogout.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewUserEmail.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
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
            Uri imageUri = data.getData();
            Picasso.get().load(imageUri).into(mProfilePic);

            byte[] compressedImage = compressImageUri(imageUri);
            uploadProfilePic(compressedImage);
        }
    }

    private byte[] compressImageUri(Uri imageUri) {

        InputStream imageStream = null;
        try {
            imageStream = getContentResolver().openInputStream(
                    imageUri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bmp = BitmapFactory.decodeStream(imageStream);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 20, stream);
        byte[] byteArray = stream.toByteArray();
        try {
            stream.flush();
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return byteArray;
    }

    private void uploadProfilePic(byte[] compressedImage) {

        final DocumentReference detailsRef = FirebaseFirestore.getInstance().collection("Users").document(mCurrentUserId).collection("User_Details").document("This User");

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("Users/" + mCurrentUserId + "/Profile_Pic");
        final StorageReference fileReference = storageRef.child("profile_pic.jpeg");

        UploadTask uploadTask = fileReference.putBytes(compressedImage);
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

                            SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor prefsEditor = myPrefs.edit();
                            prefsEditor.putString("profilePicUrl", downloadURL);
                            prefsEditor.apply();
                        }
                    }
                });
    }

    private void logOut() {

        new AlertDialog.Builder(context)
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        Map<String, Object> userToken = new HashMap<>();
                        userToken.put("User_Token_ID", "");

                        DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Token");
                        userTokenDocumentPath.set(userToken);

                        GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(context);
                        if (acct != null) {
                            mGoogleSignInClient.signOut();
                        }

                        mFireBaseAuth.signOut();

                        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
                        SharedPreferences.Editor prefsEditor = myPrefs.edit();
                        prefsEditor.putString("profilePicUrl", null);
                        prefsEditor.apply();

                        Intent i = new Intent(context, Register.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i);
                        finish();

                        Toasty.success(context, "You have been signed out.", Toast.LENGTH_LONG, true).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }
}
