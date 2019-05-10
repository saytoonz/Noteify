package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreen extends AppCompatActivity {

    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Loading shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        Boolean switchOnOff = sharedPreferences.getBoolean("switchSecurity", false);

        //If the security switch in Settings is on then do this
        if (switchOnOff == true) {
            EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                    .withFullScreen()
                    .withTargetActivity(FingerprintLogin.class) //Activity to launch after splash screen
                    .withSplashTimeOut(1000) //Timer for splash screen
                    .withBackgroundColor(Color.parseColor("#0f0f0f")) //Background color
                    //.withHeaderText("") //Insert header text here
                    //.withFooterText("") //Insert footer text here
                    //.withBeforeLogoText("") //Insert before logo text here
                    //.withAfterLogoText("") //Insert after logo text here
                    .withLogo(R.drawable.logo_splashscreen);

            /*config.getHeaderTextView().setTextColor(Color.WHITE);
            config.getFooterTextView().setTextColor(Color.WHITE);
            config.getBeforeLogoTextView().setTextColor(Color.WHITE);
            config.getAfterLogoTextView().setTextColor(Color.WHITE);*/

            View easySplashScreen = config.create();
            setContentView(easySplashScreen);
        } else {
            EasySplashScreen config = new EasySplashScreen(SplashScreen.this)
                    .withFullScreen()
                    .withTargetActivity(Register.class) //Activity to launch after splash screen
                    .withSplashTimeOut(1000) //Timer for splash screen
                    .withBackgroundColor(Color.parseColor("#0f0f0f")) //Background color
                    //.withHeaderText("") //Insert header text here
                    //.withFooterText("") //Insert footer text here
                    //.withBeforeLogoText("") //Insert before logo text here
                    //.withAfterLogoText("") //Insert after logo text here
                    .withLogo(R.drawable.logo_splashscreen);

            /*config.getHeaderTextView().setTextColor(Color.WHITE);
            config.getFooterTextView().setTextColor(Color.WHITE);
            config.getBeforeLogoTextView().setTextColor(Color.WHITE);
            config.getAfterLogoTextView().setTextColor(Color.WHITE);*/

            View easySplashScreen = config.create();
            setContentView(easySplashScreen);
        }
    }
}
