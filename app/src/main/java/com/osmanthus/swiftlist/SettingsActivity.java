package com.osmanthus.swiftlist;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private TextView transparencyAmtText;
    private SeekBar transparencyBar;

    private int transparencyAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences("AppSettings", 0);
        editor = sharedPreferences.edit();

        transparencyAmt = sharedPreferences.getInt("transparencyAmt", 100);

        transparencyAmtText = findViewById(R.id.transparencyAmountText);
        transparencyAmtText.setText(transparencyAmt + "%");

        transparencyBar = findViewById(R.id.transparencySeekBar);
        transparencyBar.setProgress(transparencyAmt);
        transparencyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                transparencyAmtText.setText(progress + "%");
                transparencyAmt = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d("BOOTY", "touch detected");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d("BOOTY", "untouch detected");
                editor.putInt("transparencyAmt", transparencyAmt);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        editor.commit();
        Log.d("BOOTY", "commited edit");
        //Save settings
    }
}
