package com.interstellarstudios.note_ify.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import com.interstellarstudios.note_ify.R;
import static android.content.Context.MODE_PRIVATE;

public class WebsiteFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_website, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        ImageView interstellarImage = getView().findViewById(R.id.interstellar_logo);
        interstellarImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://interstellarstudios.co.uk/"));
                startActivity(browserIntent);
            }
        });

        ImageView webImage = getView().findViewById(R.id.web_logo);
        webImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://interstellarstudios.co.uk/"));
                startActivity(browserIntent);
            }
        });

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", true);

        if(switchThemesOnOff) {
            ConstraintLayout layout = getView().findViewById(R.id.container2);
            layout.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.colorPrimaryDarkTheme));
            interstellarImage.setImageResource(R.drawable.interstellar_logo_white);
            ImageViewCompat.setImageTintList(webImage, ContextCompat.getColorStateList(getContext(), R.color.colorDarkThemeText));
        }
    }
}
