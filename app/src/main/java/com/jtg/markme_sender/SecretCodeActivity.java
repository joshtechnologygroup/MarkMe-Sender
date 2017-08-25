package com.jtg.markme_sender;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

import io.chirp.sdk.model.Chirp;

public class SecretCodeActivity extends AppCompatActivity {

    private Button secretCodeUpdateButton, secretCodeResetButton;
    private TextView secretCodeText;

    private void modifySecretCode(String code){
        SharedPreferences sharedPreferences = getSharedPreferences(
            getString(R.string.preference_file_name),
            MODE_PRIVATE
        );
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getString(R.string.secret_code_key_name), code);
        editor.commit();
        Toast.makeText(getApplicationContext(), "Code Updated: " + code, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secret_code);

        this.secretCodeUpdateButton = (Button) this.findViewById(R.id.secretCodeUpdateButton);
        this.secretCodeResetButton = (Button) this.findViewById(R.id.secretCodeResetButton);
        this.secretCodeText = (TextView) this.findViewById(R.id.secretCodeText);

        this.secretCodeUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = secretCodeText.getText().toString();
                modifySecretCode(code);
            }
        });

        this.secretCodeResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            modifySecretCode("");
            }
        });
    }
}
