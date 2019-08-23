package com.interstellarstudios.note_ify;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.view.GravityCompat;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = this;
    private RecyclerView.Adapter adapter;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private List<Note> noteList = new ArrayList<>();
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setMessage("Loading all notes");
        mProgressDialog.show();

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        getAllNoteData();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ImageView navDrawerMenu = findViewById(R.id.navDrawerMenu);
        navDrawerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                startActivity(i);
            }
        });

        AutoCompleteTextView searchField = findViewById(R.id.searchField);
        searchField.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (noteList != null && !noteList.isEmpty()) {
                    ((SearchAdapter) adapter).getFilter().filter(s);
                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, searchSuggestions);
        searchField.setAdapter(adapter);

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            ImageViewCompat.setImageTintList(navDrawerMenu, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            searchField.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            searchField.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_new_note) {
            Intent i = new Intent(context, NewNote.class);
            i.putExtra("folderId", "Notebook");
            startActivity(i);
        } else if (id == R.id.nav_folders) {
            Intent j = new Intent(context, Home.class);
            startActivity(j);
        } else if (id == R.id.nav_share) {
            Intent j = new Intent(context, Shared.class);
            startActivity(j);
        } else if (id == R.id.nav_grocery_list) {
            Intent k = new Intent(context, GroceryList.class);
            startActivity(k);
        } else if (id == R.id.nav_shared_grocery_list) {
            Intent k = new Intent(context, SharedGroceryList.class);
            startActivity(k);
        } else if (id == R.id.nav_bin) {
            Intent l = new Intent(context, Bin.class);
            startActivity(l);
        } else if (id == R.id.nav_themes) {
            Intent l = new Intent(context, Themes.class);
            startActivity(l);
        } else if (id == R.id.nav_settings) {
            Intent m = new Intent(context, Settings.class);
            startActivity(m);
        } else if (id == R.id.nav_account) {
            Intent n = new Intent(context, Account.class);
            startActivity(n);
        } else if (id == R.id.nav_information) {
            Intent o = new Intent(context, Information.class);
            startActivity(o);
        } else if (id == R.id.nav_faq) {
            Intent p = new Intent(context, FAQ.class);
            startActivity(p);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void getAllNoteData() {

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Note note = document.toObject(Note.class);
                                String noteId = document.getId();
                                String title = note.getTitle();
                                String description = note.getDescription();
                                String date = note.getDate();
                                String fromEmailAddress = note.getFromEmailAddress();
                                int priority = note.getPriority();
                                int revision = note.getRevision();
                                String attachmentUrl = note.getAttachmentUrl();
                                String attachmentName = note.getAttachmentName();
                                String audioDownloadUrl = note.getAudioUrl();
                                String audioZipDownloadUrl = note.getAudioZipUrl();
                                String audioZipFileName = note.getAudioZipName();

                                searchSuggestions.add(title);
                                noteList.add(new Note(noteId, "Bin", title, description, priority, date, fromEmailAddress, revision, attachmentUrl, attachmentName, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName));
                            }
                        }
                    }
                });

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Public").document("Shared").collection("Shared")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Note note = document.toObject(Note.class);
                                String noteId = document.getId();
                                String title = note.getTitle();
                                String description = note.getDescription();
                                String date = note.getDate();
                                String fromEmailAddress = note.getFromEmailAddress();
                                int priority = note.getPriority();
                                int revision = note.getRevision();
                                String attachmentUrl = note.getAttachmentUrl();
                                String attachmentName = note.getAttachmentName();
                                String audioDownloadUrl = note.getAudioUrl();
                                String audioZipDownloadUrl = note.getAudioZipUrl();
                                String audioZipFileName = note.getAudioZipName();

                                searchSuggestions.add(title);
                                noteList.add(new Note(noteId, "Shared", title, description, priority, date, fromEmailAddress, revision, attachmentUrl, attachmentName, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName));
                            }
                        }
                    }
                });

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                Collection collection = document.toObject(Collection.class);
                                final String folderName = collection.getFolder();

                                mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(folderName).collection(folderName)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        Note note = document.toObject(Note.class);
                                                        String noteId = document.getId();
                                                        String title = note.getTitle();
                                                        String description = note.getDescription();
                                                        String date = note.getDate();
                                                        String fromEmailAddress = note.getFromEmailAddress();
                                                        int priority = note.getPriority();
                                                        int revision = note.getRevision();
                                                        String attachmentUrl = note.getAttachmentUrl();
                                                        String attachmentName = note.getAttachmentName();
                                                        String audioDownloadUrl = note.getAudioUrl();
                                                        String audioZipDownloadUrl = note.getAudioZipUrl();
                                                        String audioZipFileName = note.getAudioZipName();

                                                        searchSuggestions.add(title);
                                                        noteList.add(new Note(noteId, folderName, title, description, priority, date, fromEmailAddress, revision, attachmentUrl, attachmentName, audioDownloadUrl, audioZipDownloadUrl, audioZipFileName));
                                                    }
                                                }
                                                setUpRecyclerView();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void setUpRecyclerView() {

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        adapter = new SearchAdapter(noteList, sharedPreferences);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        mProgressDialog.dismiss();
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
