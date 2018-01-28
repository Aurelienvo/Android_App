package ch.epfl.fmottier.studenthealthmonitoring;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class AsyncTaskRead extends AsyncTask<Void,Void,List<SensorData>[]>{ //to return only one sensor Data

    private MyDatabase db;
    private Context context;


    AsyncTaskRead(MyDatabase db, Context context){
        this.db = db;
        this.context = context;
    }



    @Override
    protected List<SensorData>[] doInBackground(Void... voids) {

        List<SensorData>[] tablistdata = new List[4];
        tablistdata[0] = db.sensorDataDao().getLastNValues(SensorData.HEART_RATE, OutsideActivity.nbr_data);
        tablistdata[1] = db.sensorDataDao().getLastNValues(SensorData.ALTITUDE, OutsideActivity.nbr_data);
        tablistdata[2] = db.sensorDataDao().getLastNValues(SensorData.LONGITUDE, OutsideActivity.nbr_data);
        tablistdata[3] = db.sensorDataDao().getLastNValues(SensorData.LATITUDE, OutsideActivity.nbr_data);


        for (SensorData sd : tablistdata[0] ) {
            Log.i("Liste _HR","valeur de HR ="+ String.valueOf(sd.value) );
        }
        for (SensorData sd : tablistdata[1] ) {
            Log.i("Liste _ALT","valeur de ALT="+String.valueOf(sd.value));
        }
        for (SensorData sd : tablistdata[2] ) {
            Log.i("Liste _LON","valeur de LON="+String.valueOf(sd.value));
        }
        for (SensorData sd : tablistdata[3] ) {
            Log.i("Liste _LAT","valeur de LAT="+String.valueOf(sd.value));
        }

        Log.i("AsyncTaskRead finished","******************Read Data done**********");

        OutsideActivity.nbr_data=0;

        return tablistdata;

    }

    @Override
    protected void onPostExecute(List<SensorData>[] tablistsensorData) {

        Log.i("ON_POST_EXECUTE","******************Start Envoi**********");
        ArrayList<SensorData> List_to_send = new ArrayList<>();
        for( List<SensorData> list : tablistsensorData ) {
            List_to_send.addAll(list);
        }
        SensorDataWrapper List_sd_wrapped = new SensorDataWrapper(List_to_send);
        Intent intent = new Intent(context, WearListenerService.class);
        intent.setAction(WearListenerService.ACTION_SEND_LIST_HR);
        intent.putExtra(WearListenerService.DATAMAP_LIST_HR, List_sd_wrapped);
        context.startService(intent);



    }

}
