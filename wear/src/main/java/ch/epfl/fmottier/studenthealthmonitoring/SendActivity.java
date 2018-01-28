package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SendActivity extends WearableActivity {

    private Button BtnSave,BtnDelete;
    private MyDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);

        // Enables Always-on
        setAmbientEnabled();

        BtnSave = (Button) findViewById(R.id.butonSave);
        BtnDelete = (Button) findViewById(R.id.butonDelete);

        db = MyDatabase.getDatabase(getApplicationContext());

        BtnSave.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                AsyncTaskRead asyncTaskRead = new AsyncTaskRead(db,SendActivity.this);
                asyncTaskRead.execute();

                LayoutInflater inflater = getLayoutInflater();
                View layout = inflater.inflate(R.layout.toast_send,(ViewGroup) findViewById(R.id.custom_toast_container));


                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.BOTTOM, 0, 20);
                toast.setDuration(Toast.LENGTH_SHORT);
                toast.setView(layout);
                View toast_view = toast.getView();
                toast_view.setBackgroundResource(R.drawable.toast_shape);
                toast.setView(toast_view);
                toast.show();


                Intent intentReg = new Intent(SendActivity.this, MainActivity.class);
                startActivity(intentReg);

            }
        });

        BtnDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intentReg = new Intent(SendActivity.this, MainActivity.class);
                startActivity(intentReg);
            }
        });
    }
}
