package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GPSNameActivity extends AppCompatActivity
{
    // Layout //
    private TextView lblGPSName;
    private TextInputLayout txtGPSName;
    private Button btnGPSNameSave;

    // Toolbar //
    private Toolbar toolbarGPSName;

    // ProgressBar //
    private ProgressDialog progressDialogGPSName;

    // FireBase Database //
    private DatabaseReference mGPSDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        // Receive the value of the status field by intent
        String strGPSNameSend = getIntent().getStringExtra("nameSend");

        // Layout //
        lblGPSName = (TextView) findViewById(R.id.lblName);
        txtGPSName = (TextInputLayout) findViewById(R.id.txtName);
        txtGPSName.getEditText().setText(strGPSNameSend); // Store the value of status field //
        btnGPSNameSave = (Button) findViewById(R.id.btnNameSave);

        lblGPSName.setText(getResources().getString(R.string.change_your_GPS_name));
        txtGPSName.setHint(getResources().getString(R.string.your_GPS_name));

        // Toolbar //
        toolbarGPSName = (Toolbar) findViewById(R.id.toolbarName);
        setSupportActionBar(toolbarGPSName);
        getSupportActionBar().setTitle(getString(R.string.toolbar_gps_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogGPSName = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();
        mGPSDatabase = FirebaseDatabase.getInstance().getReference().child("GPS").child(mCurrentUserID);
        // Firebase offline capability //
        mGPSDatabase.keepSynced(true);

        // Save Button click //
        btnGPSNameSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogGPSName.setTitle((getString(R.string.saving_changes)));
                progressDialogGPSName.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogGPSName.setCanceledOnTouchOutside(false);
                ;
                progressDialogGPSName.show();

                String strGPSName = txtGPSName.getEditText().getText().toString();

                mGPSDatabase.child("name").setValue(strGPSName).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            progressDialogGPSName.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_in_saving_changes), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}