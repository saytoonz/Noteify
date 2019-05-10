package com.interstellarstudios.note_ify;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.richeditor.RichEditor;
import sendinblue.ApiClient;
import sendinblue.ApiException;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.SmtpApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import spencerstudios.com.bungeelib.Bungee;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class NewNotebookNote extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Context context = this;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseUser user = firebaseAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CONTACT_PICKER_RESULT = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int PICK_DOCUMENT_REQUEST = 4;
    private Uri mImageUri;
    private EditText editTextTitle;
    private NumberPicker numberPickerPriority;
    private String noteDate;
    private ImageView button_choose_image;
    private ImageView button_camera;
    private ImageView button_attachment;
    private ImageView button_reminder;
    private EditText sharedUserEmailInput;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 2;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 3;
    private TextView toolbarContacts;
    private TextView toolbarSave;
    private String pathToFile;
    private File photoFile = null;
    private RichEditor mEditor;
    private String reminderDateString;
    private String downloadUrl;
    private ProgressDialog progressDialog;
    private TextView attachment_textview;
    private ImageView attachment_icon;
    private String attachment_name = "";
    private String filePath;
    private String fileName;
    private String localNoteId = UUID.randomUUID().toString();
    private String sharedNoteId = UUID.randomUUID().toString();
    private String title;
    private String description;
    private int priority;
    private String date;
    private String lowercasetitle;
    private String sharedUserEmail;
    private String userIdDetail;
    private String thisUserEmail = user.getEmail();
    private String downloadURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarContacts = toolbar.findViewById(R.id.toolbar_contacts);
        toolbarContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewNotebookNote.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        toolbarSave = toolbar.findViewById(R.id.toolbar_save);
        toolbarSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveNote();
                saveSharedNote();
                sendMail();

                attachment_name = attachment_textview.getText().toString();
                if (!attachment_name.equals("")) {
                    attachmentUpload();
                }
                Bungee.zoom(context);
            }
        });

        button_choose_image = findViewById(R.id.button_choose_image);
        button_choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        button_camera = (ImageView) findViewById(R.id.button_camera);
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(NewNotebookNote.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToUseCamera();
                }
                if (ContextCompat.checkSelfPermission(NewNotebookNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                }
                else if (ContextCompat.checkSelfPermission(NewNotebookNote.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(NewNotebookNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                }
            }
        });

        button_attachment = (ImageView) findViewById(R.id.button_attachment);
        button_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(NewNotebookNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else {
                    new MaterialFilePicker()
                            .withActivity(NewNotebookNote.this)
                            .withRequestCode(PICK_DOCUMENT_REQUEST)
                            .withHiddenFiles(true)
                            .start();
                }
            }
        });

        button_reminder = (ImageView)findViewById(R.id.button_reminder);
        button_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        mEditor = (RichEditor)findViewById(R.id.mEditor);
        mEditor.setEditorFontColor(Color.parseColor("#ffffff"));
        mEditor.setEditorFontSize(16);
        mEditor.setBackgroundColor(Color.parseColor("#171717"));
        //mEditor.setPlaceholder("Start Writing");

        findViewById(R.id.action_clear).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.removeFormat();
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.insert_link_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText)promptsView.findViewById(R.id.editTextDialogUserInput);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        String link = userInput.getText().toString();
                                        mEditor.insertLink(link, link);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.redo();
            }
        });

        CheckBox action_bold = (CheckBox)findViewById(R.id.action_bold);
        action_bold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setBold();
            }
        });

        CheckBox action_italic = (CheckBox)findViewById(R.id.action_italic);
        action_italic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setItalic();
            }
        });

        CheckBox action_underline = (CheckBox)findViewById(R.id.action_underline);
        action_underline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setUnderline();
            }
        });

        CheckBox action_strikethrough = (CheckBox)findViewById(R.id.action_strikethrough);
        action_strikethrough.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        editTextTitle = findViewById(R.id.edit_text_title);
        numberPickerPriority = findViewById(R.id.number_picker_priority);
        sharedUserEmailInput = findViewById(R.id.sharedUserEmail);
        attachment_textview = findViewById(R.id.attachment_textview);
        attachment_icon = findViewById(R.id.attachment_icon);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
        noteDate = sdf.format(calendar.getTime());

        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

        progressDialog = new ProgressDialog(this);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

        Bundle analyticsBundle = new Bundle();

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        c.set(Calendar.HOUR_OF_DAY, 6);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.MILLISECOND, 0);
        startAlarm(c);

        reminderDateString = DateFormat.getDateInstance().format(c.getTime());
        Toasty.success(NewNotebookNote.this, "Email reminder set for 06:00 on " + reminderDateString, Toast.LENGTH_LONG, true).show();

        mFirebaseAnalytics.logEvent("reminder_set", analyticsBundle);
    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent (this, AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, i, 0);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pendingIntent);
    }

    public void getPermissionToReadUserContacts() {

        new AlertDialog.Builder(this)
                .setTitle("Permission needed to access Contacts")
                .setMessage("This permission is needed in order to get an email address for a selected contact. Manually enable in Settings > Apps & notifications > Note-ify > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS},
                                READ_CONTACTS_PERMISSIONS_REQUEST);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, READ_CONTACTS_PERMISSIONS_REQUEST);
    }

    public void getPermissionToUseCamera() {

        new AlertDialog.Builder(this)
                .setTitle("Permission needed to access Camera")
                .setMessage("This permission is needed in order to take a photo immediately for use in Notes. Manually enable in Settings > Apps & notifications > Note-ify > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA},
                                MY_CAMERA_REQUEST_CODE);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

        requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
    }

    public void getPermissionToWriteStorage() {

        new AlertDialog.Builder(this)
                .setTitle("Permission needed to Write to External Storage")
                .setMessage("This permission is needed in order save images taken with the camera when accessed by the App. Manually enable in Settings > Apps & notifications > Note-ify > Permissions.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                WRITE_EXTERNAL_STORAGE_REQUEST);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_STORAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == READ_CONTACTS_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(NewNotebookNote.this, "Read Contacts permission granted.", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(NewNotebookNote.this, "Read Contacts permission denied.", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(NewNotebookNote.this, "Camera permission granted.", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(NewNotebookNote.this, "Camera permission denied.", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(NewNotebookNote.this, "External storage permission granted.", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(NewNotebookNote.this, "External storage permission denied.", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void dispatchTakePictureIntent() {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            photoFile = createPhotoFile();

            if (photoFile != null) {
                pathToFile = photoFile.getAbsolutePath();
                Uri photoURI = FileProvider.getUriForFile(NewNotebookNote.this, "com.interstellarstudios.note_ify.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    private File createPhotoFile() {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        String name = sdf.format(calendar.getTime());

        File storageDir = getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File image = null;

        try {
            image = File.createTempFile(name,".jpg", storageDir);
        } catch (IOException e) {
        }
        return image;
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(pathToFile);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void doLaunchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            final Bundle analyticsBundle = new Bundle();

            mImageUri = data.getData();

            //if (isOnline() == true) {

                progressDialog.setMessage("Uploading Image...");
                progressDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream imageStream = getContentResolver().openInputStream(mImageUri);
                            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            selectedImage = handleSamplingAndRotationBitmap(NewNotebookNote.this, mImageUri);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageData = baos.toByteArray();

                            StorageReference ImageStorageRef = FirebaseStorage.getInstance().getReference("Users/" + current_user_id + "/Images");
                            final StorageReference fileReference = ImageStorageRef.child(System.currentTimeMillis() + ".jpeg");

                            UploadTask uploadTask = fileReference.putBytes(imageData);
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return fileReference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {

                                        Uri downloadUri = task.getResult();
                                        downloadUrl = downloadUri.toString();
                                        mEditor.insertImage(downloadUrl, "image_upload");

                                        mFirebaseAnalytics.logEvent("image_uploaded", analyticsBundle);
                                        progressDialog.dismiss();
                                    }
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                });thread.start();
            //} else {
                //Toasty.error(NewNotebookNote.this, "Image upload failed - no active network connection.", Toast.LENGTH_LONG, true).show();
            //}
        }

        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK) {

            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            fileName = filePath.substring(filePath.lastIndexOf("/")+1);
            attachment_textview.setVisibility(View.VISIBLE);
            attachment_icon.setVisibility(View.VISIBLE);
            attachment_textview.setText(fileName);
        }

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
                Toasty.info(NewNotebookNote.this, "No email address stored for this contact.", Toast.LENGTH_LONG, true).show();
            } else {
                sharedUserEmailInput.setText(email);
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            final Bundle analyticsBundle = new Bundle();

            galleryAddPic();
            mImageUri = Uri.fromFile(photoFile);

            //if (isOnline() == true) {

                progressDialog.setMessage("Uploading Image...");
                progressDialog.show();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InputStream imageStream = getContentResolver().openInputStream(mImageUri);
                            Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                            selectedImage = handleSamplingAndRotationBitmap(NewNotebookNote.this, mImageUri);

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] imageData = baos.toByteArray();

                            StorageReference ImageStorageRef = FirebaseStorage.getInstance().getReference("Users/" + current_user_id + "/Images");
                            final StorageReference fileReference = ImageStorageRef.child(System.currentTimeMillis() + ".jpeg");

                            UploadTask uploadTask = fileReference.putBytes(imageData);
                            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                @Override
                                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                    if (!task.isSuccessful()) {
                                        throw task.getException();
                                    }
                                    return fileReference.getDownloadUrl();
                                }
                            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {

                                        Uri downloadUri = task.getResult();
                                        downloadUrl = downloadUri.toString();
                                        mEditor.insertImage(downloadUrl, "image_upload");

                                        mFirebaseAnalytics.logEvent("camera_image_uploaded", analyticsBundle);
                                        progressDialog.dismiss();
                                    }
                                }
                            });

                        } catch (Exception e) {
                        }
                    }
                });
                thread.start();
            //} else {
                //Toasty.error(NewNotebookNote.this, "Image upload failed - no active network connection.", Toast.LENGTH_LONG, true).show();
            //}
        }
    }

    private void attachmentUpload() {

        final Uri file = Uri.fromFile(new File(filePath));
        StorageReference AttachmentStorageRef = FirebaseStorage.getInstance().getReference("Users/" + current_user_id + "/Attachments");
        final StorageReference fileReference = AttachmentStorageRef.child(fileName);

        //if (isOnline() == true) {

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        UploadTask uploadTask = fileReference.putFile(file);

                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return fileReference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {

                                    Uri downloadUri = task.getResult();
                                    downloadURL = downloadUri.toString();

                                    final DocumentReference documentPath = db.collection("Users").document(current_user_id).collection("Main").document("Notebook").collection("Notebook").document(localNoteId);
                                    documentPath.set(new Note(title, lowercasetitle, description, priority, date, "", 1, downloadURL));

                                    if (!sharedUserEmail.equals("")) {

                                        DocumentReference userDetailsRef = db.collection("User_List").document(sharedUserEmail);
                                        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    DocumentSnapshot document = task.getResult();

                                                    if (document.exists()) {
                                                        final DocumentReference sharedDocumentPath = db.collection("Users").document(userIdDetail).collection("Public").document("Shared").collection("Shared").document(sharedNoteId);
                                                        sharedDocumentPath.set(new Note(title, lowercasetitle, description, priority, date, thisUserEmail, 1, downloadURL));
                                                    }
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            });thread.start();
        //} else {
            //Toasty.error(NewNotebookNote.this, "Attachment upload failed - please ensure that there is an active connection to use attachments.", Toast.LENGTH_LONG, true).show();
        //}
    }

    public Boolean isOnline() {
        try {
            Process p1 = java.lang.Runtime.getRuntime().exec("ping -c 1 www.google.com");
            int returnVal = p1.waitFor();
            boolean reachable = (returnVal==0);
            return reachable;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Bitmap handleSamplingAndRotationBitmap(Context context, Uri selectedImage) throws IOException {

        int MAX_HEIGHT = 290;
        int MAX_WIDTH = 290;

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        InputStream imageStream = context.getContentResolver().openInputStream(selectedImage);
        BitmapFactory.decodeStream(imageStream, null, options);
        imageStream.close();

        options.inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        options.inJustDecodeBounds = false;
        imageStream = context.getContentResolver().openInputStream(selectedImage);
        Bitmap img = BitmapFactory.decodeStream(imageStream, null, options);

        img = rotateImageIfRequired(context, img, selectedImage);
        return img;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap rotateImageIfRequired(Context context, Bitmap img, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    private void saveNote() {

        Bundle analyticsBundle = new Bundle();

        title = editTextTitle.getText().toString();
        description = mEditor.getHtml();
        priority = numberPickerPriority.getValue();
        date = noteDate;
        lowercasetitle = title.toLowerCase();

        if (title.trim().isEmpty()) {
            Toasty.info(NewNotebookNote.this, "Please enter a title.", Toast.LENGTH_LONG, true).show();
            return;
        }

        DocumentReference folderPath = db.collection("Users").document(current_user_id).collection("Main").document("Notebook");
        folderPath.set(new Collection("Notebook", "notebook", noteDate));

        final DocumentReference documentPath = db.collection("Users").document(current_user_id).collection("Main").document("Notebook").collection("Notebook").document(localNoteId);
        documentPath.set(new Note(title, lowercasetitle, description, priority, date, "", 1, ""));

        Toasty.success(NewNotebookNote.this, "Note Saved.", Toast.LENGTH_LONG, true).show();
        finish();
        mFirebaseAnalytics.logEvent("save_note_called", analyticsBundle);
    }

    private void saveSharedNote() {

        final Bundle analyticsBundle = new Bundle();

        title = editTextTitle.getText().toString();
        description = mEditor.getHtml();
        priority = numberPickerPriority.getValue();
        date = noteDate;
        lowercasetitle = title.toLowerCase();
        sharedUserEmail = sharedUserEmailInput.getText().toString().trim();

        if (sharedUserEmail.trim().isEmpty()) {
            return;
        }

        DocumentReference userDetailsRef = db.collection("User_List").document(sharedUserEmail);
        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDetailsModel userDetails = document.toObject(UserDetailsModel.class);
                        userIdDetail = userDetails.getUserId();

                        //NOTIFICATIONS
                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("from", thisUserEmail);
                        CollectionReference notificationPath = db.collection("Users").document(userIdDetail).collection("Public").document("Notifications").collection("Notifications");
                        notificationPath.add(notificationMessage);

                        final DocumentReference sharedDocumentPath = db.collection("Users").document(userIdDetail).collection("Public").document("Shared").collection("Shared").document(sharedNoteId);
                        sharedDocumentPath.set(new Note(title, lowercasetitle, description, priority, date, thisUserEmail, 1, ""));

                        Toasty.success(NewNotebookNote.this, "Note shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
                        mFirebaseAnalytics.logEvent("note_share_called", analyticsBundle);
                    } else {
                        Toasty.success(NewNotebookNote.this, "Note emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
                        mFirebaseAnalytics.logEvent("note_share_email_only_called", analyticsBundle);
                    }
                } else {
                    Toasty.error(NewNotebookNote.this, "Please ensure that there is an active network connection to share a note.", Toast.LENGTH_LONG, true).show();
                    finish();
                }
            }
        });
    }

    private void sendMail() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ApiClient defaultClient = Configuration.getDefaultApiClient();

                    ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
                    apiKey.setApiKey("API KEY GOES HERE");

                    SmtpApi apiInstance = new SmtpApi();

                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    String currentUserEmail = user.getEmail();

                    List<SendSmtpEmailTo> emailArrayList = new ArrayList<SendSmtpEmailTo>();
                    emailArrayList.add(new SendSmtpEmailTo().email(sharedUserEmail));

                    SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
                    sendSmtpEmail.sender(new SendSmtpEmailSender().email("note-ify@interstellarstudios.co.uk").name("Note-ify"));
                    sendSmtpEmail.to(emailArrayList);
                    sendSmtpEmail.subject("You've received a Note from " + currentUserEmail);
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
                            "            <div>\n" +
                            "                \n" +
                            "                <table class=\"rnb-del-min-width rnb-tmpl-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:590px;\" name=\"Layout_9\" id=\"Layout_9\">\n" +
                            "                    \n" +
                            "                    <tbody><tr>\n" +
                            "                        <td class=\"rnb-del-min-width\" valign=\"top\" align=\"center\" style=\"min-width: 590px;\">\n" +
                            "                            <table width=\"100%\" cellpadding=\"0\" border=\"0\" bgcolor=\"#f9fafc\" align=\"center\" cellspacing=\"0\" style=\"background-color: rgb(249, 250, 252);\">\n" +
                            "                                <tbody><tr>\n" +
                            "                                    <td height=\"10\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
                            "                                </tr>\n" +
                            "                                <tr>\n" +
                            "                                    <td align=\"center\" height=\"20\" style=\"font-family:Arial,Helvetica,sans-serif; color:#666666;font-size:13px;font-weight:normal;text-align: center;\">\n" +
                            "                                        <span style=\"color: rgb(102, 102, 102); text-decoration: underline;\">\n" +
                            "                                            <a target=\"_blank\" href=\"{{ mirror }}\" style=\"text-decoration: underline; color: rgb(102, 102, 102);\">View in browser</a></span>\n" +
                            "                                    </td>\n" +
                            "                                </tr>\n" +
                            "                                <tr>\n" +
                            "                                    <td height=\"10\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
                            "                                </tr>\n" +
                            "                            </tbody></table>\n" +
                            "                        </td>\n" +
                            "                    </tr>\n" +
                            "                </tbody></table>\n" +
                            "                \n" +
                            "            </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div>\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
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
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "                                    <table width=\"100%\" cellpadding=\"0\" border=\"0\" align=\"center\" cellspacing=\"0\">\n" +
                            "                                        <tbody><tr>\n" +
                            "                                            <td valign=\"top\" align=\"center\">\n" +
                            "                                                <table cellpadding=\"0\" border=\"0\" align=\"center\" cellspacing=\"0\" class=\"logo-img-center\"> \n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td valign=\"middle\" align=\"center\" style=\"line-height: 0px;\">\n" +
                            "                                                            <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block; \" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><div><img width=\"550\" vspace=\"0\" hspace=\"0\" border=\"0\" alt=\"Note-ify\" style=\"float: left;max-width:550px;display:block;\" class=\"rnb-logo-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c2802ec31b92f71e41dd1de.jpg\"></div></div></td>\n" +
                            "                                                    </tr>\n" +
                            "                                                </tbody></table>\n" +
                            "                                                </td>\n" +
                            "                                        </tr>\n" +
                            "                                    </tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "            <div>\n" +
                            "            \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
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
                            "                                            <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                                                    <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\"><div><span style=\"font-size:16px;\"><strong>" + title + "</strong></span></div>\n" +
                            "\n" +
                            "<div><br>\n" +
                            "" + description + "</div>\n" +
                            "\n" +
                            "<div><br>\n" +
                            "<em>Priority: " + priority + "<br>\n" +
                            "Revision: 1</em></div>\n" +
                            "\n" +
                            "<div><br>\n" +
                            "<span style=\"font-size:12px;\">Date &amp; Time: " + noteDate + "</span></div>\n" +
                            "</td>\n" +
                            "                                                                </tr>\n" +
                            "                                                                </tbody></table>\n" +
                            "\n" +
                            "                                                            </td></tr>\n" +
                            "                                                </tbody></table></td>\n" +
                            "                                        </tr>\n" +
                            "                                        <tr>\n" +
                            "                                            <td height=\"20\" style=\"font-size:1px; line-height:0px\">&nbsp;</td>\n" +
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
                            "            <div>\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
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
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                                                            <img ng-if=\"col.img.source != 'url'\" width=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-1-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c27674ccf29bcec2a435996.png\" style=\"vertical-align: top; max-width: 200px; float: left;\"></a></div><div style=\"clear:both;\"></div>\n" +
                            "                                                                            </div></td>\n" +
                            "                                                                            </tr>\n" +
                            "                                                                        </tbody></table>\n" +
                            "\n" +
                            "                                                                    </td>\n" +
                            "                                                                    </tr>\n" +
                            "                                                                </tbody>\n" +
                            "                                                                </table></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                            "                                                            <div><div style=\"text-align: center;\">Download the free App now to see attachments.</div>\n" +
                            "</div>\n" +
                            "                                                        </td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "\n" +
                            "                                                </td></tr>\n" +
                            "                                    </tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "            <div>\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
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
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                                                                    <div style=\"border-top:1px Solid #9c9c9c;border-right:1px Solid #9c9c9c;border-bottom:1px Solid #9c9c9c;border-left:1px Solid #9c9c9c;display:inline-block;\"><div><img border=\"0\" width=\"263\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-2-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c4392438696e366516c5d85.jpg\" style=\"vertical-align: top; max-width: 300px; float: left;\"></div><div style=\"clear:both;\"></div>\n" +
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
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                                                                    <div style=\"border-top:1px Solid #9c9c9c;border-right:1px Solid #9c9c9c;border-bottom:1px Solid #9c9c9c;border-left:1px Solid #9c9c9c;display:inline-block;\"><div><img border=\"0\" width=\"263\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-2-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c4392438696e3662461432d.jpg\" style=\"vertical-align: top; max-width: 300px; float: left;\"></div><div style=\"clear:both;\"></div>\n" +
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
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "            <div>\n" +
                            "                \n" +
                            "                <!--[if mso 15]>\n" +
                            "                <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
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
                            "                                                        <td width=\"100%\" style=\"line-height: 0px;\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                            "                                                            <div style=\"border-top:0px none #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\"><div><a target=\"_blank\" href=\"https://noteify.interstellarstudios.co.uk\"><img ng-if=\"col.img.source != 'url'\" alt=\"\" border=\"0\" hspace=\"0\" vspace=\"0\" width=\"180\" style=\"vertical-align:top; float: left; max-width:270px !important; \" class=\"rnb-col-2-img-side-xl\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c4b80730d48fbeb3c5c753d.png\"></a></div><div style=\"clear:both;\"></div></div></td>\n" +
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
                            "                                                        <td height=\"10\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "            <div>\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"left\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
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
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                                                            <img ng-if=\"col.img.source != 'url'\" width=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-1-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5cd3fccc27351d028e2b7a1b.png\" style=\"vertical-align: top; max-width: 200px; float: left;\"></a></div><div style=\"clear:both;\"></div>\n" +
                            "                                                                            </div></td>\n" +
                            "                                                                            </tr>\n" +
                            "                                                                        </tbody></table>\n" +
                            "\n" +
                            "                                                                    </td>\n" +
                            "                                                                    </tr>\n" +
                            "                                                                </tbody>\n" +
                            "                                                                </table></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px;\">&nbsp;</td>\n" +
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
                            "            <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:590px;\" name=\"Layout_4701\" id=\"Layout_4701\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" valign=\"top\" align=\"center\" style=\"min-width:590px;\">\n" +
                            "                        <table width=\"100%\" cellpadding=\"0\" border=\"0\" height=\"38\" cellspacing=\"0\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td valign=\"top\" height=\"38\">\n" +
                            "                                    <img width=\"20\" height=\"38\" style=\"display:block; max-height:38px; max-width:20px;\" alt=\"\" src=\"http://img.mailinblue.com/new_images/rnb/rnb_space.gif\">\n" +
                            "                                </td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table>\n" +
                            "            </td>\n" +
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
                    try {
                        CreateSmtpEmail result = apiInstance.sendTransacEmail(sendSmtpEmail);
                    } catch (ApiException e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        DocumentReference DraftsDocumentPath = db.collection("Users").document(current_user_id).collection("Main").document("Drafts");
        DraftsDocumentPath.set(new Collection("Drafts", "drafts", noteDate));

        saveDraft();
        Bungee.zoom(context);
    }

    private void saveDraft() {

        title = editTextTitle.getText().toString();
        description = mEditor.getHtml();
        priority = numberPickerPriority.getValue();
        date = noteDate;
        lowercasetitle = title.toLowerCase();

        if (title.trim().isEmpty() && description == null) {
            return;
        }

        String id = UUID.randomUUID().toString();

        final DocumentReference documentPath = db.collection("Users").document(current_user_id).collection("Main").document("Drafts").collection("Drafts").document(id);
        documentPath.set(new Note(title, lowercasetitle, description, priority, date, "", 1, ""));

        Toasty.success(NewNotebookNote.this, "Note saved to Drafts.", Toast.LENGTH_LONG, true).show();
        finish();
    }
}
