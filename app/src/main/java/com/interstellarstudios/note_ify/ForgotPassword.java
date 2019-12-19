package com.interstellarstudios.note_ify;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
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

    private Context context = this;
    private EditText editTextEmail;
    private FirebaseAuth mFireBaseAuth;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mFireBaseAuth = FirebaseAuth.getInstance();

        editTextEmail = findViewById(R.id.edit_text_email);

        Button buttonSendLink = findViewById(R.id.button_send_link);
        buttonSendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendLink();
            }
        });

        TextView textViewSignIn = findViewById(R.id.text_view_go_to_sign_in);
        textViewSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        ImageView imageViewDarkMode = findViewById(R.id.image_view_dark_mode);
        ImageView imageViewLightMode = findViewById(R.id.image_view_light_mode);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        Window window = this.getWindow();
        View container = findViewById(R.id.container2);

        if (switchThemesOnOff) {

            imageViewLightMode.setVisibility(View.VISIBLE);
            imageViewDarkMode.setVisibility(View.GONE);

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }

            editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorPrimary));

        } else {

            imageViewLightMode.setVisibility(View.GONE);
            imageViewDarkMode.setVisibility(View.VISIBLE);

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
            }

            editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
            DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));
        }

        imageViewDarkMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewLightMode.setVisibility(View.VISIBLE);
                imageViewDarkMode.setVisibility(View.GONE);

                window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                if (container != null) {
                    container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                }

                editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
                DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorPrimary));

                saveDarkModePreference();
            }
        });

        imageViewLightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViewLightMode.setVisibility(View.GONE);
                imageViewDarkMode.setVisibility(View.VISIBLE);

                window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                if (container != null) {
                    container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                    container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                }

                editTextEmail.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                editTextEmail.setHintTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                DrawableCompat.setTint(editTextEmail.getBackground(), ContextCompat.getColor(context, R.color.colorLightThemeText));

                saveLightModePreference();
            }
        });
    }

    private void saveLightModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("switchThemes", false);
        prefsEditor.apply();
    }

    private void saveDarkModePreference() {

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putBoolean("switchThemes", true);
        prefsEditor.apply();
    }

    private void sendLink() {

        String email = editTextEmail.getText().toString().trim().toLowerCase();

        if (TextUtils.isEmpty(email)) {
            Toasty.info(context, "Please enter your registered email address", Toast.LENGTH_LONG, true).show();
            return;
        }

        mFireBaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            hideKeyboard(ForgotPassword.this);
                            onBackPressed();

                            Toasty.success(context, "Password reset email sent", Toast.LENGTH_LONG, true).show();
                        } else {
                            Toasty.error(context, "Error sending password reset email", Toast.LENGTH_LONG, true).show();
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
