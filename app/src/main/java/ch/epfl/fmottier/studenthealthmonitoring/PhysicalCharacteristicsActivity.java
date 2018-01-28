package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class PhysicalCharacteristicsActivity extends AppCompatActivity
{
    // Layout //
    //private TextInputLayout txtGender;
    private RadioGroup radioGroupGender;
    private RadioButton radioButtonGender;
    private RadioButton radioButtonMale;
    private RadioButton radioButtonFemale;
    private TextInputLayout txtBirthDate;
    private TextInputLayout txtHeight;
    private TextInputLayout txtWeight;
    private Button btnPhysicalCharacteristicsSave;

    // Toolbar //
    private Toolbar toolbarPhysicalCharacteristics;

    // ProgressBar //
    private ProgressDialog progressDialogPhysicalCharacteristics;

    // FireBase Database //
    private DatabaseReference mDatabaseRefPhysicalCharacteristics;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_characteristics);

        // Receive the value of the physical characteristics fields by intent
        String strGenderSend = getIntent().getStringExtra("genderSend");
        String strBirthDateSend = getIntent().getStringExtra("birthDateSend");
        String strHeightSend = getIntent().getStringExtra("heightSend");
        String strWeightSend = getIntent().getStringExtra("weightSend");

        // Layout //
        //txtGender = (TextInputLayout) findViewById(R.id.txtGender);
        //txtGender.getEditText().setText(strGenderSend); // Store the value of gender field //

        radioGroupGender = (RadioGroup) findViewById(R.id.radioGroupGender);

        radioButtonMale = (RadioButton) findViewById(R.id.radioButtonMale);
        radioButtonFemale = (RadioButton) findViewById(R.id.radioButtonFemale);

        if(strGenderSend.equals("m"))
        {
            radioButtonMale.toggle();
        }
        else
        {
            radioButtonFemale.toggle();
        }

        txtBirthDate = (TextInputLayout) findViewById(R.id.txtBirthDate);
        txtBirthDate.getEditText().setText(strBirthDateSend); // Store the value of birth_date field //

        txtHeight = (TextInputLayout) findViewById(R.id.txtHeight);
        txtHeight.getEditText().setText(strHeightSend); // Store the value of height field //

        txtWeight = (TextInputLayout) findViewById(R.id.txtWeight);
        txtWeight.getEditText().setText(strWeightSend); // Store the value of weight field //

        btnPhysicalCharacteristicsSave = (Button) findViewById(R.id.btnPhysicalCharacteristicsSave);

        // Toolbar //
        toolbarPhysicalCharacteristics = (Toolbar) findViewById(R.id.toolbarSurvey);
        setSupportActionBar(toolbarPhysicalCharacteristics);
        getSupportActionBar().setTitle(getString(R.string.toolbar_physical_characteristics));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogPhysicalCharacteristics = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = mCurrentUser.getUid();
        mDatabaseRefPhysicalCharacteristics = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        // Save Button click //
        btnPhysicalCharacteristicsSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogPhysicalCharacteristics.setTitle((getString(R.string.saving_changes)));
                progressDialogPhysicalCharacteristics.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogPhysicalCharacteristics.setCanceledOnTouchOutside(false);;
                progressDialogPhysicalCharacteristics.show();

                // Save Gender //
                String strGender;
                    // Checking of radioButton position //
                radioButtonGender = (RadioButton) findViewById(radioGroupGender.getCheckedRadioButtonId());
                if(radioButtonGender.getText().toString().equals(getString(R.string.male)))
                {
                    strGender = "m";
                }
                else
                {
                    strGender = "f";
                }

                mDatabaseRefPhysicalCharacteristics.child("gender").setValue(strGender).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            progressDialogPhysicalCharacteristics.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Save Birth Date //
                String birthDate = txtBirthDate.getEditText().getText().toString();

                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
                sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

                SimpleDateFormat sdf_MMddyyyy = new SimpleDateFormat(getString(R.string.sdf_MMddyyyy));
                sdf_MMddyyyy.setTimeZone(TimeZone.getTimeZone("GMT"));

                DateTime time = new DateTime();

                try
                {
                    time = new DateTime(sdf_MMddyyyy.parse(birthDate));
                }
                catch (java.text.ParseException e)
                {
                    Log.e("Exception", "Parse error: " + e.toString());
                }

                String strBirthDate = sdf.format(new Date((long) (time.toDate().getTime())));

                mDatabaseRefPhysicalCharacteristics.child("birth_date").setValue(strBirthDate).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            progressDialogPhysicalCharacteristics.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Save Height //
                String strHeight = txtHeight.getEditText().getText().toString();

                if(Integer.valueOf(strHeight) < 50 || Integer.valueOf(strHeight) > 250)
                    strHeight = "170";

                mDatabaseRefPhysicalCharacteristics.child("height").setValue(strHeight).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            progressDialogPhysicalCharacteristics.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), getString(R.string.error_in_saving_changes), Toast.LENGTH_LONG).show();
                        }
                    }
                });

                // Save Weight //
                String strWeight = txtWeight.getEditText().getText().toString();

                if(Integer.valueOf(strWeight) < 30 || Integer.valueOf(strHeight) > 250)
                    strWeight = "70";

                mDatabaseRefPhysicalCharacteristics.child("weight").setValue(strWeight).addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if(task.isSuccessful())
                        {
                            progressDialogPhysicalCharacteristics.dismiss();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"There was some error in saving changes", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
}