package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ProfileUserActivity extends AppCompatActivity
{
    // Layout //
    private ImageView imageViewProfileUser;
    private TextView lblProfileUserName;
    private TextView lblProfileUserStatus;
    private TextView lblProfileUserFriends;
    private Button btnProfileUserAddFriend;

    // Toolbar //
    private Toolbar toolbarProfileUser;

    // ProgressBar //
    private ProgressDialog progressDialogProfileUser;

    // FireBase Database //
    private String userID;
    private DatabaseReference mUserDatabase;
    private DatabaseReference mFriendDatabase;
    private FirebaseUser mCurrentUser;


    //
    private String mCurrentState;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_user);

        // Layout //
        imageViewProfileUser = (ImageView) findViewById(R.id.imageViewProfileUser);
        lblProfileUserName = (TextView) findViewById(R.id.lblProfileUserName);
        lblProfileUserStatus = (TextView) findViewById(R.id.lblProfileUserStatus);
        lblProfileUserFriends = (TextView) findViewById(R.id.lblProfileUserFriends);
        btnProfileUserAddFriend = (Button) findViewById(R.id.btnProfileUserAddFriend);

        // Toolbar //
        toolbarProfileUser = (Toolbar) findViewById(R.id.toolbarProfileUser) ;
        setSupportActionBar(toolbarProfileUser);
        getSupportActionBar().setTitle(getString(R.string.toolbar_user_profil));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Register to Start

        // ProgressDialog //
        progressDialogProfileUser = new ProgressDialog(this);
        progressDialogProfileUser.setTitle(getString(R.string.loading_user_profile));
        progressDialogProfileUser.setMessage(getString(R.string.please_wait_while_we_load_the_user_data));
        progressDialogProfileUser.setCanceledOnTouchOutside(false);;
        progressDialogProfileUser.show();

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userID = getIntent().getStringExtra("user_id"); // Get the ID user selected //
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");

        // Firebase offline capability //
        mUserDatabase.keepSynced(true);
        mFriendDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                // Piccasso offline capability //
                Picasso picasso = Picasso.with(ProfileUserActivity.this);
                picasso.setIndicatorsEnabled(false);
                picasso.load(image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profil).into(imageViewProfileUser, new Callback()
                {
                    @Override
                    public void onSuccess()
                    {

                    }

                    @Override
                    public void onError()
                    {
                        // If Piccasso offline capability is not available try to use online capability //
                        Picasso picasso = Picasso.with(ProfileUserActivity.this);
                        picasso.setIndicatorsEnabled(false);
                        picasso.load(image).placeholder(R.drawable.default_profil).into(imageViewProfileUser);
                    }
                });

                lblProfileUserName.setText(name);
                lblProfileUserStatus.setText("\"" + status + "\"");

                // Detect if the user already are friend or not //
                mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if(dataSnapshot.hasChild(userID))
                        {
                            btnProfileUserAddFriend.setText(R.string.unfriend_this_person_cap);
                            btnProfileUserAddFriend.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                        }
                        else
                        {
                            btnProfileUserAddFriend.setText(R.string.friend_this_person_cap);
                            btnProfileUserAddFriend.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                // Count the total friends of user //
                mFriendDatabase.child(userID).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        lblProfileUserFriends.setText(Long.toString(dataSnapshot.getChildrenCount()));
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });

                progressDialogProfileUser.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });

        // Add Friend Click //
        btnProfileUserAddFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(userID))
                        {
                            mFriendDatabase.child(mCurrentUser.getUid()).child(userID).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {

                                }


                            });

                            btnProfileUserAddFriend.setText(R.string.friend_this_person_cap);
                            btnProfileUserAddFriend.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_green_dark));
                        }
                        else
                        {
                            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                            sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                            String mCurrentDate = sdf.format(new Date());

                            mFriendDatabase.child(mCurrentUser.getUid()).child(userID).child("date").setValue(mCurrentDate).addOnSuccessListener(new OnSuccessListener<Void>()
                            {
                                @Override
                                public void onSuccess(Void aVoid)
                                {

                                }
                            });

                            btnProfileUserAddFriend.setText(R.string.unfriend_this_person_cap);
                            btnProfileUserAddFriend.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_orange_dark));
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                });
            }
        });
    }
}