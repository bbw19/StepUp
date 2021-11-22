package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.permissionx.guolindev.PermissionX;

public class MainActivity extends AppCompatActivity {
    NumberPicker numberPicker;
    int stepsValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        numberPicker = findViewById(R.id.numberPicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(50000);
    }

    public void onClick(View view) {
        stepsValue = numberPicker.getValue();

        if (stepsValue == 0) {
            Toast.makeText(this, "Please take at least one step!", Toast.LENGTH_SHORT).show();
        } else {
            Intent i = new Intent(MainActivity.this, StepCounterActivity.class);
            i.putExtra("steps", stepsValue);
            startActivity(i);
        }
    }
}