package com.interstellarstudios.note_ify.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.models.Collection;
import com.interstellarstudios.note_ify.R;

public class CollectionAdapter extends FirestoreRecyclerAdapter<Collection, CollectionAdapter.CollectionHolder> {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionAdapter.OnItemClickListener listener;
    private boolean switchThemesOnOff;
    private Context mContext;

    public CollectionAdapter(@NonNull FirestoreRecyclerOptions<Collection> options, SharedPreferences sharedPreferences, Context context) {
        super(options);
        switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull CollectionAdapter.CollectionHolder holder, int position, @NonNull Collection model) {
        holder.folder.setText(model.getFolder());
        holder.folderDate.setText(model.getFolderDate());

        if (switchThemesOnOff) {
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorPrimary));
            holder.folder.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.folderDate.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.container.setBackgroundResource(R.color.colorPrimaryDark);
            holder.container2.setBackgroundResource(R.drawable.rounded_edges_dark);
        }
    }

    @NonNull
    @Override
    public CollectionAdapter.CollectionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_item,
                parent, false);
        return new CollectionAdapter.CollectionHolder(v);
    }

    public void deleteItem(final int position) {

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();
        CollectionReference collectionPath = db.collection("Users").document(current_user_id).collection("Main").document(id).collection(id);

        collectionPath.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean isEmpty = task.getResult().isEmpty();
                    if (isEmpty == true) {
                        getSnapshots().getSnapshot(position).getReference().delete();
                    }
                }
            }
        });
    }

    class CollectionHolder extends RecyclerView.ViewHolder {

        TextView folder;
        TextView folderDate;
        ImageView folderImageView;
        ConstraintLayout container;
        ConstraintLayout container2;

        public CollectionHolder(View itemView) {
            super(itemView);
            folder = itemView.findViewById(R.id.text_view_folder);
            folderDate = itemView.findViewById(R.id.folderDate);
            folderImageView = itemView.findViewById(R.id.folderImageView);
            container = itemView.findViewById(R.id.container);
            container2 = itemView.findViewById(R.id.container2);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(CollectionAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
