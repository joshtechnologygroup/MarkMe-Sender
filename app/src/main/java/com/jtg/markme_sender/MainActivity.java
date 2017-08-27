package com.jtg.markme_sender;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Map;

import io.chirp.sdk.ChirpSDK;
import io.chirp.sdk.ChirpSDKListener;
import io.chirp.sdk.ChirpSDKStatusListener;
import io.chirp.sdk.model.Chirp;
import io.chirp.sdk.model.ChirpError;
import io.chirp.sdk.model.ChirpProtocolName;
import io.chirp.sdk.result.ChirpProtocolResult;

public class MainActivity extends AppCompatActivity {

    private ChirpSDK chirpSDK;
    private Button markEntryButton, markExitButton, setSecretCodeButton;
    private TextView statusText;
    private AudioManager audioManager;
    private String lastRequest = "";
    private String lastRequestType = "";
    private String userSecretCode;

    public final String ENTRY_CODE = "01";
    public final String EXIT_CODE = "02";

    public final String ACCEPT_MESSAGE = "%s Marked. :)";
    public final String REJECT_MESSAGE = "Request Taking Too Long/Failed. Try Again.";

    private void initSecretCode(){
        SharedPreferences sharedPreferences = getSharedPreferences(
            getString(R.string.preference_file_name),
            MODE_PRIVATE
        );
        this.userSecretCode = sharedPreferences.getString(getString(R.string.secret_code_key_name), "");
        if(this.userSecretCode.isEmpty()){
            chirpSDK.stop();
            Intent secretCodePage = new Intent(MainActivity.this, SecretCodeActivity.class);
            startActivity(secretCodePage);
        }
    }

    public ChirpSDKListener sdkListener = new ChirpSDKListener() {

        @Override
        public void onChirpHeard(final Chirp chirp) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String chirpData = chirp.getChirpData().getIdentifier();
                    System.out.println("Heard: " + chirpData + "\n");
                    System.out.println("lastreq: " + lastRequest + "\n");
                    if(chirpData.contentEquals(lastRequest)) {
                        System.out.println("in here");
                        String requestTypeText = "";
                        if(lastRequestType == ENTRY_CODE){
                            requestTypeText = "Entry";
                        }
                        else{
                            requestTypeText = "Exit";
                        }
                        System.out.println("in here 2" + String.format(ACCEPT_MESSAGE, requestTypeText));
                        statusText.setText(String.format(ACCEPT_MESSAGE, requestTypeText));
                        System.out.println("in here 3");
                        lastRequest = "";
                        lastRequestType = "";
                    }
                }
            });
        }

        @Override
        public void onChirpHearStarted() {
            System.out.println("Starting hearing");
        }

        @Override
        public void onChirpHearFailed() {
            System.out.println("hearing failed");
        }

        @Override
        public void onChirpError(ChirpError chirpError) {
            System.out.println("hearing error "+ chirpError.getMessage());
        }
    };

    protected void initChirp() {
        String API_KEY = "xhYw3G5PKSHzzy1rIBoOrMSbZ";
        String API_SECRET = "tou2ncHSbP6uE2qihwdhHItO4AqGm0abOmWRq1KVxvzSIuMxew";

        this.chirpSDK = new ChirpSDK(this.getApplicationContext(), API_KEY, API_SECRET, new ChirpSDKStatusListener() {

            @Override
            public void onAuthenticationSuccess() {
                System.out.println("Auth - ok");
            }

            @Override
            public void onChirpError(ChirpError chirpError) {
                System.out.println("init " + chirpError.getMessage());
            }
        });

        ChirpProtocolResult chirpProtocolResult = chirpSDK.setProtocolNamed(ChirpProtocolName.ChirpProtocolNameUltrasonic);

        if (chirpProtocolResult != ChirpProtocolResult.OK) {
            System.out.println("Error setting ultrasonic mode: " + chirpProtocolResult.getDescription());
        }

        chirpSDK.setListener(sdkListener);
    }

    protected void sendChirp(String chirpData, String lastRequestType) {
        int origVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        System.out.println(origVolume + " , " + maxVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
        String combinedChirpData = chirpData + lastRequestType;
        System.out.println("1 -- " + combinedChirpData);
        this.lastRequest = combinedChirpData;
        this.lastRequestType = lastRequestType;
        statusText.setText(".....");
        chirpSDK.chirp(new Chirp(combinedChirpData));
//            TODO: Reset volume to original after sending data.
//            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, origVolume, 0);
        new Handler().postDelayed(new Runnable() {
            public void run() {
                // If last request is still pending.
                if(!lastRequest.isEmpty())
                    statusText.setText(REJECT_MESSAGE);
            }
        }, 4000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.initSecretCode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.markEntryButton = (Button) this.findViewById(R.id.markEntryButton);
        this.markExitButton = (Button) this.findViewById(R.id.markExitButton);
        this.setSecretCodeButton = (Button) this.findViewById(R.id.setSecretCodeButton);
        this.statusText = (TextView) this.findViewById(R.id.statusText);

        this.initChirp();
        this.audioManager = (AudioManager) getApplicationContext().getSystemService(
            this.getApplicationContext().AUDIO_SERVICE
        );

        chirpSDK.start();
        chirpSDK.enableLogs();

        this.markEntryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSecretCode();
                sendChirp(userSecretCode, ENTRY_CODE);
            }
        });

        this.markExitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSecretCode();
                sendChirp(userSecretCode, EXIT_CODE);
            }
        });

        this.setSecretCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chirpSDK.stop();
                Intent secretCodePage = new Intent(MainActivity.this, SecretCodeActivity.class);
                startActivity(secretCodePage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

}
