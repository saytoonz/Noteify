package com.interstellarstudios.note_ify;

import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
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

    private Switch switchSecurity;
    private Switch switchPriorityColor;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView settingsHeading = findViewById(R.id.settingsHeading);
        TextView fingerprintTextView = findViewById(R.id.fingerprintTextView);
        TextView fingerprintDescription = findViewById(R.id.fingerprintDescription);
        TextView priorityTextView = findViewById(R.id.priorityTextView);
        TextView priorityDescription = findViewById(R.id.priorityDescription);
        ImageView settingsIcon = findViewById(R.id.settingsIcon);

        switchSecurity = findViewById(R.id.switchSecurity);
        switchSecurity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                        fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                        keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                        if (!keyguardManager.isKeyguardSecure()){
                            Toasty.error(Settings.this, "A PIN, password or pattern is required to use this feature.\nGo to 'Settings -> Security -> Screenlock' to set up a lock screen.", Toast.LENGTH_LONG, true).show();
                        } else {
                            savePreferences();
                        }
                    } else {
                        Toasty.error(Settings.this, "This feature is not supported on the installed version of Android.", Toast.LENGTH_LONG, true).show();
                    }
                } else {
                    savePreferences();
                }
            }
        });

        switchPriorityColor = findViewById(R.id.switchPriorityColor);
        switchPriorityColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                savePreferences();
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchSecurityOnOff = sharedPreferences.getBoolean("switchSecurity", false);
        boolean switchPriorityOnOff = sharedPreferences.getBoolean("switchPriorityColor", false);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        switchSecurity.setChecked(switchSecurityOnOff);
        switchPriorityColor.setChecked(switchPriorityOnOff);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(Settings.this, R.color.colorPrimaryDarkTheme));
            settingsHeading.setTextColor(ContextCompat.getColor(Settings.this, R.color.colorDarkThemeText));
            fingerprintTextView.setTextColor(ContextCompat.getColor(Settings.this, R.color.colorDarkThemeText));
            fingerprintDescription.setTextColor(ContextCompat.getColor(Settings.this, R.color.colorDarkThemeText));
            priorityTextView.setTextColor(ContextCompat.getColor(Settings.this, R.color.colorDarkThemeText));
            priorityDescription.setTextColor(ContextCompat.getColor(Settings.this, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(settingsIcon, ContextCompat.getColorStateList(this, R.color.colorDarkThemeText));
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
