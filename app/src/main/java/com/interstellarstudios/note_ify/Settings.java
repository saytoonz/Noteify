package com.interstellarstudios.note_ify;

import android.app.KeyguardManager;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import es.dmoral.toasty.Toasty;

public class Settings extends AppCompatActivity {

    private Switch switchSecurity;
    private Switch switchPriorityColor;
    private static final String SWITCHSECURITY = "switchSecurity";
    private static final String SWITCHPRIORITYCOLOR = "switchPriorityColor";
    private boolean switchOnOff;
    private boolean switchOnOff2;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

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
                if (isChecked){
                    savePreferences();
                } else {
                    savePreferences();
                }
            }
        });

        loadData();
        updateViews();
    }

    public void savePreferences() {
        SharedPreferences myPrefs = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean("switchSecurity", switchSecurity.isChecked());
        prefsEditor.putBoolean("switchPriorityColor", switchPriorityColor.isChecked());
        prefsEditor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        switchOnOff = sharedPreferences.getBoolean(SWITCHSECURITY, false);
        switchOnOff2 = sharedPreferences.getBoolean(SWITCHPRIORITYCOLOR, false);
    }

    public void updateViews() {
        switchSecurity.setChecked(switchOnOff);
        switchPriorityColor.setChecked(switchOnOff2);
    }

    @Override
    public void onBackPressed () {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
