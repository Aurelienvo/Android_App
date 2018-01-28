package ch.epfl.fmottier.studenthealthmonitoring;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mottier on 14.12.2017.
 */

public class GPS
{
    public List<DateTime> time = new ArrayList<DateTime>();
    public List<LatLng> latitude_longitude = new ArrayList<LatLng>();
    public List<Double> elevation = new ArrayList<Double>();
    public List<Double> heart_rate = new ArrayList<Double>();

    public GPS(List<DateTime> time, List<LatLng> latitude_longitude, List<Double> elevation, List<Double> heart_rate)
    {
        this.time = time;
        this.latitude_longitude = latitude_longitude;
        this.elevation = elevation;
        this.heart_rate = heart_rate;
    }

    public List<DateTime> getTime()
    {
        return time;
    }

    public void setTime(List<DateTime> time)
    {
        this.time = time;
    }

    public List<LatLng> getLatitudeLongitude()
    {
        return latitude_longitude;
    }

    public void setLatitudeLongitude(List<LatLng> latitude_longitude)
    {
        this.latitude_longitude = latitude_longitude;
    }

    public List<Double> getElevation()
    {
        return elevation;
    }

    public void setElevation(List<Double> elevation)
    {
        this.elevation = elevation;
    }

    public List<Double> getHeartRate()
    {
        return heart_rate;
    }

    public void setHeartRate(List<Double> heart_rate)
    {
        this.heart_rate = heart_rate;
    }
}