package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.wear.widget.BoxInsetLayout;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class FitActivity extends WearableActivity implements SensorEventListener {
    ConstraintLayout mLayout;
    TextView text_fit;
    private boolean storeData=true;

    private MyDatabase db;
    private String Date;

    public static List<SensorData> Liste_HR;

    private ProgressBar hr_progressbar1 ,hr_progressbar2 ;
    private TextView hr_value;

    private Button DoneBtn;


    public FitActivity(){
        Liste_HR = new ArrayList<SensorData>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit);
        mLayout = findViewById(R.id.layout_fit);
        setAmbientEnabled();

        db = MyDatabase.getDatabase(getApplicationContext());

        hr_progressbar1 = findViewById(R.id.progressBar1);
        hr_progressbar1.setProgress(0);
        hr_progressbar2 = findViewById(R.id.progressBar2);
        hr_progressbar2.setProgress(0);

        hr_value = findViewById(R.id.hr_value);
        hr_value.setText("0");

        DoneBtn = findViewById(R.id.DoneBtn);


        //Asking for permission to access HR sensor
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M //M is for Marshmallow
                && checkSelfPermission("android.permission.BODY_SENSORS") ==
                PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.BODY_SENSORS"}, 0);
        }

        //Registering listener for HR sensor
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            Sensor hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
            sensorManager.registerListener(this, hrSensor, SensorManager.SENSOR_DELAY_UI);
        }

        DoneBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Sensor hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                sensorManager.unregisterListener(FitActivity.this,hrSensor);
                Intent intent = new Intent(FitActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }


    private void updateDisplay() {
        if (isAmbient()) {
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
        } else {
            mLayout.setBackground(null);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {



        int hrvalue =(int) (0.5*sensorEvent.values[0]);

        if (hrvalue!= 0) {
            hr_progressbar1.setProgress(hrvalue);
            hr_progressbar2.setProgress(hrvalue);
            hr_value.setText(String.valueOf(sensorEvent.values[0]));


            Intent intent = new Intent(FitActivity.this, WearListenerService.class);
            intent.setAction(WearListenerService.ACTION_SEND_HEART_RATE);
            intent.putExtra(WearListenerService.DATAMAP_INT_HEART_RATE, (int) sensorEvent.values[0]);

            startService(intent);
        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}