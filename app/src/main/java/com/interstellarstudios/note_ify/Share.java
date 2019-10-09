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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.email.SendMail;
import com.interstellarstudios.note_ify.email.SendMailGrocery;
import com.interstellarstudios.note_ify.email.SendMailWithAttachment;
import com.interstellarstudios.note_ify.models.GroceryItem;
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
    private String mCurrentUserId, mSharedUserId, currentUserEmail, sharedUserEmail, fromActivity;
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
    private ArrayList<String> groceryArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            FirebaseUser mUser = mFireBaseAuth.getCurrentUser();
            currentUserEmail = mUser.getEmail();
        }

        mSharedUserEmailText = findViewById(R.id.sharedUserEmail);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            fromActivity = bundle.getString("fromActivity");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView toolbarCheck = toolbar.findViewById(R.id.toolbar_check);
        toolbarCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (fromActivity) {
                    case "GroceryList":
                        shareGroceryList();
                        hideKeyboard(Share.this);
                        break;
                    case "SharedGroceryList":
                        shareGroceryListShared();
                        hideKeyboard(Share.this);
                        break;
                    case "Note":
                        saveSharedNote();
                        hideKeyboard(Share.this);
                        break;
                }
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
                if(fromActivity.equals("GroceryList") || fromActivity.equals("SharedGroceryList")) {
                    whatsAppGroceryList();
                } else if (fromActivity.equals("Note")) {
                    whatsAppNote();
                }
            }
        });

        ImageView whatsAppIcon = findViewById(R.id.whatsapp_icon);
        whatsAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fromActivity.equals("GroceryList") || fromActivity.equals("SharedGroceryList")) {
                    whatsAppGroceryList();
                } else if (fromActivity.equals("Note")) {
                    whatsAppNote();
                }
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
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(toolbarCheck, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            mSharedUserEmailText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mSharedUserEmailText.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mSharedUserEmailText.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            whatsAppText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            importContactText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(importContactIcon, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));

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

    private void shareGroceryList() {

        sharedUserEmail = mSharedUserEmailText.getText().toString().trim().toLowerCase();

        if (sharedUserEmail.trim().isEmpty()) {
            Toasty.info(context, "Please enter an email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groceryArrayList = bundle.getStringArrayList("groceryArrayList");
        }

        DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_List").document(sharedUserEmail);
        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        SendMailGrocery.sendMail(context, sharedUserEmail, currentUserEmail, groceryArrayList);

                        UserDetailsModel userDetails = document.toObject(UserDetailsModel.class);
                        mSharedUserId = userDetails.getUserId();

                        mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                String documentId = document.getId();
                                                mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(documentId).delete();
                                            }

                                            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Grocery_List").collection("Grocery_List")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                                    String documentId = document.getId();

                                                                    GroceryItem groceryItem = document.toObject(GroceryItem.class);
                                                                    String setItem = groceryItem.getItem();

                                                                    DocumentReference groceryListPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(documentId);
                                                                    groceryListPath.set(new GroceryItem(setItem));
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("from", currentUserEmail);
                        CollectionReference notificationPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Grocery_Notifications").collection("Grocery_Notifications");
                        notificationPath.add(notificationMessage);

                        Toasty.success(context, "Grocery List shared with and emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();

                    } else {

                        SendMailGrocery.sendMail(context, sharedUserEmail, currentUserEmail, groceryArrayList);

                        Toasty.success(context, "Grocery List emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
                    }

                } else {
                    Toasty.error(context, "Please ensure that there is an active network connection to share a grocery list", Toast.LENGTH_LONG, true).show();
                }
            }
        });
    }

    private void shareGroceryListShared() {

        sharedUserEmail = mSharedUserEmailText.getText().toString().trim().toLowerCase();

        if (sharedUserEmail.trim().isEmpty()) {
            Toasty.info(context, "Please enter an email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groceryArrayList = bundle.getStringArrayList("groceryArrayList");
        }

        DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_List").document(sharedUserEmail);
        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        SendMailGrocery.sendMail(context, sharedUserEmail, currentUserEmail, groceryArrayList);

                        UserDetailsModel userDetails = document.toObject(UserDetailsModel.class);
                        mSharedUserId = userDetails.getUserId();

                        mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                String documentId = document.getId();
                                                mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(documentId).delete();
                                            }

                                            mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                                    String documentId = document.getId();

                                                                    GroceryItem groceryItem = document.toObject(GroceryItem.class);
                                                                    String setItem = groceryItem.getItem();

                                                                    DocumentReference groceryListPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(documentId);
                                                                    groceryListPath.set(new GroceryItem(setItem));
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("from", currentUserEmail);
                        CollectionReference notificationPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Grocery_Notifications").collection("Grocery_Notifications");
                        notificationPath.add(notificationMessage);

                        Toasty.success(context, "Grocery List shared with and emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();

                    } else {

                        SendMailGrocery.sendMail(context, sharedUserEmail, currentUserEmail, groceryArrayList);

                        Toasty.success(context, "Grocery List emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
                    }

                } else {
                    Toasty.error(context, "Please ensure that there is an active network connection to share a grocery list", Toast.LENGTH_LONG, true).show();
                }
            }
        });
    }

    private void whatsAppGroceryList() {

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            groceryArrayList = bundle.getStringArrayList("groceryArrayList");
        }

        String groceryString = "Grocery List: \n\n" + groceryArrayList.toString();
        String modGroceryString = groceryString.replaceAll(",", "\n");

        Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
        whatsAppIntent.setType("text/plain"); //html
        whatsAppIntent.setPackage("com.whatsapp");
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.interstellarstudios.note_ify\n\n" + modGroceryString);

        try {
            startActivity(whatsAppIntent);
        } catch (android.content.ActivityNotFoundException e) {
            e.printStackTrace();
            Toasty.error(context, "WhatsApp is not installed", Toast.LENGTH_LONG, true).show();
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
                            SendMail.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate);
                        }
                        if (!audioZipFileName.equals("") && attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else if (audioZipFileName.equals("") && !attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));

                            SendMailWithAttachment.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        }

                        Toasty.success(context, "Note shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();

                    } else {

                        List<SendSmtpEmailAttachment> attachmentList = new ArrayList<>();

                        if (attachment_name.equals("") && audioZipFileName.equals("")) {
                            SendMail.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate);
                        }
                        if (!audioZipFileName.equals("") && attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else if (audioZipFileName.equals("") && !attachment_name.equals("")) {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));

                            SendMailWithAttachment.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        } else {
                            attachmentList.add(new SendSmtpEmailAttachment().url(attachmentUrl).name(attachment_name));
                            attachmentList.add(new SendSmtpEmailAttachment().url(audioZipDownloadUrl).name(audioZipFileName));

                            SendMailWithAttachment.sendMail(context, sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate, attachmentList);
                        }

                        Toasty.success(context, "Note emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
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
