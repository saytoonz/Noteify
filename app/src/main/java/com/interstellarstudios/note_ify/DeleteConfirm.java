package com.interstellarstudios.note_ify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import es.dmoral.toasty.Toasty;

public class DeleteConfirm extends AppCompatActivity {

    private Context context = this;
    private FirebaseAuth mFireBaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_confirm);

        mFireBaseAuth = FirebaseAuth.getInstance();

        ImageView logoImageView = findViewById(R.id.logoImageView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);

        Button confirm_delete = findViewById(R.id.confirm_delete);
        confirm_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseUser user = mFireBaseAuth.getCurrentUser();

                user.delete()
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Intent i = new Intent(context, Register.class);
                                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(i);
                                    finish();
                                    Toasty.success(context, "Your Account has been Deleted", Toast.LENGTH_LONG, true).show();
                                }
                            }
                        });
            }
        });

        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        boolean switchThemesOnOff = sharedPreferences.getBoolean("switchThemes", false);

        if(switchThemesOnOff) {
            ConstraintLayout layout = findViewById(R.id.container);
            layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDarkTheme));
            logoImageView.setImageResource(R.drawable.name_logo);
            descriptionTextView.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
            confirm_delete.setTextColor(ContextCompat.getColor(context, R.color.colorDarkThemeText));
        }
    }
}
