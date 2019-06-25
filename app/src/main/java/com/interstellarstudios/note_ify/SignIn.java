package com.interstellarstudios.note_ify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import java.util.HashMap;
import java.util.Map;
import es.dmoral.toasty.Toasty;

public class SignIn extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth mFireBaseAuth;
    private FirebaseFirestore mFireBaseFireStore;
    private String mCurrentUserId;
    private ProgressDialog mProgressDialog;
    private Switch switchThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mFireBaseFireStore = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        ImageView logoImageView = findViewById(R.id.logoImageView);
        mProgressDialog = new ProgressDialog(this);

        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        TextView textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView textViewForgot = findViewById(R.id.textViewForgot);
        textViewForgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignIn.this, ForgotPassword.class));
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

        switchThemes = findViewById(R.id.switchThemes);
        switchThemes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreferences();
            }
        });

        switchThemes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(getIntent());
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);
        switchThemes.setChecked(switchThemesOnOff);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container2);
            layout.setBackgroundColor(ContextCompat.getColor(SignIn.this, R.color.colorPrimaryDarkTheme));
            logoImageView.setImageResource(R.drawable.name_logo);
            editTextEmail.setTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            editTextEmail.setHintTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            editTextPassword.setTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            editTextPassword.setHintTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextPassword.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            textViewRegister.setTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            textViewForgot.setTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            buttonSignIn.setTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
            switchThemes.setTextColor(ContextCompat.getColor(SignIn.this, R.color.colorDarkThemeText));
        }
    }

    public void savePreferences() {

        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();

        if (switchThemes.isChecked()) {
            prefsEditor.putBoolean("switchThemes", true);
        } else {
            prefsEditor.putBoolean("switchThemes", false);
        }
        prefsEditor.apply();
    }

    private void userLogin() {

        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(SignIn.this, "Please enter your email address", Toast.LENGTH_LONG, true).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toasty.info(SignIn.this, "Please enter your password", Toast.LENGTH_LONG, true).show();
            return;
        }

        mProgressDialog.setMessage("Signing In");
        mProgressDialog.show();

        mFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            if (mFireBaseAuth.getCurrentUser() != null) {
                                mCurrentUserId = mFireBaseAuth.getCurrentUser().getUid();
                            }

                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String deviceToken = instanceIdResult.getToken();

                                    Map<String, Object> userToken = new HashMap<>();
                                    userToken.put("User_Token_ID", deviceToken);

                                    DocumentReference userTokenDocumentPath = mFireBaseFireStore.collection("Users").document(mCurrentUserId).collection("User_Details").document("User_Token");
                                    userTokenDocumentPath.set(userToken);
                                }
                            });

                            Intent i = new Intent(SignIn.this, Home.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            SignIn.this.finish();

                            Toasty.success(SignIn.this, "Sign In Successful", Toast.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(SignIn.this, "Sign In Error, please try again. Please ensure that your email address and password are correct.", Toast.LENGTH_LONG, true).show();
                        }
                        mProgressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
