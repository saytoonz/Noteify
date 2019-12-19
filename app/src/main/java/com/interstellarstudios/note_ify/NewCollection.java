package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.interstellarstudios.note_ify.models.Collection;

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
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {
            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }

            mNewFolder.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            mNewFolder.setHintTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            DrawableCompat.setTint(mNewFolder.getBackground(), ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            ImageViewCompat.setImageTintList(folderImageView, ContextCompat.getColorStateList(context, R.color.colorPrimary));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void saveFolder() {

        String folder = mNewFolder.getText().toString().trim();
        String lowerCaseFolder = folder.toLowerCase();

        if (folder.trim().isEmpty()) {
            Toasty.info(context, "Please enter a folder name", Toast.LENGTH_LONG, true).show();
            return;
        }

        if (folder.contains("/")) {
            Toasty.info(context, "Folder names may not contain a '/'", Toast.LENGTH_LONG, true).show();
            return;
        }

        DocumentReference documentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folder);
        documentPath.set(new Collection(folder, mFolderDate));

        Toasty.success(context, "Folder Created", Toast.LENGTH_LONG, true).show();
        finish();
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
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }
}
