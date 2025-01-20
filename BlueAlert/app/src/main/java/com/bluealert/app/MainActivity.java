package com.bluealert.app;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mqttapp.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import android.app.Notification;
import android.app.NotificationManager;
import androidx.core.app.NotificationCompat;
import android.content.Intent;
import android.app.PendingIntent;
import com.github.anastr.speedviewlib.SpeedView;
import com.google.android.material.slider.Slider;

public class MainActivity extends AppCompatActivity {

    private static final String BROKER_URL = "tcp://192.168.0.108:1883";
    private static final String CLIENT_ID = "android_app";
    private MqttHandler mqttHandler;
    private TextView temperatureValue;

    private TextView sliderValue;
    private Slider slider;
    private Button updateSpeedButton;
    private int message_speed = 2;

    private SpeedView humidityView;
    private SpeedView ppmView;
    private Switch statusSwitch;

    private static final String CHANNEL_ID = "default_channel";

    GraphView graph;

    LineGraphSeries<DataPoint> series;

    private float x = 1;
    private float xP = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttHandler = new MqttHandler(this);
        mqttHandler.connect(BROKER_URL,CLIENT_ID);


        // Initialise slider and message speed
        slider = findViewById(R.id.slider);
        sliderValue = findViewById(R.id.sliderValue);
        updateSpeedButton = findViewById(R.id.updateSpeedButton);

        // Set initial slider value
        slider.setValue(message_speed);

        // Set a listener for value changes
        slider.addOnChangeListener((slider, value, fromUser) -> {
            message_speed = (int) value; // Update the speed value (cast to int if necessary)
            sliderValue.setText(String.valueOf(message_speed) + " s"); // Update the UI to show the current value


        });

        updateSpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Publish the new speed value to the MQTT topic
                publishMessage("actuatori/viteza", String.valueOf(message_speed));
            }
        });



        // Check and request notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestNotificationPermission();
        }

        // Change the status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            // Set the color of the status bar
            window.setStatusBarColor(getResources().getColor(R.color.statusBarColor)); // You can change the color
        }

        statusSwitch = findViewById(R.id.switchState);

        // Set a listener to handle switch state changes
        statusSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                // Switch is ON, publish "on" to the topic
                publishMessage("actuatori/status", "on");
            } else {
                // Switch is OFF, publish "off" to the topic
                publishMessage("actuatori/status", "off");
            }
        });




        // Create notification channel if needed (Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Default Channel";
            String description = "This is the default notification channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Initialize the GraphView and Series
        graph = findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        graph.addSeries(series);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(true);

        temperatureValue = findViewById(R.id.temperatureValue);
        humidityView = findViewById(R.id.humidityGauge);
        humidityView.speedPercentTo(40);
        ppmView = findViewById(R.id.ppmGauge);
        ppmView.speedPercentTo(33);

        // Subscribe to the topics
        subscribeToTopic("senzori/temperatura");
        subscribeToTopic("senzori/umiditate");
        subscribeToTopic("senzori/ppm");
        subscribeToTopic("actuatori/update_status");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            }
        }
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
    }

    // Method to handle the arrival of the message on the subscribed topic
    public void onMessageArrived(String topic, String message) {
        runOnUiThread(() -> {
            switch (topic) {
                case "senzori/temperatura":
                    temperatureValue.setText(message + " °C");

                    // Parse the temperature value
                    double y = Double.parseDouble(message);

                    // Append the new data point to the graph
                    addDataPoint(x, y);
                    x++; // Increment x for the next point
                    break;

                case "senzori/umiditate":
                    int humidityVal = (int) Float.parseFloat(message);

                    // Update humidity gauge
                    humidityView.speedTo(humidityVal, 10);

                    break;

                case "senzori/ppm":
                    int ppmVal = (int) Float.parseFloat(message);

                    // Update PPM gauge
                    ppmView.speedPercentTo(ppmVal / 10, 10);

                    if (ppmVal > 600) {
                        sendNotification("Warning! Air quality is very low", "The air quality has reached dangerous levels.");
                    }

                    break;

                case "actuatori/update_status":
                    statusSwitch.setChecked("on".equals(message));
                    break;

                default:
                    Log.w("MQTT", "Unhandled topic: " + topic);
                    break;
            }
        });
    }


    private void publishMessage(String topic, String message){
        mqttHandler.publish(topic,message);
    }
    private void subscribeToTopic(String topic){
        mqttHandler.subscribe(topic);
    }

    private void unsubscribeFromTopic(String topic){
        mqttHandler.unsubscribe(topic);
    }

    private void sendNotification(String title, String message) {
        Intent intent = new Intent(this, MainActivity.class);  // Open MainActivity when notification is tapped
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);  // Add intent to open activity

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
    }

    private void requestNotificationPermission() {
        ActivityResultLauncher<String> requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                    if (!isGranted) {
                        Toast.makeText(this, "Notification permissions are required for full functionality.", Toast.LENGTH_SHORT).show();
                    }
                });

        // Request permission
        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS);
    }

    private void addDataPoint(double x, double y) {
        series.appendData(new DataPoint(x, y), true, 50); // Keep the latest 50 points in the series

        // Center the graph around the new data point
        double viewportStart = Math.max(x - 13, 0); // Center X-axis around the new point (15 points wide)
        double viewportEnd = viewportStart + 16;     // Show 15 points at a time
        graph.getViewport().setMinX(viewportStart);
        graph.getViewport().setMaxX(viewportEnd);

        double padding = 5; // Adjust this for more or less space around the point
        double yViewportStart = Math.max(y - padding, 0); // Ensure Y-axis does not go negative
        double yViewportEnd = y + padding;
        graph.getViewport().setMinY(yViewportStart);
        graph.getViewport().setMaxY(yViewportEnd);
    }

    private void turnFlashlightOn() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0]; // Usually, 0 is the rear camera
            cameraManager.setTorchMode(cameraId, true);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnFlashlightOff() {
        CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error turning flashlight off", Toast.LENGTH_SHORT).show();
        }
    }


}