package com.interstellarstudios.note_ify;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import es.dmoral.toasty.Toasty;

public class DeleteAccount extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        Button buttonDelete = findViewById(R.id.confirm_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userLogin();
            }
        });

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        progressDialog = new ProgressDialog(this);
    }

    private void userLogin(){

        String email = editTextEmail.getText().toString().trim();
        String password  = editTextPassword.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toasty.info(DeleteAccount.this, "Please enter email.", Toast.LENGTH_LONG, true).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toasty.info(DeleteAccount.this, "Please enter your password.", Toast.LENGTH_LONG, true).show();
            return;
        }

        progressDialog.setMessage("Verifying");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            Intent i = new Intent(DeleteAccount.this, DeleteConfirm.class);
                            startActivity(i);
                            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        } else {
                            Toasty.error(DeleteAccount.this, "Email address or password incorrect, please try again.", Toast.LENGTH_LONG, true).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
