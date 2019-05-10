package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import es.dmoral.toasty.Toasty;
import spencerstudios.com.bungeelib.Bungee;

public class Bin extends AppCompatActivity {

    private Context context = this;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference notebookRef = db.collection("Users").document(current_user_id).collection("Bin");
    private NoteAdapter adapter;
    private EditText searchField;
    private ImageView searchButton;
    private ImageView emptyView;
    private TextView emptyViewText;
    private String userFullName;
    private String profilePicURL;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_main:
                    Intent i = new Intent(Bin.this, Collections.class);
                    startActivity(i);
                    return true;
                case R.id.navigation_share:
                    Intent j = new Intent(Bin.this, Shared.class);
                    startActivity(j);
                    return true;
                case R.id.navigation_add:
                    Intent k = new Intent(Bin.this, NewNotebookNote.class);
                    startActivity(k);
                    Bungee.zoom(context);
                    return true;
                case R.id.navigation_account:
                    Intent l = new Intent(Bin.this, Account.class);
                    l.putExtra("userFullName", userFullName);
                    l.putExtra("profilePicURL", profilePicURL);
                    startActivity(l);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bin);

        DocumentReference detailsRef = db.collection("Users").document(current_user_id).collection("User_Details").document("This User");
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.success(Bin.this, "Swipe left to permanently delete a note.\nSwipe right to restore a note.", Toast.LENGTH_LONG, true).show();
            }
        });

        searchField = (EditText)findViewById(R.id.searchField);
        searchField.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_CENTER:
                        case KeyEvent.KEYCODE_ENTER:
                            String searchInput = searchField.getText().toString().toLowerCase();
                            Intent i = new Intent(Bin.this, BinSearchResults.class);
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

        searchButton = (ImageView)findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String searchInput = searchField.getText().toString().toLowerCase();
                Intent i = new Intent(Bin.this, BinSearchResults.class);
                i.putExtra("searchInput", searchInput);
                startActivity(i);
            }
        });

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //Disable shift mode for bottom navigation
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);

        //To change size of bottom navigation icons
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 2; i < 3; i++) {

            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            // set your height here
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
            // set your width here
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);

            iconView.setLayoutParams(layoutParams);
        }

        emptyView = (ImageView)findViewById(R.id.emptyView);
        emptyViewText = (TextView)findViewById(R.id.emptyViewText);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        //Loading shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        final Drawable swipeBackground2 = new ColorDrawable(Color.parseColor("#3bbe19"));
        final Drawable deleteForeverIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete_forever);
        final Drawable restoreIcon = ContextCompat.getDrawable(this, R.drawable.ic_restore);

        //Sets up a query to order notes
        Query query = notebookRef.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new NoteAdapter(options, sharedPreferences);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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

        //Left swipe calls delete item
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT) {
            //onMove is for drag and drop, not used here
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());

            }

            //Left swipe
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (dX < 0) {
                    swipeBackground.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());

                    //Places the delete icon
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

        //Right swipe calls move item back to notebook
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            //onMove is for drag and drop, not used here
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.moveItem2(viewHolder.getAdapterPosition());
            }

            //Right swipe
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (dX > 0) {
                    swipeBackground2.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());

                    //Places the delete icon
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
                //Draw
                swipeBackground2.draw(c);
                restoreIcon.draw(c);
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        }).attachToRecyclerView(recyclerView);

        //This is for handling click events on individual note items in the recycler view
        adapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {
                Toasty.info(Bin.this, "Restore a Note to edit it.", Toast.LENGTH_LONG, true).show();
            }
        });
    }

    //App only listens for database changes when goes into the foreground so as not to waste resources
    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }

    //Stops listening for changes when app goes into the background
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
