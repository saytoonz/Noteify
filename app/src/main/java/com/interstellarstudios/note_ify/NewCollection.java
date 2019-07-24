package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.app.AppCompatActivity;
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

    private Context context = this;
    private FirebaseFirestore mFireBaseFireStore;
    private EditText mNewFolder;
    private String mFolderDate;
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collection);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
        String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "New Folder" + "</font>"));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));

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
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            mNewFolder.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mNewFolder.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mNewFolder.getBackground(), ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            ImageViewCompat.setImageTintList(folderImageView, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            buttonSave.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
        }
    }

    private void saveFolder() {

        String folder = mNewFolder.getText().toString().trim();
        String lowerCaseFolder = folder.toLowerCase();

        if (folder.trim().isEmpty()) {
            Toasty.info(context, "Please enter a folder name", Toast.LENGTH_LONG, true).show();
            return;
        }

        if(folder.contains("/")){
            Toasty.info(context, "Folder names may not contain a '/'", Toast.LENGTH_LONG, true).show();
            return;
        }

        DocumentReference documentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folder);
        documentPath.set(new Collection(folder, lowerCaseFolder, mFolderDate));

        Toasty.success(context, "Folder Created", Toast.LENGTH_LONG, true).show();
        finish();
    }
}
