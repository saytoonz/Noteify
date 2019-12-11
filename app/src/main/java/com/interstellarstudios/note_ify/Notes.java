package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;

import android.view.View;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.adapters.NoteAdapter;
import com.interstellarstudios.note_ify.models.Note;

public class Notes extends AppCompatActivity {

    private Context context = this;
    private String mCurrentUserID;
    private FirebaseFirestore mFireBaseFireStore;
    private NoteAdapter adapter;
    private ImageView emptyView;
    private TextView emptyViewText;
    private String folderId;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final Bundle bundle = getIntent().getExtras();

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserID = mFireBaseAuth.getCurrentUser().getUid();
        }

        if (bundle != null) {
            folderId = bundle.getString("folderId");
        } else {
            return;
        }

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setOverflowIcon(ContextCompat.getDrawable(context, R.drawable.ic_sort));
        setSupportActionBar(toolbar);

        TextView textViewFragmentTitle = findViewById(R.id.text_view_fragment_title);
        textViewFragmentTitle.setText(folderId);

        ImageView imageViewBack = findViewById(R.id.image_view_back);
        imageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String folderId = bundle.getString("folderId");
                Intent i = new Intent(context, NewNote.class);
                i.putExtra("folderId", folderId);
                i.putExtra("noteType", "note");
                startActivity(i);
            }
        });

        emptyView = findViewById(R.id.emptyView);
        emptyViewText = findViewById(R.id.emptyViewText);

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }

            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            textViewFragmentTitle.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            emptyViewText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(imageViewBack, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(emptyView, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setUpRecyclerView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sort_alphabetically) {
            Bundle bundle = getIntent().getExtras();
            String folderId = bundle.getString("folderId");
            Intent i = new Intent(context, NotesSortAlphabetical.class);
            i.putExtra("folderId", folderId);
            startActivity(i);
            finish();
            return true;
        }
        if (id == R.id.sort_priority) {
            Bundle bundle = getIntent().getExtras();
            String folderId = bundle.getString("folderId");
            Intent i = new Intent(context, NotesSortPriority.class);
            i.putExtra("folderId", folderId);
            startActivity(i);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setUpRecyclerView() {

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        Drawable swipeBackground = new ColorDrawable(Color.parseColor("#e22018"));
        Drawable swipeBackground2 = new ColorDrawable(Color.parseColor("#0b37cb"));
        Drawable deleteIcon = ContextCompat.getDrawable(context, R.drawable.ic_delete);
        Drawable moveIcon = ContextCompat.getDrawable(context, R.drawable.ic_move);

        CollectionReference notebookRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document(folderId).collection(folderId);
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
                adapter.moveItem1(viewHolder.getAdapterPosition());
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

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {

                Bundle bundle = getIntent().getExtras();
                String folderId = bundle.getString("folderId");
                String directory = "Main";
                adapter.moveItem3(viewHolder.getAdapterPosition(), context, folderId, directory);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;
                if (dX > 0) {
                    swipeBackground2.setBounds(itemView.getLeft(), itemView.getTop(), (int) dX, itemView.getBottom());

                    int itemHeight = itemView.getBottom() - itemView.getTop();
                    int itemWidth = itemView.getRight() - itemView.getLeft();
                    int intrinsicWidth = moveIcon.getIntrinsicWidth();
                    int intrinsicHeight = moveIcon.getIntrinsicWidth();

                    int xMarkLeft = itemView.getLeft() + (((itemWidth - intrinsicWidth) / 2) / 8);
                    int xMarkRight = xMarkLeft + intrinsicWidth;
                    int xMarkTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                    int xMarkBottom = xMarkTop + intrinsicHeight;

                    moveIcon.setBounds(xMarkLeft, xMarkTop, xMarkRight, xMarkBottom);
                }
                swipeBackground2.draw(c);
                moveIcon.draw(c);
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
                i.putExtra("folderId", folderId);
                i.putExtra("noteId", noteId);
                i.putExtra("title", title);
                i.putExtra("description", description);
                i.putExtra("priority", priority);
                i.putExtra("revision", revision);
                i.putExtra("attachmentUrl", attachmentUrl);
                i.putExtra("attachmentName", attachmentName);
                i.putExtra("collectionId", "Main");
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

        CollectionReference myListsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserID).collection("Main").document(folderId).collection(folderId);
        myListsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots.isEmpty()) {
                    recyclerView.setVisibility(View.INVISIBLE);
                    emptyView.setVisibility(View.VISIBLE);
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
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}
