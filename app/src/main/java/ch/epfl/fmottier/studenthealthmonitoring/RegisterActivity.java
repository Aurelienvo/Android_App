package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{
    // Layout //
    private TextInputLayout txtRegDisplayName;
    private TextInputLayout txtRegEmail;
    private TextInputLayout txtRegPassword;
    private Button btnRegCreateAccount;

    //Toolbar //
    private Toolbar toolbarReg;

    // ProgressBar //
    private ProgressDialog progressDialogReg;

    // FireBase Authentication //
    private FirebaseAuth mAuth;

    // FireBase Database //
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Layout //
        txtRegDisplayName = (TextInputLayout) findViewById(R.id.txtLoginEmail);
        txtRegEmail = (TextInputLayout) findViewById(R.id.txtRegEmail);
        txtRegPassword = (TextInputLayout) findViewById(R.id.txtLoginPassword);
        btnRegCreateAccount = (Button) findViewById(R.id.btnRegCreateAccount);

        // Toolbar //
        toolbarReg = (Toolbar) findViewById(R.id.toolbarReg);
        setSupportActionBar(toolbarReg);
        getSupportActionBar().setTitle(R.string.toolbar_register);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Add a return button from Register to Start //

        // ProgressBar //
        progressDialogReg = new ProgressDialog(this);

        // FireBase Authentication //
        mAuth = FirebaseAuth.getInstance();

        // Create Account Button click //
        btnRegCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strRegDisplayName = txtRegDisplayName.getEditText().getText().toString();
                String strRegEmail = txtRegEmail.getEditText().getText().toString();
                String strRegPassword = txtRegPassword.getEditText().getText().toString();

                if (!TextUtils.isEmpty(strRegDisplayName) || !TextUtils.isEmpty(strRegEmail) || !TextUtils.isEmpty(strRegPassword))
                    ;
                {
                    // Launch the ProgressDialog //
                    progressDialogReg.setTitle(getString(R.string.registering_user));
                    progressDialogReg.setMessage(getString(R.string.please_wait_while_we_create_your_account));
                    progressDialogReg.setCanceledOnTouchOutside(false);
                    progressDialogReg.show();

                    // User Register //
                    registerUser(strRegDisplayName, strRegEmail, strRegPassword);
                }
            }
        });
    }

    // User Register //
    private void registerUser(final String displayName, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Firebase Database //
                    FirebaseUser currentUserReg = FirebaseAuth.getInstance().getCurrentUser();
                    String userID = currentUserReg.getUid();

                    mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

                    HashMap<String, String> userMap = new HashMap<>();
                    userMap.put("name", displayName);
                    userMap.put("status", "Hi there, I'm new here");
                    userMap.put("image", "default");
                    userMap.put("thumb_image", "default");
                    userMap.put("gender", "m");
                    userMap.put("birth_date", "01/01/1991");
                    userMap.put("height", "170");
                    userMap.put("weight", "70");

                    mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            // Close the ProgressDialog //
                            progressDialogReg.dismiss();

                            // Intent RegisterActivity to MainActivity
                            Intent intentMain = new Intent(RegisterActivity.this, MainActivity.class);
                            // Disable the return button //
                            intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intentMain);
                            finish();
                        }
                    });

                } else {
                    // Hide the ProgressDialog //
                    progressDialogReg.hide();

                    // Debug //
                    Log.i("DEBUG", "createUserWithEmail:failure", task.getException());

                    // Toast //
                    Toast.makeText(RegisterActivity.this, getString(R.string.cannot_sign_in), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}