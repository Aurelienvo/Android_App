package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class SettingsActivity extends AppCompatActivity
{
    // Layout //
    private CircleImageView circleImageViewSettingsImage;
    private TextView lblSettingsName;
    private TextView lblSettingsEmail;
    private TextView lblSettingsStatus;
    private ImageView imageViewSettingsGender;
    private TextView lblSettingsBirthDate;
    private TextView lblSettingsHeight;
    private TextView lblSettingsWeight;
    private Button btnSettingsChangeImage;
    private Button btnSettingsChangeName;
    private Button btnSettingsChangeStatus;
    private Button btnSettingsChangePhysicalCharacteristics;

    // Toolbar //
    private Toolbar toolbarSettings;

    // ProgressBar //
    private ProgressDialog progressDialogSettings;

    // Intent to the Gallery //
    private static final int GALLERY_PICK = 1;

    // FireBase Database //
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;
    private String mCurrentUserID;

    // FireBase Storage //
    private StorageReference mImageStorage;

    // Data //
    private String name;
    private String image;
    private String status;
    private String thumb_image;
    private String gender;
    private String birth_date;
    private String height;
    private String weight;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Layout //
        circleImageViewSettingsImage = (CircleImageView) findViewById(R.id.circleImageViewSettingsImage);
        lblSettingsName = (TextView) findViewById(R.id.lblSettingsName);
        lblSettingsStatus = (TextView) findViewById(R.id.lblSettingsStatus);
        lblSettingsEmail = (TextView) findViewById(R.id.lblSettingsEmail);
        imageViewSettingsGender = (ImageView) findViewById(R.id.imageViewSettingsGender);
        lblSettingsBirthDate = (TextView) findViewById(R.id.lblSettingsBirthDate);
        lblSettingsHeight = (TextView) findViewById(R.id.lblSettingsHeight);
        lblSettingsWeight = (TextView) findViewById(R.id.lblSettingsWeight);
        btnSettingsChangeImage = (Button) findViewById(R.id.btnSettingsChangeImage);
        btnSettingsChangeName = (Button) findViewById(R.id.btnSettingsChangeName);
        btnSettingsChangeStatus = (Button) findViewById(R.id.btnSettingsChangeStatus);
        btnSettingsChangePhysicalCharacteristics = (Button) findViewById(R.id.btnSettingsChangePhysicalCharacteristics);

        // Toolbar //
        toolbarSettings = (Toolbar) findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbarSettings);
        getSupportActionBar().setTitle(getString(R.string.toolbar_settings));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Settings to Main

        // ProgressBar //
        progressDialogSettings = new ProgressDialog(this);

        // FireBase Database //
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        mCurrentUserID = mCurrentUser.getUid();

        // FireBase Database //
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUserID);

        // Firebase offline capability //
        mUserDatabase.keepSynced(true);

        mUserDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Load of all FireBase data
                name = dataSnapshot.child("name").getValue().toString();
                image = dataSnapshot.child("image").getValue().toString();
                status = dataSnapshot.child("status").getValue().toString();
                thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                gender = dataSnapshot.child("gender").getValue().toString();
                birth_date = dataSnapshot.child("birth_date").getValue().toString();
                height = dataSnapshot.child("height").getValue().toString();
                weight = dataSnapshot.child("weight").getValue().toString();

                // Display of thumb image //
                if(!thumb_image.equals("default"))
                {
                    // Piccasso offline capability //
                    Picasso picasso = Picasso.with(SettingsActivity.this);
                    picasso.setIndicatorsEnabled(false);
                    picasso.load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).placeholder(R.drawable.default_profil).into(circleImageViewSettingsImage, new Callback()
                    {
                        @Override
                        public void onSuccess()
                        {

                        }

                        @Override
                        public void onError()
                        {
                            // If Piccasso offline capability is not available try to use online capability //
                            Picasso picasso = Picasso.with(SettingsActivity.this);
                            picasso.setIndicatorsEnabled(false);
                            picasso.load(thumb_image).placeholder(R.drawable.default_profil).into(circleImageViewSettingsImage);
                        }
                    });
                }

                // Display of Name //
                lblSettingsName.setText(name);

                // Display of Email //
                lblSettingsEmail.setText(mCurrentUser.getEmail().toString());

                // Display of Status //
                lblSettingsStatus.setText("\"" + status + "\"");

                // Display of Gender //
                if(gender.equals("m"))
                {
                    imageViewSettingsGender.setImageResource(R.drawable.symbol_male);
                }
                else
                {
                    imageViewSettingsGender.setImageResource(R.drawable.symbol_female);
                }

                // Display of Birth Date //
                lblSettingsBirthDate.setText(birthDate(birth_date));

                // Display of Height //
                lblSettingsHeight.setText(height + " kg");

                // Display of Weight //
                lblSettingsWeight.setText(weight + " cm");
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(SettingsActivity.this, getString(R.string.a_problem_occurred), Toast.LENGTH_LONG).show();
            }
        });

        // FireBase Storage //
        mImageStorage = FirebaseStorage.getInstance().getReference().child("Users").child(mCurrentUserID);

        // Change Image Click //
        btnSettingsChangeImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Intent from SettingsActivity to Gallery //
                Intent intentGallery = new Intent();
                intentGallery.setType("image/*");
                intentGallery.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intentGallery,"SELECT IMAGE"),GALLERY_PICK);
            }
        });

        // Change Name Click //
        btnSettingsChangeName.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Value of the Name field //
                String nameSend = name;

                // Intent from SettingsActivity to NameActivity //
                Intent intentName = new Intent(SettingsActivity.this, NameActivity.class);
                intentName.putExtra("nameSend", nameSend); // Send the value of the status field to StatusActivity //
                startActivity(intentName);
            }
        });

        // Change Status Click //
        btnSettingsChangeStatus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Value of the Status field //
                String statusSend = status;

                // Intent from SettingsActivity to StatusActivity //
                Intent intentStatus = new Intent(SettingsActivity.this, StatusActivity.class);
                intentStatus.putExtra("statusSend", statusSend); // Send the value of the status field to StatusActivity //
                startActivity(intentStatus);
            }
        });

        // Change Physical Characteristics Click //
        btnSettingsChangePhysicalCharacteristics.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                // Value of the Status field //
                String genderSend = gender;
                String birthDateSend = birth_date;
                String heightSend = height;
                String weightSend = weight;

                // Intent from SettingsActivity to StatusActivity //
                Intent intentPhysicalCharacteristics = new Intent(SettingsActivity.this, PhysicalCharacteristicsActivity.class);
                // Send the value of the status field to PhysicalCharacteristicsActivity //
                intentPhysicalCharacteristics.putExtra("genderSend", genderSend);
                intentPhysicalCharacteristics.putExtra("birthDateSend", birthDateSend);
                intentPhysicalCharacteristics.putExtra("heightSend", heightSend);
                intentPhysicalCharacteristics.putExtra("weightSend", weightSend);

                startActivity(intentPhysicalCharacteristics);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK)
        {
            // Recovery of Image Uri //
            Uri uriImage = data.getData();
            // Open a image manager with a square shape //
            CropImage.activity(uriImage).setGuidelines(CropImageView.Guidelines.ON).setCropShape(CropImageView.CropShape.RECTANGLE).setAspectRatio(1,1).start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                // ProgressDialog //
                progressDialogSettings.setTitle(getString(R.string.uploading_image));
                progressDialogSettings.setMessage(getString(R.string.please_wait_while_we_upload_and_process_the_image));
                progressDialogSettings.setCanceledOnTouchOutside(false);;
                progressDialogSettings.show();

                Uri uriResult = result.getUri();

                File thumbFile = new File(uriResult.getPath());

                // Compressing of image //
                Bitmap thumbBitmap = new Compressor(this).setMaxWidth(200).setMaxHeight(200).setQuality(75).compressToBitmap(thumbFile);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                final byte[] thumbByte = baos.toByteArray();

                // Storage of image and thumb_image (image compressed) //
                StorageReference filepath = mImageStorage.child("image").child("image.jpg");
                final StorageReference thumbFilePath = mImageStorage.child("thumb_image").child("thumb_image.jpg");

                filepath.putFile(uriResult).addOnCompleteListener((new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbFilePath.putBytes(thumbByte);

                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumbTask)
                                {
                                    final String thumbDownloadUrl = thumbTask.getResult().getDownloadUrl().toString();

                                    if(thumbTask.isSuccessful())
                                    {
                                        // Add a part of HashMap //
                                        Map updateHashMap = new HashMap<>();
                                        updateHashMap.put("image", downloadUrl);
                                        updateHashMap.put("thumb_image", thumbDownloadUrl);

                                        mUserDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    progressDialogSettings.dismiss();
                                                    Toast.makeText(SettingsActivity.this, getString(R.string.image_uploaded), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    }
                                    else
                                    {
                                        progressDialogSettings.dismiss();
                                        Toast.makeText(SettingsActivity.this, getString(R.string.image_not_uploaded), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                }));
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Toast.makeText(SettingsActivity.this, getString(R.string.a_problem_occurred), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Date //
    private String birthDate(String date)
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
}