package com.interstellarstudios.note_ify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.speech.RecognizerIntent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.view.GravityCompat;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.interstellarstudios.note_ify.adapters.RecentSearchesAdapter;
import com.interstellarstudios.note_ify.adapters.SearchAdapter;
import com.interstellarstudios.note_ify.database.NoteEntity;
import com.interstellarstudios.note_ify.database.RecentSearches;
import com.interstellarstudios.note_ify.firestore.GetData;
import com.interstellarstudios.note_ify.repository.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Search extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = this;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView mRecentSearchesRecyclerView;
    private View newNoteOverlay;
    private static final int SPEECH_INPUT_REQUEST = 2;
    private AutoCompleteTextView searchField;
    private SharedPreferences sharedPreferences;
    private String mSearchTerm;
    private Repository repository;
    private List<RecentSearches> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private List<NoteEntity> searchNoteList = new ArrayList<>();
    private String mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        repository = new Repository(getApplication());

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        FirebaseFirestore mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);

        mRecentSearchesRecyclerView = findViewById(R.id.recent_searches_recycler);
        mRecentSearchesRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        mRecentSearchesRecyclerView.setNestedScrollingEnabled(false);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            mSearchTerm = bundle.getString("searchTerm");
        }

        ImageView voiceSearch = findViewById(R.id.voice_search);
        voiceSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSpeechInput();
            }
        });

        TextView textNote = findViewById(R.id.textView_text_note);
        TextView textSpeech = findViewById(R.id.textView_speech);
        TextView textVoice = findViewById(R.id.textView_voice);
        TextView textAttachment = findViewById(R.id.textView_attachment);

        newNoteOverlay = findViewById(R.id.new_note_overlay);
        newNoteOverlay.setVisibility(View.INVISIBLE);
        newNoteOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newNoteOverlay.getVisibility() == View.VISIBLE) {
                    newNoteOverlay.setVisibility(View.INVISIBLE);
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

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView navDrawerMenu = findViewById(R.id.navDrawerMenu);
        navDrawerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(GravityCompat.START);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                newNoteOverlay.setVisibility(View.VISIBLE);
            }
        });

        searchField = findViewById(R.id.searchField);
        searchField.setText(mSearchTerm);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {

                    search();
                    hideKeyboard(Search.this);

                    return true;
                }
                return false;
            }
        });

        ArrayAdapter<String> autocompleteAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_list_item_1, searchSuggestions);
        searchField.setAdapter(autocompleteAdapter);

        SwipeRefreshLayout mSwipeRefreshLayout = findViewById(R.id.swipe);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(true);

                GetData.allNotes(mFireBaseFireStore, mCurrentUserId, repository);

                searchNoteList = repository.searchNotes(mSearchTerm);
                adapter = new SearchAdapter(searchNoteList, sharedPreferences);
                recyclerView.setAdapter(adapter);

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            newNoteOverlay.setBackgroundResource(R.drawable.transparent_overlay_primary_dark);
            textNote.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textSpeech.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textVoice.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            textAttachment.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(navDrawerMenu, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            searchField.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            searchField.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(voiceSearch, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        loadDataFromRepository();
    }

    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (newNoteOverlay.getVisibility() == View.VISIBLE) {
            newNoteOverlay.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_folders) {
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

    private void loadDataFromRepository() {

        searchNoteList = repository.searchNotes(mSearchTerm);
        adapter = new SearchAdapter(searchNoteList, sharedPreferences);
        recyclerView.setAdapter(adapter);

        List<NoteEntity> noteList = repository.getAllNotes();

        for (NoteEntity noteEntity : noteList) {

            String searchTitle = noteEntity.getTitle();
            searchSuggestions.add(searchTitle);
        }

        recentSearchesList = repository.getRecentSearches();

        if (recentSearchesList.isEmpty()) {
            mRecentSearchesRecyclerView.setVisibility(View.GONE);
        }

        mRecentSearchesRecyclerView.setAdapter(new RecentSearchesAdapter(recentSearchesList, sharedPreferences, context, new RecentSearchesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecentSearches item) {
                searchField.setText(item.getSearchTerm());

                mSearchTerm = searchField.getText().toString().trim();

                searchNoteList.clear();
                searchNoteList = repository.searchNotes(mSearchTerm);

                adapter = new SearchAdapter(searchNoteList, sharedPreferences);
                recyclerView.setAdapter(adapter);
            }
        }));
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

    @Override
    protected void onRestart() {
        super.onRestart();
        if (newNoteOverlay.getVisibility() == View.VISIBLE) {
            newNoteOverlay.setVisibility(View.INVISIBLE);
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

    private void search() {

        mSearchTerm = searchField.getText().toString().trim().toLowerCase();

        recentSearchesList.clear();
        recentSearchesStringArrayList.clear();

        recentSearchesList = repository.getRecentSearches();

        for (RecentSearches recentSearches : recentSearchesList) {
            String recentSearchesListString = recentSearches.getSearchTerm();
            recentSearchesStringArrayList.add(recentSearchesListString);
        }

        if (!recentSearchesStringArrayList.contains(mSearchTerm) && !mSearchTerm.equals("")) {
            mRecentSearchesRecyclerView.setVisibility(View.VISIBLE);
            long timeStamp = System.currentTimeMillis();
            RecentSearches recentSearches = new RecentSearches(timeStamp, mSearchTerm);
            repository.insert(recentSearches);

        } else if (recentSearchesStringArrayList.contains(mSearchTerm)) {
            long timeStampQuery = repository.getTimeStamp(mSearchTerm);
            RecentSearches recentSearchesOld = new RecentSearches(timeStampQuery, mSearchTerm);
            repository.delete(recentSearchesOld);

            long timeStamp = System.currentTimeMillis();
            RecentSearches recentSearchesNew = new RecentSearches(timeStamp, mSearchTerm);
            repository.insert(recentSearchesNew);
        }

        searchNoteList.clear();
        searchNoteList = repository.searchNotes(mSearchTerm);

        adapter = new SearchAdapter(searchNoteList, sharedPreferences);
        recyclerView.setAdapter(adapter);

        List<RecentSearches> recentSearchesList = repository.getRecentSearches();

        if (recentSearchesList.isEmpty()) {
            mRecentSearchesRecyclerView.setVisibility(View.GONE);
        }

        mRecentSearchesRecyclerView.setAdapter(new RecentSearchesAdapter(recentSearchesList, sharedPreferences, context, new RecentSearchesAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(RecentSearches item) {
                searchField.setText(item.getSearchTerm());

                mSearchTerm = searchField.getText().toString().trim();

                searchNoteList.clear();
                searchNoteList = repository.searchNotes(mSearchTerm);

                adapter = new SearchAdapter(searchNoteList, sharedPreferences);
                recyclerView.setAdapter(adapter);
            }
        }));
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
