package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class SurveyActivity extends AppCompatActivity
{
    // Layout //
    private RadioGroup radioGroupDrug;
    private RadioButton radioButtonDrug;
    private RadioGroup radioGroupDiabetes;
    private RadioButton radioButtonDiabetes;
    private TextInputLayout txtpbcardiaque;
    private TextInputLayout txtpbalcohol;
    private TextInputLayout txtpbsleep;
    private TextInputLayout txtpbexercise;
    private Button btnSurveySave;

    // Toolbar //
    private Toolbar toolbarSurvey;

    // ProgressBar //
    private ProgressDialog progressDialogSurvey;

    // FireBase Database //
    private DatabaseReference mSurveyDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;

    // FireBase Storage //
    private StorageReference mSurveyStorage;

    // Write/Read File //
    private File path;
    private File file;
    final private String fileName = "survey.csv";
    ArrayList<String> listSurvey = new ArrayList<String>();

    // Time //
    private Date currentDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        // Layout //
        radioGroupDrug = (RadioGroup) findViewById(R.id.radioGroupDrug);
        radioGroupDiabetes = (RadioGroup) findViewById(R.id.radioGroupDiabetes);
        txtpbcardiaque = (TextInputLayout) findViewById(R.id.txtpbcardiaque);
        txtpbalcohol = (TextInputLayout) findViewById(R.id.txtpbalcohol);
        txtpbsleep = (TextInputLayout) findViewById(R.id.txtpbsleep);
        txtpbexercise = (TextInputLayout) findViewById(R.id.txtpbexercise);
        btnSurveySave = (Button) findViewById(R.id.btnSurveySave);

        // Toolbar //
        toolbarSurvey = (Toolbar) findViewById(R.id.toolbarSurvey);
        setSupportActionBar(toolbarSurvey);
        getSupportActionBar().setTitle(getString(R.string.toolbar_survey));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Status to Settings

        // ProgressDialog //
        progressDialogSurvey = new ProgressDialog(this);

        // File path //
        path = getFilesDir().getAbsoluteFile();

        // Date //
        currentDate = Calendar.getInstance().getTime();

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();
        mSurveyDatabase = FirebaseDatabase.getInstance().getReference().child("Survey").child(mCurrentUserID);
        // Firebase offline capability //
        mSurveyDatabase.keepSynced(true);

        // FireBase Storage //
        mSurveyStorage = FirebaseStorage.getInstance().getReference().child("Survey").child(mCurrentUserID);

        // Save Button click //
        btnSurveySave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // ProgressDialog //
                progressDialogSurvey.setTitle((getString(R.string.saving_changes)));
                progressDialogSurvey.setMessage(getString(R.string.please_wait_while_we_save_the_changes));
                progressDialogSurvey.setCanceledOnTouchOutside(false);;
                progressDialogSurvey.show();

                // Question: Drug //
                String drug;
                // Checking of radioButton position //
                radioButtonDrug = (RadioButton) findViewById(radioGroupDrug.getCheckedRadioButtonId());
                if(radioButtonDrug.getText().toString().equals(getString(R.string.pbdrug_no)))
                {
                    drug = "No";
                }
                else
                {
                    drug = "Yes";
                }

                // Question: Diabetes //
                String diabete;
                // Checking of radioButton position //
                radioButtonDiabetes = (RadioButton) findViewById(radioGroupDiabetes.getCheckedRadioButtonId());
                if(radioButtonDiabetes.getText().toString().equals(getString(R.string.pbdiabetes_no)))
                {
                    diabete = "No";
                }
                else
                {
                    diabete = "Yes";
                }

                // Question: Cardiaque //
                String cardiaque = txtpbcardiaque.getEditText().getText().toString();

                // Question: Alcohol //
                String alcohol = txtpbalcohol.getEditText().getText().toString();

                // Question: Sleep //
                String sleep = txtpbsleep.getEditText().getText().toString();

                // Question: Exercise //
                String exercise = txtpbexercise.getEditText().getText().toString();

                listSurvey.add(drug);
                listSurvey.add(diabete);
                listSurvey.add(cardiaque);
                listSurvey.add(alcohol);
                listSurvey.add(sleep);
                listSurvey.add(exercise);

                // Write hr.csv file //
                file = new File(path, fileName);
                writeCSVFileSurvey(file, listSurvey);

                // Load the uri from hr.csv file //
                Uri fileUri = Uri.fromFile(file);

                // Storage of gps.csv to GPS Storage //
                StorageReference filepath = mSurveyStorage.child("survey.csv");
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
                            updateHashMap.put("survey", downloadUrl);
                            updateHashMap.put("date", new SimpleDateFormat("MM/dd/yyyy").format(currentDate));
                            // Storage of URL from gps.csv to GPS DataBase //
                            mSurveyDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if(task.isSuccessful())
                                    {
                                        progressDialogSurvey.dismiss();
                                        Toast.makeText(SurveyActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            progressDialogSurvey.dismiss();
                            Toast.makeText(SurveyActivity.this, getString(R.string.file_not_uploaded), Toast.LENGTH_LONG).show();
                        }
                    }
                }));
                file.delete();
            }
        });
    }

    // Write a local file from a Survey data //
    private File writeCSVFileSurvey(File file, ArrayList<String> listSurvey)
    {
        // Delimiter used in .csv file //
        final String COMMA_DELIMITER = ";";

        //final String NEW_LINE_SEPARATOR = ";;;\n";
        final String NEW_LINE_SEPARATOR = "\n";

        // .csv file header //
        final String FILE_HEADER = "n°;answers";

        try
        {
            FileOutputStream fos = new FileOutputStream(file, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);

            // Write the .csv file header //
            osw.write(FILE_HEADER);

            // Add a new line separator after the header //
            osw.write(NEW_LINE_SEPARATOR);

            // Write new heart_rate/time data //
            for(int i = 0; i < listSurvey.size(); i++)
            {
                osw.write(new Integer(i+1).toString());
                osw.write(COMMA_DELIMITER);
                osw.write(listSurvey.get(i).toString());
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
        return file;
    }
}