package com.interstellarstudios.note_ify;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.provider.ContactsContract;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
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

public class GroceryList extends AppCompatActivity {

    private Context context = this;
    private ArrayList<String> groceryArrayList = new ArrayList<>();
    private EditText mEditTextName;
    private TextView mTextViewAmount;
    private int mAmount = 0;
    private EditText mSharedUserEmailText;
    private FirebaseFirestore mFireBaseFireStore;
    private String mSharedUserId;
    private String sharedUserEmail;
    private String currentUserEmail;
    private String mCurrentUserID;
    private GroceryListAdapter adapter;
    private static final int CONTACT_PICKER_RESULT = 2;
    private static final int PERMISSION_READ_CONTACTS_REQUEST = 11;

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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarContacts = toolbar.findViewById(R.id.toolbar_contacts);
        toolbarContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    getPermissionToReadUserContacts();
                } else {
                    doLaunchContactPicker();
                }
            }
        });

        TextView whatsAppText = findViewById(R.id.whatsapp_text);
        whatsAppText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                                    if (groceryArrayList.isEmpty()) {
                                        Toasty.info(context, "This grocery list is empty", Toast.LENGTH_LONG, true).show();

                                    } else {

                                        String groceryString  = "Grocery List: \n\n" + groceryArrayList.toString();
                                        String modGroceryString = groceryString.replaceAll(",", "\n");

                                        Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
                                        whatsAppIntent.setType("text/plain"); //html
                                        whatsAppIntent.setPackage("com.whatsapp");
                                        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.interstellarstudios.note_ify\n\n" + modGroceryString);

                                        try {
                                            startActivity(whatsAppIntent);
                                        } catch (android.content.ActivityNotFoundException e) {
                                            e.printStackTrace();
                                            Toasty.error(context, "WhatsApp is not installed", Toast.LENGTH_LONG, true).show();
                                        }
                                    }
                                }
                            }
                        });
            }
        });

        ImageView whatsAppIcon = findViewById(R.id.whatsapp_icon);
        whatsAppIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

                                    if (groceryArrayList.isEmpty()) {
                                        Toasty.info(context, "This grocery list is empty", Toast.LENGTH_LONG, true).show();

                                    } else {

                                        String groceryString  = "Grocery List: \n\n" + groceryArrayList.toString();
                                        String modGroceryString = groceryString.replaceAll(",", "\n");

                                        Intent whatsAppIntent = new Intent(Intent.ACTION_SEND);
                                        whatsAppIntent.setType("text/plain"); //html
                                        whatsAppIntent.setPackage("com.whatsapp");
                                        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.interstellarstudios.note_ify\n\n" + modGroceryString);

                                        try {
                                            startActivity(whatsAppIntent);
                                        } catch (android.content.ActivityNotFoundException e) {
                                            e.printStackTrace();
                                            Toasty.error(context, "WhatsApp is not installed", Toast.LENGTH_LONG, true).show();
                                        }
                                    }
                                }
                            }
                        });
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.success(context, "Swipe left or right to remove an item", Toast.LENGTH_LONG, true).show();
            }
        });

        mEditTextName = findViewById(R.id.grocery_item);
        mTextViewAmount = findViewById(R.id.grocery_amount);
        mSharedUserEmailText = findViewById(R.id.sharedUserEmail);
        ImageView itemIcon = findViewById(R.id.item_icon);

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
                            Toasty.success(context, "Grocery list shared with and emailed to: " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
                            return true;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        final Button buttonIncrease = findViewById(R.id.button_increase);
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increase();
            }
        });

        final Button buttonDecrease = findViewById(R.id.button_decrease);
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

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if(switchThemesOnOff) {
            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            toolbarContacts.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mEditTextName.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mEditTextName.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mEditTextName.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mTextViewAmount.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonIncrease.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            buttonIncrease.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonDecrease.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            buttonDecrease.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            itemIcon.setImageResource(R.drawable.ic_grocery_white);
            DrawableCompat.setTint(mSharedUserEmailText.getBackground(), ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setUpRecyclerView();
    }

    public void getPermissionToReadUserContacts() {

        new AlertDialog.Builder(context)
                .setTitle("Permission needed to access contacts")
                .setMessage("This permission is needed in order to get an email address for a selected contact. Manually enable in Settings > Apps & notifications > Note-ify > Permissions.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_READ_CONTACTS_REQUEST);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_READ_CONTACTS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(context, "Read Contacts permission granted", Toast.LENGTH_LONG, true).show();
                doLaunchContactPicker();
            } else {
                Toasty.error(context, "Read Contacts permission denied", Toast.LENGTH_LONG, true).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void doLaunchContactPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICKER_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CONTACT_PICKER_RESULT && resultCode == RESULT_OK) {

            String email = "";

            Uri result = data.getData();
            String id = result.getLastPathSegment();

            Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "=?", new String[]{id}, null);

            if (cursor.moveToFirst()) {
                email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            if (cursor != null) {
                cursor.close();
            }
            if (email.length() == 0) {
                Toasty.info(context, "No email address stored for this contact", Toast.LENGTH_LONG, true).show();
            } else {
                mSharedUserEmailText.setText(email);
            }
        }
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
        super.onBackPressed();
    }

    private void setUpRecyclerView() {

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final CollectionReference groceryListRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document("Grocery_List").collection("Grocery_List");
        Query query = groceryListRef.orderBy("item", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<GroceryItem> options = new FirestoreRecyclerOptions.Builder<GroceryItem>()
                .setQuery(query, GroceryItem.class)
                .build();

        adapter = new GroceryListAdapter(options, sharedPreferences, context);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
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

        sharedUserEmail = mSharedUserEmailText.getText().toString().trim().toLowerCase();

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
                                Toasty.info(context, "This grocery list is empty", Toast.LENGTH_LONG, true).show();
                            } else {
                                SendMailGrocery.sendMail(context, sharedUserEmail, currentUserEmail, groceryArrayList);
                                Toasty.success(context, "Grocery List shared with and emailed to " + sharedUserEmail, Toast.LENGTH_LONG, true).show();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }
}
