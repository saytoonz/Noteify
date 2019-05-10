package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import es.dmoral.toasty.Toasty;
import hotchemi.android.rate.AppRate;
import spencerstudios.com.bungeelib.Bungee;

public class Collections extends AppCompatActivity {

    private Context mContext = this;
    private CollectionAdapter adapter;
    private String mCurrentUserID;
    private FirebaseFirestore mFireBaseFireStore;
    private EditText searchField;
    private String userFullName;
    private String profilePicURL;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_share:
                    Intent i = new Intent(Collections.this, Shared.class);
                    startActivity(i);
                    return true;
                case R.id.navigation_add:
                    Intent j = new Intent(Collections.this, NewNotebookNote.class);
                    startActivity(j);
                    Bungee.zoom(mContext);
                    return true;
                case R.id.navigation_account:
                    Intent k = new Intent(Collections.this, Account.class);
                    k.putExtra("userFullName", userFullName);
                    k.putExtra("profilePicURL", profilePicURL);
                    startActivity(k);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collections);

        //app rate
        AppRate.with(this)
                .setInstallDays(7)
                .setLaunchTimes(5)
                .setRemindInterval(2)
                .monitor();
        AppRate.showRateDialogIfMeetsConditions(this);
        //AppRate.with(this).showRateDialog(this);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserID = mFireBaseAuth.getCurrentUser().getUid();
        }

        DocumentReference detailsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("User_Details").document("This User");
        detailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Details details = document.toObject(Details.class);
                        String userFirstName = details.getFirstName();
                        String userLastName = details.getLastName();
                        userFullName = (userFirstName + " " + userLastName);
                        profilePicURL = details.getProfilePic();
                    }
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.success(Collections.this, "Swipe left to delete a folder.\nA folder must be empty before it can be removed.", Toast.LENGTH_LONG, true).show();
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
                            Intent i = new Intent(Collections.this, FolderSearchResults.class);
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
                Intent i = new Intent(Collections.this, FolderSearchResults.class);
                i.putExtra("searchInput", searchInput);
                startActivity(i);
            }
        });

        TextView newFolderText = findViewById(R.id.new_folder_text);
        newFolderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Collections.this, NewCollection.class);
                startActivity(i);
                Bungee.zoom(mContext);
            }
        });

        ImageView newFolder = findViewById(R.id.new_folder_image_view);
        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Collections.this, NewCollection.class);
                startActivity(i);
                Bungee.zoom(mContext);
            }
        });

        TextView groceryList = findViewById(R.id.grocery_list_text);
        groceryList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Collections.this, GroceryList.class);
                startActivity(i);
            }
        });

        ImageView groceryListIcon = findViewById(R.id.grocery_list_icon);
        groceryListIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Collections.this, GroceryList.class);
                startActivity(i);
            }
        });

        TextView binText = findViewById(R.id.bin_text);
        binText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Collections.this, Bin.class);
                startActivity(i);
            }
        });

        ImageView binIcon = findViewById(R.id.bin_icon);
        binIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Collections.this, Bin.class);
                startActivity(i);
            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 2; i < 3; i++) {

            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);

            iconView.setLayoutParams(layoutParams);
        }
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        final Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        final Drawable deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete_forever);

        CollectionReference folderRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main");
        Query query = folderRef.orderBy("folder", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Collection> options = new FirestoreRecyclerOptions.Builder<Collection>()
                .setQuery(query, Collection.class)
                .build();

        adapter = new CollectionAdapter(options);

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

                    //Places the delete icon
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

                //Collection collection = documentSnapshot.toObject(Collection.class);
                String folderId = documentSnapshot.getId();
                Intent i = new Intent(Collections.this, MainActivity.class);
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
}
