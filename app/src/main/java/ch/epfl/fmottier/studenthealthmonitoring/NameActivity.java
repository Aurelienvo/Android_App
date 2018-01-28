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

public class NameActivity extends AppCompatActivity
{
    // Layout //
    private TextInputLayout txtName;
    private Button btnNameSave;

    // Toolbar //
    private Toolbar toolbarName;

    // ProgressBar //
    private ProgressDialog progressDialogName;

    // FireBase Database //
    private DatabaseReference mDatabaseRefName;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_name);

        // Receive the value of the status field by intent
        String strNameSend = getIntent().getStringExtra("nameSend");

        // Layout //
        txtName = (TextInputLayout) findViewById(R.id.txtName);
        txtName.getEditText().setText(strNameSend); // Store the value of status field //
        btnNameSave = (Button) findViewById(R.id.btnNameSave);

        // Toolbar //
        toolbarName = (Toolbar) findViewById(R.id.toolbarName);
        setSupportActionBar(toolbarName);
        getSupportActionBar().setTitle(getString(R.string.toolbar_name));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogName = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mDatabaseRefName = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        // Save Button click //
        btnNameSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogName.setTitle((getString(R.string.saving_changes)));
                progressDialogName.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogName.setCanceledOnTouchOutside(false);;
                progressDialogName.show();

                String strName = txtName.getEditText().getText().toString();

                mDatabaseRefName.child("name").setValue(strName).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            progressDialogName.dismiss();
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