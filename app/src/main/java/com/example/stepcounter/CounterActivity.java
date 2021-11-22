package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.permissionx.guolindev.PermissionX;

public class CounterActivity extends AppCompatActivity  implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean running = false;
    private float totalSteps = 0;
    private float previousTotalSteps = 0;
    private int goalSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PermissionX.init(this)
                .permissions(Manifest.permission.ACTIVITY_RECOGNITION)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
                    }
                });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            goalSteps = extras.getInt("steps");
            TextView textView = findViewById(R.id.tv_totalMax);
            textView.setText("/"+String.valueOf(goalSteps));
        }

        loadData();
        resetSteps();

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        running = true;

        Sensor stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor == null) {
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show();
        } else {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //TextView tv_stepsTaken = findViewById(R.id.tv_stepsTaken);
        CircularProgressBar circularProgressBar = findViewById(R.id.circularProgressBar);

        if (running) {
            totalSteps = event.values[0];
            int currentSteps = (int) totalSteps - (int) previousTotalSteps;
            //tv_stepsTaken.setText(currentSteps);

            circularProgressBar.setProgressWithAnimation((float) currentSteps);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void resetSteps() {
        //TextView tv_stepsTaken = findViewById(R.id.tv_stepsTaken);

        CounterActivity instance = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(instance, "Long tap to reset steps", Toast.LENGTH_SHORT).show();
            }
        };
        //tv_stepsTaken.setOnClickListener(listener);

        View.OnLongClickListener longListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                previousTotalSteps = totalSteps;

                //tv_stepsTaken.setText(String.valueOf(0));

                saveData();

                return true;
            }
        };
        //tv_stepsTaken.setOnLongClickListener(longListener);
    }

    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("key1", previousTotalSteps);
        editor.apply();
    }

    private void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        float savedNumber = sharedPreferences.getFloat("key1", 0);

        Log.d("CounterActivity", String.valueOf(savedNumber));

        previousTotalSteps = savedNumber;
    }
}