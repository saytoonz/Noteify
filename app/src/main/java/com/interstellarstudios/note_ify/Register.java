package com.interstellarstudios.note_ify;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Pattern;
import es.dmoral.toasty.Toasty;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.SmtpApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

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

    private Switch switchThemes;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            finish();
            Intent i = new Intent(context, Home.class);
            startActivity(i);
        }

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirmPassword);
        mProgressDialog = new ProgressDialog(context);

        final Button buttonSignUp = findViewById(R.id.buttonSignup);
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        final Button buttonGuestMode = findViewById(R.id.buttonGuestMode);
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

        final TextView textViewSignIn = findViewById(R.id.textViewSignin);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SignIn.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        final TextView textViewSignIn2 = findViewById(R.id.textViewSignin2);
        textViewSignIn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, SignIn.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        switchThemes = findViewById(R.id.switchThemes);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
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
            editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textViewSignIn2.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonSignUp.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            switchThemes.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonGuestMode.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            buttonGuestMode.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));

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
                    editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    textViewSignIn2.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    buttonSignUp.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    switchThemes.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    buttonGuestMode.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    buttonGuestMode.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));

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
                    editTextConfirmPassword.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    editTextConfirmPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    DrawableCompat.setTint(editTextConfirmPassword.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));
                    textViewSignIn.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    textViewSignIn2.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    buttonSignUp.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    switchThemes.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    buttonGuestMode.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    buttonGuestMode.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

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

        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();

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

                                    DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Token");
                                    userTokenDocumentPath.set(userToken);
                                }
                            });

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

    private void registerUser() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toasty.info(context, "Please enter a valid email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        else if (TextUtils.isEmpty(password)) {
            Toasty.info(context, "Please enter a password", Toast.LENGTH_LONG, true).show();
            return;
        }

        else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            Toasty.info(context, "Your password must be at least 8 characters and must contain at least 1 number", Toast.LENGTH_LONG, true).show();
            return;
        }

        else if (!password.equals(confirmPassword)) {
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

                            saveNonGuestPreferences();

                            Intent i = new Intent(context, Home.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();

                            sendMail();

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

        String guestEmail = "guest" + randomNumber + "@interstellarstudios.co.uk";
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

                            saveGuestPreferences();

                            Intent i = new Intent(context, Home.class);
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

    public static String md5(String s)
    {
        MessageDigest digest;
        try
        {
            digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes(Charset.forName("US-ASCII")),0,s.length());
            byte[] magnitude = digest.digest();
            BigInteger bi = new BigInteger(1, magnitude);
            String hash = String.format("%0" + (magnitude.length << 1) + "x", bi);
            return hash;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        return "";
    }

    private void saveGuestPreferences() {

        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("guestAccount", true);
        prefsEditor.apply();
    }

    private void saveNonGuestPreferences() {

        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("guestAccount", false);
        prefsEditor.apply();
    }

    private void sendMail() {

        ApiClient defaultClient = Configuration.getDefaultApiClient();

        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey("YOUR API KEY HERE");

        final SmtpApi apiInstance = new SmtpApi();

        List<SendSmtpEmailTo> emailArrayList = new ArrayList<>();
        emailArrayList.add(new SendSmtpEmailTo().email(mCurrentUserEmail));

        final SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
        sendSmtpEmail.sender(new SendSmtpEmailSender().email("note-ify@interstellarstudios.co.uk").name("Note-ify"));
        sendSmtpEmail.to(emailArrayList);
        sendSmtpEmail.subject("You've successfully registered for Note-ify");
        sendSmtpEmail.htmlContent("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" /><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta name=\"x-apple-disable-message-reformatting\" /><meta name=\"apple-mobile-web-app-capable\" content=\"yes\" /><meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" /><meta name=\"format-detection\" content=\"telephone=no\" /><title></title><style type=\"text/css\">\n" +
                "        /* Resets */\n" +
                "        .ReadMsgBody { width: 100%; background-color: #ebebeb;}\n" +
                "        .ExternalClass {width: 100%; background-color: #ebebeb;}\n" +
                "        .ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div {line-height:100%;}\n" +
                "        a[x-apple-data-detectors]{\n" +
                "            color:inherit !important;\n" +
                "            text-decoration:none !important;\n" +
                "            font-size:inherit !important;\n" +
                "            font-family:inherit !important;\n" +
                "            font-weight:inherit !important;\n" +
                "            line-height:inherit !important;\n" +
                "        }        \n" +
                "        body {-webkit-text-size-adjust:none; -ms-text-size-adjust:none;}\n" +
                "        body {margin:0; padding:0;}\n" +
                "        .yshortcuts a {border-bottom: none !important;}\n" +
                "        .rnb-del-min-width{ min-width: 0 !important; }\n" +
                "\n" +
                "        /* Add new outlook css start */\n" +
                "        .templateContainer{\n" +
                "            max-width:590px !important;\n" +
                "            width:auto !important;\n" +
                "        }\n" +
                "        /* Add new outlook css end */\n" +
                "\n" +
                "        /* Image width by default for 3 columns */\n" +
                "        img[class=\"rnb-col-3-img\"] {\n" +
                "        max-width:170px;\n" +
                "        }\n" +
                "\n" +
                "        /* Image width by default for 2 columns */\n" +
                "        img[class=\"rnb-col-2-img\"] {\n" +
                "        max-width:264px;\n" +
                "        }\n" +
                "\n" +
                "        /* Image width by default for 2 columns aside small size */\n" +
                "        img[class=\"rnb-col-2-img-side-xs\"] {\n" +
                "        max-width:180px;\n" +
                "        }\n" +
                "\n" +
                "        /* Image width by default for 2 columns aside big size */\n" +
                "        img[class=\"rnb-col-2-img-side-xl\"] {\n" +
                "        max-width:350px;\n" +
                "        }\n" +
                "\n" +
                "        /* Image width by default for 1 column */\n" +
                "        img[class=\"rnb-col-1-img\"] {\n" +
                "        max-width:550px;\n" +
                "        }\n" +
                "\n" +
                "        /* Image width by default for header */\n" +
                "        img[class=\"rnb-header-img\"] {\n" +
                "        max-width:590px;\n" +
                "        }\n" +
                "\n" +
                "        /* Ckeditor line-height spacing */\n" +
                "        .rnb-force-col p, ul, ol{margin:0px!important;}\n" +
                "        .rnb-del-min-width p, ul, ol{margin:0px!important;}\n" +
                "\n" +
                "        /* tmpl-2 preview */\n" +
                "        .rnb-tmpl-width{ width:100%!important;}\n" +
                "\n" +
                "        /* tmpl-11 preview */\n" +
                "        .rnb-social-width{padding-right:15px!important;}\n" +
                "\n" +
                "        /* tmpl-11 preview */\n" +
                "        .rnb-social-align{float:right!important;}\n" +
                "\n" +
                "        /* Ul Li outlook extra spacing fix */\n" +
                "        li{mso-margin-top-alt: 0; mso-margin-bottom-alt: 0;}        \n" +
                "\n" +
                "        /* Outlook fix */\n" +
                "        table {mso-table-lspace:0pt; mso-table-rspace:0pt;}\n" +
                "    \n" +
                "        /* Outlook fix */\n" +
                "        table, tr, td {border-collapse: collapse;}\n" +
                "\n" +
                "        /* Outlook fix */\n" +
                "        p,a,li,blockquote {mso-line-height-rule:exactly;} \n" +
                "\n" +
                "        /* Outlook fix */\n" +
                "        .msib-right-img { mso-padding-alt: 0 !important;}\n" +
                "\n" +
                "        @media only screen and (min-width:590px){\n" +
                "        /* mac fix width */\n" +
                "        .templateContainer{width:590px !important;}\n" +
                "        }\n" +
                "\n" +
                "        @media screen and (max-width: 360px){\n" +
                "        /* yahoo app fix width \"tmpl-2 tmpl-10 tmpl-13\" in android devices */\n" +
                "        .rnb-yahoo-width{ width:360px !important;}\n" +
                "        }\n" +
                "\n" +
                "        @media screen and (max-width: 380px){\n" +
                "        /* fix width and font size \"tmpl-4 tmpl-6\" in mobile preview */\n" +
                "        .element-img-text{ font-size:24px !important;}\n" +
                "        .element-img-text2{ width:230px !important;}\n" +
                "        .content-img-text-tmpl-6{ font-size:24px !important;}\n" +
                "        .content-img-text2-tmpl-6{ width:220px !important;}\n" +
                "        }\n" +
                "\n" +
                "        @media screen and (max-width: 480px) {\n" +
                "        td[class=\"rnb-container-padding\"] {\n" +
                "        padding-left: 10px !important;\n" +
                "        padding-right: 10px !important;\n" +
                "        }\n" +
                "\n" +
                "        /* force container nav to (horizontal) blocks */\n" +
                "        td.rnb-force-nav {\n" +
                "        display: inherit;\n" +
                "        }\n" +
                "        }\n" +
                "\n" +
                "        @media only screen and (max-width: 600px) {\n" +
                "\n" +
                "        /* center the address &amp; social icons */\n" +
                "        .rnb-text-center {text-align:center !important;}\n" +
                "\n" +
                "        /* force container columns to (horizontal) blocks */\n" +
                "        td.rnb-force-col {\n" +
                "        display: block;\n" +
                "        padding-right: 0 !important;\n" +
                "        padding-left: 0 !important;\n" +
                "        width:100%;\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-container {\n" +
                "         width: 100% !important;\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-btn-col-content {\n" +
                "        width: 100% !important;\n" +
                "        }\n" +
                "        table.rnb-col-3 {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "\n" +
                "        /* change left/right padding and margins to top/bottom ones */\n" +
                "        margin-bottom: 10px;\n" +
                "        padding-bottom: 10px;\n" +
                "        /*border-bottom: 1px solid #eee;*/\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-last-col-3 {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "        }\n" +
                "\n" +
                "        table[class~=\"rnb-col-2\"] {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "\n" +
                "        /* change left/right padding and margins to top/bottom ones */\n" +
                "        margin-bottom: 10px;\n" +
                "        padding-bottom: 10px;\n" +
                "        /*border-bottom: 1px solid #eee;*/\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-col-2-noborder-onright {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "\n" +
                "        /* change left/right padding and margins to top/bottom ones */\n" +
                "        margin-bottom: 10px;\n" +
                "        padding-bottom: 10px;\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-col-2-noborder-onleft {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "\n" +
                "        /* change left/right padding and margins to top/bottom ones */\n" +
                "        margin-top: 10px;\n" +
                "        padding-top: 10px;\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-last-col-2 {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "        }\n" +
                "\n" +
                "        table.rnb-col-1 {\n" +
                "        /* unset table align=\"left/right\" */\n" +
                "        float: none !important;\n" +
                "        width: 100% !important;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-col-3-img {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-col-2-img {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-col-2-img-side-xs {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-col-2-img-side-xl {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-col-1-img {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-header-img {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        margin:0 auto;\n" +
                "        }\n" +
                "\n" +
                "        img.rnb-logo-img {\n" +
                "        /**max-width:none !important;**/\n" +
                "        width:100% !important;\n" +
                "        }\n" +
                "\n" +
                "        td.rnb-mbl-float-none {\n" +
                "        float:inherit !important;\n" +
                "        }\n" +
                "\n" +
                "        .img-block-center{text-align:center !important;}\n" +
                "\n" +
                "        .logo-img-center\n" +
                "        {\n" +
                "            float:inherit !important;\n" +
                "        }\n" +
                "\n" +
                "        /* tmpl-11 preview */\n" +
                "        .rnb-social-align{margin:0 auto !important; float:inherit !important;}\n" +
                "\n" +
                "        /* tmpl-11 preview */\n" +
                "        .rnb-social-center{display:inline-block;}\n" +
                "\n" +
                "        /* tmpl-11 preview */\n" +
                "        .social-text-spacing{margin-bottom:0px !important; padding-bottom:0px !important;}\n" +
                "\n" +
                "        /* tmpl-11 preview */\n" +
                "        .social-text-spacing2{padding-top:15px !important;}\n" +
                "\n" +
                "    }</style><!--[if gte mso 11]><style type=\"text/css\">table{border-spacing: 0; }table td {border-collapse: separate;}</style><![endif]--><!--[if !mso]><!--><style type=\"text/css\">table{border-spacing: 0;} table td {border-collapse: collapse;}</style> <!--<![endif]--><!--[if gte mso 15]><xml><o:OfficeDocumentSettings><o:AllowPNG/><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]--><!--[if gte mso 9]><xml><o:OfficeDocumentSettings><o:AllowPNG/><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]--></head><body>\n" +
                "\n" +
                "<table border=\"0\" align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" class=\"main-template\" bgcolor=\"#f9fafc\" style=\"background-color: rgb(249, 250, 252);\">\n" +
                "\n" +
                "    <tbody><tr style=\"display:none !important; font-size:1px; mso-hide: all;\"><td></td><td></td></tr><tr>\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "        <!--[if gte mso 9]>\n" +
                "                        <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"590\" style=\"width:590px;\">\n" +
                "                        <tr>\n" +
                "                        <td align=\"center\" valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                        <![endif]-->\n" +
                "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"templateContainer\" style=\"max-width:590px!important; width: 590px;\">\n" +
                "        <tbody><tr>\n" +
                "\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "\n" +
                "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                "                <tr>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                <![endif]-->\n" +
                "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:590px;\" name=\"Layout_8\" id=\"Layout_8\">\n" +
                "                <tbody><tr>\n" +
                "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\" style=\"min-width:590px;\">\n" +
                "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"background-color: rgb(255, 255, 255); border-radius: 0px; padding-left: 20px; padding-right: 20px; border-collapse: separate;\">\n" +
                "                            <tbody><tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                "                                    <table width=\"100%\" cellpadding=\"0\" border=\"0\" align=\"center\" cellspacing=\"0\">\n" +
                "                                        <tbody><tr>\n" +
                "                                            <td valign=\"top\" align=\"center\">\n" +
                "                                                <table cellpadding=\"0\" border=\"0\" align=\"center\" cellspacing=\"0\" class=\"logo-img-center\"> \n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td valign=\"middle\" align=\"center\" style=\"line-height: 1px;\">\n" +
                "                                                            <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block; \" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><div><img width=\"550\" vspace=\"0\" hspace=\"0\" border=\"0\" alt=\"Note-ify\" style=\"float: left;max-width:550px;display:block;\" class=\"rnb-logo-img\" src=\"https://firebasestorage.googleapis.com/v0/b/note-ify-d3325.appspot.com/o/Email%20Images%2Femail_header.jpg?alt=media&token=bd0debdd-ae94-416b-9368-9540ac271aa1\"></div></div></td>\n" +
                "                                                    </tr>\n" +
                "                                                </tbody></table>\n" +
                "                                                </td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody></table></td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                        </tbody></table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody></table>\n" +
                "            <!--[if mso]>\n" +
                "                </td>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                </tr>\n" +
                "                </table>\n" +
                "                <![endif]-->\n" +
                "            \n" +
                "        </div></td>\n" +
                "    </tr><tr>\n" +
                "\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "\n" +
                "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                "            \n" +
                "                <!--[if mso]>\n" +
                "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                "                <tr>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                <![endif]-->\n" +
                "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_7\">\n" +
                "                <tbody><tr>\n" +
                "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"background-color: rgb(255, 255, 255); padding-left: 20px; padding-right: 20px; border-collapse: separate; border-radius: 0px; border-bottom: 0px none rgb(200, 200, 200);\">\n" +
                "\n" +
                "                                        <tbody><tr>\n" +
                "                                            <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                        <tr>\n" +
                "                                            <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                "\n" +
                "                                                <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td class=\"rnb-force-col\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                "\n" +
                "                                                            <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" align=\"left\" class=\"rnb-col-1\">\n" +
                "\n" +
                "                                                                <tbody><tr>\n" +
                "                                                                    <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\"><div>\n" +
                "<div><strong>You've Successfully Registered!</strong><br>\n" +
                "<br>\n" +
                "You can now use your email address and password to log in to the App from any of your devices. Open the App to start creating and sharing beautiful notes.<br>\n" +
                "<br>\n" +
                "Please contact us should you have any questions. We hope you enjoy your experience.</div>\n" +
                "</div>\n" +
                "</td>\n" +
                "                                                                </tr>\n" +
                "                                                                </tbody></table>\n" +
                "\n" +
                "                                                            </td></tr>\n" +
                "                                                </tbody></table></td>\n" +
                "                                        </tr>\n" +
                "                                        <tr>\n" +
                "                                            <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                        </tr>\n" +
                "                                    </tbody></table>\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody></table><!--[if mso]>\n" +
                "                </td>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                </tr>\n" +
                "                </table>\n" +
                "                <![endif]-->\n" +
                "\n" +
                "            </div></td>\n" +
                "    </tr><tr>\n" +
                "\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "\n" +
                "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                "                <tr>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                <![endif]-->\n" +
                "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_6\" id=\"Layout_6\">\n" +
                "                <tbody><tr>\n" +
                "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding-left: 20px; padding-right: 20px;\">\n" +
                "                            <tbody><tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                "\n" +
                "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                "                                        <tbody><tr>\n" +
                "                                            <td class=\"rnb-force-col\" width=\"550\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-1\" width=\"550\">\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"center\">\n" +
                "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                                <tbody>\n" +
                "                                                                    <tr>\n" +
                "                                                                        <td width=\"100%\" valign=\"top\" align=\"center\" class=\"img-block-center\">\n" +
                "\n" +
                "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                                            <tbody><tr>\n" +
                "                                                                                <td>\n" +
                "                                                                        <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\">\n" +
                "                                                                            <div><a target=\"_blank\" href=\"https://play.google.com/store/apps/details?id=com.interstellarstudios.note_ify\">\n" +
                "                                                                            <img ng-if=\"col.img.source != 'url'\" width=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-1-img\" src=\"https://firebasestorage.googleapis.com/v0/b/note-ify-d3325.appspot.com/o/Email%20Images%2Fgoogle_play_small.jpg?alt=media&token=c7147e53-bbba-4ab7-804e-1c138fba5ac8\" style=\"vertical-align: top; max-width: 200px; float: left;\"></a></div><div style=\"clear:both;\"></div>\n" +
                "                                                                            </div></td>\n" +
                "                                                                            </tr>\n" +
                "                                                                        </tbody></table>\n" +
                "\n" +
                "                                                                    </td>\n" +
                "                                                                    </tr>\n" +
                "                                                                </tbody>\n" +
                "                                                                </table></td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                "                                                            <div><div style=\"text-align: center;\">Download the free App now.</div>\n" +
                "</div>\n" +
                "                                                        </td>\n" +
                "                                                    </tr>\n" +
                "                                                    </tbody></table>\n" +
                "\n" +
                "                                                </td></tr>\n" +
                "                                    </tbody></table></td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                        </tbody></table>\n" +
                "\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody></table><!--[if mso]>\n" +
                "                </td>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                </tr>\n" +
                "                </table>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "            </div></td>\n" +
                "    </tr><tr>\n" +
                "\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "\n" +
                "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                "                <tr>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                <![endif]-->\n" +
                "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_5\" id=\"Layout_5\">\n" +
                "                <tbody><tr>\n" +
                "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding-left: 20px; padding-right: 20px;\">\n" +
                "                            <tbody><tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                "\n" +
                "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                "                                        <tbody><tr>\n" +
                "                                            <td class=\"rnb-force-col\" width=\"263\" valign=\"top\" style=\"padding-right: 20px;\">\n" +
                "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-2\" width=\"263\">\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td width=\"100%\" valign=\"top\" align=\"left\" class=\"img-block-center\">\n" +
                "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                                            <tbody><tr>\n" +
                "                                                                                <td>\n" +
                "                                                                                    <div style=\"border-top:1px Solid #9c9c9c;border-right:1px Solid #9c9c9c;border-bottom:1px Solid #9c9c9c;border-left:1px Solid #9c9c9c;display:inline-block;\"><div><img border=\"0\" width=\"263\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-2-img\" src=\"https://firebasestorage.googleapis.com/v0/b/note-ify-d3325.appspot.com/o/Email%20Images%2Ffirebase_comp.jpg?alt=media&token=8e8f24f7-8998-4881-a215-280f668ef245\" style=\"vertical-align: top; max-width: 300px; float: left;\"></div><div style=\"clear:both;\"></div>\n" +
                "                                                                                    </div>\n" +
                "                                                                            </td>\n" +
                "                                                                            </tr>\n" +
                "                                                                        </tbody></table>\n" +
                "\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table></td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                "                                                            <div><div>All of your notes automatically synced to the Cloud. Stored securely with Google Firebase.</div>\n" +
                "</div>\n" +
                "                                                        </td>\n" +
                "                                                    </tr>\n" +
                "                                                    </tbody></table>\n" +
                "\n" +
                "                                                </td><td class=\"rnb-force-col\" width=\"263\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-last-col-2\" width=\"263\">\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                            <tbody>\n" +
                "                                                                <tr>\n" +
                "                                                                    <td width=\"100%\" valign=\"top\" align=\"left\" class=\"img-block-center\">\n" +
                "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                                            <tbody><tr>\n" +
                "                                                                                <td>\n" +
                "                                                                                    <div style=\"border-top:1px Solid #9c9c9c;border-right:1px Solid #9c9c9c;border-bottom:1px Solid #9c9c9c;border-left:1px Solid #9c9c9c;display:inline-block;\"><div><img border=\"0\" width=\"263\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-2-img\" src=\"https://firebasestorage.googleapis.com/v0/b/note-ify-d3325.appspot.com/o/Email%20Images%2Fall_devices.jpg?alt=media&token=59379e41-17cd-45e4-8e36-2238a346717a\" style=\"vertical-align: top; max-width: 300px; float: left;\"></div><div style=\"clear:both;\"></div>\n" +
                "                                                                                    </div>\n" +
                "                                                                            </td>\n" +
                "                                                                            </tr>\n" +
                "                                                                        </tbody></table>\n" +
                "\n" +
                "                                                                    </td>\n" +
                "                                                                </tr>\n" +
                "                                                            </tbody>\n" +
                "                                                        </table></td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                "                                                            <div><div>All of your notes on all of your devices. Share documents instantly via email and device-to-device.</div>\n" +
                "</div>\n" +
                "                                                        </td>\n" +
                "                                                    </tr>\n" +
                "                                                    </tbody></table>\n" +
                "\n" +
                "                                                </td></tr>\n" +
                "                                    </tbody></table></td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                        </tbody></table>\n" +
                "\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody></table><!--[if mso]>\n" +
                "                </td>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                </tr>\n" +
                "                </table>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "            </div></td>\n" +
                "    </tr><tr>\n" +
                "\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "\n" +
                "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                "                \n" +
                "                <!--[if mso 15]>\n" +
                "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                "                <tr>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso 15]>\n" +
                "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                <![endif]-->\n" +
                "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_11\" id=\"Layout_11\">\n" +
                "                <tbody><tr>\n" +
                "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding: 20px;\">\n" +
                "                            <tbody><tr>\n" +
                "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                "\n" +
                "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                "                                        <tbody><tr>\n" +
                "\n" +
                "                                            <td class=\"rnb-force-col img-block-center\" valign=\"top\" width=\"180\" style=\"padding-right: 20px;\">\n" +
                "\n" +
                "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-2-noborder-onright\" width=\"180\">\n" +
                "\n" +
                "\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td width=\"100%\" style=\"line-height: 1px;\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                "                                                            <div style=\"border-top:0px none #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\"><div><a target=\"_blank\" href=\"https://noteify.interstellarstudios.co.uk\"><img ng-if=\"col.img.source != 'url'\" alt=\"\" border=\"0\" hspace=\"0\" vspace=\"0\" width=\"180\" style=\"vertical-align:top; float: left; max-width:270px !important; \" class=\"rnb-col-2-img-side-xl\" src=\"https://firebasestorage.googleapis.com/v0/b/note-ify-d3325.appspot.com/o/Email%20Images%2Fweb_computer.jpg?alt=media&token=96b0cbb5-abae-454a-8dd4-a9698f22f163\"></a></div><div style=\"clear:both;\"></div></div></td>\n" +
                "                                                    </tr>\n" +
                "                                                    </tbody></table>\n" +
                "                                                </td><td class=\"rnb-force-col\" valign=\"top\">\n" +
                "\n" +
                "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" width=\"350\" align=\"left\" class=\"rnb-last-col-2\">\n" +
                "\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td style=\"font-size:24px; font-family:Arial,Helvetica,sans-serif; color:#3c4858; text-align:left;\">\n" +
                "                                                            <span style=\"color:#3c4858; \"><strong><span style=\"font-size:18px;\">Website</span></strong></span></td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td height=\"10\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td class=\"rnb-mbl-float-none\" style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif;color:#3c4858;float:right;width:350px; line-height: 21px;\"><div>Need some information? Check out our website:&nbsp;<a href=\"https://noteify.interstellarstudios.co.uk/\" style=\"text-decoration: underline; color: rgb(52, 153, 219);\">https://noteify.interstellarstudios.co.uk\u200B</a></div>\n" +
                "</td>\n" +
                "                                                    </tr>\n" +
                "                                                    </tbody></table>\n" +
                "                                                </td>\n" +
                "\n" +
                "                                            </tr></tbody></table></td>\n" +
                "                            </tr>\n" +
                "                        </tbody></table>\n" +
                "\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody></table>\n" +
                "            <!--[if mso 15]>\n" +
                "                </td>\n" +
                "                <![endif]-->\n" +
                "\n" +
                "                <!--[if mso 15]>\n" +
                "                </tr>\n" +
                "                </table>\n" +
                "                <![endif]-->\n" +
                "            \n" +
                "        </div></td>\n" +
                "    </tr><tr>\n" +
                "\n" +
                "        <td align=\"center\" valign=\"top\">\n" +
                "\n" +
                "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                "                <tr>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                "                <![endif]-->\n" +
                "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_12\" id=\"Layout_12\">\n" +
                "                <tbody><tr>\n" +
                "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding-left: 20px; padding-right: 20px;\">\n" +
                "                            <tbody><tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                "\n" +
                "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                "                                        <tbody><tr>\n" +
                "                                            <td class=\"rnb-force-col\" width=\"550\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-1\" width=\"550\">\n" +
                "                                                    <tbody><tr>\n" +
                "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"center\">\n" +
                "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                                <tbody>\n" +
                "                                                                    <tr>\n" +
                "                                                                        <td width=\"100%\" valign=\"top\" align=\"center\" class=\"img-block-center\">\n" +
                "\n" +
                "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                "                                                                            <tbody><tr>\n" +
                "                                                                                <td>\n" +
                "                                                                        <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\">\n" +
                "                                                                            <div><a target=\"_blank\" href=\"https://github.com/craigspicer\">\n" +
                "                                                                            <img ng-if=\"col.img.source != 'url'\" width=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-1-img\" src=\"https://firebasestorage.googleapis.com/v0/b/note-ify-d3325.appspot.com/o/Email%20Images%2FGitHub_logo.jpg?alt=media&token=2b531655-ee12-4919-8f63-f30bf1ddc17b\" style=\"vertical-align: top; max-width: 200px; float: left;\"></a></div><div style=\"clear:both;\"></div>\n" +
                "                                                                            </div></td>\n" +
                "                                                                            </tr>\n" +
                "                                                                        </tbody></table>\n" +
                "\n" +
                "                                                                    </td>\n" +
                "                                                                    </tr>\n" +
                "                                                                </tbody>\n" +
                "                                                                </table></td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                                                    </tr><tr>\n" +
                "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                "                                                            <div><div style=\"text-align: center;\">© 2019 Note-ify. All Rights Reserved.</div>\n" +
                "</div>\n" +
                "                                                        </td>\n" +
                "                                                    </tr>\n" +
                "                                                    </tbody></table>\n" +
                "\n" +
                "                                                </td></tr>\n" +
                "                                    </tbody></table></td>\n" +
                "                            </tr>\n" +
                "                            <tr>\n" +
                "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                "                            </tr>\n" +
                "                        </tbody></table>\n" +
                "\n" +
                "                    </td>\n" +
                "                </tr>\n" +
                "            </tbody></table><!--[if mso]>\n" +
                "                </td>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "                <!--[if mso]>\n" +
                "                </tr>\n" +
                "                </table>\n" +
                "                <![endif]-->\n" +
                "                \n" +
                "            </div></td>\n" +
                "    </tr></tbody></table>\n" +
                "            <!--[if gte mso 9]>\n" +
                "                        </td>\n" +
                "                        </tr>\n" +
                "                        </table>\n" +
                "                        <![endif]-->\n" +
                "                        </td>\n" +
                "        </tr>\n" +
                "        </tbody></table>\n" +
                "\n" +
                "</body></html>");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    apiInstance.sendTransacEmail(sendSmtpEmail);
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
