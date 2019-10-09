package com.interstellarstudios.note_ify.adapters;

import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;
import com.interstellarstudios.note_ify.models.GroceryItem;
import com.interstellarstudios.note_ify.R;

import android.content.Context;

public class GroceryListAdapter extends FirestoreRecyclerAdapter<GroceryItem, GroceryListAdapter.GroceryItemHolder> {

    private OnItemClickListener listener;
    private boolean switchThemesOnOff;
    private Context mContext;

    public GroceryListAdapter(@NonNull FirestoreRecyclerOptions<GroceryItem> options, SharedPreferences sharedPreferences, Context context) {
        super(options);

        switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);
        mContext = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull GroceryItemHolder holder, int position, @NonNull GroceryItem model) {

        holder.groceryItemTextView.setText(model.getItem());

        if (switchThemesOnOff) {
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorDarkThemeText));
            holder.groceryItemTextView.setTextColor(Color.parseColor(colorDarkThemeTextString));
            holder.groceryItemCard.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDarkTheme));
        }
    }

    @NonNull
    @Override
    public GroceryItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shared_grocery_item,
                parent, false);
        return new GroceryItemHolder(v);
    }

    public void deleteItem(int position) {

        getSnapshots().getSnapshot(position).getReference().delete();
    }

    class GroceryItemHolder extends RecyclerView.ViewHolder {

        TextView groceryItemTextView;
        LinearLayout groceryItemCard;

        public GroceryItemHolder(View itemView) {
            super(itemView);

            groceryItemTextView = itemView.findViewById(R.id.textview_name_item);
            groceryItemCard = itemView.findViewById(R.id.groceryItem);

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
}
