package com.interstellarstudios.note_ify;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;

public class Settings extends AppCompatActivity {

    private Context context = this;
    private Switch switchSecurity;
    private Switch switchPriorityColor;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        final TextView settingsHeading = findViewById(R.id.settingsHeading);
        final TextView fingerprintTextView = findViewById(R.id.fingerprintTextView);
        final TextView fingerprintDescription = findViewById(R.id.fingerprintDescription);
        final TextView priorityTextView = findViewById(R.id.priorityTextView);
        final TextView priorityDescription = findViewById(R.id.priorityDescription);
        final ImageView settingsIcon = findViewById(R.id.settingsIcon);

        switchSecurity = findViewById(R.id.switchSecurity);
        switchPriorityColor = findViewById(R.id.switchPriorityColor);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchSecurityOnOff = sharedPreferences.getBoolean("switchSecurity", false);
        boolean switchPriorityOnOff = sharedPreferences.getBoolean("switchPriorityColor", false);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        switchSecurity.setChecked(switchSecurityOnOff);
        switchPriorityColor.setChecked(switchPriorityOnOff);

        switchSecurity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                        if (keyguardManager != null) {
                            if (!keyguardManager.isKeyguardSecure()){
                                Toasty.error(context, "A PIN, password or pattern is required to use this feature.\nGo to 'Settings -> Security -> Screenlock' to set up a lock screen.", Toast.LENGTH_LONG, true).show();
                                switchSecurity.setChecked(false);
                            } else {
                                savePreferences();
                            }
                        }
                    } else {
                        Toasty.error(context, "This feature is supported on Android 6.0 (Marshmallow) and up", Toast.LENGTH_LONG, true).show();
                        switchSecurity.setChecked(false);
                    }
                } else {
                    savePreferences();
                }
            }
        });

        switchPriorityColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreferences();
            }
        });

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            settingsHeading.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            fingerprintTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            fingerprintDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            priorityTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            priorityDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(settingsIcon, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
        }
    }

    public void savePreferences() {
        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("switchSecurity", switchSecurity.isChecked());
        prefsEditor.putBoolean("switchPriorityColor", switchPriorityColor.isChecked());
        prefsEditor.apply();
    }
}
