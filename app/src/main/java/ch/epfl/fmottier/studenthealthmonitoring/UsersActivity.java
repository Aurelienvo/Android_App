package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity
{
    // Layout //
    private RecyclerView recyclerViewUsers;

    // Toolbar //
    private Toolbar toolbarUsers;

    // ProgressBar //
    private ProgressDialog progressDialogUsers;

    // FireBase Authentication //
    private FirebaseAuth mAuth;

    // FireBase DataBase //
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        // Layout //
        recyclerViewUsers = (RecyclerView) findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setHasFixedSize(true);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        // Toolbar //
        toolbarUsers = (Toolbar) findViewById(R.id.toolbarUsers) ;
        setSupportActionBar(toolbarUsers);
        getSupportActionBar().setTitle(getString(R.string.toolbar_users));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Register to Start

        // ProgressBar //
        progressDialogUsers = new ProgressDialog(this);
        //progressBarReg = new ProgressBar(this, null, android.R.attr.progressBarStyleSmall);

        // FireBase Authentication //
        mAuth = FirebaseAuth.getInstance();

        // FireBase DataBase //
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        // Firebase offline capability //
        mUsersDatabase.keepSynced(true);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Users, UsersViewHolder> UsersRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>(Users.class, R.layout.layout_user, UsersViewHolder.class, mUsersDatabase)
        {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position)
            {
                usersViewHolder.setUserName(users.getName());
                usersViewHolder.setUserStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getThumbImage(), getApplicationContext());

                final String userID = getRef(position).getKey(); // final to access into onClick //

                // Add button on list //
                usersViewHolder.viewUsers.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Intent intentProfile = new Intent(UsersActivity.this, ProfileUserActivity.class);
                        intentProfile.putExtra("user_id", userID);
                        startActivity(intentProfile);
                    }
                });
            }
        };

        recyclerViewUsers.setAdapter(UsersRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        private View viewUsers;
        private String thumb_image;
        private CircleImageView circleImageViewUserImage;

        public UsersViewHolder(View itemView)
        {
            super(itemView);

            viewUsers = itemView;
        }

        public void setUserName(String name)
        {
            TextView lblUserName = (TextView) viewUsers.findViewById(R.id.lblUserName);
            lblUserName.setText(name);
        }

        public void setUserStatus(String status)
        {
            TextView lblUserStatue = (TextView) viewUsers.findViewById(R.id.lblUserStatus);
            lblUserStatue.setText("\"" + status + "\"");
        }

        public void setUserImage(String thumbImage, final Context context)
        {
            thumb_image = thumbImage;
            circleImageViewUserImage = (CircleImageView) viewUsers.findViewById(R.id.circleImageViewUserImage);

            if(!thumb_image.equals("default"))
            {
                // Piccasso offline capability //
                Picasso picasso = Picasso.with(context);
                picasso.setIndicatorsEnabled(false);
                picasso.load(thumbImage).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profil).into(circleImageViewUserImage, new Callback()
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
                        picasso.load(thumb_image).placeholder(R.drawable.default_profil).into(circleImageViewUserImage);
                    }
                });
            }
        }
    }
}