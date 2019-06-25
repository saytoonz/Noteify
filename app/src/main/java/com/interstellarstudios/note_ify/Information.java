package com.interstellarstudios.note_ify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Information extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information);

        ImageView logoImageView = findViewById(R.id.logoImageView);
        ImageView interstellarImageView = findViewById(R.id.interstellarImageView);

        ImageView websiteLink = findViewById(R.id.interstellarImageView);
        websiteLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://interstellarstudios.co.uk/"));
                startActivity(browserIntent);
            }
        });

        Button termsOfService = findViewById(R.id.terms_of_service);
        termsOfService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Information.this, TermsOfService.class);
                startActivity(i);
            }
        });

        Button privacyPolicy = findViewById(R.id.privacy_policy);
        privacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Information.this, PrivacyPolicy.class);
                startActivity(i);
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(Information.this, R.color.colorPrimaryDarkTheme));
            privacyPolicy.setTextColor(ContextCompat.getColor(Information.this, R.color.colorDarkThemeText));
            termsOfService.setTextColor(ContextCompat.getColor(Information.this, R.color.colorDarkThemeText));
            logoImageView.setImageResource(R.drawable.name_logo);
            interstellarImageView.setImageResource(R.drawable.interstellar_logo_white);
        }
    }
}
