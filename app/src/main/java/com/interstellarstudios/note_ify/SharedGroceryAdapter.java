package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class SharedGroceryAdapter extends RecyclerView.Adapter<SharedGroceryAdapter.ViewHolder> {

    private ArrayList<String> mItemNames;
    private Context mContext;
    private boolean switchThemesOnOff;

    public SharedGroceryAdapter(ArrayList<String> itemNames, Context context, SharedPreferences sharedPreferences) {
        mItemNames = itemNames;
        mContext = context;
        switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shared_grocery_item, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {

        viewHolder.item.setText(mItemNames.get(i));

        if (switchThemesOnOff){
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(mContext, R.color.colorDarkThemeText));
            viewHolder.item.setTextColor(Color.parseColor(colorDarkThemeTextString));
            viewHolder.groceryItem.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDarkTheme));
        }
    }

    @Override
    public int getItemCount() {
        return mItemNames.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView item;
        LinearLayout groceryItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            item = itemView.findViewById(R.id.textview_name_item);
            groceryItem = itemView.findViewById(R.id.groceryItem);
        }
    }
}
