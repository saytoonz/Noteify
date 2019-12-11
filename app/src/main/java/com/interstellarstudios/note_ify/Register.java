package com.interstellarstudios.note_ify;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.interstellarstudios.note_ify.email.RegistrationEmail;
import com.interstellarstudios.note_ify.models.Collection;
import com.interstellarstudios.note_ify.models.UserDetailsModel;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;
import sendinblue.ApiException;

public class Register extends AppCompatActivity {

    private Context context = this;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //"(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{8,}" +               //at least 8 characters
                    "$");

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private ProgressDialog mProgressDialog;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private String mCurrentUserEmail;
    private FirebaseUser mCurrentUser;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {

            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();

            finish();
            Intent i = new Intent(context, MainActivity.class);
            startActivity(i);
        }

        editTextEmail = findViewById(R.id.edit_text_email);
        editTextPassword = findViewById(R.id.edit_text_password);
        editTextConfirmPassword = findViewById(R.id.edit_text_confirm_password);
        mProgressDialog = new ProgressDialog(context);

        final Button buttonSignUp = findViewById(R.id.button_sign_up);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        final Button buttonGuestMode = findViewById(R.id.button_sign_up_later);
        buttonGuestMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(context)
                        .setTitle("Are you sure you don't want to register?")
                        .setMessage("You won't be able to log in on another device and see all of your notes and documents.")
                        .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                guestMode();
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }
        });

        ImageView googleSignUp = findViewById(R.id.google_sign_in);
        googleSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignIn();
            }
        });

        final TextView textViewSignIn = findViewById(R.id.text_view_go_to_sign_in);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SignIn.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        final TextView textViewSignIn2 = findViewById(R.id.text_view_go_to_sign_in_2);
        textViewSignIn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SignIn.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        ImageView imageViewDarkMode = findViewById(R.id.image_view_dark_mode);
        ImageView imageViewLightMode = findViewById(R.id.image_view_light_mode);

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        final Window window = this.getWindow();
        final View container = findViewById(R.id.container2);

        if (switchThemesOnOff) {

            imageViewLightMode.setVisibility(View.VISIBLE);
            imageViewDarkMode.setVisibility(View.GONE);

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
            editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

        } else {

            imageViewLightMode.setVisibility(View.GONE);
            imageViewDarkMode.setVisibility(View.VISIBLE);

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
            editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));
            textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
        }

        imageViewDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewLightMode.setVisibility(View.VISIBLE);
                imageViewDarkMode.setVisibility(View.GONE);

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
                editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
                textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

                saveDarkModePreference();
            }
        });

        imageViewLightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewLightMode.setVisibility(View.GONE);
                imageViewDarkMode.setVisibility(View.VISIBLE);

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
                editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));
                textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));

                saveLightModePreference();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void saveLightModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("switchThemes", false);
        prefsEditor.apply();
    }

    private void saveDarkModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("switchThemes", true);
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

                            registerToken();

                            Intent i = new Intent(context, MainActivity.class);
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

    private void registerUser() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toasty.info(context, "Please enter a valid email address", Toast.LENGTH_LONG, true).show();
            return;
        } else if (TextUtils.isEmpty(password)) {
            Toasty.info(context, "Please enter a password", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toasty.info(context, "Your password must be at least 8 characters and must contain at least 1 number", Toast.LENGTH_LONG, true).show();
            return;
        } else if (!password.equals(confirmPassword)) {
            Toasty.info(context, "Please enter the same password in the confirm password field", Toast.LENGTH_LONG, true).show();
            return;
        }

        mProgressDialog.setMessage("Registering");
        mProgressDialog.show();

        mFireBaseAuth.createUserWithEmailAndPassword(email, password)
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

                            registerToken();

                            Intent i = new Intent(context, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                            hideKeyboard(Register.this);

                            RegistrationEmail.sendMail(mCurrentUserEmail);

                            Toasty.success(context, "Registration Successful", Toast.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(context, "Registration error, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    private void guestMode() {

        Random rand = new Random();
        int num = rand.nextInt(9000000) + 1000000;
        String randomNumber = Integer.toString(num);

        String guestEmail = "guest" + randomNumber + "@nullparams.com";
        String guestPassword = md5(guestEmail);

        mProgressDialog.setMessage("Launching");
        mProgressDialog.show();

        mFireBaseAuth.createUserWithEmailAndPassword(guestEmail, guestPassword)
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

                            registerToken();

                            Intent i = new Intent(context, MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            Toasty.error(context, "Registration error, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    public static String md5(String s) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")), 0, s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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

    private void registerToken() {

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String deviceToken = instanceIdResult.getToken();

                Map<String, Object> userToken = new HashMap<>();
                userToken.put("User_Token_ID", deviceToken);

                DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Token");
                userTokenDocumentPath.set(userToken);
            }
        });
    }
}
