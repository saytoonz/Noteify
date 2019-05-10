package com.interstellarstudios.note_ify;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import es.dmoral.toasty.Toasty;

public class ForgotPassword extends AppCompatActivity implements View.OnClickListener {

    private EditText editTextEmail;
    private Button buttonSendLink;
    private TextView textViewSignin;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        buttonSendLink = (Button) findViewById(R.id.buttonSendLink);
        textViewSignin = (TextView) findViewById(R.id.textViewSignin);

        buttonSendLink.setOnClickListener(this);
        textViewSignin.setOnClickListener(this);
    }

    //Creating method for sending email link
    private void sendLink(){

        String email = editTextEmail.getText().toString().trim();

        if(TextUtils.isEmpty(email)){
            Toasty.info(ForgotPassword.this, "Please enter your registered email address.", Toast.LENGTH_LONG, true).show();
            return;
        }

        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        //checking for successful registration
                        if (task.isSuccessful()) {
                            Toasty.success(ForgotPassword.this, "Password reset email sent.", Toast.LENGTH_LONG, true).show();
                            startActivity(new Intent(getApplicationContext(), SignIn.class));
                            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                        }else{
                            Toasty.error(ForgotPassword.this, "Error sending password reset email.", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
    }

    @Override
    public void onClick(View view) {

        //Call method for sending link
        if(view == buttonSendLink){
            sendLink();
        }

        if(view == textViewSignin){
            startActivity(new Intent(ForgotPassword.this, SignIn.class));
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        startActivity(new Intent(ForgotPassword.this, SignIn.class));
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
