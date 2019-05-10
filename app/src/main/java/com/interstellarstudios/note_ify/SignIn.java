package com.interstellarstudios.note_ify;

import android.app.ProgressDialog;
import android.content.Context;
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
import spencerstudios.com.bungeelib.Bungee;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    private Context context = this;
    private Button buttonSignIn;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewRegister;
    private TextView textViewForgot;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonSignIn = (Button) findViewById(R.id.buttonSignIn);
        textViewRegister = (TextView) findViewById(R.id.textViewRegister);
        textViewForgot = (TextView) findViewById(R.id.textViewForgot);

        progressDialog = new ProgressDialog(this);

        buttonSignIn.setOnClickListener(this);
        textViewRegister.setOnClickListener(this);
        textViewForgot.setOnClickListener(this);
    }

    private void userLogin() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(SignIn.this, "Please enter your email address.", Toast.LENGTH_LONG, true).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toasty.info(SignIn.this, "Please enter your password.", Toast.LENGTH_LONG, true).show();
            return;
        }

        progressDialog.setMessage("Signing In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            final String current_user_id = firebaseAuth.getCurrentUser().getUid();
                            final FirebaseFirestore db = FirebaseFirestore.getInstance();

                            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                                @Override
                                public void onSuccess(InstanceIdResult instanceIdResult) {
                                    String deviceToken = instanceIdResult.getToken();

                                    Map<String, Object> userToken = new HashMap<>();
                                    userToken.put("User_Token_ID", deviceToken);

                                    DocumentReference userTokenDocumentPath = db.collection("Users").document(current_user_id).collection("User_Details").document("User_Token");
                                    userTokenDocumentPath.set(userToken);
                                }
                            });

                            Intent i = new Intent(SignIn.this, Collections.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            SignIn.this.finish();

                            Toasty.success(SignIn.this, "Sign In Successful.", Toast.LENGTH_LONG, true).show();
                            Bungee.zoom(context);
                        } else {
                            Toasty.error(SignIn.this, "Sign In Error, please try again. Please ensure that your email address and password are correct.", Toast.LENGTH_LONG, true).show();
                        }
                        progressDialog.dismiss();
                    }
                });
    }

    @Override
    public void onClick(View view) {
        if (view == buttonSignIn) {
            userLogin();
        }

        if (view == textViewRegister) {
            finish();
            startActivity(new Intent(SignIn.this, Register.class));
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        if (view == textViewForgot) {
            finish();
            startActivity(new Intent(SignIn.this, ForgotPassword.class));
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
