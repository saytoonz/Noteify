package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.MotionEvent;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.interstellarstudios.note_ify.adapters.GroceryListAdapter;
import com.interstellarstudios.note_ify.models.GroceryItem;

import java.util.ArrayList;
import java.util.UUID;
import es.dmoral.toasty.Toasty;

public class GroceryList extends AppCompatActivity {

    private Context context = this;
    private EditText mEditTextName;
    private TextView mTextViewAmount;
    private int mAmount = 0;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private GroceryListAdapter adapter;
    private ArrayList<String> groceryArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        FirebaseAuth mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarShare = toolbar.findViewById(R.id.toolbar_share);
        toolbarShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                share();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toasty.success(context, "Swipe left or right to remove an item", Toast.LENGTH_LONG, true).show();
            }
        });

        mEditTextName = findViewById(R.id.grocery_item);
        mTextViewAmount = findViewById(R.id.grocery_amount);
        ImageView itemIcon = findViewById(R.id.item_icon);

        final Button buttonIncrease = findViewById(R.id.button_increase);
        buttonIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increase();
            }
        });

        final Button buttonDecrease = findViewById(R.id.button_decrease);
        buttonDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrease();
            }
        });

        Button buttonAdd = findViewById(R.id.button_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addItem();
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
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            toolbarShare.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mEditTextName.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mEditTextName.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(mEditTextName.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            mTextViewAmount.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonIncrease.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            buttonIncrease.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonDecrease.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            buttonDecrease.setBackgroundColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(itemIcon, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        setUpRecyclerView();
    }

    private void increase() {
        mAmount++;
        mTextViewAmount.setText(String.valueOf(mAmount));
    }

    private void decrease() {
        if (mAmount > 0) {
            mAmount--;
            mTextViewAmount.setText(String.valueOf(mAmount));
        }
    }

    private void addItem() {

        if (mEditTextName.getText().toString().trim().length() == 0 || mAmount == 0) {
            return;
        }

        String name = mEditTextName.getText().toString();
        String amount = mTextViewAmount.getText().toString();
        String newItem = amount + " " + name;

        String randomId = UUID.randomUUID().toString();
        String groceryDocumentString = "item " + randomId;

        DocumentReference groceryListPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Grocery_List").collection("Grocery_List").document(groceryDocumentString);
        groceryListPath.set(new GroceryItem(newItem));

        recreate();
        mEditTextName.getText().clear();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void setUpRecyclerView() {

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        final CollectionReference groceryListRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Grocery_List").collection("Grocery_List");
        Query query = groceryListRef.orderBy("item", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<GroceryItem> options = new FirestoreRecyclerOptions.Builder<GroceryItem>()
                .setQuery(query, GroceryItem.class)
                .build();

        adapter = new GroceryListAdapter(options, sharedPreferences, context);

        final RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
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
        }).attachToRecyclerView(recyclerView);

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                adapter.deleteItem(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int) event.getRawX(), (int) event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(event);
    }

    private void share() {

        mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("Main").document("Grocery_List").collection("Grocery_List")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            groceryArrayList.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {

                                GroceryItem groceryItem = document.toObject(GroceryItem.class);
                                String setItem = groceryItem.getItem();
                                groceryArrayList.add(setItem);
                            }
                            if (groceryArrayList.isEmpty()) {
                                Toasty.info(context, "This grocery list is empty", Toast.LENGTH_LONG, true).show();

                            } else {
                                Intent i = new Intent(context, Share.class);
                                i.putExtra("fromActivity", "GroceryList");
                                i.putExtra("groceryArrayList", groceryArrayList);
                                startActivity(i);
                            }
                        }
                    }
                });
    }
}
