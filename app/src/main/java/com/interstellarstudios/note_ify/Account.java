package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import es.dmoral.toasty.Toasty;
import spencerstudios.com.bungeelib.Bungee;

public class Account extends AppCompatActivity implements View.OnClickListener {

    private Context context = this;
    private FirebaseAuth mFireBaseAuth;
    private String mCurrentUserId;
    private FirebaseFirestore mFireBaseFireStore;
    private Button buttonLogout;
    private Button editAccountDetails;
    private TextView privacyPolicy;
    private TextView termsOfService;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_main:
                    Intent i = new Intent(Account.this, Collections.class);
                    startActivity(i);
                    return true;
                case R.id.navigation_share:
                    Intent j = new Intent(Account.this, Shared.class);
                    startActivity(j);
                    return true;
                case R.id.navigation_add:
                    Intent k = new Intent(Account.this, NewNotebookNote.class);
                    startActivity(k);
                    Bungee.zoom(context);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
        }

        ImageView websiteLink = findViewById(R.id.website_link);
        websiteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://interstellarstudios.co.uk/"));
                startActivity(browserIntent);
            }
        });

        ImageView imageViewSettings = findViewById(R.id.image_view_settings);
        imageViewSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Account.this, Settings.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navigation.getChildAt(0);
        for (int i = 2; i < 3; i++) {

            final View iconView = menuView.getChildAt(i).findViewById(android.support.design.R.id.icon);
            final ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
            final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            layoutParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
            layoutParams.width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, displayMetrics);
            iconView.setLayoutParams(layoutParams);
        }

        TextView textUserFullName = findViewById(R.id.userFullName);
        TextView textViewUserEmail = findViewById(R.id.textViewUserEmail);
        buttonLogout = findViewById(R.id.buttonLogout);
        editAccountDetails = findViewById(R.id.editAccountDetails);
        ImageView profilePic = findViewById(R.id.profile_pic);
        privacyPolicy = findViewById(R.id.privacyPolicy);
        termsOfService = findViewById(R.id.termsOfService);

        Bundle bundle = getIntent().getExtras();
        String userFullName = bundle.getString("userFullName");
        String profilePicURL = bundle.getString("profilePicURL");

        FirebaseUser mUser = mFireBaseAuth.getCurrentUser();
        textViewUserEmail.setText(mUser.getEmail());

        if (userFullName != null) {
            textUserFullName.setText(userFullName);
        }
        if (profilePicURL != null && !profilePicURL.equals("Profile Pic Not Uploaded")) {
            Picasso.get().load(profilePicURL).into(profilePic);
        }

        buttonLogout.setOnClickListener(this);
        editAccountDetails.setOnClickListener(this);
        privacyPolicy.setOnClickListener(this);
        termsOfService.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view == buttonLogout){

            Map<String, Object> userToken = new HashMap<>();
            userToken.put("User_Token_ID", "");

            DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Token");
            userTokenDocumentPath.set(userToken);

            mFireBaseAuth.signOut();

            Intent i = new Intent(Account.this, Register.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            Account.this.finish();

            Toasty.success(Account.this, "You have been signed out.", Toast.LENGTH_LONG, true).show();
            Bungee.zoom(context);
        }

        if(view == editAccountDetails){
            Intent i = new Intent(this, EditAccountDetails.class);
            startActivity(i);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }

        if(view == termsOfService){
            startActivity(new Intent(this, TermsOfService.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }

        if(view == privacyPolicy){
            startActivity(new Intent(this, PrivacyPolicy.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }
}
