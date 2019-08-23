package com.interstellarstudios.note_ify;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Window window = this.getWindow();
        final View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            themesTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            themesDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }

        switchThemes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    if (container != null) {
                        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                        container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    }
                    toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
                    toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    themesTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
                    themesDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

                    savePreferences();
                } else {
                    window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    if (container != null) {
                        container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                        container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    }
                    toolbar.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    toolbar.setTitleTextColor(ContextCompat.getColor(context, R.color.colorLightThemeText));
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
