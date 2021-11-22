package com.example.stepcounter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;
import com.permissionx.guolindev.PermissionX;

public class StepCounterActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private boolean running = false;
    private float totalSteps = 0;
    private float previousTotalSteps = 0;
    private int goalSteps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            goalSteps = extras.getInt("steps");
            TextView textView = findViewById(R.id.tv_totalMax);
            textView.setText("/"+String.valueOf(goalSteps));
        }

        PermissionX.init(this)
                .permissions(Manifest.permission.ACTIVITY_RECOGNITION)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        Toast.makeText(this, "All permissions are granted", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "These permissions are denied: $deniedList", Toast.LENGTH_LONG).show();
                    }
                });

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
        TextView tv_stepsTaken = findViewById(R.id.tv_stepsTaken);
        CircularProgressBar circularProgressBar = findViewById(R.id.circularProgressBar);

        if (running) {
            totalSteps = event.values[0];
            int currentSteps = (int) totalSteps - (int) previousTotalSteps;
            tv_stepsTaken.setText(currentSteps);

            circularProgressBar.setProgressWithAnimation((float) currentSteps);

            if (currentSteps >= goalSteps) {
                createNotificationChannel();
                createNotification();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void resetSteps() {
        TextView tv_stepsTaken = findViewById(R.id.tv_stepsTaken);

        StepCounterActivity instance = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(instance, "Long tap to reset steps", Toast.LENGTH_SHORT).show();
            }
        };
        tv_stepsTaken.setOnClickListener(listener);

        View.OnLongClickListener longListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                previousTotalSteps = totalSteps;

                tv_stepsTaken.setText(String.valueOf(0));

                saveData();

                return true;
            }
        };
        tv_stepsTaken.setOnLongClickListener(longListener);
    }

    public void imDone(View view) {
        goalSteps = (int) totalSteps - (int) previousTotalSteps;
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

    public void cancel(View view) {

        Intent i = new Intent(StepCounterActivity.this, MainActivity.class);
        startActivity(i);
    }

    private static final String CHANNEL_ID = "my_chanel";

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("My notification")
                .setContentText("Much longer text that cannot fit one line...")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Much longer text that cannot fit one line..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1, builder.build());
    }


}