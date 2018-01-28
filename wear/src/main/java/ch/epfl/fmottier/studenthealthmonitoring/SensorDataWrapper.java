package ch.epfl.fmottier.studenthealthmonitoring;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by ASUS on 10/01/2018.
 */

public class SensorDataWrapper implements Serializable {

    private ArrayList<SensorData> List_sd;


    public SensorDataWrapper (ArrayList<SensorData> liste){
        this.List_sd = liste;
    }

    public ArrayList<SensorData> getList_sd() {
        return List_sd;
    }
}
