package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;


import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.maps.android.SphericalUtil;

import org.joda.time.DateTime;

/**
 * A simple {@link Fragment} subclass.
 */
public class GPSFragment extends Fragment implements OnMapReadyCallback
{
    // View //
    private View mMainView;
    private ViewGroup mContainer;

    // Layout //
    private TextView lblGPSName;
    private TextView lblGPSDate;
    private ImageButton imageButtonGPSEdit;
    private TextView lblGPSStartTime;
    private TextView lblGPSEndTime;
    private TextView lblGPSTotalTime;
    private TextView lblGPSTotalDistance;
    private TextView lblGPSAverageSpeed;
    private TextView lblGPSMaxSpeed;

    // ProgressBar //
    private ProgressDialog progressDialogGPS;

    // FireBase Database //
    private DatabaseReference mGPSDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;
    private String strGPSDate;
    private String strGPSName;
    private String urlGPS;

    // FireBase Storage //
    private StorageReference mGPSStorage;

    // Google Map //
    private GoogleMap mGoogleMap;

    // Time //
    private Date currentDate;
    private long refTimestamp;

    // Write/Read File //
    private File path;
    private File file;
    final private String fileName = "gps.csv";
    private GPS gps;
    private List<DateTime> time = new ArrayList<DateTime>();
    private List<LatLng> latitude_longitude = new ArrayList<LatLng>();
    private List<Double> elevation = new ArrayList<Double>();
    private List<Double> heart_rate = new ArrayList<Double>();

    // LineChart //
    private LineChart mChart;

    // Public GPS Fragment Constructor //
    public GPSFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mContainer = container;
        mMainView = inflater.inflate(R.layout.fragment_gps, container, false);

        // Layout //
        lblGPSDate = (TextView) mMainView.findViewById(R.id.lblGPSDate);
        lblGPSName = (TextView) mMainView.findViewById(R.id.lblGPSName);
        imageButtonGPSEdit = (ImageButton) mMainView.findViewById(R.id.imageButtonGPSEdit);
        lblGPSStartTime = (TextView) mMainView.findViewById(R.id.lblGPSStartTime);
        lblGPSEndTime = (TextView) mMainView.findViewById(R.id.lblGPSEndTime);
        lblGPSTotalTime = (TextView) mMainView.findViewById(R.id.lblGPSTotalTime);
        lblGPSTotalDistance = (TextView) mMainView.findViewById(R.id.lblGPSTotalDistance);
        lblGPSAverageSpeed = (TextView) mMainView.findViewById(R.id.lblGPSAverageSpeed);
        lblGPSMaxSpeed = (TextView) mMainView.findViewById(R.id.lblGPSMaxSpeed);
        mChart = (LineChart) mMainView.findViewById(R.id.lineChartGPSElevationProfile);

        plotGPS(mChart, new GPS(null,null,null,null));

        // ProgressBar //
        progressDialogGPS = new ProgressDialog(container.getContext());

        // File path //
        path = getActivity().getFilesDir().getAbsoluteFile();

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();


        mGPSDatabase = FirebaseDatabase.getInstance().getReference().child("GPS").child(mCurrentUserID);
        // Firebase offline capability //
        mGPSDatabase.keepSynced(true);

        // Recovering of gps, date and name from GPS DataBase
        mGPSDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.child("gps").exists())
                {
                    urlGPS = dataSnapshot.child("gps").getValue().toString();
                }
                else
                {
                    urlGPS = null;
                }

                if(dataSnapshot.child("date").exists())
                {
                    strGPSDate = dataSnapshot.child("date").getValue().toString();
                }
                else
                {
                    strGPSDate = "";
                }
                lblGPSDate.setText(dateGPS(strGPSDate));

                if(dataSnapshot.child("name").exists())
                {
                    strGPSName = dataSnapshot.child("name").getValue().toString();
                }
                else
                {
                    strGPSName = "";
                }
                lblGPSName.setText(strGPSName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(getActivity(), getString(R.string.a_problem_occurred), Toast.LENGTH_LONG).show();
            }
        });



        // FireBase Storage //
        mGPSStorage = FirebaseStorage.getInstance().getReference().child("GPS").child(mCurrentUserID);

        // Recovering of gps.csv from GPS Storage //
        StorageReference filepath = mGPSStorage.child(fileName);
        file = new File(path, fileName);
        filepath.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
        {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
            {
                // Read .csv //
                gps = readCSVFileGPS(file);

                //time = gps.getTime();
                //latitude_longitude = gps.getLatitudeLongitude();
                //elevation = gps.getElevation();
                //heart_rate = gps.getHeartRate();

                // Calculate date, start/end/total time, total distance, average/max speed //
                lblGPSStartTime.setText(startTimeGPS(gps));
                lblGPSEndTime.setText(endTimeGPS(gps));
                lblGPSTotalTime.setText(totalTimeGPS(gps));
                lblGPSTotalDistance.setText(totalDistanceGPS(gps));
                lblGPSAverageSpeed.setText(averageSpeedGPS(gps));
                lblGPSMaxSpeed.setText(maxSpeedGPS(gps));

                // MPChart Plot of Elevation and Heart Rate //
                plotGPS(mChart, gps);

                // Draw the LatLng points //
                drawPolyLineOnMap(mGoogleMap, gps);

                file.delete();
            }
        }).addOnFailureListener(new OnFailureListener()
        {
            @Override
            public void onFailure(@NonNull Exception exception)
            {
                //plotGPS(mChart, new GPS(null,null,null,null));
                //Toast.makeText(getActivity(), getString(R.string.gps_file_not_found), Toast.LENGTH_LONG).show();
                //gps = new GPS(null,null,null,null);
                // MPChart Plot of Elevation and Heart Rate //
                //plotGPS(mChart, new GPS(null,null,null,null));
            }
        });

        // Edit Button click //
        imageButtonGPSEdit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Value of the Name field //
                String nameSend = strGPSName;

                // Intent from HeartRateFragment to NameActivity //
                Intent intentName = new Intent(getActivity(), GPSNameActivity.class);
                intentName.putExtra("nameSend", nameSend); // Send the value of the status field to GPSNameActivity //
                startActivity(intentName);
            }
        });

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        final SupportMapFragment map = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        map.getMapAsync(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap = googleMap;
    }

    // Draw polyline on map
    public void drawPolyLineOnMap(GoogleMap googleMap, GPS gps)
    {
        int BOUND_PADDING = 100;

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(Color.RED);
        polyOptions.width(5);

        polyOptions.addAll(gps.getLatitudeLongitude());

        googleMap.clear();
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        googleMap.addPolyline(polyOptions);

        googleMap.addMarker(new MarkerOptions().position(gps.getLatitudeLongitude().get(0)).title("Start").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        googleMap.addMarker(new MarkerOptions().position(gps.getLatitudeLongitude().get(gps.getLatitudeLongitude().size()-1)).title("End").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng latLng : gps.getLatitudeLongitude())
        {
            builder.include(latLng);
        }

        final LatLngBounds bounds = builder.build();

        //BOUND_PADDING is an int to specify padding of bound.. try 100.
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, BOUND_PADDING);

        googleMap.animateCamera(cu);
    }

    // Read a hr.csv from a local path //
    private GPS readCSVFileGPS(File file)
    {
        // Delimiter used in CSV file //
        final String COMMA_DELIMITER = ";";

        // Display of times //
        SimpleDateFormat sdf_yyyyMMddTHHmmssZ = new SimpleDateFormat(getString(R.string.sdf_yyyyMMddTHHmmssZ));
        sdf_yyyyMMddTHHmmssZ.setTimeZone(TimeZone.getTimeZone("GMT"));

        // GPS data //
        List<DateTime> time = new ArrayList<DateTime>();
        List<LatLng> latitude_longitude = new ArrayList<LatLng>();
        List<Double> elevation = new ArrayList<Double>();
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
                            time.add(new DateTime(sdf_yyyyMMddTHHmmssZ.parse(data[0])));
                        }
                        catch (java.text.ParseException e)
                        {
                            Log.e("Exception", "Parse error: " + e.toString());
                        }

                        // Latitude and Longitude data //
                        latitude_longitude.add(new LatLng(Double.valueOf(data[1]),Double.valueOf(data[2])));

                        // Elevation data //
                        elevation.add(Double.valueOf(data[3]));

                        // Heart Rate data //
                        heart_rate.add(Double.valueOf(data[4]));
                    }
                }
                br.close();
            }
            else
            {
                time = null;
                latitude_longitude = null;
                elevation = null;
                heart_rate = null;
            }
        }
        catch (FileNotFoundException e)
        {
            Log.e("Exception", "File not found: " + e.toString());
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        // Return a HeartRate object //
        GPS gps = new GPS(time, latitude_longitude, elevation, heart_rate);
        return gps;
    }

    // Date //
    private String dateGPS(String date)
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

    // Start time //
    private String startTimeGPS(GPS gps)
    {
        if(gps.getTime() != null)
        {
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(getString(R.string.sdf_hhmmss));
            sdf_HHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));

            return sdf_HHmmss.format(gps.getTime().get(0).getMillis());
        }
        else
        {
            return "";
        }
    }

    // End time //
    private String endTimeGPS(GPS gps)
    {
        if(gps.getTime() != null)
        {
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(getString(R.string.sdf_hhmmss));
            sdf_HHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));

            return sdf_HHmmss.format(gps.getTime().get(gps.getTime().size()-1).getMillis());
        }
        else
        {
            return "";
        }
    }

    // Total time //
    private String totalTimeGPS(GPS gps)
    {
        if(gps.getTime() != null)
        {
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(getString(R.string.sdf_hhmmss));
            sdf_HHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));

            return sdf_HHmmss.format(gps.getTime().get(gps.getTime().size()-1).getMillis()-gps.getTime().get(0).getMillis());
        }
        else
        {
            return "";
        }
    }

    // Total distance //
    private String totalDistanceGPS(GPS gps)
    {
        if (gps.getLatitudeLongitude() != null)
        {
            return String.format("%.2f km", SphericalUtil.computeLength(gps.getLatitudeLongitude()) / 1000);
        }
        else
        {
            return "";
        }
    }

    // Average speed //
    private String averageSpeedGPS(GPS gps)
    {
        if(gps.getTime() != null && gps.getLatitudeLongitude() != null)
        {
            return String.format("%.2f km/h",3600.0*SphericalUtil.computeLength(gps.getLatitudeLongitude())/(gps.getTime().get(gps.getTime().size()-1).getMillis()-gps.getTime().get(0).getMillis()));
        }
        else
        {
            return "";
        }
    }

    // Maximum speed //
    private String maxSpeedGPS(GPS gps)
    {
        if(gps.getTime() != null && gps.getLatitudeLongitude() != null)
        {

            double speed = 0;
            double maxSpeed = 0;

            for (int i = 0; i < gps.getLatitudeLongitude().size() - 1; i++)
            {
                speed = 3600.0*SphericalUtil.computeDistanceBetween(gps.getLatitudeLongitude().get(i), gps.getLatitudeLongitude().get(i + 1)) / (gps.getTime().get(i + 1).getMillis() - gps.getTime().get(i).getMillis());

                if (maxSpeed < speed)
                {
                    maxSpeed = speed;
                }
            }

            return String.format("%.2f km/h", maxSpeed);
        }
        else
        {
            return "";
        }
    }

    // GPS MPChart Plot //
    private void plotGPS(LineChart lineChart, GPS gps)
    {
        Description desc = new Description();

        if(gps.getTime() != null && gps.getElevation() != null)
        {
            final long refTimestamp = gps.getTime().get(0).toDate().getTime();

            // X-Axis //
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IAxisValueFormatter()
            {
                @Override
                public String getFormattedValue(float value, AxisBase axis)
                {
                    SimpleDateFormat sdf_mmss = new SimpleDateFormat(getString(R.string.sdf_hhmmss));
                    sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return sdf_mmss.format(new Date((long) (value + refTimestamp)));
                }
            });

            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            desc.setText(getString(R.string.axisTime));

            // Y-Axis //
            lineChart.getAxisLeft().setEnabled(true);
            lineChart.getAxisRight().setEnabled(true);

            // Data //
                // Elevation //
            ArrayList<Entry> xy1Vals = new ArrayList<Entry>();
            for (int i = 0; i < gps.getElevation().size(); i++)
            {
                xy1Vals.add(new Entry(new Long(gps.getTime().get(i).toDate().getTime()-refTimestamp).floatValue(), new Double(gps.getElevation().get(i)).floatValue()));
            }

            LineDataSet dataSet1 = new LineDataSet(xy1Vals, getString(R.string.axisElevation));
            dataSet1.setLineWidth(2f);
            dataSet1.setDrawCircleHole(false);
            dataSet1.setCircleRadius(3f);
            dataSet1.setColor(Color.BLUE);
            dataSet1.setCircleColor(Color.BLUE);
            dataSet1.setDrawVerticalHighlightIndicator(true);
            dataSet1.setAxisDependency(lineChart.getAxisLeft().getAxisDependency());

                // HeartRate //
            ArrayList<Entry> xy2Vals = new ArrayList<Entry>();
            for (int i = 0; i < gps.getHeartRate().size(); i++)
            {
                xy2Vals.add(new Entry(new Long(gps.getTime().get(i).toDate().getTime()-refTimestamp).floatValue(), new Double(gps.getHeartRate().get(i)).floatValue()));
            }

            LineDataSet dataSet2 = new LineDataSet(xy2Vals, getString(R.string.axisHeartRate));
            dataSet2.setLineWidth(2f);
            dataSet2.setDrawCircleHole(false);
            dataSet2.setCircleRadius(3f);
            dataSet2.setColor(Color.GREEN);
            dataSet2.setCircleColor(Color.GREEN);
            dataSet2.setDrawVerticalHighlightIndicator(true);
            dataSet2.setAxisDependency(lineChart.getAxisRight().getAxisDependency());

            ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
            dataSets.add(dataSet1);
            dataSets.add(dataSet2);

            LineData data = new LineData(dataSets);

            // Loading of data //
            lineChart.setData(data);
            lineChart.invalidate();
        }
        else
        {
            lineChart.setNoDataText(getString(R.string.noDataGPS));
            desc.setText("");
        }

        lineChart.setDescription(desc);
    }
}