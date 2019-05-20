package com.interstellarstudios.note_ify;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import es.dmoral.toasty.Toasty;

public class NewCollection extends AppCompatActivity {

    private FirebaseAuth mFireBaseAuth;
    private FirebaseFirestore mFireBaseFireStore;
    private EditText mNewFolder;
    private String mFolderDate;
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        int colorLightThemeText = getResources().getColor(R.color.colorLightThemeText);
        String colorLightThemeTextString = Integer.toString(colorLightThemeText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorLightThemeTextString + "\">" + "New Folder" + "</font>"));

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFolder();
            }
        });

        mNewFolder = findViewById(R.id.new_folder);
        ImageView folderImageView = findViewById(R.id.folderImageView);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
        mFolderDate = sdf.format(calendar.getTime());

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(NewCollection.this, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "New Folder" + "</font>"));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            mNewFolder.setTextColor(ContextCompat.getColor(NewCollection.this, R.color.colorDarkThemeText));
            mNewFolder.setHintTextColor(ContextCompat.getColor(NewCollection.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mNewFolder.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(folderImageView, ContextCompat.getColorStateList(this, R.color.colorDarkThemeText));
            buttonSave.setTextColor(ContextCompat.getColor(NewCollection.this, R.color.colorDarkThemeText));
        }
    }

    private void saveFolder() {

        String folder = mNewFolder.getText().toString();
        String lowerCaseFolder = folder.toLowerCase();

        if (folder.trim().isEmpty()) {
            Toasty.info(NewCollection.this, "Please enter a folder name.", Toast.LENGTH_LONG, true).show();
            return;
        }

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        DocumentReference documentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folder);
        documentPath.set(new Collection(folder, lowerCaseFolder, mFolderDate));

        Toasty.success(NewCollection.this, "Folder Created.", Toast.LENGTH_LONG, true).show();
        finish();
    }
}
