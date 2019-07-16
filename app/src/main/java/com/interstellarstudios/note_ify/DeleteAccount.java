package com.interstellarstudios.note_ify;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import es.dmoral.toasty.Toasty;

public class DeleteAccount extends AppCompatActivity {

    private Context context = this;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth mFireBaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        mFireBaseAuth = FirebaseAuth.getInstance();

        Button buttonDelete = findViewById(R.id.confirm_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.confirmPassword);
        ImageView logoImageView = findViewById(R.id.logoImageView);
        progressDialog = new ProgressDialog(context);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            logoImageView.setImageResource(R.drawable.name_logo);
            editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextPassword.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            editTextPassword.setHintTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            DrawableCompat.setTint(editTextPassword.getBackground(), ContextCompat.getColor(context, R.color.colorDarkThemeText));
            buttonDelete.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
        }
    }

    private void userLogin(){

        String email = editTextEmail.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toasty.info(context, "Please enter email.", Toast.LENGTH_LONG, true).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toasty.info(context, "Please enter your password.", Toast.LENGTH_LONG, true).show();
            return;
        }

        progressDialog.setMessage("Verifying");
        progressDialog.show();

        mFireBaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent i = new Intent(context, DeleteConfirm.class);
                            startActivity(i);
                        } else {
                            Toasty.error(context, "Email address or password incorrect, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }
}
