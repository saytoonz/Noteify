package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.interstellarstudios.note_ify.adapters.CollectionAdapter;
import com.interstellarstudios.note_ify.models.Collection;

import es.dmoral.toasty.Toasty;

public class MoveSelectFolder extends AppCompatActivity {

    private Context context = this;
    private CollectionAdapter adapter;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_move_select_folder);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView textViewFragmentTitle = findViewById(R.id.text_view_fragment_title);
        textViewFragmentTitle.setText("Move Note");

        ImageView imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {
            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setUpRecyclerView();
    }

    private void setUpRecyclerView() {

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        CollectionReference folderRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main");
        Query query = folderRef.orderBy("folder", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Collection> options = new FirestoreRecyclerOptions.Builder<Collection>()
                .setQuery(query, Collection.class)
                .build();

        adapter = new CollectionAdapter(options, sharedPreferences, context);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new CollectionAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(DocumentSnapshot documentSnapshot, int position) {

                String toFolderId = documentSnapshot.getId();

                Bundle bundle = getIntent().getExtras();
                String documentId = bundle.getString("documentId");
                String fromFolderId = bundle.getString("fromFolderId");
                String directory = bundle.getString("directory");

                if (fromFolderId.equals(toFolderId)) {
                    Toasty.info(context, "Select a different folder to move your note to.", Toast.LENGTH_LONG, true).show();
                } else {
                    DocumentReference from = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection(directory).document(fromFolderId).collection(fromFolderId).document(documentId);
                    DocumentReference to = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document(toFolderId).collection(toFolderId).document(documentId);
                    moveFireStoreDocument(from, to);

                    Intent i = new Intent(context, MainActivity.class);
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
    public void moveFireStoreDocument(final DocumentReference fromPath, final DocumentReference toPath) {
        fromPath.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document != null && document.getData() != null) {
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
