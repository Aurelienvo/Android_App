package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends WearableActivity {
    ConstraintLayout mLayout;
    private ImageButton btnStartGps;
    private ImageButton btnStartFit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.layout_main);
        setAmbientEnabled();

        btnStartGps = (ImageButton) findViewById(R.id.btnStartGps);
        btnStartFit = (ImageButton) findViewById(R.id.btnStartFit);

        // Already Account Button click //
        btnStartGps.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intentLogin = new Intent(MainActivity.this, OutsideActivity.class);
                startActivity(intentLogin);
            }
        });

        // New Account Button click //
        btnStartFit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intentReg = new Intent(MainActivity.this, FitActivity.class);
                startActivity(intentReg);
            }
        });

    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }


    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        super.onExitAmbient();
        updateDisplay();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            mLayout.setBackgroundColor(getResources().getColor(android.R.color.black, getTheme()));
        } else {
            mLayout.setBackground(null);
        }
    }

}
