package com.interstellarstudios.note_ify;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.interstellarstudios.note_ify.email.SendMail;
import com.interstellarstudios.note_ify.email.SendMailWithAttachment;
import com.interstellarstudios.note_ify.models.Note;
import com.interstellarstudios.note_ify.models.UserDetailsModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import es.dmoral.toasty.Toasty;
import sibModel.SendSmtpEmailAttachment;

public class Share extends AppCompatActivity {

    private Context context = this;
    private String currentUserEmail, sharedUserEmail;
    private FirebaseFirestore mFireBaseFireStore;
    private EditText mSharedUserEmailText;
    private static final int CONTACT_PICKER_RESULT = 2;
    private static final int PERMISSION_READ_CONTACTS_REQUEST = 11;
    private String sharedNoteId = UUID.randomUUID().toString();
    private String title;
    private String description;
    private int priority;
    private int updatedRevision;
    private String noteDate;
    private String attachmentUrl;
    private String attachment_name;
    private String audioDownloadUrl;
    private String audioZipDownloadUrl;
    private String audioZipFileName;
    private FirebaseAnalytics mFireBaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            FirebaseUser mUser = mFireBaseAuth.getCurrentUser();
            currentUserEmail = mUser.getEmail();
        }

        mSharedUserEmailText = findViewById(R.id.sharedUserEmail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView toolbarCheck = toolbar.findViewById(R.id.toolbar_check);
        toolbarCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSharedNote();
                hideKeyboard(Share.this);
            }
        });

        TextView importContactText = findViewById(R.id.import_text);
        importContactText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        ImageView importContactIcon = findViewById(R.id.import_icon);
        importContactIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        TextView whatsAppText = findViewById(R.id.whatsapp_text);
        whatsAppText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whatsAppNote();
            }
        });

        ImageView whatsAppIcon = findViewById(R.id.whatsapp_icon);
        whatsAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                whatsAppNote();
            }
        });

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {
            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            ImageViewCompat.setImageTintList(toolbarCheck, ContextCompat.getColorStateList(context, R.color.colorPrimary));
            mSharedUserEmailText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            mSharedUserEmailText.setHintTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            DrawableCompat.setTint(mSharedUserEmailText.getBackground(), ContextCompat.getColor(context, R.color.colorPrimary));
            whatsAppText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            importContactText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            ImageViewCompat.setImageTintList(importContactIcon, ContextCompat.getColorStateList(context, R.color.colorPrimary));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    public void getPermissionToReadUserContacts() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to access contacts")
                .setMessage("This permission is needed in order to get an email address for a selected contact. Manually enable in Settings > Apps & notifications > Note-ify > Permissions.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS_REQUEST);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_READ_CONTACTS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "Read Contacts permission granted", Toast.LENGTH_LONG, true).show();
                doLaunchContactPicker();
            } else {
                Toasty.error(context, "Read Contacts permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void doLaunchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_RESULT && resultCode == RESULT_OK) {

            String email = "";

            Uri result = data.getData();
            String id = result.getLastPathSegment();

            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);

            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            if (cursor != null) {
                cursor.close();
            }
            if (email.length() == 0) {
                Toasty.info(context, "No email address stored for this contact", Toast.LENGTH_LONG, true).show();
            } else {
                mSharedUserEmailText.setText(email);
            }
        }
    }

    private void saveSharedNote() {

        sharedUserEmail = mSharedUserEmailText.getText().toString().trim().toLowerCase();
        if (sharedUserEmail.trim().isEmpty()) {
            return;
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString("title");
            description = bundle.getString("description");
            priority = bundle.getInt("priority");
            updatedRevision = bundle.getInt("revision");
            noteDate = bundle.getString("noteDate");
            attachmentUrl = bundle.getString("attachmentUrl");
            attachment_name = bundle.getString("attachment_name");
            audioDownloadUrl = bundle.getString("audioDownloadUrl");
            audioZipDownloadUrl = bundle.getString("audioZipDownloadUrl");
            audioZipFileName = bundle.getString("audioZipFileName");
        }

        DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_List").document(sharedUserEmail);
        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDetailsModel userDetails = document.toObject(UserDetailsModel.class);
                        String sharedUserId = userDetails.getUserId();

                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("from", currentUserEmail);
                        CollectionReference notificationPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Public").document("Notifications").collection("Notifications");
                        notificationPath.add(notificationMessage);

                        DocumentReference sharedDocumentPath = mFireBaseFireStore.collection("Users").document(sharedUserId).collection("Public").document("Shared").collection("Shared").document(sharedNoteId);
                        sharedDocumentPath.set(new Note(sharedNoteId, "", title, description, priority, noteDate, currentUserEmail, updatedRevision, attachmentUrl, attachment_name, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName));

                        List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();

                        if (attachment_name.equals("") && audioZipFileName.equals("")) {
                            SendMail.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate);
                        }
                        if (!audioZipFileName.equals("") && attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else if (audioZipFileName.equals("") && !attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));

                            SendMailWithAttachment.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        }

                        Bundle data = new Bundle();
                        data.putString("note_name", title);
                        mFireBaseAnalytics.logEvent("share_device", data);

                        Toasty.success(context, "Note shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();

                    } else {

                        List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();

                        if (attachment_name.equals("") && audioZipFileName.equals("")) {
                            SendMail.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate);
                        }
                        if (!audioZipFileName.equals("") && attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else if (audioZipFileName.equals("") && !attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));

                            SendMailWithAttachment.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        }

                        Toasty.success(context, "Note emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();

                        Bundle data = new Bundle();
                        data.putString("note_name", title);
                        mFireBaseAnalytics.logEvent("share_email", data);
                    }

                } else {
                    Toasty.error(context, "Please ensure that there is an active network connection to share a note", Toast.LENGTH_LONG, true).show();
                }
            }
        });
    }

    private void whatsAppNote() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            title = bundle.getString("title");
            description = bundle.getString("description");
        }

        String plainTextDescription = Html.fromHtml(description).toString();

        Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
        whatsAppIntent.setType("text/plain");
        whatsAppIntent.setPackage("com.whatsapp");
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.interstellarstudios.note_ify\n\n" + title + "\n\n" + plainTextDescription);

        try {
            startActivity(whatsAppIntent);

            Bundle data = new Bundle();
            data.putString("note_name", title);
            mFireBaseAnalytics.logEvent("share_whatsapp", data);

        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
            Toasty.error(context, "WhatsApp is not installed", Toast.LENGTH_LONG, true).show();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event);
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
