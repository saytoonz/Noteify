package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.MenuItemCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.CompoundButton;
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
import es.dmoral.toasty.Toasty;

public class Bin extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context context = this;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private NoteAdapter adapter;
    private EditText searchField;
    private ImageView emptyView;
    private TextView emptyViewText;
    private SwitchCompat switchThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        int colorLightThemeText = getResources().getColor(R.color.colorLightThemeText);
        String colorLightThemeTextString = Integer.toString(colorLightThemeText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorLightThemeTextString + "\">" + "Bin" + "</font>"));

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", "Notebook");
                startActivity(i);
            }
        });

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

        searchField = findViewById(R.id.searchField);
        searchField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            String searchInput = searchField.getText().toString().toLowerCase();
                            Intent i = new Intent(context, BinSearchResults.class);
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
                Intent i = new Intent(context, BinSearchResults.class);
                i.putExtra("searchInput", searchInput);
                startActivity(i);
            }
        });

        emptyView = findViewById(R.id.emptyView);
        emptyViewText = findViewById(R.id.emptyViewText);

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
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Bin" + "</font>"));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            searchField.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            searchField.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(searchField.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(navDrawerMenu, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            emptyViewText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
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

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        final Drawable swipeBackground2 = new ColorDrawable(Color.parseColor("#3bbe19"));
        final Drawable deleteForeverIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_forever);
        final Drawable restoreIcon = ContextCompat.getDrawable(context, R.drawable.ic_restore);

        CollectionReference notebookRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin");
        Query query = notebookRef.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new NoteAdapter(options, sharedPreferences, context);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        notebookRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if(queryDocumentSnapshots.isEmpty()){
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyView.setVisibility(View.VISIBLE);
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
                adapter.deleteItem(viewHolder.getAdapterPosition());

            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (dX < 0) {
                    swipeBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int itemWidth = itemView.getRight() - itemView.getLeft();
                    int intrinsicWidth = deleteForeverIcon.getIntrinsicWidth();
                    int intrinsicHeight = deleteForeverIcon.getIntrinsicWidth();

                    int xMarkLeft = itemView.getLeft() + (((itemWidth - intrinsicWidth) / 2) * 2) - 40;
                    int xMarkRight = xMarkLeft + intrinsicWidth;
                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;

                    deleteForeverIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                }
                //Draw
                swipeBackground.draw(c);
                deleteForeverIcon.draw(c);

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
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.moveItem2(viewHolder.getAdapterPosition());
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (dX > 0) {
                    swipeBackground2.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());

                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int itemWidth = itemView.getRight() - itemView.getLeft();
                    int intrinsicWidth = restoreIcon.getIntrinsicWidth();
                    int intrinsicHeight = restoreIcon.getIntrinsicWidth();

                    int xMarkLeft = itemView.getLeft() + (((itemWidth - intrinsicWidth) / 2) / 8);
                    int xMarkRight = xMarkLeft + intrinsicWidth;
                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;

                    restoreIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                }
                swipeBackground2.draw(c);
                restoreIcon.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Toasty.info(context, "Restore a Note to edit it", Toast.LENGTH_LONG, true).show();
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
        } else if (id == R.id.nav_dark) {

        } else if (id == R.id.nav_settings) {
            Intent m = new Intent(context, Settings.class);
            startActivity(m);
        } else if (id == R.id.nav_account) {
            Intent n = new Intent(context, Account.class);
            startActivity(n);
        } else if (id == R.id.nav_information) {
            Intent o = new Intent(context, Information.class);
            startActivity(o);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
