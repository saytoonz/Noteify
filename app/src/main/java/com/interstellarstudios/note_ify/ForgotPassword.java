package com.interstellarstudios.note_ify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import es.dmoral.toasty.Toasty;

public class ForgotPassword extends AppCompatActivity {

    private EditText editTextEmail;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = findViewById(R.id.editTextEmail);
        ImageView logoImageView = findViewById(R.id.logoImageView);

        Button buttonSendLink = findViewById(R.id.buttonSendLink);
        buttonSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLink();
            }
        });

        TextView textViewSignIn = findViewById(R.id.textViewSignin);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ForgotPassword.this, SignIn.class);
                startActivity(i);
                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(ForgotPassword.this, R.color.colorPrimaryDarkTheme));
            logoImageView.setImageResource(R.drawable.name_logo);
            editTextEmail.setTextColor(ContextCompat.getColor(ForgotPassword.this, R.color.colorDarkThemeText));
            editTextEmail.setHintTextColor(ContextCompat.getColor(ForgotPassword.this, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(this, R.color.colorDarkThemeText));
            textViewSignIn.setTextColor(ContextCompat.getColor(ForgotPassword.this, R.color.colorDarkThemeText));
            buttonSendLink.setTextColor(ContextCompat.getColor(ForgotPassword.this, R.color.colorDarkThemeText));
        }
    }

    private void sendLink(){

        String email = editTextEmail.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toasty.info(ForgotPassword.this, "Please enter your registered email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toasty.success(ForgotPassword.this, "Password reset email sent", Toast.LENGTH_LONG, true).show();
                            startActivity(new Intent(getApplicationContext(), SignIn.class));
                            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                        }else{
                            Toasty.error(ForgotPassword.this, "Error sending password reset email", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        startActivity(new Intent(ForgotPassword.this, SignIn.class));
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
