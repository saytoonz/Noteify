package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.ImageViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import hotchemi.android.rate.AppRate;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private CollectionAdapter adapter;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private EditText searchField;
    private SwitchCompat switchThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        AppRate.with(this)
                .setInstallDays(7)
                .setLaunchTimes(5)
                .setRemindInterval(2)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        int colorLightThemeText = getResources().getColor(R.color.colorLightThemeText);
        String colorLightThemeTextString = Integer.toString(colorLightThemeText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorLightThemeTextString + "\">" + "Folders" + "</font>"));

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        ImageView navDrawerMenu = findViewById(R.id.navDrawerMenu);
        navDrawerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home.this, NewNotebookNote.class);
                startActivity(i);
            }
        });

        searchField = findViewById(R.id.searchField);
        searchField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            String searchInput = searchField.getText().toString().toLowerCase();
                            Intent i = new Intent(Home.this, FolderSearchResults.class);
                            i.putExtra("searchInput", searchInput);
                            startActivity(i);
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        ImageView searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchInput = searchField.getText().toString().toLowerCase();
                Intent i = new Intent(Home.this, FolderSearchResults.class);
                i.putExtra("searchInput", searchInput);
                startActivity(i);
            }
        });

        TextView newFolderText = findViewById(R.id.new_folder_text);
        newFolderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home.this, NewCollection.class);
                startActivity(i);
            }
        });

        ImageView newFolder = findViewById(R.id.new_folder_image_view);
        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Home.this, NewCollection.class);
                startActivity(i);
            }
        });

        Menu menu = navigationView.getMenu();
        MenuItem menuItem = menu.findItem(R.id.nav_dark);
        View actionView = MenuItemCompat.getActionView(menuItem);

        switchThemes = actionView.findViewById(R.id.drawer_switch);
        switchThemes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreferences();
            }
        });

        switchThemes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);
        switchThemes.setChecked(switchThemesOnOff);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(Home.this, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Folders" + "</font>"));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            searchField.setTextColor(ContextCompat.getColor(Home.this, R.color.colorDarkThemeText));
            searchField.setHintTextColor(ContextCompat.getColor(Home.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(navDrawerMenu, ContextCompat.getColorStateList(this, R.color.colorDarkThemeText));
        }
        setUpRecyclerView();
    }

    public void savePreferences() {

        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();

        if (switchThemes.isChecked()) {
            prefsEditor.putBoolean("switchThemes", true);
        } else {
            prefsEditor.putBoolean("switchThemes", false);
        }
        prefsEditor.apply();
    }

    private void setUpRecyclerView() {

        Context context = this;
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        final Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete_forever);

        CollectionReference folderRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main");
        Query query = folderRef.orderBy("folder", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Collection> options = new FirestoreRecyclerOptions.Builder<Collection>()
                .setQuery(query, Collection.class)
                .build();

        adapter = new CollectionAdapter(options, sharedPreferences, context);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
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

        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                String folderId = documentSnapshot.getId();
                Intent i = new Intent(Home.this, Notes.class);
                i.putExtra("folderId", folderId);
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
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.nav_new_note) {
            Intent i = new Intent(Home.this, NewNotebookNote.class);
            startActivity(i);
        } else if (id == R.id.nav_share) {
            Intent j = new Intent(Home.this, Shared.class);
            startActivity(j);
        } else if (id == R.id.nav_grocery_list) {
            Intent k = new Intent(Home.this, GroceryList.class);
            startActivity(k);
        } else if (id == R.id.nav_bin) {
            Intent l = new Intent(Home.this, Bin.class);
            startActivity(l);
        } else if (id == R.id.nav_settings) {
            Intent m = new Intent(Home.this, Settings.class);
            startActivity(m);
        } else if (id == R.id.nav_account) {
            Intent n = new Intent(Home.this, Account.class);
            startActivity(n);
        } else if (id == R.id.nav_information) {
            Intent o = new Intent(Home.this, Information.class);
            startActivity(o);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
