package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartActivity extends AppCompatActivity
{
    // Layout //
    Button btnStartAlreadyAccount;
    Button btnStartNewAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Layout //
        btnStartAlreadyAccount = (Button) findViewById(R.id.btnStartAlreadyAccount);
        btnStartNewAccount = (Button) findViewById(R.id.btnStartNewAccount);

        // Already Account Button click //
        btnStartAlreadyAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intentLogin = new Intent(StartActivity.this, LoginActivity.class);
                startActivity(intentLogin);
            }
        });

        // New Account Button click //
        btnStartNewAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intentReg = new Intent(StartActivity.this, RegisterActivity.class);
                startActivity(intentReg);
            }
        });
    }
}