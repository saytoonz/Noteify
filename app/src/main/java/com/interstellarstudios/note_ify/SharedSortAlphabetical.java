package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.database.NoteEntity;
import com.interstellarstudios.note_ify.database.RecentSearches;
import com.interstellarstudios.note_ify.repository.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SharedSortAlphabetical extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = this;
    private String mCurrentUserID;
    private FirebaseFirestore mFireBaseFireStore;
    private NoteAdapter adapter;
    private ImageView emptyView;
    private TextView emptyViewText;
    private View newNoteOverlay;
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private static final int SPEECH_INPUT_REQUEST = 2;
    private AutoCompleteTextView searchField;
    private List<RecentSearches> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();
    private Repository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        repository = new Repository(getApplication());

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserID = mFireBaseAuth.getCurrentUser().getUid();
        }

        TextView textNote = findViewById(R.id.textView_text_note);
        TextView textSpeech = findViewById(R.id.textView_speech);
        TextView textVoice = findViewById(R.id.textView_voice);
        TextView textAttachment = findViewById(R.id.textView_attachment);

        newNoteOverlay = findViewById(R.id.new_note_overlay);
        newNoteOverlay.setVisibility(View.GONE);
        newNoteOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNoteOverlay.getVisibility() == View.VISIBLE) {
                    newNoteOverlay.setVisibility(View.GONE);
                }
            }
        });

        FloatingActionButton fabText = findViewById(R.id.fab_text);
        fabText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                i.putExtra("noteType", "note");
                startActivity(i);
            }
        });

        FloatingActionButton fabSpeechText = findViewById(R.id.fab_speech_text);
        fabSpeechText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                i.putExtra("noteType", "speech");
                startActivity(i);
            }
        });

        FloatingActionButton fabVoiceNote = findViewById(R.id.fab_voice_note);
        fabVoiceNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                i.putExtra("noteType", "voice");
                startActivity(i);
            }
        });

        FloatingActionButton fabAttachment = findViewById(R.id.fab_attach);
        fabAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                i.putExtra("noteType", "attachment");
                startActivity(i);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Shared (Sort: Alphabetical)");
        toolbar.setOverflowIcon(ContextCompat.getDrawable(context, R.drawable.ic_sort));
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        final ImageView navDrawerMenu = findViewById(R.id.navDrawerMenu);
        navDrawerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newNoteOverlay.setVisibility(View.VISIBLE);
            }
        });

        emptyView = findViewById(R.id.emptyView);
        emptyViewText = findViewById(R.id.emptyViewText);

        searchField = findViewById(R.id.searchField);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    search();

                    return true;
                }
                return false;
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, searchSuggestions);
        searchField.setAdapter(adapter);

        ImageView voiceSearch = findViewById(R.id.voice_search);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSpeechInput();
            }
        });

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if(switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            newNoteOverlay.setBackgroundResource(R.drawable.transparent_overlay_primary_dark);
            textNote.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textSpeech.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textVoice.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textAttachment.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(navDrawerMenu, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            emptyViewText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            searchField.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            searchField.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(voiceSearch, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setUpRecyclerView();
        loadDataFromRepository();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_date) {
            Intent i = new Intent(context, Shared.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.sort_priority) {
            Intent j = new Intent(context, SharedSortPriority.class);
            startActivity(j);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        final Drawable swipeBackground2 = new ColorDrawable(Color.parseColor("#0b37cb"));
        final Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        final Drawable moveIcon = ContextCompat.getDrawable(context, R.drawable.ic_move);

        CollectionReference notebookRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Public").document("Shared").collection("Shared");
        Query query = notebookRef.orderBy("title", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new NoteAdapter(options, sharedPreferences, context);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        notebookRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyView.setImageResource(R.drawable.ic_share);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyViewText.setText("Notes shared with you will appear here.");
                    emptyViewText.setVisibility(View.VISIBLE);
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.moveItem1(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                if (dX < 0) {
                    swipeBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int itemWidth = itemView.getRight() - itemView.getLeft();
                    int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                    int intrinsicHeight = deleteIcon.getIntrinsicWidth();

                    int xMarkLeft = itemView.getLeft() + (((itemWidth - intrinsicWidth) / 2) * 2) - 40;
                    int xMarkRight = xMarkLeft + intrinsicWidth;
                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;

                    deleteIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                }
                swipeBackground.draw(c);
                deleteIcon.draw(c);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                String folderId = "Shared";
                String directory = "Public";
                adapter.moveItem3(viewHolder.getAdapterPosition(), context, folderId, directory);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (dX > 0) {
                    swipeBackground2.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());

                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int itemWidth = itemView.getRight() - itemView.getLeft();
                    int intrinsicWidth = moveIcon.getIntrinsicWidth();
                    int intrinsicHeight = moveIcon.getIntrinsicWidth();

                    int xMarkLeft = itemView.getLeft() + (((itemWidth - intrinsicWidth) / 2) / 8);
                    int xMarkRight = xMarkLeft + intrinsicWidth;
                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;

                    moveIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                }
                swipeBackground2.draw(c);
                moveIcon.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                Note note = documentSnapshot.toObject(Note.class);

                String noteId = documentSnapshot.getId();
                String title = note.getTitle();
                String description = note.getDescription();
                int priority = note.getPriority();
                String fromEmailAddress = note.getFromEmailAddress();
                int revision = note.getRevision();
                String attachmentUrl = note.getAttachmentUrl();
                String attachmentName = note.getAttachmentName();
                String audioDownloadUrl = note.getAudioUrl();
                String audioZipDownloadUrl = note.getAudioZipUrl();
                String audioZipFileName = note.getAudioZipName();

                Intent i = new Intent(context, EditNote.class);
                i.putExtra("noteId", noteId);
                i.putExtra("title", title);
                i.putExtra("description", description);
                i.putExtra("priority", priority);
                i.putExtra("fromEmailAddress", fromEmailAddress);
                i.putExtra("revision", revision);
                i.putExtra("attachmentUrl", attachmentUrl);
                i.putExtra("attachmentName", attachmentName);
                i.putExtra("folderId", "Shared");
                i.putExtra("audioDownloadUrl", audioDownloadUrl);
                i.putExtra("audioZipDownloadUrl", audioZipDownloadUrl);
                i.putExtra("audioZipFileName", audioZipFileName);
                startActivity(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (newNoteOverlay.getVisibility() == View.VISIBLE) {
            newNoteOverlay.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_search) {
            Intent i = new Intent(context, Search.class);
            i.putExtra("searchTerm", "");
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

    @Override
    protected void onRestart() {
        super.onRestart();
        recreate();
    }

    private void loadDataFromRepository() {

        List<NoteEntity> noteList = repository.getAllNotes();

        for (NoteEntity noteEntity : noteList) {

            String searchTitle = noteEntity.getTitle();
            searchSuggestions.add(searchTitle);
        }
    }

    private void getSpeechInput() {

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, SPEECH_INPUT_REQUEST);
        } else {
            Toast.makeText(this, "This device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SPEECH_INPUT_REQUEST && resultCode == RESULT_OK) {

            if (data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                searchField.setText(result.get(0));

                search();
            }
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
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void search() {

        recentSearchesList.clear();
        recentSearchesStringArrayList.clear();

        String searchTerm = searchField.getText().toString().trim().toLowerCase();

        recentSearchesList = repository.getRecentSearches();

        for (RecentSearches recentSearches : recentSearchesList) {
            String recentSearchesListString = recentSearches.getSearchTerm();
            recentSearchesStringArrayList.add(recentSearchesListString);
        }

        if (!recentSearchesStringArrayList.contains(searchTerm) && !searchTerm.equals("")) {
            long timeStamp = System.currentTimeMillis();
            RecentSearches recentSearches = new RecentSearches(timeStamp, searchTerm);
            repository.insert(recentSearches);

        } else if (recentSearchesStringArrayList.contains(searchTerm)) {
            long timeStampQuery = repository.getTimeStamp(searchTerm);
            RecentSearches recentSearchesOld = new RecentSearches(timeStampQuery, searchTerm);
            repository.delete(recentSearchesOld);

            long timeStamp = System.currentTimeMillis();
            RecentSearches recentSearchesNew = new RecentSearches(timeStamp, searchTerm);
            repository.insert(recentSearchesNew);
        }

        Intent i = new Intent(context, Search.class);
        i.putExtra("searchTerm", searchTerm);
        startActivity(i);
    }
}
