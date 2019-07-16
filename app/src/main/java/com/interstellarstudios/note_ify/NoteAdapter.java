package com.interstellarstudios.note_ify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import android.content.Context;
import jp.wasabeef.richeditor.RichEditor;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.NoteHolder> {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String current_user_id = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private OnItemClickListener listener;
    private boolean switchPriorityOnOff;
    private boolean switchThemesOnOff;
    private Context mContext;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, SharedPreferences sharedPreferences, Context context) {
        super(options);
        switchPriorityOnOff = sharedPreferences.getBoolean("switchPriorityColor", false);
        switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull NoteHolder holder, int position, @NonNull Note model) {

        holder.textViewTitle.setText(model.getTitle());
        holder.mEditor.setHtml(model.getDescription());

        if (switchThemesOnOff) {
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorDarkThemeText));
            holder.textViewTitle.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewDate.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewFromUserEmail.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewRevision.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.textViewPriority.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(mContext, R.color.cardBackgroundDarkTheme));

            String colorDarkThemeCardBackgroundString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.cardBackgroundDarkTheme));
            holder.mEditor.setBackgroundColor(Color.parseColor(colorDarkThemeCardBackgroundString));
            holder.mEditor.setEditorFontColor(Color.parseColor(colorDarkThemeTextString));
        }

        if (switchPriorityOnOff) {
            int priority = model.getPriority();
            if (priority >= 1 && priority <= 3) {
                holder.textViewPriority.setText("Priority: " + priority);
            } else if (priority >= 4 && priority <= 5) {
                holder.textViewPriority.setTextColor(Color.GREEN);
                holder.textViewPriority.setText("Priority: " + priority);
            } else if (priority >= 6 && priority <= 8) {
                holder.textViewPriority.setTextColor(Color.YELLOW);
                holder.textViewPriority.setText("Priority: " + priority);
            } else {
                holder.textViewPriority.setTextColor(Color.RED);
                holder.textViewPriority.setText("Priority: " + priority);
            }
        } else {
            holder.textViewPriority.setText("Priority: " + model.getPriority());
        }

        holder.textViewDate.setText(model.getDate());
        holder.textViewFromUserEmail.setText(model.getFromEmailAddress());
        holder.textViewRevision.setText("Revision: " + model.getRevision());
        holder.attachmentName.setText(model.getAttachmentName());

        String attachmentURL = model.getAttachmentUrl();
        if (!attachmentURL.equals("")) {
            holder.attachment_icon.setVisibility(View.VISIBLE);
        }
    }

    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_item,
                parent, false);
        return new NoteHolder(v);
    }

    //Move - folder to Bin
    public void moveItem1(int position) {

        getSnapshots().getSnapshot(position).getReference();

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();

        DocumentReference from = snapshot.getReference();
        DocumentReference to = db.collection("Users").document(current_user_id).collection("Bin").document(id);
        moveFirestoreDocument(from, to);
    }

    //Move - Bin to Notebook
    public void moveItem2(int position) {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd\nHH:mm");
        String date = sdf.format(calendar.getTime());

        DocumentReference RestoredDocumentPath = db.collection("Users").document(current_user_id).collection("Main").document("Restored");
        RestoredDocumentPath.set(new Collection("Restored", "restored", date));

        getSnapshots().getSnapshot(position).getReference();

        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();

        DocumentReference from = db.collection("Users").document(current_user_id).collection("Bin").document(id);
        DocumentReference to = db.collection("Users").document(current_user_id).collection("Main").document("Restored").collection("Restored").document(id);
        moveFirestoreDocument(from, to);
    }

    //Move - folder to folder
    public void moveItem3(int position, Context context, String folderId, String directory) {

        getSnapshots().getSnapshot(position).getReference();
        DocumentSnapshot snapshot = getSnapshots().getSnapshot(position);
        String id = snapshot.getId();

        Intent i = new Intent(context, MoveSelectFolder.class);
        i.putExtra("documentId", id);
        i.putExtra("fromFolderId", folderId);
        i.putExtra("directory", directory);
        context.startActivity(i);
    }

    public void deleteItem(int position) {

        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class NoteHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        RichEditor mEditor;
        TextView textViewPriority;
        TextView textViewDate;
        TextView textViewFromUserEmail;
        TextView textViewRevision;
        ImageView attachment_icon;
        CardView cardView;
        TextView attachmentName;

        public NoteHolder(View itemView) {
            super(itemView);

            String colorLightThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorLightThemeText));
            String colorLightThemeCardBackgroundString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.cardBackground));
            mEditor = itemView.findViewById(R.id.mEditor);
            mEditor.setInputEnabled(false);
            mEditor.setBackgroundColor(Color.parseColor(colorLightThemeCardBackgroundString));
            mEditor.setEditorFontColor(Color.parseColor(colorLightThemeTextString));

            textViewTitle = itemView.findViewById(R.id.text_view_title);
            textViewPriority = itemView.findViewById(R.id.text_view_priority);
            textViewDate = itemView.findViewById(R.id.text_view_date);
            textViewFromUserEmail = itemView.findViewById(R.id.fromUserEmail);
            textViewRevision = itemView.findViewById(R.id.revision);
            attachment_icon = itemView.findViewById(R.id.attachment_icon);
            cardView = itemView.findViewById(R.id.cardView);
            attachmentName = itemView.findViewById(R.id.attachmentName);

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

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
