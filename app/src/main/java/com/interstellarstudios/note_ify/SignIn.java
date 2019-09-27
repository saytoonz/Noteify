package com.interstellarstudios.note_ify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.interstellarstudios.note_ify.database.NoteEntity;
import com.interstellarstudios.note_ify.database.ProfilePicEntity;
import com.interstellarstudios.note_ify.repository.Repository;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import es.dmoral.toasty.Toasty;
import sendinblue.ApiException;

public class SignIn extends AppCompatActivity {

    private Context context = this;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseFirestore mFireBaseFireStore;
    private ProgressDialog mProgressDialog;
    private Switch switchThemes;
    private String mCurrentUserId;
    private String mCurrentUserEmail;
    private FirebaseUser mCurrentUser;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private Repository repository;
    private SharedPreferences sharedPreferences;
    private String androidUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        androidUUID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        repository = new Repository(getApplication());

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.confirmPassword);
        mProgressDialog = new ProgressDialog(context);

        final Button buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        final ImageView googleSignUp = findViewById(R.id.google_sign_in);
        googleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        final TextView textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextView textViewRegister2 = findViewById(R.id.textViewRegister2);
        textViewRegister2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        final TextView textViewForgot = findViewById(R.id.textViewForgot);
        textViewForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ForgotPassword.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        final TextView textViewForgot2 = findViewById(R.id.textViewForgot2);
        textViewForgot2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, ForgotPassword.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        switchThemes = findViewById(R.id.switchThemes);

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        switchThemes.setChecked(switchThemesOnOff);

        final Window window = this.getWindow();
        final View container = findViewById(R.id.container2);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewRegister.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewRegister2.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewForgot.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewForgot2.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            switchThemes.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        switchThemes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    if (container != null) {
                        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    }
                    editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    DrawableCompat.setTint(editTextPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    textViewRegister.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    textViewRegister2.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    textViewForgot.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    textViewForgot2.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    buttonSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    switchThemes.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

                    savePreferences();

                } else {

                    window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    if (container != null) {
                        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    }
                    editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));
                    editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    DrawableCompat.setTint(editTextPassword.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));
                    textViewRegister.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    textViewRegister2.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    textViewForgot.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    textViewForgot2.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    buttonSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    switchThemes.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));

                    savePreferences();
                }
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void savePreferences() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();

        if (switchThemes.isChecked()) {
            prefsEditor.putBoolean("switchThemes", true);
        } else {
            prefsEditor.putBoolean("switchThemes", false);
        }
        prefsEditor.apply();
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN && resultCode == RESULT_OK) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account != null) {
                    fireBaseAuthWithGoogle(account);
                }
            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }

    private void fireBaseAuthWithGoogle(GoogleSignInAccount acct) {

        mProgressDialog.setMessage("Signing in with Google");
        mProgressDialog.show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
                            String date = sdf.format(calendar.getTime());

                            if (mFireBaseAuth.getCurrentUser() != null) {
                                mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
                                mCurrentUser = mFireBaseAuth.getCurrentUser();
                                mCurrentUserEmail = mCurrentUser.getEmail();
                            }

                            DocumentReference NotebookDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Notebook");
                            DocumentReference SharedDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Public").document("Shared");
                            DocumentReference DraftsDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Drafts");
                            NotebookDocumentPath.set(new Collection("Notebook", date));
                            SharedDocumentPath.set(new Collection("Shared", ""));
                            DraftsDocumentPath.set(new Collection("Drafts", date));

                            DocumentReference userMapPath = mFireBaseFireStore.collection("User_List").document(mCurrentUserEmail);
                            userMapPath.set(new UserDetailsModel(mCurrentUserId));

                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String deviceToken = instanceIdResult.getToken();

                                    Map<String, Object> userToken = new HashMap<>();
                                    userToken.put("User_Token_ID", deviceToken);

                                    DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Tokens").collection("User_Tokens").document(androidUUID);
                                    userTokenDocumentPath.set(userToken);
                                }
                            });

                            setUpSearch();
                            loadProfilePic();
                            saveNonGuestPreferences();

                            Intent i = new Intent(context, Home.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                            Toasty.success(context, "Google Sign In Successful", Toast.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(context, "Google Sign In Failed", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void userLogin() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        }
        else if (TextUtils.isEmpty(password)) {
            Toasty.info(context, "Please enter your password", Toast.LENGTH_LONG, true).show();
            return;
        }

        mProgressDialog.setMessage("Signing in");
        mProgressDialog.show();

        mFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (mFireBaseAuth.getCurrentUser() != null) {
                                mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
                            }

                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String deviceToken = instanceIdResult.getToken();

                                    Map<String, Object> userToken = new HashMap<>();
                                    userToken.put("User_Token_ID", deviceToken);

                                    DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Tokens").collection("User_Tokens").document(androidUUID);
                                    userTokenDocumentPath.set(userToken);
                                }
                            });

                            saveNonGuestPreferences();
                            setUpSearch();
                            loadProfilePic();

                            Intent i = new Intent(context, Home.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                            hideKeyboard(SignIn.this);

                            Toasty.success(context, "Sign In Successful", Toast.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(context, "Sign In Error, please try again. Please ensure that your email address and password are correct.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void saveNonGuestPreferences() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("guestAccount", false);
        prefsEditor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(context, Register.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    private void setUpSearch() {

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

    private void loadProfilePic() {

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

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
