package com.interstellarstudios.note_ify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

public class Themes extends AppCompatActivity {

    private Context context = this;
    private Switch switchThemes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        final TextView themesTextView = findViewById(R.id.dark_theme_text);
        final TextView themesDescription = findViewById(R.id.dark_theme_description);
        switchThemes = findViewById(R.id.switchThemes);
        switchThemes.setChecked(switchThemesOnOff);

        //String colorLightThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorLightThemeText));
        String colorLightThemeString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimary));
        getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + "#000000" + "\">" + "Themes" + "</font>"));
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorLightThemeString)));

        if (switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Themes" + "</font>"));
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
            themesTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            themesDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
        }

        switchThemes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ConstraintLayout layout = findViewById(R.id.container);
                    layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    String colorDarkThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    String colorDarkThemeString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + colorDarkThemeTextString + "\">" + "Themes" + "</font>"));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorDarkThemeString)));
                    themesTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    themesDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

                    savePreferences();
                } else {
                    ConstraintLayout layout = findViewById(R.id.container);
                    layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    //String colorLightThemeTextString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    String colorLightThemeString = "#" + Integer.toHexString(ContextCompat.getColor(context, R.color.colorPrimary));
                    getSupportActionBar().setTitle(Html.fromHtml("<font color=\"" + "#000000" + "\">" + "Themes" + "</font>"));
                    getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor(colorLightThemeString)));
                    themesTextView.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
                    themesDescription.setTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));

                    savePreferences();
                }
            }
        });
    }

    public void savePreferences() {
        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("switchThemes", switchThemes.isChecked());
        prefsEditor.apply();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent i = new Intent(context, Home.class);
        startActivity(i);
    }
}
