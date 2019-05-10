package com.interstellarstudios.note_ify;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class CollectionAdapter extends FirestoreRecyclerAdapter <Collection, CollectionAdapter.CollectionHolder> {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionAdapter.OnItemClickListener listener;
    //private CollectionAdapter.OnItemLongClickListener listener2;

    public CollectionAdapter(@NonNull FirestoreRecyclerOptions<Collection> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull CollectionAdapter.CollectionHolder holder, int position, @NonNull Collection model) {
        holder.folder.setText(model.getFolder());
        holder.folderDate.setText(model.getFolderDate());
    }

    @NonNull
    @Override
    public CollectionAdapter.CollectionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.collection_item,
                parent, false);
        return new CollectionAdapter.CollectionHolder(v);
    }

    //Delete item method
    public void deleteItem(final int position) {

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();
        CollectionReference collectionPath = db.collection("Users").document(current_user_id).collection("Main").document(id).collection(id);

        collectionPath.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    boolean isEmpty = task.getResult().isEmpty();
                    if (isEmpty == true){
                        getSnapshots().getSnapshot(position).getReference().delete();
                    }
                }
            }
        });
    }

    class CollectionHolder extends RecyclerView.ViewHolder {
        TextView folder;
        TextView folderDate;

        public CollectionHolder(View itemView) {
            super(itemView);
            folder = itemView.findViewById(R.id.text_view_folder);
            folderDate = itemView.findViewById(R.id.folderDate);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    //This ensures that we aren't clicking on a note that is being deleted (in animation)
                    if (position != RecyclerView.NO_POSITION && listener != null) {
                        listener.onItemClick(getSnapshots().getSnapshot(position), position);
                    }
                }
            });

            /*itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int position = getAdapterPosition();
                    //This ensures that we aren't clicking on a note that is being deleted (in animation)
                    if (position != RecyclerView.NO_POSITION && listener2 != null) {
                        listener2.onItemLongClick(getSnapshots().getSnapshot(position), position);
                    } return true;
                }
            });*/
        }
    }

    //We set the data that is being sent by the onClickListener in this interface
    public interface OnItemClickListener {
        void onItemClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemClickListener(CollectionAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }

    /*//We set the data that is being sent by the onClickListener in this interface
    public interface OnItemLongClickListener {
        void onItemLongClick(DocumentSnapshot documentSnapshot, int position);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener2) {
        this.listener2 = listener2;
    }*/
}
