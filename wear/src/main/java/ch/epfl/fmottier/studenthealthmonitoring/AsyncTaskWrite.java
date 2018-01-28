package ch.epfl.fmottier.studenthealthmonitoring;

/**
 * Created by ASUS on 21/11/2017.
 */

import android.os.AsyncTask;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

public class AsyncTaskWrite extends AsyncTask<List<SensorData>,Void,Void> {

    private MyDatabase db;

    AsyncTaskWrite(MyDatabase db){
        this.db = db;
    }


    @Override
    protected Void doInBackground(List<SensorData>[] lists) {


        //Adding the latitude into the database
        Iterator<SensorData> it = lists[0].iterator();

        for(SensorData sd:lists[0]) {
            Log.i("Check_Write_Database", "type Valeur stored " + String.valueOf(sd.type) +
                    "\n Valeur stored " + String.valueOf(sd.value));
            db.sensorDataDao().insertSensorData(sd);
        }
        Log.i("Check_Write_Database", "Stored value OK et N°Data="+OutsideActivity.nbr_data);

        return null;
    }

    @Override
    protected void onPostExecute(Void lon_lat_data) {
    }
}
