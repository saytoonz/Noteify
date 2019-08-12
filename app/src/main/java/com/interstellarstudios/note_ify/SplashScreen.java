package com.interstellarstudios.note_ify;

import android.content.SharedPreferences;
import android.graphics.Color;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean securitySwitchOn = sharedPreferences.getBoolean("switchSecurity", false);

        if (securitySwitchOn) {

            EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                    .withFullScreen()
                    .withTargetActivity(FingerprintLogin.class)
                    .withSplashTimeOut(0)
                    .withBackgroundColor(Color.parseColor("#0f0f0f"))
                    .withLogo(R.drawable.logo_splashscreen);

            View easySplashScreen = config.create();
            setContentView(easySplashScreen);

        } else {

            EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                    .withFullScreen()
                    .withTargetActivity(Register.class)
                    .withSplashTimeOut(0)
                    .withBackgroundColor(Color.parseColor("#0f0f0f"))
                    .withLogo(R.drawable.logo_splashscreen);

            View easySplashScreen = config.create();
            setContentView(easySplashScreen);
        }
    }
}
