package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.MenuItemCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.text.Html;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import androidx.core.view.GravityCompat;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import es.dmoral.toasty.Toasty;

public class GroceryList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Context mContext = this;
    private ArrayList<String> groceryArrayList = new ArrayList<>();
    private EditText mEditTextName;
    private TextView mTextViewAmount;
    private int mAmount = 0;
    private SwitchCompat switchThemes;
    private EditText mSharedUserEmailText;
    private FirebaseFirestore mFireBaseFireStore;
    private String mSharedUserId;
    private String sharedUserEmail;
    private String currentUserEmail;
    private String mCurrentUserID;
    private GroceryListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserID = mFireBaseAuth.getCurrentUser().getUid();
            FirebaseUser mUser = mFireBaseAuth.getCurrentUser();
            currentUserEmail = mUser.getEmail();
        }

        int colorLightThemeText = getResources().getColor(R.color.colorLightThemeText);
        String colorLightThemeTextString = Integer.toString(colorLightThemeText);
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorLightThemeTextString + "\">" + "Grocery List" + "</font>"));

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.success(GroceryList.this, "Swipe left or right to remove an item", Toast.LENGTH_LONG, true).show();
            }
        });

        ImageView navDrawerMenu = findViewById(R.id.navDrawerMenu);
        navDrawerMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

        mEditTextName = findViewById(R.id.grocery_item);
        mTextViewAmount = findViewById(R.id.grocery_amount);
        mSharedUserEmailText = findViewById(R.id.sharedUserEmail);

        ImageView shareList = findViewById(R.id.shareIcon);
        shareList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareGroceryList();
            }
        });

        mSharedUserEmailText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            shareGroceryList();
                            Toasty.success(GroceryList.this, "Grocery list shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        Button buttonIncrease = findViewById(R.id.button_increase);
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increase();
            }
        });

        Button buttonDecrease = findViewById(R.id.button_decrease);
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrease();
            }
        });

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
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

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);
        switchThemes.setChecked(switchThemesOnOff);

        if (switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(GroceryList.this, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Grocery List" + "</font>"));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            mEditTextName.setTextColor(ContextCompat.getColor(GroceryList.this, R.color.colorDarkThemeText));
            mEditTextName.setHintTextColor(ContextCompat.getColor(GroceryList.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mEditTextName.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            mTextViewAmount.setTextColor(ContextCompat.getColor(GroceryList.this, R.color.colorDarkThemeText));
            buttonIncrease.setTextColor(ContextCompat.getColor(GroceryList.this, R.color.colorLightThemeText));
            buttonIncrease.setBackgroundColor(ContextCompat.getColor(GroceryList.this, R.color.colorDarkThemeText));
            buttonDecrease.setTextColor(ContextCompat.getColor(GroceryList.this, R.color.colorLightThemeText));
            buttonDecrease.setBackgroundColor(ContextCompat.getColor(GroceryList.this, R.color.colorDarkThemeText));
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

    private void increase() {
        mAmount++;
        mTextViewAmount.setText(String.valueOf(mAmount));
    }

    private void decrease() {
        if (mAmount > 0) {
            mAmount--;
            mTextViewAmount.setText(String.valueOf(mAmount));
        }
    }

    private void addItem() {

        if (mEditTextName.getText().toString().trim().length() == 0 || mAmount == 0) {
            return;
        }

        String name = mEditTextName.getText().toString();
        String amount = mTextViewAmount.getText().toString();
        String newItem = amount + " " + name;

        String randomId = UUID.randomUUID().toString();
        String groceryDocumentString = "item " + randomId;

        DocumentReference groceryListPath = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document("Grocery_List").collection("Grocery_List").document(groceryDocumentString);
        groceryListPath.set(new GroceryItem(newItem));

        recreate();
        mEditTextName.getText().clear();
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
            Intent i = new Intent(GroceryList.this, NewNotebookNote.class);
            startActivity(i);
        } else if (id == R.id.nav_folders) {
            Intent j = new Intent(GroceryList.this, Home.class);
            startActivity(j);
        } else if (id == R.id.nav_share) {
            Intent j = new Intent(GroceryList.this, Shared.class);
            startActivity(j);
        } else if (id == R.id.nav_shared_grocery_list) {
            Intent k = new Intent(GroceryList.this, SharedGroceryList.class);
            startActivity(k);
        } else if (id == R.id.nav_bin) {
            Intent l = new Intent(GroceryList.this, Bin.class);
            startActivity(l);
        } else if (id == R.id.nav_dark) {

        } else if (id == R.id.nav_settings) {
            Intent m = new Intent(GroceryList.this, Settings.class);
            startActivity(m);
        } else if (id == R.id.nav_account) {
            Intent n = new Intent(GroceryList.this, Account.class);
            startActivity(n);
        } else if (id == R.id.nav_information) {
            Intent o = new Intent(GroceryList.this, Information.class);
            startActivity(o);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpRecyclerView() {

        Context context = this;
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final CollectionReference groceryListRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document("Grocery_List").collection("Grocery_List");
        Query query = groceryListRef.orderBy("item", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<GroceryItem> options = new FirestoreRecyclerOptions.Builder<GroceryItem>()
                .setQuery(query, GroceryItem.class)
                .build();

        adapter = new GroceryListAdapter(options, sharedPreferences, context);

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
        }).attachToRecyclerView(recyclerView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
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

    private void shareGroceryList() {

        sharedUserEmail = mSharedUserEmailText.getText().toString().trim();

        if (sharedUserEmail.trim().isEmpty()) {
            return;
        }

        mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document("Grocery_List").collection("Grocery_List")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            groceryArrayList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                GroceryItem groceryItem = document.toObject(GroceryItem.class);
                                String setItem = groceryItem.getItem();
                                groceryArrayList.add(setItem);
                            }
                            if(groceryArrayList.isEmpty()) {
                                Toasty.info(GroceryList.this, "This grocery list is empty", Toast.LENGTH_LONG, true).show();
                            } else {
                                SendMailGrocery.sendMail(mContext, sharedUserEmail, currentUserEmail, groceryArrayList);
                                Toasty.success(GroceryList.this, "Grocery List shared with and emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                            }
                        }
                    }
                });

        DocumentReference userDetailsRef = mFireBaseFireStore.collection("User_List").document(sharedUserEmail);
        userDetailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        UserDetailsModel userDetails = document.toObject(UserDetailsModel.class);
                        mSharedUserId = userDetails.getUserId();

                        mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List")
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {

                                                String documentId = document.getId();
                                                mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(documentId).delete();
                                            }

                                            mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document("Grocery_List").collection("Grocery_List")
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {

                                                                groceryArrayList.clear();

                                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                                    String documentId = document.getId();

                                                                    GroceryItem groceryItem = document.toObject(GroceryItem.class);
                                                                    String setItem = groceryItem.getItem();
                                                                    groceryArrayList.add(setItem);

                                                                    DocumentReference groceryListPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(documentId);
                                                                    groceryListPath.set(new GroceryItem(setItem));
                                                                }
                                                            }
                                                        }
                                                    });
                                        }
                                    }
                                });

                        Map<String, Object> notificationMessage = new HashMap<>();
                        notificationMessage.put("from", currentUserEmail);
                        CollectionReference notificationPath = mFireBaseFireStore.collection("Users").document(mSharedUserId).collection("Public").document("Grocery_Notifications").collection("Grocery_Notifications");
                        notificationPath.add(notificationMessage);
                    }
                }
            }
        });
    }
}
