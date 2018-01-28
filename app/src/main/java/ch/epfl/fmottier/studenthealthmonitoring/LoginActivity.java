package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity
{
    // Layout //
    private TextInputLayout txtLoginEmail;
    private TextInputLayout txtLoginPassword;
    private Button btnLoginLogin;

    // Toolbar //
    private Toolbar toolbarLogin;

    // ProgressBar //
    private ProgressDialog progressDialogLogin;

    // FireBase Authentication //
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Layout //
        txtLoginEmail = (TextInputLayout) findViewById(R.id.txtLoginEmail);
        txtLoginPassword = (TextInputLayout) findViewById(R.id.txtLoginPassword);
        btnLoginLogin = (Button) findViewById(R.id.btnLoginLogin);

        // Toolbar //
        toolbarLogin = (Toolbar) findViewById(R.id.toolbarLogin) ;
        setSupportActionBar(toolbarLogin);
        getSupportActionBar().setTitle(getString(R.string.toolbar_login));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Add a return button from Register to Start

        // ProgressBar //
        progressDialogLogin = new ProgressDialog(this);

        // FireBase Authentication //
        mAuth = FirebaseAuth.getInstance();

        // Login Button click //
        btnLoginLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String strLoginEmail = txtLoginEmail.getEditText().getText().toString();
                String strLoginPassword = txtLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(strLoginEmail) || !TextUtils.isEmpty(strLoginPassword));
                {
                    // ProgressDialog //
                    progressDialogLogin.setTitle(getString(R.string.logging_in));
                    progressDialogLogin.setMessage(getString(R.string.please_wait_while_we_check_your_credentials));
                    progressDialogLogin.setCanceledOnTouchOutside(false);
                    progressDialogLogin.show();

                    // User Sign in //
                    loginUser(strLoginEmail, strLoginPassword);
                }
            }
        });
    }

    // User Login //
    private void loginUser(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if(task.isSuccessful())
                {
                    // Close the ProgressDialog //
                    progressDialogLogin.dismiss();

                    // Intent LoginActivity to MainActivity
                    Intent intentMain = new Intent(LoginActivity.this, MainActivity.class);
                        // Disable the return button //
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intentMain);
                    finish();
                }
                else
                {
                    // Hide the ProgressDialog //
                    progressDialogLogin.hide();

                    // Debug //
                   // Log.i("DEBUG", "loginUserWithEmail:failure", task.getException());

                    // Toast //
                    Toast.makeText(LoginActivity.this, R.string.cannot_sign_in, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}