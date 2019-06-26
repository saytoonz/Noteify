package com.interstellarstudios.note_ify;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;
import java.util.HashMap;
import java.util.Map;
import es.dmoral.toasty.Toasty;

public class Account extends AppCompatActivity {

    private FirebaseAuth mFireBaseAuth;
    private FirebaseUser mUser;
    private String mCurrentUserId;
    private ImageView mProfilePic;
    private FirebaseFirestore mFireBaseFireStore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        if (mFireBaseAuth.getCurrentUser() != null) {
            mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
            mUser = mFireBaseAuth.getCurrentUser();
        }

        TextView textViewUserEmail = findViewById(R.id.textViewUserEmail);
        mProfilePic = findViewById(R.id.profile_pic);

        boolean guestAccountOn = sharedPreferences.getBoolean("guestAccount", false);

        if(guestAccountOn) {
            String guestAccount = "Guest Account";
            textViewUserEmail.setText(guestAccount);
        } else {
            textViewUserEmail.setText(mUser.getEmail());
        }

        DocumentReference detailsRef = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("This User");
        detailsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Details details = document.toObject(Details.class);
                        String profilePicURL = details.getProfilePic();

                        if (profilePicURL != null && !profilePicURL.equals("Profile Pic Not Uploaded")) {
                            Picasso.get().load(profilePicURL).into(mProfilePic);
                        }
                    }
                }
            }
        });

        Button buttonLogout = findViewById(R.id.buttonLogout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            }
        });

        Button editAccountDetails = findViewById(R.id.editAccountDetails);
        editAccountDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Account.this, EditAccountDetails.class);
                startActivity(i);
            }
        });

        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(Account.this, R.color.colorPrimaryDarkTheme));
            editAccountDetails.setTextColor(ContextCompat.getColor(Account.this, R.color.colorDarkThemeText));
            buttonLogout.setTextColor(ContextCompat.getColor(Account.this, R.color.colorDarkThemeText));
            textViewUserEmail.setTextColor(ContextCompat.getColor(Account.this, R.color.colorDarkThemeText));
        }
    }
}
