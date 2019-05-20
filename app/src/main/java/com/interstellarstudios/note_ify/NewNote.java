package com.interstellarstudios.note_ify;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.constraint.ConstraintLayout;
import android.support.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import android.widget.HorizontalScrollView;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import es.dmoral.toasty.Toasty;
import jp.wasabeef.richeditor.RichEditor;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class NewNote extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private Context mContext = this;
    private FirebaseAnalytics mFireBaseAnalytics;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CONTACT_PICKER_RESULT = 2;
    private static final int REQUEST_IMAGE_CAPTURE = 3;
    private static final int PICK_DOCUMENT_REQUEST = 4;
    private Uri mImageUri;
    private EditText editTextTitle;
    private NumberPicker numberPickerPriority;
    private String noteDate;
    private EditText sharedUserEmailInput;
    private static final int READ_CONTACTS_PERMISSIONS_REQUEST = 1;
    private static final int MY_CAMERA_REQUEST_CODE = 2;
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST = 3;
    private String pathToFile;
    private File photoFile = null;
    private RichEditor mEditor;
    private String downloadUrl;
    private ProgressDialog progressDialog;
    private TextView attachmentTextView;
    private ImageView attachment_icon;
    private String attachment_name = "";
    private String filePath;
    private String fileName;
    private String localNoteId = UUID.randomUUID().toString();
    private String sharedNoteId = UUID.randomUUID().toString();
    private String folderId;
    private String title;
    private String description;
    private int priority;
    private String date;
    private String lowerCaseTitle;
    private String sharedUserEmail;
    private String userIdDetail;
    private String currentUserEmail;
    private String downloadURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_note);

        mFireBaseAnalytics = FirebaseAnalytics.getInstance(this);
        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            FirebaseUser mUser = mFireBaseAuth.getCurrentUser();
            currentUserEmail = mUser.getEmail();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView priorityTextView = findViewById(R.id.priorityTextView);
        HorizontalScrollView horizontalScrollView = findViewById(R.id.horizontalScrollView);
        ImageView buttonBackground = findViewById(R.id.buttonBackground);

        TextView toolbarContacts = toolbar.findViewById(R.id.toolbar_contacts);
        toolbarContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        TextView toolbarSave = toolbar.findViewById(R.id.toolbar_save);
        toolbarSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveNote();
                saveSharedNote();

                attachment_name = attachmentTextView.getText().toString();
                if (!attachment_name.equals("")) {
                    attachmentUpload();
                }
            }
        });

        ImageView button_choose_image = findViewById(R.id.button_choose_image);
        button_choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });

        ImageView button_camera = findViewById(R.id.button_camera);
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToUseCamera();
                }
                if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    dispatchTakePictureIntent();
                }
            }
        });

        ImageView button_attachment = findViewById(R.id.button_attachment);
        button_attachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(NewNote.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToWriteStorage();
                } else {
                    new MaterialFilePicker()
                            .withActivity(NewNote.this)
                            .withRequestCode(PICK_DOCUMENT_REQUEST)
                            .withHiddenFiles(true)
                            .start();
                }
            }
        });

        ImageView button_reminder = findViewById(R.id.button_reminder);
        button_reminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment datePicker = new DatePickerFragment();
                datePicker.show(getSupportFragmentManager(), "date picker");
            }
        });

        String colorLightThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimary));
        String colorLightThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorLightThemeText));
        mEditor = findViewById(R.id.mEditor);
        mEditor.setEditorFontColor(Color.parseColor(colorLightThemeTextString));
        mEditor.setEditorFontSize(16);
        mEditor.setBackgroundColor(Color.parseColor(colorLightThemeString));
        mEditor.setPlaceholder("Start Writing");

        findViewById(R.id.action_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.removeFormat();
            }
        });

        findViewById(R.id.action_insert_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.insert_link_prompt, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = promptsView.findViewById(R.id.editTextDialogUserInput);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String link = userInput.getText().toString();
                                        mEditor.insertLink(link, link);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });

        findViewById(R.id.action_undo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.undo();
            }
        });

        findViewById(R.id.action_redo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.redo();
            }
        });

        CheckBox action_bold = findViewById(R.id.action_bold);
        action_bold.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setBold();
            }
        });

        CheckBox action_italic = findViewById(R.id.action_italic);
        action_italic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setItalic();
            }
        });

        CheckBox action_underline = findViewById(R.id.action_underline);
        action_underline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setUnderline();
            }
        });

        CheckBox action_strikethrough = findViewById(R.id.action_strikethrough);
        action_strikethrough.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mEditor.setStrikeThrough();
            }
        });

        findViewById(R.id.action_subscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSubscript();
            }
        });

        findViewById(R.id.action_superscript).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setSuperscript();
            }
        });

        findViewById(R.id.action_heading1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(1);
            }
        });

        findViewById(R.id.action_heading2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(2);
            }
        });

        findViewById(R.id.action_heading3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(3);
            }
        });

        findViewById(R.id.action_heading4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(4);
            }
        });

        findViewById(R.id.action_heading5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(5);
            }
        });

        findViewById(R.id.action_heading6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setHeading(6);
            }
        });

        findViewById(R.id.action_indent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setIndent();
            }
        });

        findViewById(R.id.action_outdent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setOutdent();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setAlignRight();
            }
        });

        findViewById(R.id.action_blockquote).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBlockquote();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditor.setNumbers();
            }
        });

        editTextTitle = findViewById(R.id.edit_text_title);
        numberPickerPriority = findViewById(R.id.number_picker_priority);
        sharedUserEmailInput = findViewById(R.id.sharedUserEmail);
        attachmentTextView = findViewById(R.id.attachment_textview);
        attachment_icon = findViewById(R.id.attachment_icon);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
        noteDate = sdf.format(calendar.getTime());

        numberPickerPriority.setMinValue(1);
        numberPickerPriority.setMaxValue(10);

        progressDialog = new ProgressDialog(this);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(NewNote.this, R.color.colorPrimaryDarkTheme));
            toolbar.setBackgroundColor(ContextCompat.getColor(NewNote.this, R.color.colorPrimaryDarkTheme));
            toolbarSave.setTextColor(ContextCompat.getColor(NewNote.this, R.color.colorDarkThemeText));
            toolbarContacts.setTextColor(ContextCompat.getColor(NewNote.this, R.color.colorDarkThemeText));
            priorityTextView.setTextColor(ContextCompat.getColor(NewNote.this, R.color.colorDarkThemeText));
            editTextTitle.setTextColor(ContextCompat.getColor(NewNote.this, R.color.colorDarkThemeText));
            editTextTitle.setHintTextColor(ContextCompat.getColor(NewNote.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextTitle.getBackground(), ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            DrawableCompat.setTint(sharedUserEmailInput.getBackground(), ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));

            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
            mEditor.setEditorFontColor(Color.parseColor(colorDarkThemeTextString));
            mEditor.setBackgroundColor(Color.parseColor(colorDarkThemeString));

            horizontalScrollView.setBackgroundColor(ContextCompat.getColor(NewNote.this, R.color.colorPrimaryDarkTheme));
            buttonBackground.setBackgroundColor(ContextCompat.getColor(NewNote.this, R.color.buttonBackground));
        }
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

        String reminderDateString = DateFormat.getDateInstance().format(c.getTime());
        Toasty.success(NewNote.this, "Email reminder set for 06:00 on " + reminderDateString, Toast.LENGTH_LONG, true).show();

        mFireBaseAnalytics.logEvent("reminder_set", analyticsBundle);
    }

    private void startAlarm(Calendar c) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(this, AlertReceiver.class);
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
                Toasty.success(NewNote.this, "Read Contacts permission granted", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(NewNote.this, "Read Contacts permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(NewNote.this, "Camera permission granted", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(NewNote.this, "Camera permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(NewNote.this, "External storage permission granted", Toast.LENGTH_LONG, true).show();
            } else {
                Toasty.error(NewNote.this, "External storage permission denied", Toast.LENGTH_LONG, true).show();
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
                Uri photoURI = FileProvider.getUriForFile(NewNote.this, "com.interstellarstudios.note_ify.fileprovider", photoFile);
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
            image = File.createTempFile(name, ".jpg", storageDir);
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

            progressDialog.setMessage("Uploading Image");
            progressDialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(mImageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage = handleSamplingAndRotationBitmap(NewNote.this, mImageUri);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageData = baos.toByteArray();

                        StorageReference ImageStorageRef = FirebaseStorage.getInstance().getReference("Users/" + mCurrentUserId + "/Images");
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

                                    mFireBaseAnalytics.logEvent("image_uploaded", analyticsBundle);
                                    progressDialog.dismiss();
                                }
                            }
                        });
                    } catch (Exception e) {
                    }
                }
            });
            thread.start();
        }

        if (requestCode == PICK_DOCUMENT_REQUEST && resultCode == RESULT_OK) {

            filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
            fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            attachmentTextView.setVisibility(View.VISIBLE);
            attachment_icon.setVisibility(View.VISIBLE);
            attachmentTextView.setText(fileName);
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
                Toasty.info(NewNote.this, "No email address stored for this contact", Toast.LENGTH_LONG, true).show();
            } else {
                sharedUserEmailInput.setText(email);
            }
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            final Bundle analyticsBundle = new Bundle();

            galleryAddPic();
            mImageUri = Uri.fromFile(photoFile);

            progressDialog.setMessage("Uploading Image");
            progressDialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        InputStream imageStream = getContentResolver().openInputStream(mImageUri);
                        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                        selectedImage = handleSamplingAndRotationBitmap(NewNote.this, mImageUri);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageData = baos.toByteArray();

                        StorageReference ImageStorageRef = FirebaseStorage.getInstance().getReference("Users/" + mCurrentUserId + "/Images");
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

                                    mFireBaseAnalytics.logEvent("camera_image_uploaded", analyticsBundle);
                                    progressDialog.dismiss();
                                }
                            }
                        });

                    } catch (Exception e) {
                    }
                }
            });
            thread.start();
        }
    }

    private void attachmentUpload() {

        final Uri file = Uri.fromFile(new File(filePath));
        StorageReference AttachmentStorageRef = FirebaseStorage.getInstance().getReference("Users/" + mCurrentUserId + "/Attachments");
        final StorageReference fileReference = AttachmentStorageRef.child(fileName);

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

                                final DocumentReference documentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folderId).collection(folderId).document(localNoteId);
                                documentPath.set(new Note(title, lowerCaseTitle, description, priority, date, "", 1, downloadURL));

                                if (!sharedUserEmail.equals("")) {

                                    DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_List").document(sharedUserEmail);
                                    userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();

                                                if (document.exists()) {
                                                    final DocumentReference sharedDocumentPath = mFireBaseFireStore.collection("Users").document(userIdDetail).collection("Public").document("Shared").collection("Shared").document(sharedNoteId);
                                                    sharedDocumentPath.set(new Note(title, lowerCaseTitle, description, priority, date, currentUserEmail, 1, downloadURL));
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
        });
        thread.start();
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

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            folderId = bundle.getString("folderId");
        } else {
            return;
        }

        title = editTextTitle.getText().toString();
        description = mEditor.getHtml();
        priority = numberPickerPriority.getValue();
        date = noteDate;
        lowerCaseTitle = title.toLowerCase();
        String lowerCaseFolder = folderId.toLowerCase();

        if (title.trim().isEmpty()) {
            Toasty.info(NewNote.this, "Please enter a title", Toast.LENGTH_LONG, true).show();
            return;
        }

        DocumentReference folderPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folderId);
        folderPath.set(new Collection(folderId, lowerCaseFolder, noteDate));

        final DocumentReference documentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folderId).collection(folderId).document(localNoteId);
        documentPath.set(new Note(title, lowerCaseTitle, description, priority, date, "", 1, ""));

        Toasty.success(NewNote.this, "Note Saved", Toast.LENGTH_LONG, true).show();
        finish();
        mFireBaseAnalytics.logEvent("save_note_called", analyticsBundle);
    }

    private void saveSharedNote() {

        final Bundle analyticsBundle = new Bundle();

        title = editTextTitle.getText().toString();
        description = mEditor.getHtml();
        priority = numberPickerPriority.getValue();
        date = noteDate;
        lowerCaseTitle = title.toLowerCase();
        sharedUserEmail = sharedUserEmailInput.getText().toString().trim();
        int updatedRevision = 1;

        if (sharedUserEmail.trim().isEmpty()) {
            return;
        }

        SendMail.sendMail(sharedUserEmail, currentUserEmail, title, description, priority, updatedRevision, noteDate);

        DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_List").document(sharedUserEmail);
        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDetailsModel userDetails = document.toObject(UserDetailsModel.class);
                        userIdDetail = userDetails.getUserId();

                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("from", currentUserEmail);
                        CollectionReference notificationPath = mFireBaseFireStore.collection("Users").document(userIdDetail).collection("Public").document("Notifications").collection("Notifications");
                        notificationPath.add(notificationMessage);

                        final DocumentReference sharedDocumentPath = mFireBaseFireStore.collection("Users").document(userIdDetail).collection("Public").document("Shared").collection("Shared").document(sharedNoteId);
                        sharedDocumentPath.set(new Note(title, lowerCaseTitle, description, priority, date, currentUserEmail, 1, ""));

                        Toasty.success(NewNote.this, "Note shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
                        mFireBaseAnalytics.logEvent("note_share_called", analyticsBundle);
                    } else {
                        Toasty.success(NewNote.this, "Note emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                        finish();
                        mFireBaseAnalytics.logEvent("note_share_email_only_called", analyticsBundle);
                    }
                } else {
                    Toasty.error(NewNote.this, "Please ensure that there is an active network connection to share a note", Toast.LENGTH_LONG, true).show();
                    finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        DocumentReference DraftsDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Drafts");
        DraftsDocumentPath.set(new Collection("Drafts", "drafts", noteDate));

        saveDraft();
    }

    private void saveDraft() {

        title = editTextTitle.getText().toString();
        description = mEditor.getHtml();
        priority = numberPickerPriority.getValue();
        date = noteDate;
        lowerCaseTitle = title.toLowerCase();

        if (title.trim().isEmpty() && description == null) {
            return;
        }

        String id = UUID.randomUUID().toString();

        final DocumentReference documentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Drafts").collection("Drafts").document(id);
        documentPath.set(new Note(title, lowerCaseTitle, description, priority, date, "", 1, ""));

        Toasty.success(NewNote.this, "Note saved to Drafts", Toast.LENGTH_LONG, true).show();
        finish();
    }
}