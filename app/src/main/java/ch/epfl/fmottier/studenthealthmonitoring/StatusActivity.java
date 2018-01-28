package ch.epfl.fmottier.studenthealthmonitoring;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity
{
    // Layout //
    private TextInputLayout txtStatus;
    private Button btnStatusSave;

    // Toolbar //
    private Toolbar toolbarStatus;

    // ProgressBar //
    private ProgressDialog progressDialogStatus;

    // FireBase Database //
    private DatabaseReference mStatusDatabase;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        // Receive the value of the status field by intent
        String strStatusSend = getIntent().getStringExtra("statusSend");

        // Layout //
        txtStatus = (TextInputLayout) findViewById(R.id.txtStatus);
        txtStatus.getEditText().setText(strStatusSend); // Store the value of status field //
        btnStatusSave = (Button) findViewById(R.id.btnStatusSave);

        // Toolbar //
        toolbarStatus = (Toolbar) findViewById(R.id.toolbarStatus);
        setSupportActionBar(toolbarStatus);
        getSupportActionBar().setTitle(getString(R.string.toolbar_status));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogStatus = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mStatusDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        // Save Button click //
        btnStatusSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogStatus.setTitle((getString(R.string.saving_changes)));
                progressDialogStatus.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogStatus.setCanceledOnTouchOutside(false);;
                progressDialogStatus.show();

                String strStatus = txtStatus.getEditText().getText().toString();

                mStatusDatabase.child("status").setValue(strStatus).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            progressDialogStatus.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),getString(R.string.error_in_saving_changes), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}
