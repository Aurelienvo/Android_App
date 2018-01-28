package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import org.joda.time.DateTime;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

public class SyncActivity extends AppCompatActivity
{
    //BroadCast //
        // Action ID //
    public static final String ACTION_SEND_HEART_RATE = "ACTION_SEND_HEART_RATE";
    public static final String ACTION_SEND_LOCATION="ACTION_SEND_LOCATION";
    public static final String ACTION_SEND_LIST_SENSOR_DATA="ACTION_SEND_LIST_SENSOR_DATA";
    public static final String ACTION_SEND_SENSOR_DATA="ACTION_SEND_SENSOR_DATA";

        // PutExtra parameter ID //
    public static final String INT_HEART_RATE ="INT_HEART_RATE";
    public static final String INTEGER_KEY_LOCATION="INTEGER_KEY_LOCATION";
    public static final String FLOAT_ARRAY_LOCATION="FLOAT_ARRAY_LOCATION";
    public static final String SENSOR_LIST_DATA="SENSOR_LIST_DATA";
    public static final String SENSOR_DATA="SENSOR_DATA";

    ArrayList<SensorData> list_sensordata;

    // Add this function to receive data on the activity you want //
    // Here we just change the textview but we can do something else after //
    private BroadcastReceiver mHeartRateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            int heartRateWatch = intent.getIntExtra(INT_HEART_RATE, -1);
        }
    };

    private BroadcastReceiver mSensorDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            SensorDataWrapper sd_stock_wrapper = (SensorDataWrapper) intent.getSerializableExtra(SENSOR_LIST_DATA);
            list_sensordata = sd_stock_wrapper.getList_sd();

            for(SensorData sd : list_sensordata)
            {
                Log.v("Check Value", "Value " +
                        String.valueOf(sd.uid) + "****" +
                        String.valueOf(sd.type) + "****" +
                        String.valueOf(sd.timestamp) + "****" +
                        String.valueOf(sd.value) + "***************");
            }
        }
    };

    // Layout //
    Button btnSync;

    // Toolbar //
    private Toolbar toolbarSync;

    // ProgressBar //
    private ProgressDialog progressDialogSync;

    // FireBase Database //
    private DatabaseReference mGPSDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;

    // FireBase Storage //
    private StorageReference mGPSStorage;

    // Write/Read File //
    private File path;
    private File file;
    final private String fileName = "gps.csv";
    private GPS gps;
    private List<DateTime> time = new ArrayList<DateTime>();
    private List<LatLng> latitude_longitude = new ArrayList<LatLng>();
    private List<Double> elevation = new ArrayList<Double>();
    private List<Double> heart_rate = new ArrayList<Double>();

    // Time //
    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        // Broadcast //
        LocalBroadcastManager.getInstance(this).registerReceiver(mHeartRateReceiver,new IntentFilter(ACTION_SEND_HEART_RATE));
        LocalBroadcastManager.getInstance(this).registerReceiver(mSensorDataReceiver,new IntentFilter(ACTION_SEND_LIST_SENSOR_DATA));

        // File path //
        path = getFilesDir().getAbsoluteFile();

        // Date //
        currentDate = Calendar.getInstance().getTime();

        // Layout //
        btnSync = (Button) findViewById(R.id.btnSync);

        // Toolbar //
        toolbarSync = (Toolbar) findViewById(R.id.toolbarSync);
        setSupportActionBar(toolbarSync);
        getSupportActionBar().setTitle(getString(R.string.toolbar_sync));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogSync = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();
        mGPSDatabase = FirebaseDatabase.getInstance().getReference().child("GPS").child(mCurrentUserID);
        // Firebase offline capability //
        mGPSDatabase.keepSynced(true);

        // FireBase Storage //
        mGPSStorage = FirebaseStorage.getInstance().getReference().child("GPS").child(mCurrentUserID);

        // Save Button click //
        btnSync.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogSync.setTitle((getString(R.string.saving_changes)));
                progressDialogSync.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogSync.setCanceledOnTouchOutside(false);
                progressDialogSync.show();

                // GPS Data //
                gps = sensorDataToGPS(list_sensordata);

                // Write hr.csv file //
                file = new File(path, fileName);
                writeCSVFileGPS(file, gps);

                // Load the uri from hr.csv file //
                Uri fileUri = Uri.fromFile(file);

                // Storage of gps.csv to GPS Storage //
                StorageReference filepath = mGPSStorage.child("gps.csv");
                filepath.putFile(fileUri).addOnCompleteListener((new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            // Add a part of HashMap //
                            Map updateHashMap = new HashMap<>();
                            updateHashMap.put("gps", downloadUrl);
                            updateHashMap.put("date", new SimpleDateFormat("MM/dd/yyyy").format(currentDate));
                            // Storage of URL from gps.csv to GPS DataBase //
                            mGPSDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        progressDialogSync.dismiss();
                                        Toast.makeText(SyncActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            progressDialogSync.dismiss();
                            Toast.makeText(SyncActivity.this, getString(R.string.file_not_uploaded), Toast.LENGTH_LONG).show();
                        }
                    }
                }));
                file.delete();
            }
        });
    }

    // Convert SensorData object to GPS object //
    private GPS sensorDataToGPS(ArrayList<SensorData> dataList)
    {
        SimpleDateFormat sdf_yyyyMMddTHHmmssZ = new SimpleDateFormat(getString(R.string.sdf_yyyyMMddTHHmmssZ));
        sdf_yyyyMMddTHHmmssZ.setTimeZone(TimeZone.getTimeZone("GMT"));

        GPS gps;
        List<DateTime> time = new ArrayList<DateTime>();
        List<LatLng> latitude_longitude = new ArrayList<LatLng>();
        List<Double> latitude = new ArrayList<Double>();
        List<Double> longitude = new ArrayList<Double>();
        List<Double> elevation = new ArrayList<Double>();
        List<Double> heart_rate = new ArrayList<Double>();

        for(SensorData sd : dataList)
        {
            int type = sd.type;
            switch (type) {


                case SensorData.LATITUDE:
                    try
                    {
                        time.add(new DateTime(sdf_yyyyMMddTHHmmssZ.parse(sd.timestamp)));
                    }
                    catch (java.text.ParseException e)
                    {
                        Log.e("Exception", "Parse error: " + e.toString());
                    }

                    latitude.add(sd.value);

                    break;

                case SensorData.LONGITUDE:
                    longitude.add(sd.value);
                    break;

                case SensorData.ALTITUDE:
                    elevation.add(sd.value);
                    break;

                case SensorData.HEART_RATE:
                    heart_rate.add(sd.value);
                    break;

                default:
                    break;
            }
        }

        for(int i = 0; i < latitude.size(); i++)
        {
            latitude_longitude.add(new LatLng(latitude.get(i),longitude.get(i)));
        }

        Collections.reverse(time);
        Collections.reverse(latitude_longitude);
        Collections.reverse(elevation);
        Collections.reverse(heart_rate);

        gps = new GPS(time,latitude_longitude,elevation,heart_rate);
        return gps;
    }

    // Write a local file from a HeartRate data //
    private File writeCSVFileGPS(File file, GPS gps)
    {
        // Delimiter used in .csv file //
        final String COMMA_DELIMITER = ";";

        //final String NEW_LINE_SEPARATOR = ";;;\n";
        final String NEW_LINE_SEPARATOR = "\n";

        // .csv file header //
        final String FILE_HEADER = "time [yyyy-MM-dd'T'HH:mm:ss'Z'];lat [dec. deg.];lon [dec. deg.];ele [m];hr [bpm]";

        // Display of times //
        SimpleDateFormat sdf_mmss = new SimpleDateFormat(getString(R.string.sdf_yyyyMMddTHHmmssZ));
        sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Time data //
        List<DateTime> time = new ArrayList<DateTime>();
        time = gps.getTime();

        // Latitude and Longitude data //
        List<LatLng> latitude_longitude = new ArrayList<LatLng>();
        latitude_longitude = gps.getLatitudeLongitude();

        // Elevation data //
        List<Double> elevation = new ArrayList<Double>();
        elevation = gps.getElevation();

        // Heart Rate data //
        List<Double> heart_rate = new ArrayList<Double>();
        heart_rate = gps.getHeartRate();

        try
        {
            FileOutputStream fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            // Write the .csv file header //
            osw.write(FILE_HEADER);

            // Add a new line separator after the header //
            osw.write(NEW_LINE_SEPARATOR);

            // Write new heart_rate/time data //
            for(int i = 0; i < time.size(); i++)
            {
                osw.write(sdf_mmss.format(new Date((long) (time.get(i).toDate().getTime()))));
                osw.write(COMMA_DELIMITER);
                osw.write(String.valueOf(latitude_longitude.get(i).latitude));
                osw.write(COMMA_DELIMITER);
                osw.write(String.valueOf(latitude_longitude.get(i).longitude));
                osw.write(COMMA_DELIMITER);
                osw.write(elevation.get(i).toString());
                osw.write(COMMA_DELIMITER);
                osw.write(heart_rate.get(i).toString());
                osw.write(NEW_LINE_SEPARATOR);
            }

            // Close file //
            osw.close();
        }
        catch (FileNotFoundException e)
        {
            Log.e("Exception", "File not found: " + e.toString());
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }
        return file;
    }
}