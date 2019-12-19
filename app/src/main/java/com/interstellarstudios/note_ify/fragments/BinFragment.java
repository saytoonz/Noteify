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
import androidx.core.widget.ImageViewCompat;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.EditNote;
import com.interstellarstudios.note_ify.R;
import com.interstellarstudios.note_ify.adapters.NoteAdapter;
import com.interstellarstudios.note_ify.models.Note;

import static android.content.Context.MODE_PRIVATE;

public class BinFragment extends Fragment {

    private Context context;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private ConstraintLayout layout;
    private SharedPreferences sharedPreferences;
    private NoteAdapter adapter;
    private RecyclerView recyclerView;
    private ImageView emptyView;
    private TextView emptyViewText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_shared, container, false);

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

        emptyView = view.findViewById(R.id.emptyView);
        emptyViewText = view.findViewById(R.id.emptyViewText);

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
        emptyViewText.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
        ImageViewCompat.setImageTintList(emptyView, ContextCompat.getColorStateList(context, R.color.colorLightThemeText));
    }

    private void darkMode() {

        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        emptyViewText.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
        ImageViewCompat.setImageTintList(emptyView, ContextCompat.getColorStateList(context, R.color.colorPrimary));
    }

    private void setUpRecyclerView() {

        Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        Drawable swipeBackground2 = new ColorDrawable(Color.parseColor("#3bbe19"));
        Drawable deleteForeverIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete_forever);
        Drawable restoreIcon = ContextCompat.getDrawable(context, R.drawable.ic_restore);

        CollectionReference notebookRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin");
        Query query = notebookRef.orderBy("date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();

        adapter = new NoteAdapter(options, sharedPreferences, context);
        recyclerView.setAdapter(adapter);

        notebookRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyView.setImageResource(R.drawable.ic_bin);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyViewText.setText("Deleted notes will appear here");
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

                Note note = documentSnapshot.toObject(Note.class);

                String noteId = documentSnapshot.getId();
                String title = note.getTitle();
                String description = note.getDescription();
                int priority = note.getPriority();
                int revision = note.getRevision();
                String attachmentUrl = note.getAttachmentUrl();
                String attachmentName = note.getAttachmentName();
                String audioDownloadUrl = note.getAudioUrl();
                String audioZipDownloadUrl = note.getAudioZipUrl();
                String audioZipFileName = note.getAudioZipName();

                Intent i = new Intent(context, EditNote.class);
                i.putExtra("folderId", "Bin");
                i.putExtra("noteId", noteId);
                i.putExtra("title", title);
                i.putExtra("description", description);
                i.putExtra("priority", priority);
                i.putExtra("revision", revision);
                i.putExtra("attachmentUrl", attachmentUrl);
                i.putExtra("attachmentName", attachmentName);
                i.putExtra("attachmentName", attachmentName);
                i.putExtra("audioDownloadUrl", audioDownloadUrl);
                i.putExtra("audioZipDownloadUrl", audioZipDownloadUrl);
                i.putExtra("audioZipFileName", audioZipFileName);
                startActivity(i);
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        CollectionReference myListsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Bin");
        myListsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyView.setImageResource(R.drawable.ic_bin);
                    emptyView.setVisibility(View.VISIBLE);
                    emptyViewText.setText("Deleted notes will appear here");
                    emptyViewText.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    emptyView.setVisibility(View.INVISIBLE);
                    emptyViewText.setVisibility(View.INVISIBLE);
                }
            }
        });

        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
