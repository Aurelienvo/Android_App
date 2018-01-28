package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.constraint.ConstraintLayout;
import android.support.wear.widget.BoxInsetLayout;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class OutsideActivity extends WearableActivity implements SensorEventListener, LocationListener {

    ConstraintLayout mLayout;

    private Chronometer Chronometer;
    private static boolean store_data = false;
    private static double ALT_stock, LON_stock, LAT_stock;
    public static int nbr_data=0;
    private static boolean first_start=true;
    private TextView hr_value, altitude_value;
    private String date;

    private Button BtnStopOutside,BtnStart, BtnPause;
    private long timeWhenStopped = 0;

    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outside);
        mLayout = findViewById(R.id.layout_gps);

        Chronometer = (Chronometer) findViewById(R.id.chronometer1);
        BtnStart = (Button) findViewById(R.id.butonStart);
        hr_value = (TextView) findViewById(R.id.heart_rate);
        hr_value.setText("0");
        altitude_value = (TextView) findViewById(R.id.altitude_value);
        altitude_value.setText("0");

        setAmbientEnabled();

        db = MyDatabase.getDatabase(getApplicationContext());

        BtnStopOutside = (Button) findViewById(R.id.butonStop);
        BtnStart = (Button) findViewById(R.id.butonStart);
        BtnPause = (Button) findViewById(R.id.butonPause);



        //Asking for permission to access fine location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission("android.permission.ACCESS_FINE_LOCATION") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 0);
        }

        //Asking for permission to access coarse location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == PackageManager.PERMISSION_DENIED) {
            requestPermissions(new String[]{"android.permission.ACCESS_COARSE_LOCATION"}, 0);
        }

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

        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);


        BtnStopOutside.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Sensor hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
                sensorManager.unregisterListener(OutsideActivity.this,hrSensor);
                locationManager.removeUpdates(OutsideActivity.this);
                Intent intent = new Intent(OutsideActivity.this, SendActivity.class);
                startActivity(intent);
            }
        });

        BtnStart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(first_start) {
                    Chronometer.setBase(SystemClock.elapsedRealtime());
                    first_start=false;
                    Chronometer.start();
                }
                Chronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                Chronometer.start();
                store_data = true;
            }
        });

        BtnPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                timeWhenStopped = Chronometer.getBase() - SystemClock.elapsedRealtime();
                Chronometer.stop();
                store_data = false;
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

    public void onSensorChanged(SensorEvent sensorEvent) {

        SimpleDateFormat sdf_MMddyyyy = new SimpleDateFormat(getString(R.string.sdf_yyyyMMddTHHmmssZ));
        date = sdf_MMddyyyy.format(Calendar.getInstance().getTime());
        Log.i("Date","Date de la data****************" + String.valueOf(date)+"********");





        SensorData sensorData_Lat = new SensorData();
        sensorData_Lat.timestamp = String.valueOf(date);
        sensorData_Lat.type = SensorData.LATITUDE;
        sensorData_Lat.value = LAT_stock;

        SensorData sensorData_Lon = new SensorData();
        sensorData_Lon.timestamp = String.valueOf(date);
        sensorData_Lon.type = SensorData.LONGITUDE;
        sensorData_Lon.value = LON_stock;

        SensorData sensorData_Alt= new SensorData();
        sensorData_Alt.timestamp = String.valueOf(date);
        sensorData_Alt.type = SensorData.ALTITUDE;
        sensorData_Alt.value = ALT_stock;

        SensorData sensorData_Hr = new SensorData();
        sensorData_Hr.timestamp = String.valueOf(date);
        sensorData_Hr.type = SensorData.HEART_RATE;
        sensorData_Hr.value = sensorEvent.values[0];

        hr_value.setText(String.valueOf(sensorEvent.values[0]));
        altitude_value.setText(String.valueOf(ALT_stock));

        Log.i("Valeur Capteurs", "onSensorChanged entered");
        Log.i("Valeurs des capteurs", "Date" + String.valueOf(date) + "\nLat: " + String.valueOf(LAT_stock) +
                "\nLong: "+ String.valueOf(LON_stock) +
                "\nALt: "+ String.valueOf(ALT_stock) + "\nHR: " + String.valueOf(sensorEvent.values[0]));


        if(store_data) {
            Log.i("Store Data","Début Storage Data ######################################");
            ArrayList<SensorData> List_async = new ArrayList<>();
            List_async.add(sensorData_Alt);
            List_async.add(sensorData_Lat);
            List_async.add(sensorData_Lon);
            List_async.add(sensorData_Hr);

            nbr_data++;

            AsyncTaskWrite locationAsyncTask = new AsyncTaskWrite(db);
            locationAsyncTask.execute(List_async);
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i("GPS Value","Je viens de choper une nouvelle mesure de GPS *********");
        LAT_stock = location.getLatitude();
        LON_stock = location.getLongitude();
        ALT_stock = location.getAltitude();

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public void onAccuracyChanged(Sensor sensor, int i) {

    }

}