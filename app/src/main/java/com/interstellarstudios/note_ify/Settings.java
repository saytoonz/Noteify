package com.interstellarstudios.note_ify;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.interstellarstudios.note_ify.repository.Repository;
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

        TextView settingsHeading = findViewById(R.id.settingsHeading);
        TextView fingerprintTextView = findViewById(R.id.fingerprintTextView);
        TextView fingerprintDescription = findViewById(R.id.fingerprintDescription);
        TextView priorityTextView = findViewById(R.id.priorityTextView);
        TextView priorityDescription = findViewById(R.id.priorityDescription);
        ImageView settingsIcon = findViewById(R.id.settingsIcon);
        TextView searchHistoryTextView = findViewById(R.id.search_history);
        TextView searchHistoryDescriptionTextView = findViewById(R.id.search_history_description);

        switchSecurity = findViewById(R.id.switchSecurity);
        switchPriorityColor = findViewById(R.id.switchPriorityColor);

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchSecurityOnOff = sharedPreferences.getBoolean("switchSecurity", false);
        boolean switchPriorityOnOff = sharedPreferences.getBoolean("switchPriorityColor", false);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        TextView versionText = findViewById(R.id.version_text);

        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info != null) {
            String version = "Version " + info.versionName;
            versionText.setText(version);
        }

        switchSecurity.setChecked(switchSecurityOnOff);
        switchPriorityColor.setChecked(switchPriorityOnOff);

        switchSecurity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                    if (keyguardManager != null) {
                        if (!keyguardManager.isKeyguardSecure()) {
                            Toasty.error(context, "A PIN, password or pattern is required to use this feature.\nGo to 'Settings -> Security -> Screenlock' to set up a lock screen.", Toast.LENGTH_LONG, true).show();
                            switchSecurity.setChecked(false);
                        } else {
                            savePreferences();
                        }
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

        ImageView clearSearchHistory = findViewById(R.id.clear_button);
        clearSearchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                YoYo.with(Techniques.Wobble)
                        .duration(750)
                        .playOn(clearSearchHistory);

                Repository repository = new Repository(getApplication());
                repository.deleteAllRecentSearches();
                Toasty.success(context, "Search history cleared", Toast.LENGTH_LONG, true).show();
            }
        });

        Window window = this.getWindow();
        View container = findViewById(R.id.container);

        if (switchThemesOnOff) {

            if (container != null) {
                container.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            }
            settingsHeading.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            fingerprintTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            fingerprintDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            priorityTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            priorityDescription.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            ImageViewCompat.setImageTintList(settingsIcon, ContextCompat.getColorStateList(context, R.color.colorDarkThemeText));
            searchHistoryTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            searchHistoryDescriptionTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            versionText.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));

        } else {

            window.setStatusBarColor(ContextCompat.getColor(context, R.color.colorPrimary));
            if (container != null) {
                container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
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
