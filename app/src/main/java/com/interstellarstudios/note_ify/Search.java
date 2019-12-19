package com.interstellarstudios.note_ify;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.KeyEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.interstellarstudios.note_ify.adapters.RecentSearchesAdapter;
import com.interstellarstudios.note_ify.adapters.SearchAdapter;
import com.interstellarstudios.note_ify.database.NoteEntity;
import com.interstellarstudios.note_ify.database.RecentSearches;
import com.interstellarstudios.note_ify.repository.Repository;

import java.util.ArrayList;
import java.util.List;

public class Search extends AppCompatActivity {

    private Context context = this;
    private RecyclerView.Adapter adapter;
    private RecyclerView recyclerView;
    private RecyclerView mRecentSearchesRecyclerView;
    private AutoCompleteTextView searchField;
    private SharedPreferences sharedPreferences;
    private String mSearchTerm;
    private Repository repository;
    private List<RecentSearches> recentSearchesList = new ArrayList<>();
    private ArrayList<String> recentSearchesStringArrayList = new ArrayList<>();
    private ArrayList<String> searchSuggestions = new ArrayList<>();
    private List<NoteEntity> searchNoteList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        repository = new Repository(getApplication());

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView textViewFragmentTitle = findViewById(R.id.text_view_fragment_title);
        textViewFragmentTitle.setText("Search");

        ImageView imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                i.putExtra("noteType", "note");
                startActivity(i);
            }
        });

        searchField = findViewById(R.id.searchField);
        searchField.setText(mSearchTerm);
        searchField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId,
                                          KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
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

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }

            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.colorPrimary));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        loadDataFromRepository();
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
