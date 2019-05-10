package com.interstellarstudios.note_ify;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import es.dmoral.toasty.Toasty;
import spencerstudios.com.bungeelib.Bungee;

public class NewCollection extends AppCompatActivity {

    private Context mContext = this;
    private FirebaseAnalytics mFireBaseAnalytics;
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

        mFireBaseAnalytics = FirebaseAnalytics.getInstance(this);
        final Bundle analyticsBundle = new Bundle();

        Button buttonSave = findViewById(R.id.buttonSave);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFolder();
                Bungee.zoom(mContext);
                mFireBaseAnalytics.logEvent("new_folder_saved", analyticsBundle);
            }
        });

        mNewFolder = findViewById(R.id.new_folder);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
        mFolderDate = sdf.format(calendar.getTime());
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

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        Bungee.zoom(mContext);
    }
}
