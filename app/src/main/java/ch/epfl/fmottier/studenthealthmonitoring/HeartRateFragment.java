package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Chronometer;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.components.LimitLine.LimitLabelPosition;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;

/**
 * A simple {@link Fragment} subclass.
 */
public class HeartRateFragment extends Fragment
{
    // View //
    private View mMainView;
    private ViewGroup mContainer;

    // Layout //
    private Button btnHeartRateStart;
    private Chronometer chronoHeartRateChronometer;
    private TextView lblHeartRateInstruction;
    private ImageView imageViewHeartRate;
    private TextView lblHeartRateData;
    private TextView lblHeartRateDate;
    private TextView lblHeartRateName;
    private ImageButton imageButtonHeartRateEdit;
    private TextView lblHeartRateRest;
    private TextView lblHeartRateStand;
    private TextView lblHeartRateDiff;
    private TextView lblHeartRateMax;

    // ProgressBar //
    private ProgressDialog progressDialogHeartRate;

    // FireBase Database //
    private DatabaseReference mHeartRateDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;
    private String strHeartRateDate;
    private String strHeartRateName;
    private String strUsersGender;
    private String strUsersBirthDate;
    private String strUsersHeight;
    private String strUsersWeight;
    private String urlHeartRate;

    // FireBase Storage //
    private StorageReference mHeartRateStorage;

    // User Data //
    private boolean genderFemale;
    private Date birthDate;
    private int age;
    private int height;
    private int weight;

    // Wear data //
    private int dataHeartRate;

    // Chronometer //
    private static final long MILLIS_TO_SECONDS = 1000;
    private static final long MILLIS_TO_MINUTES = 60000;
    private static final long MILLIS_TO_HOURS = 3600000;
    private long startTime;
    private long lastPause = 0;
    private boolean startButtonEnabled = false;
    private int seconds_before = 0;
    private int minutes_before = 0;
    private int hours_before = 0;

    // Time //
    private Date currentDate;
    //private long refTimestamp;

    // Write/Read File //
    private File path;
    private File file;
    final private String fileName = "hr.csv";
    private HeartRate hr;
    private List<DateTime> time = new ArrayList<DateTime>();
    private List<Double> heart_rate = new ArrayList<Double>();

    //ID de l'action
    public static final String ACTION_SEND_HEART_RATE = "ACTION_SEND_HEART_RATE";

    //ID du paramètre passé dans le putExtra
    public static final String INT_HEART_RATE ="INT_HEART_RATE";

    private BroadcastReceiver mHeartRateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            int heartRateWatch = intent.getIntExtra(INT_HEART_RATE, -1);
            dataHeartRate = heartRateWatch;
            lblHeartRateData.setText(String.valueOf(dataHeartRate));
        }
    };

    // LineChart //
    private LineChart mChart;

    // HeartRate Fragment Constructor //
    public HeartRateFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContainer = container;
        //mContext = getContext();
        mMainView = inflater.inflate(R.layout.fragment_heart_rate, container, false);

        // Layout //
        btnHeartRateStart = (Button) mMainView.findViewById(R.id.btnHeartRateStart);
        chronoHeartRateChronometer = (Chronometer) mMainView.findViewById(R.id.chronoHeartRateChronometer);
        lblHeartRateInstruction = (TextView) mMainView.findViewById(R.id.lblHeartRateInstruction);
        imageViewHeartRate = (ImageView) mMainView.findViewById(R.id.imageViewHeartRate);
        lblHeartRateData = (TextView) mMainView.findViewById(R.id.lblHeartRateData);
        lblHeartRateDate = (TextView) mMainView.findViewById(R.id.lblHeartRateDate);
        lblHeartRateName = (TextView) mMainView.findViewById(R.id.lblHeartRateName);
        imageButtonHeartRateEdit = (ImageButton) mMainView.findViewById(R.id.imageButtonHeartRateEdit);
        mChart = (LineChart) mMainView.findViewById(R.id.lineChartHearRate);
        lblHeartRateRest = (TextView) mMainView.findViewById(R.id.lblHeartRateRest);
        lblHeartRateStand = (TextView) mMainView.findViewById(R.id.lblHeartRateStand);
        lblHeartRateDiff = (TextView) mMainView.findViewById(R.id.lblHeartRateDiff);
        lblHeartRateMax = (TextView) mMainView.findViewById(R.id.lblHeartRateMax);

        lblHeartRateInstruction.setText(getResources().getString(R.string.instruction_0));
        imageViewHeartRate.setImageResource(R.drawable.watch);

        plotHeartRate(mChart, new HeartRate(null,null));

        // ProgressBar //
        progressDialogHeartRate = new ProgressDialog(container.getContext());

        // File path //
        path = getActivity().getFilesDir().getAbsoluteFile();

        // Listener Heart Rate //
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mHeartRateReceiver,new IntentFilter(ACTION_SEND_HEART_RATE));

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);
        // Firebase offline capability //
        mUsersDatabase.keepSynced(true);

        // Recovering of gender, birth_date, height and weight from Users DataBase
        mUsersDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                strUsersGender = dataSnapshot.child("gender").getValue().toString();
                strUsersBirthDate = dataSnapshot.child("birth_date").getValue().toString();
                strUsersHeight = dataSnapshot.child("height").getValue().toString();
                strUsersWeight = dataSnapshot.child("weight").getValue().toString();


                if(strUsersGender.equals("f"))
                {
                    genderFemale = true;
                }
                else
                {
                    genderFemale = false;
                }

                try
                {
                    birthDate = new SimpleDateFormat("MM/dd/yyyy").parse(strUsersBirthDate);
                }
                catch (java.text.ParseException e)
                {
                    Log.e("Exception", "Parse error: " + e.toString());
                }

                currentDate = Calendar.getInstance().getTime();

                int years = Math.round(currentDate.getTime() / 1000 - birthDate.getTime() / 1000) / 31536000;
                int months = Math.round(currentDate.getTime() / 1000 - birthDate.getTime() / 1000 - years * 31536000) / 2628000;

                age = years;

                height = Integer.valueOf(strUsersHeight);
                weight = Integer.valueOf(strUsersWeight);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(getActivity(), getString(R.string.a_problem_occurred), Toast.LENGTH_LONG).show();
            }
        });

        mHeartRateDatabase = FirebaseDatabase.getInstance().getReference().child("HeartRate").child(mCurrentUserID);
        // Firebase offline capability //
        mHeartRateDatabase.keepSynced(true);

        // Recovering of hr, date and name from Heart Rate DataBase
        mHeartRateDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("hr").exists())
                {
                    urlHeartRate = dataSnapshot.child("hr").getValue().toString();
                }
                else
                {
                    urlHeartRate = null;
                }

                if(dataSnapshot.child("date").exists())
                {
                    strHeartRateDate = dataSnapshot.child("date").getValue().toString();
                }
                else
                {
                    strHeartRateDate = "";
                }
                lblHeartRateDate.setText(dateHeartRate(strHeartRateDate));

                if(dataSnapshot.child("name").exists())
                {
                    strHeartRateName = dataSnapshot.child("name").getValue().toString();
                }
                else
                {
                    strHeartRateName = "";
                }
                lblHeartRateName.setText(strHeartRateName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(getActivity(), getString(R.string.a_problem_occurred), Toast.LENGTH_LONG).show();
            }
        });

        // FireBase Storage //
        mHeartRateStorage = FirebaseStorage.getInstance().getReference().child("HeartRate").child(mCurrentUserID);

        // Recovering of hr.csv from Heart Rate Storage //
        StorageReference filepath = mHeartRateStorage.child(fileName);
        file = new File(path, fileName);
        filepath.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
            {
                // Read .csv //
                hr = readCSVFileHeartRate(file);

                time = hr.getTime();
                heart_rate = hr.getHeartRate();

                // MPChart Plot of Heart Rate test
                //plotHeartRateWithLimit(mChart,hr,genderFemale,age);
                plotHeartRate(mChart,hr);

                // HR rest //
                lblHeartRateRest.setText(restHeartRate(hr));

                // HR stand //
                lblHeartRateStand.setText(standHeartRate(hr));

                // HR diff //
                lblHeartRateDiff.setText(diffHeartRate(hr));

                // Hr max //
                lblHeartRateMax.setText(maxHeartRate(hr));

            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                Toast.makeText(getActivity(), getString(R.string.heart_rate_file_not_found), Toast.LENGTH_LONG).show();
            }
        });

        file.delete();

        // Start Button click //
        btnHeartRateStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(!startButtonEnabled)
                {
                    btnHeartRateStart.setText("QUIT");
                    btnHeartRateStart.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    startButtonEnabled = true;
                    //plotHeartRateWithLimit(mChart,hr,genderFemale,age);
                    //plotHeartRate(mChart,hr);
                    lblHeartRateInstruction.setText(getResources().getString(R.string.instruction_1));
                    imageViewHeartRate.setImageResource(R.drawable.rest);
                    startChrono();
                }
                else
                {
                    pauseChrono();
                    resetChrono();
                    btnHeartRateStart.setText("START");
                    btnHeartRateStart.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
                    startButtonEnabled = false;
                    lblHeartRateInstruction.setText(getResources().getString(R.string.instruction_0));
                    imageViewHeartRate.setImageResource(R.drawable.watch);

                }
                heart_rate.clear();
                time.clear();
            }
        });

        // Edit Button click //
        imageButtonHeartRateEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Value of the Name field //
                String nameSend = strHeartRateName;

                // Intent from HeartRateFragment to NameActivity //
                Intent intentName = new Intent(getActivity(), HeartRateNameActivity.class);
                intentName.putExtra("nameSend", nameSend); // Send the value of the status field to GPSNameActivity //
                startActivity(intentName);
            }
        });

        // Chronometer //
        chronoHeartRateChronometer.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener()
        {
            @Override
            public void onChronometerTick(Chronometer chrono)
            {
                long elapsed_time = SystemClock.elapsedRealtime() - chrono.getBase();
                int seconds = (int) ((elapsed_time / MILLIS_TO_SECONDS) % 60);
                int minutes = (int) ((elapsed_time / MILLIS_TO_MINUTES) % 60);
                int hours = (int) ((elapsed_time / MILLIS_TO_HOURS) % 24);

                if(minutes >= 1)
                //if(seconds >= 5)
                {
                    int min = 70;
                    int max = 95;
                    Random r = new Random();
                    //dataHeartRate = r.nextInt(max - min + 1) + min;

                    lblHeartRateInstruction.setText(getResources().getString(R.string.instruction_2));
                    imageViewHeartRate.setImageResource(R.drawable.stand);
                }
                else
                {
                    int min = 35;
                    int max = 50;
                    Random r = new Random();
                    //dataHeartRate = r.nextInt(max - min + 1) + min;
                }

                // X-Axis Times //
                DateTime date = new DateTime(elapsed_time);
                time.add(new DateTime(date));

                // Y-Axis Heart Rate //
                heart_rate.add(new Double(dataHeartRate));

                hr = new HeartRate(time, heart_rate);

                // Update of MPChart Plot //
                //plotHeartRateWithLimit(mChart, hr, genderFemale, age);
                plotHeartRate(mChart, hr);

                if (minutes >= 2)
                //if (seconds >= 10)
                {
                    // Stop Chronometer //
                    chrono.stop();

                    lblHeartRateInstruction.setText(getResources().getString(R.string.instruction_3));

                    // ProgressDialog //
                    progressDialogHeartRate.setTitle((getString(R.string.saving_changes)));
                    progressDialogHeartRate.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                    progressDialogHeartRate.setCanceledOnTouchOutside(false);;
                    progressDialogHeartRate.show();

                    // Heart Rate Data //
                    hr = new HeartRate(time,heart_rate);

                    // Write hr.csv file //
                    file = new File(path, fileName);
                    writeCSVFileHeartRate(file, hr);

                    // Load the uri from hr.csv file //
                    Uri fileUri = Uri.fromFile(file);

                    // Storage of hr.csv to Heart Rate Storage //
                    StorageReference filepath = mHeartRateStorage.child("hr.csv");
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
                                updateHashMap.put("hr", downloadUrl);
                                updateHashMap.put("date", new SimpleDateFormat("MM/dd/yyyy").format(currentDate));

                                // Storage of URL from hr.csv to Heart Rate DataBase //
                                mHeartRateDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                                {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                        if(task.isSuccessful())
                                        {
                                            progressDialogHeartRate.dismiss();
                                            Toast.makeText(getContext(), getString(R.string.file_uploaded), Toast.LENGTH_LONG).show();

                                            // HR rest //
                                            lblHeartRateRest.setText(restHeartRate(hr));

                                            // HR stand //
                                            lblHeartRateStand.setText(standHeartRate(hr));

                                            // HR diff //
                                            lblHeartRateDiff.setText(diffHeartRate(hr));

                                            // Hr max //
                                            lblHeartRateMax.setText(maxHeartRate(hr));

                                            // Intent from SettingsActivity to StatusActivity //
                                            Intent intentSurvey = new Intent(getContext(), SurveyActivity.class);
                                            startActivity(intentSurvey);

                                            file.delete();
                                        }
                                    }
                                });
                            }
                            else
                            {
                                progressDialogHeartRate.dismiss();
                                Toast.makeText(getContext(), getString(R.string.file_not_uploaded), Toast.LENGTH_LONG).show();
                            }
                        }
                    }));
                }
            }
        });

        // Inflate the layout for this fragment //
        return mMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    // Start the Chronometer //
    private void startChrono()
    {
        if(lastPause != 0)
        {
            chronoHeartRateChronometer.setBase(chronoHeartRateChronometer.getBase());
        }
        else
        {
            chronoHeartRateChronometer.setBase(SystemClock.elapsedRealtime());
        }

        startTime = System.currentTimeMillis();
        chronoHeartRateChronometer.start();

    }

    // Stop the Chronometer //
    private void pauseChrono()
    {
        lastPause = SystemClock.elapsedRealtime();
        chronoHeartRateChronometer.stop();
    }

    // Reset the Chronometer //
    private void resetChrono()
    {
        chronoHeartRateChronometer.setBase(SystemClock.elapsedRealtime());
        lastPause = 0;
    }

    // Read a hr.csv from a local path //
    private HeartRate readCSVFileHeartRate(File file)
    {
        // Delimiter used in CSV file //
        final String COMMA_DELIMITER = ";";

        // Display of times //
        SimpleDateFormat sdf_mmss = new SimpleDateFormat(getString(R.string.sdf_mmss));
        sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Heart Rate data //
        List<DateTime> time = new ArrayList<DateTime>();
        List<Double> heart_rate = new ArrayList<Double>();

        try
        {
            FileInputStream fis = new FileInputStream(file);

            if (fis != null)
            {
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader br = new BufferedReader(isr);

                String line;
                br.readLine(); // Skip the first line (header) //
                while (( line = br.readLine() ) != null)
                {
                    String[] data = line.split(COMMA_DELIMITER);
                    if (data.length > 0)
                    {
                        // Time data //
                        try
                        {
                            time.add(new DateTime(sdf_mmss.parse(data[0])));
                        }
                        catch (java.text.ParseException e)
                        {
                            Log.e("Exception", "Parse error: " + e.toString());
                        }

                        // Heart Rate data //
                        heart_rate.add(Double.valueOf(data[1]));
                    }
                }
                br.close();
            }
            else
            {
                time = null;
                heart_rate = null;
            }
        }
        catch (FileNotFoundException e)
        {
            Log.e("Exception", "File not found: " + e.toString());
            //time = null;
            //heart_rate = null;
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        // Return a HeartRate object //
        HeartRate hr = new HeartRate(time,heart_rate);
        return hr;
    }

    // Write a local file from a HeartRate data //
    private File writeCSVFileHeartRate(File file, HeartRate hr)
    {
        // Delimiter used in .csv file //
        final String COMMA_DELIMITER = ";";
        //final String NEW_LINE_SEPARATOR = ";;;\n";
        final String NEW_LINE_SEPARATOR = "\n";

        // .csv file header //
        final String FILE_HEADER = "time [mm:ss];hr [bpm]";

        // Display of times //
        SimpleDateFormat sdf_mmss = new SimpleDateFormat(getString(R.string.sdf_mmss));
        sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));

        // Time data //
        List<DateTime> time = new ArrayList<DateTime>();
        time = hr.getTime();

        // Heart Rate data //
        List<Double> heart_rate = new ArrayList<Double>();
        heart_rate = hr.getHeartRate();

        try
        {
            FileOutputStream fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            // Write the .csv file header //
            osw.write(FILE_HEADER);

            // Add a new line separator after the header //
            osw.write(NEW_LINE_SEPARATOR);

            // Write new heart_rate/time data //
            for (int i = 0; i < time.size(); i++)
            {
                osw.write(sdf_mmss.format(new Date((long) (time.get(i).toDate().getTime()))));
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

        return path;
    }

    // Date //
    private String dateHeartRate(String date)
    {
        if(!date.equals(""))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat sdf_MMddyyyy = new SimpleDateFormat(getString(R.string.sdf_MMddyyyy));
            sdf_MMddyyyy.setTimeZone(TimeZone.getTimeZone("GMT"));

            DateTime time = new DateTime();

            try
            {
                time = new DateTime(sdf.parse(date));
            }
            catch (java.text.ParseException e)
            {
                Log.e("Exception", "Parse error: " + e.toString());
            }

            return sdf_MMddyyyy.format(new Date((long) (time.toDate().getTime())));
        }
        else
        {
            return "";
        }
    }

    // HR rest //
    private String restHeartRate(HeartRate hr)
    {
        if(hr.getTime() != null && hr.getHeartRate() != null)
        {
            List<Double> restList = new ArrayList<Double>();
            double rest = 0;

            for (int i = 0; i < hr.getTime().size(); i++)
            {
                if(hr.getTime().get(i).getMinuteOfHour() < 1)
                {
                    restList.add(hr.getHeartRate().get(i));
                }
            }

            for (int i = 0; i < restList.size(); i++)
            {
                rest += restList.get(i);
            }

            return String.format("%.2f bpm", rest/restList.size());
        }
        else
        {
            return "";
        }
    }

    // HR stand //
    private String standHeartRate(HeartRate hr)
    {
        if(hr.getTime() != null && hr.getHeartRate() != null)
        {
            List<Double> standList = new ArrayList<Double>();
            double stand = 0;

            for (int i = 0; i < hr.getTime().size(); i++)
            {
                if(hr.getTime().get(i).getMinuteOfHour() >= 1)
                {
                    standList.add(hr.getHeartRate().get(i));
                }
            }

            for (int i = 0; i < standList.size(); i++)
            {
                stand += standList.get(i);
            }

            return String.format("%.2f bpm", stand/standList.size());
        }
        else
        {
            return "";
        }
    }

    // HR diff //
    private String diffHeartRate(HeartRate hr)
    {
        if(hr.getTime() != null && hr.getHeartRate() != null)
        {
            List<Double> restList = new ArrayList<Double>();
            List<Double> standList = new ArrayList<Double>();
            double rest = 0;
            double stand = 0;

            for (int i = 0; i < hr.getTime().size(); i++)
            {
                if(hr.getTime().get(i).getMinuteOfHour() < 1)
                {
                    restList.add(hr.getHeartRate().get(i));
                }

                else if(hr.getTime().get(i).getMinuteOfHour() >= 1)
                {
                    standList.add(hr.getHeartRate().get(i));
                }
            }

            for (int i = 0; i < restList.size(); i++)
            {
                rest += restList.get(i);
            }

            for (int i = 0; i < standList.size(); i++)
            {
                stand += standList.get(i);
            }

            return String.format("%.2f bpm", rest/restList.size() - stand/standList.size());
        }
        else
        {
            return "";
        }
    }

    // HR max //
    private String maxHeartRate(HeartRate hr)
    {
        if(hr.getHeartRate() != null)
        {
            double heartRate = 0;
            double maxHeartRate = 0;

            for (int i = 0; i < hr.getHeartRate().size(); i++)
            {
                heartRate = hr.getHeartRate().get(i);

                if (maxHeartRate < heartRate)
                {
                    maxHeartRate = heartRate;
                }
            }
            return String.format("%.2f bpm", maxHeartRate);
        }
        else
        {
            return "";
        }
    }

    // Give the Heart Rate limits of each User //
    private ArrayList<Integer> heartRateZones(boolean female, int age)
    {
        ArrayList<Integer> zones = new ArrayList<Integer>();

        // https://healthiack.com/heart-rate-zone-calculator //
        if(!female)
        {
            zones.add((int)((220-age)*0.5));
            zones.add((int)((220-age)*0.6));
            zones.add((int)((220-age)*0.7));
            zones.add((int)((220-age)*0.8));
            zones.add((int)((220-age)*0.9));
        }
        else
        {
            zones.add((int)((226-age)*0.5));
            zones.add((int)((226-age)*0.6));
            zones.add((int)((226-age)*0.7));
            zones.add((int)((226-age)*0.8));
            zones.add((int)((226-age)*0.9));
        }

        return zones;
    }

    // Add Y limit line on the plot //
    private void addYLimitLine(LineChart lineChart, int max, String name)
    {
        LimitLine limitLine = new LimitLine(max, name);
        limitLine.setLineWidth(2f);
        limitLine.setLineColor(Color.RED);
        limitLine.setTextSize(12f);
        limitLine.setTextColor(Color.RED);
        limitLine.setLabelPosition(LimitLabelPosition.LEFT_TOP);
        lineChart.getAxisLeft().addLimitLine(limitLine);
    }

    // Display with MPCHart the fix part of the plot //
    private void plotHeartRateWithLimit(LineChart lineChart, HeartRate hr, boolean gender, int age)
    {
        Description desc = new Description();

        if(hr.getTime() != null && hr.getHeartRate() != null)
        {
            final long refTimestamp = hr.getTime().get(0).toDate().getTime();

            // X-Axis //
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IAxisValueFormatter()
            {
                @Override
                public String getFormattedValue(float value, AxisBase axis)
                {
                    SimpleDateFormat sdf_mmss = new SimpleDateFormat(getString(R.string.sdf_mmss));
                    sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return sdf_mmss.format(new Date((long) (value + refTimestamp)));
                }
            });

            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            desc.setText(getString(R.string.axisTime));

            // Y-Axis //
            lineChart.getAxisRight().setEnabled(false);

            // Zones //
            ArrayList<Integer> zones = heartRateZones(gender, age);

            // Remove All Y-Limit before //
            lineChart.getAxisLeft().removeAllLimitLines();

            // Y-Limit "Light intensity" //
            addYLimitLine(lineChart,zones.get(0), getString(R.string.zone_0));

            // Y-Limit "Moderate intensity" //
            addYLimitLine(lineChart, zones.get(1), getString(R.string.zone_1));

            // Y-Limit "Intense" //
            addYLimitLine(lineChart, zones.get(2), getString(R.string.zone_2));

            // Y-Limit "Very intense" //
            addYLimitLine(lineChart, zones.get(3), getString(R.string.zone_3));

            // Y-Limit "All out intensity" //
            addYLimitLine(lineChart, zones.get(4), getString(R.string.zone_4));

            // Data //
            ArrayList<Entry> xyVals = new ArrayList<Entry>();
            for (int i = 0; i < hr.getHeartRate().size(); i++)
            {
                xyVals.add(new Entry(new Long(hr.getTime().get(i).toDate().getTime()-refTimestamp).floatValue(), new Double(hr.getHeartRate().get(i)).floatValue()));
            }

            LineDataSet dataSet1 = new LineDataSet(xyVals, getString(R.string.axisHeartRate));
            dataSet1.setLineWidth(2f);
            dataSet1.setDrawCircleHole(false);
            dataSet1.setCircleRadius(3f);
            dataSet1.setColor(Color.GREEN);
            dataSet1.setCircleColor(Color.GREEN);
            dataSet1.setDrawVerticalHighlightIndicator(true);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataSet1);

            LineData data = new LineData(dataSets);

            // Loading of data //
            lineChart.setData(data);
            lineChart.invalidate();
        }
        else
        {
            lineChart.setNoDataText(getString(R.string.noDataHeartRate));
            desc.setText("");
        }

        lineChart.setDescription(desc);
    }

    // Display with MPCHart the fix part of the plot //
    private void plotHeartRate(LineChart lineChart, HeartRate hr)
    {
        Description desc = new Description();

        if(hr.getTime() != null && hr.getHeartRate() != null)
        {
            final long refTimestamp = hr.getTime().get(0).toDate().getTime();

            // X-Axis //
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IAxisValueFormatter()
            {
                @Override
                public String getFormattedValue(float value, AxisBase axis)
                {
                    SimpleDateFormat sdf_mmss = new SimpleDateFormat(getString(R.string.sdf_mmss));
                    sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return sdf_mmss.format(new Date((long) (value + refTimestamp)));
                }
            });

            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            desc.setText(getString(R.string.axisTime));

            // Y-Axis //
            lineChart.getAxisRight().setEnabled(false);

            // Data //
            ArrayList<Entry> xyVals = new ArrayList<Entry>();
            for (int i = 0; i < hr.getHeartRate().size(); i++)
            {
                xyVals.add(new Entry(new Long(hr.getTime().get(i).toDate().getTime()-refTimestamp).floatValue(), new Double(hr.getHeartRate().get(i)).floatValue()));
            }

            LineDataSet dataSet1 = new LineDataSet(xyVals, getString(R.string.axisHeartRate));
            dataSet1.setLineWidth(2f);
            dataSet1.setDrawCircleHole(false);
            dataSet1.setCircleRadius(3f);
            dataSet1.setColor(Color.GREEN);
            dataSet1.setCircleColor(Color.GREEN);
            dataSet1.setDrawVerticalHighlightIndicator(true);

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataSet1);

            LineData data = new LineData(dataSets);

            // Loading of data //
            lineChart.setData(data);
            lineChart.invalidate();
        }
        else
        {
            lineChart.setNoDataText(getString(R.string.noDataHeartRate));
            desc.setText("");
        }

        lineChart.setDescription(desc);
    }
}