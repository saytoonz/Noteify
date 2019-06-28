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
import com.sendgrid.SendGrid;
import com.sendgrid.SendGridException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import es.dmoral.toasty.Toasty;

public class SharedGroceryList extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

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
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorLightThemeTextString + "\">" + "Shared Grocery List" + "</font>"));

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.drawer_view);
        navigationView.setNavigationItemSelectedListener(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.success(SharedGroceryList.this, "Swipe left or right to remove an item", Toast.LENGTH_LONG, true).show();
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
                Toasty.success(SharedGroceryList.this, "Grocery list shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
            }
        });

        mSharedUserEmailText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            shareGroceryList();
                            Toasty.success(SharedGroceryList.this, "Grocery list shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
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
            layout.setBackgroundColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorDarkThemeText));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Shared Grocery List" + "</font>"));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(this, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            mEditTextName.setTextColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorDarkThemeText));
            mEditTextName.setHintTextColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mEditTextName.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            mTextViewAmount.setTextColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorDarkThemeText));
            buttonIncrease.setTextColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorLightThemeText));
            buttonIncrease.setBackgroundColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorDarkThemeText));
            buttonDecrease.setTextColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorLightThemeText));
            buttonDecrease.setBackgroundColor(ContextCompat.getColor(SharedGroceryList.this, R.color.colorDarkThemeText));
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

        DocumentReference groceryListPath = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List").document(groceryDocumentString);
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
            Intent i = new Intent(SharedGroceryList.this, NewNotebookNote.class);
            startActivity(i);
        } else if (id == R.id.nav_folders) {
            Intent j = new Intent(SharedGroceryList.this, Home.class);
            startActivity(j);
        } else if (id == R.id.nav_share) {
            Intent j = new Intent(SharedGroceryList.this, Shared.class);
            startActivity(j);
        } else if (id == R.id.nav_grocery_list) {
            Intent k = new Intent(SharedGroceryList.this, GroceryList.class);
            startActivity(k);
        } else if (id == R.id.nav_bin) {
            Intent l = new Intent(SharedGroceryList.this, Bin.class);
            startActivity(l);
        } else if (id == R.id.nav_dark) {

        } else if (id == R.id.nav_settings) {
            Intent m = new Intent(SharedGroceryList.this, Settings.class);
            startActivity(m);
        } else if (id == R.id.nav_account) {
            Intent n = new Intent(SharedGroceryList.this, Account.class);
            startActivity(n);
        } else if (id == R.id.nav_information) {
            Intent o = new Intent(SharedGroceryList.this, Information.class);
            startActivity(o);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setUpRecyclerView() {

        Context context = this;
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final CollectionReference groceryListRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List");
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

                                            mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Public").document("Shared_Grocery_List").collection("Shared_Grocery_List")
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

                                                                sendMail();
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

    private void sendMail() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SendGrid sendgrid = new SendGrid("API KEY GOES HERE");

                    SendGrid.Email email = new SendGrid.Email();

                    email.addTo(sharedUserEmail);
                    email.setFrom("note-ify@interstellarstudios.co.uk");
                    email.setFromName("Note-ify");
                    email.setSubject("You've received a Grocery List from " + currentUserEmail);
                    email.setHtml("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\" xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:o=\"urn:schemas-microsoft-com:office:office\"><head><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\" /><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" /><meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" /><meta name=\"x-apple-disable-message-reformatting\" /><meta name=\"apple-mobile-web-app-capable\" content=\"yes\" /><meta name=\"apple-mobile-web-app-status-bar-style\" content=\"black\" /><meta name=\"format-detection\" content=\"telephone=no\" /><title></title><style type=\"text/css\">\n" +
                            "        /* Resets */\n" +
                            "        .ReadMsgBody { width: 100%; background-color: #ebebeb;}\n" +
                            "        .ExternalClass {width: 100%; background-color: #ebebeb;}\n" +
                            "        .ExternalClass, .ExternalClass p, .ExternalClass span, .ExternalClass font, .ExternalClass td, .ExternalClass div {line-height:100%;}\n" +
                            "        a[x-apple-data-detectors]{\n" +
                            "            color:inherit !important;\n" +
                            "            text-decoration:none !important;\n" +
                            "            font-size:inherit !important;\n" +
                            "            font-family:inherit !important;\n" +
                            "            font-weight:inherit !important;\n" +
                            "            line-height:inherit !important;\n" +
                            "        }        \n" +
                            "        body {-webkit-text-size-adjust:none; -ms-text-size-adjust:none;}\n" +
                            "        body {margin:0; padding:0;}\n" +
                            "        .yshortcuts a {border-bottom: none !important;}\n" +
                            "        .rnb-del-min-width{ min-width: 0 !important; }\n" +
                            "\n" +
                            "        /* Add new outlook css start */\n" +
                            "        .templateContainer{\n" +
                            "            max-width:590px !important;\n" +
                            "            width:auto !important;\n" +
                            "        }\n" +
                            "        /* Add new outlook css end */\n" +
                            "\n" +
                            "        /* Image width by default for 3 columns */\n" +
                            "        img[class=\"rnb-col-3-img\"] {\n" +
                            "        max-width:170px;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* Image width by default for 2 columns */\n" +
                            "        img[class=\"rnb-col-2-img\"] {\n" +
                            "        max-width:264px;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* Image width by default for 2 columns aside small size */\n" +
                            "        img[class=\"rnb-col-2-img-side-xs\"] {\n" +
                            "        max-width:180px;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* Image width by default for 2 columns aside big size */\n" +
                            "        img[class=\"rnb-col-2-img-side-xl\"] {\n" +
                            "        max-width:350px;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* Image width by default for 1 column */\n" +
                            "        img[class=\"rnb-col-1-img\"] {\n" +
                            "        max-width:550px;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* Image width by default for header */\n" +
                            "        img[class=\"rnb-header-img\"] {\n" +
                            "        max-width:590px;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* Ckeditor line-height spacing */\n" +
                            "        .rnb-force-col p, ul, ol{margin:0px!important;}\n" +
                            "        .rnb-del-min-width p, ul, ol{margin:0px!important;}\n" +
                            "\n" +
                            "        /* tmpl-2 preview */\n" +
                            "        .rnb-tmpl-width{ width:100%!important;}\n" +
                            "\n" +
                            "        /* tmpl-11 preview */\n" +
                            "        .rnb-social-width{padding-right:15px!important;}\n" +
                            "\n" +
                            "        /* tmpl-11 preview */\n" +
                            "        .rnb-social-align{float:right!important;}\n" +
                            "\n" +
                            "        /* Ul Li outlook extra spacing fix */\n" +
                            "        li{mso-margin-top-alt: 0; mso-margin-bottom-alt: 0;}        \n" +
                            "\n" +
                            "        /* Outlook fix */\n" +
                            "        table {mso-table-lspace:0pt; mso-table-rspace:0pt;}\n" +
                            "    \n" +
                            "        /* Outlook fix */\n" +
                            "        table, tr, td {border-collapse: collapse;}\n" +
                            "\n" +
                            "        /* Outlook fix */\n" +
                            "        p,a,li,blockquote {mso-line-height-rule:exactly;} \n" +
                            "\n" +
                            "        /* Outlook fix */\n" +
                            "        .msib-right-img { mso-padding-alt: 0 !important;}\n" +
                            "\n" +
                            "        @media only screen and (min-width:590px){\n" +
                            "        /* mac fix width */\n" +
                            "        .templateContainer{width:590px !important;}\n" +
                            "        }\n" +
                            "\n" +
                            "        @media screen and (max-width: 360px){\n" +
                            "        /* yahoo app fix width \"tmpl-2 tmpl-10 tmpl-13\" in android devices */\n" +
                            "        .rnb-yahoo-width{ width:360px !important;}\n" +
                            "        }\n" +
                            "\n" +
                            "        @media screen and (max-width: 380px){\n" +
                            "        /* fix width and font size \"tmpl-4 tmpl-6\" in mobile preview */\n" +
                            "        .element-img-text{ font-size:24px !important;}\n" +
                            "        .element-img-text2{ width:230px !important;}\n" +
                            "        .content-img-text-tmpl-6{ font-size:24px !important;}\n" +
                            "        .content-img-text2-tmpl-6{ width:220px !important;}\n" +
                            "        }\n" +
                            "\n" +
                            "        @media screen and (max-width: 480px) {\n" +
                            "        td[class=\"rnb-container-padding\"] {\n" +
                            "        padding-left: 10px !important;\n" +
                            "        padding-right: 10px !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* force container nav to (horizontal) blocks */\n" +
                            "        td.rnb-force-nav {\n" +
                            "        display: inherit;\n" +
                            "        }\n" +
                            "        }\n" +
                            "\n" +
                            "        @media only screen and (max-width: 600px) {\n" +
                            "\n" +
                            "        /* center the address &amp; social icons */\n" +
                            "        .rnb-text-center {text-align:center !important;}\n" +
                            "\n" +
                            "        /* force container columns to (horizontal) blocks */\n" +
                            "        td.rnb-force-col {\n" +
                            "        display: block;\n" +
                            "        padding-right: 0 !important;\n" +
                            "        padding-left: 0 !important;\n" +
                            "        width:100%;\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-container {\n" +
                            "         width: 100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-btn-col-content {\n" +
                            "        width: 100% !important;\n" +
                            "        }\n" +
                            "        table.rnb-col-3 {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "\n" +
                            "        /* change left/right padding and margins to top/bottom ones */\n" +
                            "        margin-bottom: 10px;\n" +
                            "        padding-bottom: 10px;\n" +
                            "        /*border-bottom: 1px solid #eee;*/\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-last-col-3 {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        table[class~=\"rnb-col-2\"] {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "\n" +
                            "        /* change left/right padding and margins to top/bottom ones */\n" +
                            "        margin-bottom: 10px;\n" +
                            "        padding-bottom: 10px;\n" +
                            "        /*border-bottom: 1px solid #eee;*/\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-col-2-noborder-onright {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "\n" +
                            "        /* change left/right padding and margins to top/bottom ones */\n" +
                            "        margin-bottom: 10px;\n" +
                            "        padding-bottom: 10px;\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-col-2-noborder-onleft {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "\n" +
                            "        /* change left/right padding and margins to top/bottom ones */\n" +
                            "        margin-top: 10px;\n" +
                            "        padding-top: 10px;\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-last-col-2 {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        table.rnb-col-1 {\n" +
                            "        /* unset table align=\"left/right\" */\n" +
                            "        float: none !important;\n" +
                            "        width: 100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-col-3-img {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-col-2-img {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-col-2-img-side-xs {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-col-2-img-side-xl {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-col-1-img {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-header-img {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        margin:0 auto;\n" +
                            "        }\n" +
                            "\n" +
                            "        img.rnb-logo-img {\n" +
                            "        /**max-width:none !important;**/\n" +
                            "        width:100% !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        td.rnb-mbl-float-none {\n" +
                            "        float:inherit !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        .img-block-center{text-align:center !important;}\n" +
                            "\n" +
                            "        .logo-img-center\n" +
                            "        {\n" +
                            "            float:inherit !important;\n" +
                            "        }\n" +
                            "\n" +
                            "        /* tmpl-11 preview */\n" +
                            "        .rnb-social-align{margin:0 auto !important; float:inherit !important;}\n" +
                            "\n" +
                            "        /* tmpl-11 preview */\n" +
                            "        .rnb-social-center{display:inline-block;}\n" +
                            "\n" +
                            "        /* tmpl-11 preview */\n" +
                            "        .social-text-spacing{margin-bottom:0px !important; padding-bottom:0px !important;}\n" +
                            "\n" +
                            "        /* tmpl-11 preview */\n" +
                            "        .social-text-spacing2{padding-top:15px !important;}\n" +
                            "\n" +
                            "    }</style><!--[if gte mso 11]><style type=\"text/css\">table{border-spacing: 0; }table td {border-collapse: separate;}</style><![endif]--><!--[if !mso]><!--><style type=\"text/css\">table{border-spacing: 0;} table td {border-collapse: collapse;}</style> <!--<![endif]--><!--[if gte mso 15]><xml><o:OfficeDocumentSettings><o:AllowPNG/><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]--><!--[if gte mso 9]><xml><o:OfficeDocumentSettings><o:AllowPNG/><o:PixelsPerInch>96</o:PixelsPerInch></o:OfficeDocumentSettings></xml><![endif]--></head><body>\n" +
                            "\n" +
                            "<table border=\"0\" align=\"center\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" class=\"main-template\" bgcolor=\"#f9fafc\" style=\"background-color: rgb(249, 250, 252);\">\n" +
                            "\n" +
                            "    <tbody><tr style=\"display:none !important; font-size:1px; mso-hide: all;\"><td></td><td></td></tr><tr>\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "        <!--[if gte mso 9]>\n" +
                            "                        <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"590\" style=\"width:590px;\">\n" +
                            "                        <tr>\n" +
                            "                        <td align=\"center\" valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                        <![endif]-->\n" +
                            "            <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\" class=\"templateContainer\" style=\"max-width:590px!important; width: 590px;\">\n" +
                            "        <tbody><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:590px;\" name=\"Layout_5078\" id=\"Layout_5078\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" valign=\"top\" align=\"center\" style=\"min-width:590px;\">\n" +
                            "                        <table width=\"100%\" cellpadding=\"0\" border=\"0\" height=\"38\" cellspacing=\"0\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td valign=\"top\" height=\"38\">\n" +
                            "                                    <img width=\"20\" height=\"38\" style=\"display:block; max-height:38px; max-width:20px;\" alt=\"\" src=\"http://img.mailinblue.com/new_images/rnb/rnb_space.gif\">\n" +
                            "                                </td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table>\n" +
                            "            </td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                            "                <tr>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                <![endif]-->\n" +
                            "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:590px;\" name=\"Layout_8\" id=\"Layout_8\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\" style=\"min-width:590px;\">\n" +
                            "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"background-color: rgb(255, 255, 255); border-radius: 0px; padding-left: 20px; padding-right: 20px; border-collapse: separate;\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "                                    <table width=\"100%\" cellpadding=\"0\" border=\"0\" align=\"center\" cellspacing=\"0\">\n" +
                            "                                        <tbody><tr>\n" +
                            "                                            <td valign=\"top\" align=\"center\">\n" +
                            "                                                <table cellpadding=\"0\" border=\"0\" align=\"center\" cellspacing=\"0\" class=\"logo-img-center\"> \n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td valign=\"middle\" align=\"center\" style=\"line-height: 1px;\">\n" +
                            "                                                            <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block; \" cellspacing=\"0\" cellpadding=\"0\" border=\"0\"><div><img width=\"550\" vspace=\"0\" hspace=\"0\" border=\"0\" alt=\"Note-ify\" style=\"float: left;max-width:550px;display:block;\" class=\"rnb-logo-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c2802ec31b92f71e41dd1de.jpg\"></div></div></td>\n" +
                            "                                                    </tr>\n" +
                            "                                                </tbody></table>\n" +
                            "                                                </td>\n" +
                            "                                        </tr>\n" +
                            "                                    </tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table>\n" +
                            "            <!--[if mso]>\n" +
                            "                </td>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                </tr>\n" +
                            "                </table>\n" +
                            "                <![endif]-->\n" +
                            "            \n" +
                            "        </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                            "            \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                            "                <tr>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                <![endif]-->\n" +
                            "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_7\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                            "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"background-color: rgb(255, 255, 255); padding-left: 20px; padding-right: 20px; border-collapse: separate; border-radius: 0px; border-bottom: 0px none rgb(200, 200, 200);\">\n" +
                            "\n" +
                            "                                        <tbody><tr>\n" +
                            "                                            <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                        </tr>\n" +
                            "                                        <tr>\n" +
                            "                                            <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "\n" +
                            "                                                <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td class=\"rnb-force-col\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                            "\n" +
                            "                                                            <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" align=\"left\" class=\"rnb-col-1\">\n" +
                            "\n" +
                            "                                                                <tbody><tr>\n" +
                            "                                                                    <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\"><div><strong>Shared Grocery List</strong></div>\n" +
                            "\n" +
                            "<div><br>\n" +
                            "" + groceryArrayList + "</div>\n" +
                            "</td>\n" +
                            "                                                                </tr>\n" +
                            "                                                                </tbody></table>\n" +
                            "\n" +
                            "                                                            </td></tr>\n" +
                            "                                                </tbody></table></td>\n" +
                            "                                        </tr>\n" +
                            "                                        <tr>\n" +
                            "                                            <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                        </tr>\n" +
                            "                                    </tbody></table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table><!--[if mso]>\n" +
                            "                </td>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                </tr>\n" +
                            "                </table>\n" +
                            "                <![endif]-->\n" +
                            "\n" +
                            "            </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                            "                <tr>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                <![endif]-->\n" +
                            "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_6\" id=\"Layout_6\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                            "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding-left: 20px; padding-right: 20px;\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "\n" +
                            "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                            "                                        <tbody><tr>\n" +
                            "                                            <td class=\"rnb-force-col\" width=\"550\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                            "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-1\" width=\"550\">\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"center\">\n" +
                            "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                                <tbody>\n" +
                            "                                                                    <tr>\n" +
                            "                                                                        <td width=\"100%\" valign=\"top\" align=\"center\" class=\"img-block-center\">\n" +
                            "\n" +
                            "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                                            <tbody><tr>\n" +
                            "                                                                                <td>\n" +
                            "                                                                        <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\">\n" +
                            "                                                                            <div><a target=\"_blank\" href=\"https://play.google.com/store/apps/details?id=com.interstellarstudios.note_ify\">\n" +
                            "                                                                            <img ng-if=\"col.img.source != 'url'\" width=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-1-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c27674ccf29bcec2a435996.png\" style=\"vertical-align: top; max-width: 200px; float: left;\"></a></div><div style=\"clear:both;\"></div>\n" +
                            "                                                                            </div></td>\n" +
                            "                                                                            </tr>\n" +
                            "                                                                        </tbody></table>\n" +
                            "\n" +
                            "                                                                    </td>\n" +
                            "                                                                    </tr>\n" +
                            "                                                                </tbody>\n" +
                            "                                                                </table></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                            "                                                            <div><div style=\"text-align: center;\">Download the free App now.</div>\n" +
                            "</div>\n" +
                            "                                                        </td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "\n" +
                            "                                                </td></tr>\n" +
                            "                                    </tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table><!--[if mso]>\n" +
                            "                </td>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                </tr>\n" +
                            "                </table>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "            </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                            "                <tr>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                <![endif]-->\n" +
                            "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_5\" id=\"Layout_5\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                            "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding-left: 20px; padding-right: 20px;\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "\n" +
                            "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                            "                                        <tbody><tr>\n" +
                            "                                            <td class=\"rnb-force-col\" width=\"263\" valign=\"top\" style=\"padding-right: 20px;\">\n" +
                            "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-2\" width=\"263\">\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                            "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                            <tbody>\n" +
                            "                                                                <tr>\n" +
                            "                                                                    <td width=\"100%\" valign=\"top\" align=\"left\" class=\"img-block-center\">\n" +
                            "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                                            <tbody><tr>\n" +
                            "                                                                                <td>\n" +
                            "                                                                                    <div style=\"border-top:1px Solid #9c9c9c;border-right:1px Solid #9c9c9c;border-bottom:1px Solid #9c9c9c;border-left:1px Solid #9c9c9c;display:inline-block;\"><div><img border=\"0\" width=\"263\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-2-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c4392438696e366516c5d85.jpg\" style=\"vertical-align: top; max-width: 300px; float: left;\"></div><div style=\"clear:both;\"></div>\n" +
                            "                                                                                    </div>\n" +
                            "                                                                            </td>\n" +
                            "                                                                            </tr>\n" +
                            "                                                                        </tbody></table>\n" +
                            "\n" +
                            "                                                                    </td>\n" +
                            "                                                                </tr>\n" +
                            "                                                            </tbody>\n" +
                            "                                                        </table></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                            "                                                            <div><div>All of your notes automatically synced to the Cloud. Stored securely with Google Firebase.</div>\n" +
                            "</div>\n" +
                            "                                                        </td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "\n" +
                            "                                                </td><td class=\"rnb-force-col\" width=\"263\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                            "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-last-col-2\" width=\"263\">\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                            "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                            <tbody>\n" +
                            "                                                                <tr>\n" +
                            "                                                                    <td width=\"100%\" valign=\"top\" align=\"left\" class=\"img-block-center\">\n" +
                            "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                                            <tbody><tr>\n" +
                            "                                                                                <td>\n" +
                            "                                                                                    <div style=\"border-top:1px Solid #9c9c9c;border-right:1px Solid #9c9c9c;border-bottom:1px Solid #9c9c9c;border-left:1px Solid #9c9c9c;display:inline-block;\"><div><img border=\"0\" width=\"263\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-2-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c4392438696e3662461432d.jpg\" style=\"vertical-align: top; max-width: 300px; float: left;\"></div><div style=\"clear:both;\"></div>\n" +
                            "                                                                                    </div>\n" +
                            "                                                                            </td>\n" +
                            "                                                                            </tr>\n" +
                            "                                                                        </tbody></table>\n" +
                            "\n" +
                            "                                                                    </td>\n" +
                            "                                                                </tr>\n" +
                            "                                                            </tbody>\n" +
                            "                                                        </table></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                            "                                                            <div><div>All of your notes on all of your devices. Share documents instantly via email and device-to-device.</div>\n" +
                            "</div>\n" +
                            "                                                        </td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "\n" +
                            "                                                </td></tr>\n" +
                            "                                    </tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table><!--[if mso]>\n" +
                            "                </td>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                </tr>\n" +
                            "                </table>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "            </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                            "                \n" +
                            "                <!--[if mso 15]>\n" +
                            "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                            "                <tr>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso 15]>\n" +
                            "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                <![endif]-->\n" +
                            "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_11\" id=\"Layout_11\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                            "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding: 20px;\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "\n" +
                            "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                            "                                        <tbody><tr>\n" +
                            "\n" +
                            "                                            <td class=\"rnb-force-col img-block-center\" valign=\"top\" width=\"180\" style=\"padding-right: 20px;\">\n" +
                            "\n" +
                            "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-2-noborder-onright\" width=\"180\">\n" +
                            "\n" +
                            "\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td width=\"100%\" style=\"line-height: 1px;\" class=\"img-block-center\" valign=\"top\" align=\"left\">\n" +
                            "                                                            <div style=\"border-top:0px none #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\"><div><a target=\"_blank\" href=\"https://noteify.interstellarstudios.co.uk\"><img ng-if=\"col.img.source != 'url'\" alt=\"\" border=\"0\" hspace=\"0\" vspace=\"0\" width=\"180\" style=\"vertical-align:top; float: left; max-width:270px !important; \" class=\"rnb-col-2-img-side-xl\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5c4b80730d48fbeb3c5c753d.png\"></a></div><div style=\"clear:both;\"></div></div></td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "                                                </td><td class=\"rnb-force-col\" valign=\"top\">\n" +
                            "\n" +
                            "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" width=\"350\" align=\"left\" class=\"rnb-last-col-2\">\n" +
                            "\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td style=\"font-size:24px; font-family:Arial,Helvetica,sans-serif; color:#3c4858; text-align:left;\">\n" +
                            "                                                            <span style=\"color:#3c4858; \"><strong><span style=\"font-size:18px;\">Website</span></strong></span></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td class=\"rnb-mbl-float-none\" style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif;color:#3c4858;float:right;width:350px; line-height: 21px;\"><div>Need some information? Check out our website:&nbsp;<a href=\"https://noteify.interstellarstudios.co.uk/\" style=\"text-decoration: underline; color: rgb(52, 153, 219);\">https://noteify.interstellarstudios.co.uk\u200B</a></div>\n" +
                            "</td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "                                                </td>\n" +
                            "\n" +
                            "                                            </tr></tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table>\n" +
                            "            <!--[if mso 15]>\n" +
                            "                </td>\n" +
                            "                <![endif]-->\n" +
                            "\n" +
                            "                <!--[if mso 15]>\n" +
                            "                </tr>\n" +
                            "                </table>\n" +
                            "                <![endif]-->\n" +
                            "            \n" +
                            "        </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <div style=\"background-color: rgb(255, 255, 255);\">\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <table align=\"center\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\" width=\"100%\" style=\"width:100%;\">\n" +
                            "                <tr>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                <td valign=\"top\" width=\"590\" style=\"width:590px;\">\n" +
                            "                <![endif]-->\n" +
                            "                <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:100%;\" name=\"Layout_12\" id=\"Layout_12\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" align=\"center\" valign=\"top\">\n" +
                            "                        <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-container\" bgcolor=\"#ffffff\" style=\"max-width: 100%; min-width: 100%; table-layout: fixed; background-color: rgb(255, 255, 255); border-radius: 0px; border-collapse: separate; padding-left: 20px; padding-right: 20px;\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td valign=\"top\" class=\"rnb-container-padding\" align=\"left\">\n" +
                            "\n" +
                            "                                    <table width=\"100%\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" class=\"rnb-columns-container\">\n" +
                            "                                        <tbody><tr>\n" +
                            "                                            <td class=\"rnb-force-col\" width=\"550\" valign=\"top\" style=\"padding-right: 0px;\">\n" +
                            "                                                <table border=\"0\" valign=\"top\" cellspacing=\"0\" cellpadding=\"0\" align=\"left\" class=\"rnb-col-1\" width=\"550\">\n" +
                            "                                                    <tbody><tr>\n" +
                            "                                                        <td width=\"100%\" class=\"img-block-center\" valign=\"top\" align=\"center\">\n" +
                            "                                                            <table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                                <tbody>\n" +
                            "                                                                    <tr>\n" +
                            "                                                                        <td width=\"100%\" valign=\"top\" align=\"center\" class=\"img-block-center\">\n" +
                            "\n" +
                            "                                                                        <table style=\"display: inline-block;\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">\n" +
                            "                                                                            <tbody><tr>\n" +
                            "                                                                                <td>\n" +
                            "                                                                        <div style=\"border-top:0px None #000;border-right:0px None #000;border-bottom:0px None #000;border-left:0px None #000;display:inline-block;\">\n" +
                            "                                                                            <div><a target=\"_blank\" href=\"https://github.com/craigspicer\">\n" +
                            "                                                                            <img ng-if=\"col.img.source != 'url'\" width=\"200\" border=\"0\" hspace=\"0\" vspace=\"0\" alt=\"\" class=\"rnb-col-1-img\" src=\"http://img.mailinblue.com/2190383/images/rnb/original/5cd3fccc27351d028e2b7a1b.png\" style=\"vertical-align: top; max-width: 200px; float: left;\"></a></div><div style=\"clear:both;\"></div>\n" +
                            "                                                                            </div></td>\n" +
                            "                                                                            </tr>\n" +
                            "                                                                        </tbody></table>\n" +
                            "\n" +
                            "                                                                    </td>\n" +
                            "                                                                    </tr>\n" +
                            "                                                                </tbody>\n" +
                            "                                                                </table></td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td height=\"10\" class=\"col_td_gap\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                                                    </tr><tr>\n" +
                            "                                                        <td style=\"font-size:14px; font-family:Arial,Helvetica,sans-serif, sans-serif; color:#3c4858; line-height: 21px;\">\n" +
                            "                                                            <div><div style=\"text-align: center;\">© 2019 Note-ify. All Rights Reserved.</div>\n" +
                            "</div>\n" +
                            "                                                        </td>\n" +
                            "                                                    </tr>\n" +
                            "                                                    </tbody></table>\n" +
                            "\n" +
                            "                                                </td></tr>\n" +
                            "                                    </tbody></table></td>\n" +
                            "                            </tr>\n" +
                            "                            <tr>\n" +
                            "                                <td height=\"20\" style=\"font-size:1px; line-height:0px; mso-hide: all;\">&nbsp;</td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table><!--[if mso]>\n" +
                            "                </td>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "                <!--[if mso]>\n" +
                            "                </tr>\n" +
                            "                </table>\n" +
                            "                <![endif]-->\n" +
                            "                \n" +
                            "            </div></td>\n" +
                            "    </tr><tr>\n" +
                            "\n" +
                            "        <td align=\"center\" valign=\"top\">\n" +
                            "\n" +
                            "            <table class=\"rnb-del-min-width\" width=\"100%\" cellpadding=\"0\" border=\"0\" cellspacing=\"0\" style=\"min-width:590px;\" name=\"Layout_4701\" id=\"Layout_4701\">\n" +
                            "                <tbody><tr>\n" +
                            "                    <td class=\"rnb-del-min-width\" valign=\"top\" align=\"center\" style=\"min-width:590px;\">\n" +
                            "                        <table width=\"100%\" cellpadding=\"0\" border=\"0\" height=\"38\" cellspacing=\"0\">\n" +
                            "                            <tbody><tr>\n" +
                            "                                <td valign=\"top\" height=\"38\">\n" +
                            "                                    <img width=\"20\" height=\"38\" style=\"display:block; max-height:38px; max-width:20px;\" alt=\"\" src=\"http://img.mailinblue.com/new_images/rnb/rnb_space.gif\">\n" +
                            "                                </td>\n" +
                            "                            </tr>\n" +
                            "                        </tbody></table>\n" +
                            "                    </td>\n" +
                            "                </tr>\n" +
                            "            </tbody></table>\n" +
                            "            </td>\n" +
                            "    </tr></tbody></table>\n" +
                            "            <!--[if gte mso 9]>\n" +
                            "                        </td>\n" +
                            "                        </tr>\n" +
                            "                        </table>\n" +
                            "                        <![endif]-->\n" +
                            "                        </td>\n" +
                            "        </tr>\n" +
                            "        </tbody></table>\n" +
                            "\n" +
                            "</body></html>");

                    sendgrid.send(email);
                } catch (SendGridException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
