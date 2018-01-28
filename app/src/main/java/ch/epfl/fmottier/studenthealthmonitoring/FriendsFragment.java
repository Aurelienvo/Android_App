package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.maps.android.SphericalUtil;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class FriendsFragment extends Fragment
{
    private RecyclerView recyclerViewFriends;

    // FireBase DataBase //
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mGPSDatabase;
    private DatabaseReference mHeartRateDatabase;
    private FirebaseAuth mAuth;

    // FireBase Storage //
    private StorageReference mGPSStorage;
    StorageReference filepathGPS;
    private StorageReference mHeartRateStorage;
    StorageReference filepathHeartRate;

    private String mCurrentUserID;

    String listUserID;

    private View mMainView;

    // Write/Read File //
    private File path;
    private File fileGPS;
    private File fileHeartRate;
    final private String fileNameGPS = "gps.csv";
    final private String fileNameHeartRate = "hr.csv";

    String urlGPS;
    String urlHeartRate;

    // Google Map //
    //private GoogleMap mMap;

    public FriendsFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        recyclerViewFriends = (RecyclerView) mMainView.findViewById(R.id.recyclerViewFriends);
        recyclerViewFriends.setHasFixedSize(true);
        recyclerViewFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        // File path //
        path = getActivity().getFilesDir().getAbsoluteFile();

        mAuth = FirebaseAuth.getInstance();

        mCurrentUserID = mAuth.getCurrentUser().getUid();

        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUserID);
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mGPSDatabase = FirebaseDatabase.getInstance().getReference().child("GPS");
        mHeartRateDatabase = FirebaseDatabase.getInstance().getReference().child("HeartRate");

        mFriendsDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        // Inflate the layout for this fragment
        return mMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(Friends.class, R.layout.layout_friend, FriendsViewHolder.class, mFriendsDatabase)
        {
            // Displaying GoogleMap //
            /*@Override
            public void onBindViewHolder(FriendsViewHolder viewHolder, int position)
            {
                super.onBindViewHolder(viewHolder, position);
            }*/

            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position)
            {
                //super.populateViewHolder(friendsViewHolder, friends, position);


                friendsViewHolder.setUserDate(getContext(), friends.getDate());

                listUserID = getRef(position).getKey();

                mUsersDatabase.child(listUserID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        String name = dataSnapshot.child("name").getValue().toString();
                        String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                        friendsViewHolder.setUserName(name);
                        friendsViewHolder.setUserImage(getContext(), thumb_image);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                mGPSDatabase.child(listUserID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        String strGPSDate;
                        String strGPSName;

                        if(dataSnapshot.child("gps").exists())
                        {
                            urlGPS = dataSnapshot.child("gps").getValue().toString();

                            // FireBase GPS Storage //
                            mGPSStorage = FirebaseStorage.getInstance().getReference().child("GPS").child(listUserID);

                            // Recovering of gps.csv from Heart Rate Storage //
                            filepathGPS = mGPSStorage.child(fileNameGPS);
                            fileGPS = new File(path, fileNameGPS);
                            filepathGPS.getFile(fileGPS).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                {
                                    friendsViewHolder.setUserGPS(getContext(), fileGPS);
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception exception)
                                {
                                    Toast.makeText(getActivity(), getString(R.string.gps_file_not_found), Toast.LENGTH_LONG).show();
                                }
                            });

                            fileGPS.delete();
                        }
                        else
                        {
                            urlGPS = null;

                            friendsViewHolder.setUserGPSHide(getContext());
                        }

                        if(dataSnapshot.child("date").exists())
                        {
                            strGPSDate = dataSnapshot.child("date").getValue().toString();
                        }
                        else
                        {
                            strGPSDate = "";
                        }

                        if(dataSnapshot.child("name").exists())
                        {
                            strGPSName = dataSnapshot.child("name").getValue().toString();
                        }
                        else
                        {
                            strGPSName = "";
                        }

                        friendsViewHolder.setUserGPSDate(getContext(), strGPSDate);
                        friendsViewHolder.setUserGPSName(strGPSName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                mHeartRateDatabase.child(listUserID).addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        String strHeartRateDate;
                        String strHeartRateName;

                        if(dataSnapshot.child("hr").exists())
                        {
                            urlHeartRate = dataSnapshot.child("hr").getValue().toString();

                            // FireBase HeartRate Storage //
                            mHeartRateStorage = FirebaseStorage.getInstance().getReference().child("HeartRate").child(listUserID);

                            // Recovering of hr.csv from Heart Rate Storage //
                            filepathHeartRate = mHeartRateStorage.child(fileNameHeartRate);
                            fileHeartRate = new File(path, fileNameHeartRate);
                            filepathHeartRate.getFile(fileHeartRate).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot)
                                {
                                    friendsViewHolder.setUserHeartRate(getContext(), fileHeartRate);
                                }
                            }).addOnFailureListener(new OnFailureListener()
                            {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(getActivity(), getString(R.string.heart_rate_file_not_found), Toast.LENGTH_LONG).show();
                                    //friendsViewHolder.setUserHideHeartRate(getContext());
                                }
                            });

                            fileHeartRate.delete();
                        }
                        else
                        {
                            urlHeartRate = null;

                            friendsViewHolder.setUserHeartRateHide(getContext());
                        }

                        if(dataSnapshot.child("date").exists())
                        {
                            strHeartRateDate = dataSnapshot.child("date").getValue().toString();
                        }
                        else
                        {
                            strHeartRateDate = "";
                        }

                        if(dataSnapshot.child("name").exists())
                        {
                            strHeartRateName = dataSnapshot.child("name").getValue().toString();
                        }
                        else
                        {
                            strHeartRateName = "";
                        }

                        friendsViewHolder.setUserHeartRateDate(getContext(), strHeartRateDate);
                        friendsViewHolder.setUserHeartRateName(strHeartRateName);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
        };

        recyclerViewFriends.setAdapter(friendsRecyclerAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        private View viewFriends;
        //private Context context;

        // Layout //
        private ImageView friendImageView;
        private TextView lblFriendName;
        private TextView lblFriendSinceDate;

        private TextView lblFriendGPS;
        private TextView lblFriendGPSFixDate;
        private TextView lblFriendGPSDate;
        private TextView lblFriendGPSFixName;
        private TextView lblFriendGPSName;
        private MapView mapViewGPS;
        private TextView lblFriendGPSFixStartTime;
        private TextView lblFriendGPSStartTime;
        private TextView lblFriendGPSFixEndTime;
        private TextView lblFriendGPSEndTime;
        private TextView lblFriendGPSFixTotalTime;
        private TextView lblFriendGPSTotalTime;
        private TextView lblFriendGPSFixTotalDistance;
        private TextView lblFriendGPSTotalDistance;
        private TextView lblFriendGPSFixAverageSpeed;
        private TextView lblFriendGPSAverageSpeed;
        private TextView lblFriendGPSFixMaxSpeed;
        private TextView lblFriendGPSMaxSpeed;

        private TextView lblFriendHeartRate;
        private TextView lblFriendHeartRateFixDate;
        private TextView lblFriendHeartRateDate;
        private TextView lblFriendHeartRateFixName;
        private TextView lblFriendHeartRateName;
        private LineChart mChart;
        private TextView lblFriendHeartRateFixRest;
        private TextView lblFriendHeartRateRest;
        private TextView lblFriendHeartRateFixStand;
        private TextView lblFriendHeartRateStand;
        private TextView lblFriendHeartRateFixDiff;
        private TextView lblFriendHeartRateDiff;
        private TextView lblFriendHeartRateFixMax;
        private TextView lblFriendHeartRateMax;

        // User Profil //
        private String thumb_image;

        // GPS //
        private GoogleMap mGoogleMap;
        private GPS gps;
        //private List<DateTime> time_gps = new ArrayList<DateTime>();
        //private List<LatLng> latitude_longitude = new ArrayList<LatLng>();

        // Heart Rate //
        private HeartRate hr;
        //private List<DateTime> time_hr = new ArrayList<DateTime>();
        //private List<Double> heart_rate = new ArrayList<Double>();

        public FriendsViewHolder(final View itemView)
        {
            super(itemView);

            viewFriends = itemView;
            //context = viewFriends.getContext();

            mapViewGPS = (MapView) itemView.findViewById(R.id.mapFriendGPS);

            mapViewGPS.onCreate(null);
            mapViewGPS.getMapAsync(new OnMapReadyCallback()
            {
                @Override
                public void onMapReady(GoogleMap googleMap)
                {
                    mGoogleMap = googleMap;
                    MapsInitializer.initialize(itemView.getContext());
                    mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                }
            });
        }

        public void setUserImage(final Context context, String thumbImage)
        {
            thumb_image = thumbImage;
            friendImageView = (ImageView) viewFriends.findViewById(R.id.imageViewFriendImage);

            if(!thumb_image.equals("default"))
            {
                // Piccasso offline capability //
                Picasso picasso = Picasso.with(context);
                picasso.setIndicatorsEnabled(false);
                picasso.load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profil).into(friendImageView, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {

                    }

                    @Override
                    public void onError()
                    {
                        // If Piccasso offline capability is not available try to use online capability //
                        Picasso picasso = Picasso.with(context);
                        picasso.setIndicatorsEnabled(false);
                        picasso.with(context).load(thumb_image).placeholder(R.drawable.default_profil).into(friendImageView);
                    }
                });
            }
        }

        public void setUserName(String name)
        {
            lblFriendName = (TextView) viewFriends.findViewById(R.id.lblFriendName);
            lblFriendName.setText(name);
        }

        public void setUserDate(final Context context, String date)
        {
            lblFriendSinceDate = (TextView) viewFriends.findViewById(R.id.lblFriendSinceDate);
            lblFriendSinceDate.setText(dateFriends(context, date));
        }


        public void setUserGPSDate(final Context context, String date)
        {
            lblFriendGPSDate = (TextView) viewFriends.findViewById(R.id.lblFriendGPSDate);
            lblFriendGPSDate.setText(dateGPS(context, date));
        }

        public void setUserGPSName(String name)
        {
            lblFriendGPSName = (TextView) viewFriends.findViewById(R.id.lblFriendGPSName);
            lblFriendGPSName.setText(name);
        }

        public void setUserGPS(final Context context, File file)
        {
            // Read .csv //
            gps = readCSVFileGPS(context, file);

            //time_gps = gps.getTime();
            //latitude_longitude = gps.getLatitudeLongitude();

            // Layout //
            lblFriendGPSStartTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSStartTime);
            lblFriendGPSEndTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSEndTime);
            lblFriendGPSTotalTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSTotalTime);
            lblFriendGPSTotalDistance = (TextView) viewFriends.findViewById(R.id.lblFriendGPSTotalDistance);
            lblFriendGPSAverageSpeed = (TextView) viewFriends.findViewById(R.id.lblFriendGPSAverageSpeed);
            lblFriendGPSMaxSpeed = (TextView) viewFriends.findViewById(R.id.lblFriendGPSMaxSpeed);

            if(gps.getTime() != null && gps.getLatitudeLongitude() != null)
            {
                // Calculate date, start/end/total time, total distance, average/max speed //
                lblFriendGPSStartTime.setText(startTimeGPS(context, gps));
                lblFriendGPSEndTime.setText(endTimeGPS(context, gps));
                lblFriendGPSTotalTime.setText(totalTimeGPS(context, gps));
                lblFriendGPSTotalDistance.setText(totalDistanceGPS(gps));
                lblFriendGPSAverageSpeed.setText(averageSpeedGPS(gps));
                lblFriendGPSMaxSpeed.setText(maxSpeedGPS(gps));

                // Google Map //
                drawPolyLineOnMap(mGoogleMap, gps);
            }
        }

        public void setUserGPSHide(Context context)
        {
            // Layout //
            lblFriendGPS = (TextView) viewFriends.findViewById(R.id.lblFriendGPS);
            lblFriendGPSFixDate = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixDate);
            lblFriendGPSDate = (TextView) viewFriends.findViewById(R.id.lblFriendGPSDate);
            lblFriendGPSFixName = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixName);
            lblFriendGPSName = (TextView) viewFriends.findViewById(R.id.lblFriendGPSName);
            lblFriendGPSFixStartTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixStartTime);
            lblFriendGPSStartTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSStartTime);
            lblFriendGPSFixEndTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixEndTime);
            lblFriendGPSEndTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSEndTime);
            lblFriendGPSFixTotalTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixTotalTime);
            lblFriendGPSTotalTime = (TextView) viewFriends.findViewById(R.id.lblFriendGPSTotalTime);
            lblFriendGPSFixTotalDistance = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixTotalDistance);
            lblFriendGPSTotalDistance = (TextView) viewFriends.findViewById(R.id.lblFriendGPSTotalDistance);
            lblFriendGPSFixAverageSpeed = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixAverageSpeed);
            lblFriendGPSAverageSpeed = (TextView) viewFriends.findViewById(R.id.lblFriendGPSAverageSpeed);
            lblFriendGPSFixMaxSpeed = (TextView) viewFriends.findViewById(R.id.lblFriendGPSFixMaxSpeed);
            lblFriendGPSMaxSpeed = (TextView) viewFriends.findViewById(R.id.lblFriendGPSMaxSpeed);

            // Hide all the elements //
            lblFriendGPS.setText(context.getString(R.string.noDataGPS_cap));
            lblFriendGPSFixDate.setVisibility(View.GONE);
            lblFriendGPSDate.setVisibility(View.GONE);
            lblFriendGPSFixName.setVisibility(View.GONE);
            lblFriendGPSName.setVisibility(View.GONE);
            mapViewGPS.setVisibility(View.GONE);
            lblFriendGPSFixStartTime.setVisibility(View.GONE);
            lblFriendGPSStartTime.setVisibility(View.GONE);
            lblFriendGPSFixEndTime.setVisibility(View.GONE);
            lblFriendGPSEndTime.setVisibility(View.GONE);
            lblFriendGPSFixTotalTime.setVisibility(View.GONE);
            lblFriendGPSTotalTime.setVisibility(View.GONE);
            lblFriendGPSFixTotalDistance.setVisibility(View.GONE);
            lblFriendGPSTotalDistance.setVisibility(View.GONE);
            lblFriendGPSFixAverageSpeed.setVisibility(View.GONE);
            lblFriendGPSAverageSpeed.setVisibility(View.GONE);
            lblFriendGPSFixMaxSpeed.setVisibility(View.GONE);
            lblFriendGPSMaxSpeed.setVisibility(View.GONE);
        }

        public void setUserHeartRateDate(final Context context, String date)
        {
            lblFriendHeartRateDate = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateDate);
            lblFriendHeartRateDate.setText(dateHeartRate(context, date));
        }

        public void setUserHeartRateName(String name)
        {
            lblFriendHeartRateName = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateName);
            lblFriendHeartRateName.setText(name);
        }

        public void setUserHeartRate(final Context context, File file)
        {
            // Layout //
            mChart = (LineChart) viewFriends.findViewById(R.id.lineChartFriendHeartRate);
            lblFriendHeartRateRest = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateRest);
            lblFriendHeartRateStand = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateStand);
            lblFriendHeartRateDiff = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateDiff);
            lblFriendHeartRateMax = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateMax);

            // Read .csv //
            hr = readCSVFileHeartRate(context, file);

            //time_hr = hr.getTime();
            //heart_rate = hr.getHeartRate();

            if(hr.getTime() != null && hr.getHeartRate() != null)
            {
                // MPChart Plot of Heart Rate test //
                plotHeartRate(context, mChart, hr);

                // Display data of test //
                lblFriendHeartRateRest.setText(restHeartRate(hr));
                lblFriendHeartRateStand.setText(standHeartRate(hr));
                lblFriendHeartRateDiff.setText(diffHeartRate(hr));
                lblFriendHeartRateMax.setText(maxHeartRate(hr));
            }
            else
            {
                // Hide MPCHart
                mChart.setVisibility(View.GONE);
            }
        }

        public void setUserHeartRateHide(Context context)
        {
            // Layout //
            lblFriendHeartRate = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRate);
            lblFriendHeartRateFixDate = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateFixDate);
            lblFriendHeartRateDate = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateDate);
            lblFriendHeartRateFixName = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateFixName);
            lblFriendHeartRateName = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateName);
            mChart = (LineChart) viewFriends.findViewById(R.id.lineChartFriendHeartRate);
            lblFriendHeartRateFixRest = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateFixRest);
            lblFriendHeartRateRest = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateRest);
            lblFriendHeartRateFixStand = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateFixStand);
            lblFriendHeartRateStand = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateStand);
            lblFriendHeartRateFixDiff = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateFixDiff);
            lblFriendHeartRateDiff = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateDiff);
            lblFriendHeartRateFixMax = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateFixMax);
            lblFriendHeartRateMax = (TextView) viewFriends.findViewById(R.id.lblFriendHeartRateMax);

            // Hide all the elements //
            lblFriendHeartRate.setText(context.getString(R.string.noDataHeartRate_cap));
            lblFriendHeartRateFixDate.setVisibility(View.GONE);
            lblFriendHeartRateDate.setVisibility(View.GONE);
            lblFriendHeartRateFixName.setVisibility(View.GONE);
            lblFriendHeartRateName.setVisibility(View.GONE);
            mChart.setVisibility(View.GONE);
            lblFriendHeartRateFixRest.setVisibility(View.GONE);
            lblFriendHeartRateRest.setVisibility(View.GONE);
            lblFriendHeartRateFixStand.setVisibility(View.GONE);
            lblFriendHeartRateStand.setVisibility(View.GONE);
            lblFriendHeartRateFixDiff.setVisibility(View.GONE);
            lblFriendHeartRateDiff.setVisibility(View.GONE);
            lblFriendHeartRateFixMax.setVisibility(View.GONE);
            lblFriendHeartRateMax.setVisibility(View.GONE);
        }
    }

    // GPS Date //
    public static String dateFriends(Context context, String date)
    {
        if(!date.equals(""))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat sdf_MMddyyyy = new SimpleDateFormat(context.getString(R.string.sdf_MMddyyyy));
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

    // Read a gps.csv from a local path //
    public static GPS readCSVFileGPS(Context context, File file)
    {
        // Delimiter used in CSV file //
        final String COMMA_DELIMITER = ";";

        // Display of times //
        SimpleDateFormat sdf_yyyyMMddTHHmmssZ = new SimpleDateFormat(context.getString(R.string.sdf_yyyyMMddTHHmmssZ));
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
            //time = null;
            //heart_rate = null;
        }
        catch (IOException e)
        {
            Log.e("Exception", "File write failed: " + e.toString());
        }

        // Return a GPS object //
        GPS gps = new GPS(time, latitude_longitude, elevation, heart_rate);
        return gps;
    }

    // Draw polyline on map
    public static void drawPolyLineOnMap(GoogleMap googleMap, GPS gps)
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

    // GPS Date //
    public static String dateGPS(Context context, String date)
    {
        if(!date.equals(""))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat sdf_MMddyyyy = new SimpleDateFormat(context.getString(R.string.sdf_MMddyyyy));
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
    public static String startTimeGPS(Context context, GPS gps)
    {
        if(gps.getTime() != null)
        {
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(context.getString(R.string.sdf_hhmmss));
            sdf_HHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));

            return sdf_HHmmss.format(gps.getTime().get(0).getMillis());
        }
        else
        {
            return "";
        }
    }

    // End time //
    public static String endTimeGPS(Context context, GPS gps)
    {
        if(gps.getTime() != null)
        {
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(context.getString(R.string.sdf_hhmmss));
            sdf_HHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));

            return sdf_HHmmss.format(gps.getTime().get(gps.getTime().size()-1).getMillis());
        }
        else
        {
            return "";
        }
    }

    // Total time //
    public static String totalTimeGPS(Context context, GPS gps)
    {
        if(gps.getTime() != null)
        {
            SimpleDateFormat sdf_HHmmss = new SimpleDateFormat(context.getString(R.string.sdf_hhmmss));
            sdf_HHmmss.setTimeZone(TimeZone.getTimeZone("GMT"));

            return sdf_HHmmss.format(gps.getTime().get(gps.getTime().size()-1).getMillis()-gps.getTime().get(0).getMillis());
        }
        else
        {
            return "";
        }
    }

    // Total distance //
    public static String totalDistanceGPS(GPS gps)
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
    public static String averageSpeedGPS(GPS gps)
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
    public static String maxSpeedGPS(GPS gps)
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

    // Read a hr.csv from a local path //
    public static HeartRate readCSVFileHeartRate(final Context context, File file)
    {
        // Delimiter used in CSV file //
        final String COMMA_DELIMITER = ";";

        // Display of times //
        SimpleDateFormat sdf_mmss = new SimpleDateFormat(context.getString(R.string.sdf_mmss));
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

    // HR MPChart Plot //
    public static void plotHeartRate(final Context context, LineChart lineChart, HeartRate hr)
    {
        Description desc = new Description();

        if(hr.getTime() != null && hr.getHeartRate() != null)
        {
            final long refTimestamp = hr.getTime().get(0).toDate().getTime();

            ArrayList<Entry> xyVals = new ArrayList<Entry>();
            for (int i = 0; i < hr.getHeartRate().size(); i++)
            {
                xyVals.add(new Entry(new Long(hr.getTime().get(i).toDate().getTime()-refTimestamp).floatValue(), new Double(hr.getHeartRate().get(i)).floatValue()));
            }

            // X-Axis //
            XAxis xAxis = lineChart.getXAxis();
            xAxis.setValueFormatter(new IAxisValueFormatter()
            {
                @Override
                public String getFormattedValue(float value, AxisBase axis)
                {
                    SimpleDateFormat sdf_mmss = new SimpleDateFormat(context.getString(R.string.sdf_mmss));
                    sdf_mmss.setTimeZone(TimeZone.getTimeZone("GMT"));
                    return sdf_mmss.format(new Date((long) (value + refTimestamp)));
                }
            });

            lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

            desc.setText(context.getString(R.string.axisTime));

            // Y-Axis //
            lineChart.getAxisRight().setEnabled(false);

            LineDataSet dataSet1 = new LineDataSet(xyVals, context.getString(R.string.axisHeartRate));
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
            lineChart.setNoDataText(context.getString(R.string.noDataHeartRate));
            desc.setText("");
        }

        lineChart.setDescription(desc);
    }

    // HR Date //
    public static String dateHeartRate(Context context, String date)
    {
        if(!date.equals(""))
        {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

            SimpleDateFormat sdf_MMddyyyy = new SimpleDateFormat(context.getString(R.string.sdf_MMddyyyy));
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
    public static String restHeartRate(HeartRate hr)
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
    public static String standHeartRate(HeartRate hr)
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
    public static String diffHeartRate(HeartRate hr)
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
    public static String maxHeartRate(HeartRate hr)
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
}