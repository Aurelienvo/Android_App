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

public class HeartRateNameActivity extends AppCompatActivity
{
    // Layout //
    private TextView lblHeartRateName;
    private TextInputLayout txtHeartRateName;
    private Button btnHeartRateNameSave;

    // Toolbar //
    private Toolbar toolbarHeartRateName;

    // ProgressBar //
    private ProgressDialog progressDialogHeartRateName;

    // FireBase Database //
    private DatabaseReference mHeartRateDatabase;
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
        lblHeartRateName = (TextView) findViewById(R.id.lblName);
        txtHeartRateName = (TextInputLayout) findViewById(R.id.txtName);
        txtHeartRateName.getEditText().setText(strGPSNameSend); // Store the value of status field //
        btnHeartRateNameSave = (Button) findViewById(R.id.btnNameSave);

        lblHeartRateName.setText(getResources().getString(R.string.change_your_Heart_Rate_name));
        txtHeartRateName.setHint(getResources().getString(R.string.your_Heart_Rate_name));

        // Toolbar //
        toolbarHeartRateName = (Toolbar) findViewById(R.id.toolbarName);
        setSupportActionBar(toolbarHeartRateName);
        getSupportActionBar().setTitle(getString(R.string.toolbar_heart_rate_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogHeartRateName = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();
        mHeartRateDatabase = FirebaseDatabase.getInstance().getReference().child("HeartRate").child(mCurrentUserID);
        // Firebase offline capability //
        mHeartRateDatabase.keepSynced(true);

        // Save Button click //
        btnHeartRateNameSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogHeartRateName.setTitle((getString(R.string.saving_changes)));
                progressDialogHeartRateName.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogHeartRateName.setCanceledOnTouchOutside(false);
                ;
                progressDialogHeartRateName.show();

                String strGPSName = txtHeartRateName.getEditText().getText().toString();

                mHeartRateDatabase.child("name").setValue(strGPSName).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            progressDialogHeartRateName.dismiss();
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