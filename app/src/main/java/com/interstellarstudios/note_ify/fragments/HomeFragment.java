package com.interstellarstudios.note_ify.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.interstellarstudios.note_ify.NewCollection;
import com.interstellarstudios.note_ify.Notes;
import com.interstellarstudios.note_ify.R;
import com.interstellarstudios.note_ify.adapters.CollectionAdapter;
import com.interstellarstudios.note_ify.models.Collection;

import static android.content.Context.MODE_PRIVATE;

public class HomeFragment extends Fragment {

    private Context context;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private ConstraintLayout layout;
    private SharedPreferences sharedPreferences;
    private CollectionAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        context = getActivity();

        AutoCompleteTextView searchField = getActivity().findViewById(R.id.searchField);
        searchField.setVisibility(View.VISIBLE);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            mCurrentUserId = firebaseUser.getUid();
        }

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        layout = view.findViewById(R.id.container);

        TextView newFolderText = view.findViewById(R.id.new_folder_text);
        newFolderText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewCollection.class);
                startActivity(i);
            }
        });

        ImageView newFolder = view.findViewById(R.id.new_folder_image_view);
        newFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, NewCollection.class);
                startActivity(i);
            }
        });

        sharedPreferences = context.getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean darkModeOn = sharedPreferences.getBoolean("switchThemes", true);
        if (darkModeOn) {
            darkMode();
        } else {
            lightMode();
        }

        setUpRecyclerView();

        return view;
    }

    private void lightMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
    }

    private void darkMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
    }

    private void setUpRecyclerView() {

        Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_forever);

        CollectionReference folderRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main");
        Query query = folderRef.orderBy("folder", Query.Direction.ASCENDING);

        FirestoreRecyclerOptions<Collection> options = new FirestoreRecyclerOptions.Builder<Collection>()
                .setQuery(query, Collection.class)
                .build();

        adapter = new CollectionAdapter(options, sharedPreferences, context);
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
                Intent i = new Intent(context, Notes.class);
                i.putExtra("folderId", folderId);
                startActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
