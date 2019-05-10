package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import es.dmoral.toasty.Toasty;
import spencerstudios.com.bungeelib.Bungee;

public class MoveSelectFolder extends AppCompatActivity {

    private Context context = this;
    private CollectionAdapter adapter;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference folderRef = db.collection("Users").document(current_user_id).collection("Main");
    private String userFullName;
    private String profilePicURL;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.navigation_main:
                    Intent i = new Intent(MoveSelectFolder.this, Collections.class);
                    startActivity(i);
                    return true;
                case R.id.navigation_share:
                    Intent j = new Intent(MoveSelectFolder.this, Shared.class);
                    startActivity(j);
                    return true;
                case R.id.navigation_add:
                    Intent k = new Intent(MoveSelectFolder.this, NewNotebookNote.class);
                    startActivity(k);
                    Bungee.zoom(context);
                    return true;
                case R.id.navigation_account:
                    Intent l = new Intent(MoveSelectFolder.this, Account.class);
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
        setContentView(R.layout.activity_move_select_folder);

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
                Toasty.info(MoveSelectFolder.this, "Tap a folder to move your note.", Toast.LENGTH_LONG, true).show();
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
            //iconView.setBackgroundColor(Color.parseColor("#02A72F"));
        }
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        Query query = folderRef.orderBy("folder", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Collection> options = new FirestoreRecyclerOptions.Builder<Collection>()
                .setQuery(query, Collection.class)
                .build();

        adapter = new CollectionAdapter(options);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        //This is for handling click events on individual note items in the recycler view
        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                Collection collection = documentSnapshot.toObject(Collection.class);
                String toFolderId = documentSnapshot.getId();

                Bundle bundle = getIntent().getExtras();
                String documentId = bundle.getString("documentId");
                String fromFolderId = bundle.getString("fromFolderId");
                String directory = bundle.getString("directory");

                if (fromFolderId.equals(toFolderId)) {
                    Toasty.info(MoveSelectFolder.this, "Select a different folder to move your note to.", Toast.LENGTH_LONG, true).show();
                    return;
                } else {
                    DocumentReference from = db.collection("Users").document(current_user_id).collection(directory).document(fromFolderId).collection(fromFolderId).document(documentId);
                    DocumentReference to = db.collection("Users").document(current_user_id).collection("Main").document(toFolderId).collection(toFolderId).document(documentId);
                    moveFirestoreDocument(from, to);

                    Intent i = new Intent(MoveSelectFolder.this, Collections.class);
                    startActivity(i);
                }
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

    //Move method
    public void moveFirestoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null) {
                        toPath.set(document.getData())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        fromPath.delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {

                                                    }
                                                });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                    }
                }
            }
        });
    }
}
