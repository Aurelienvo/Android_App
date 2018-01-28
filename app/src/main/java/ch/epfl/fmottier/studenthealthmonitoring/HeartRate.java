package ch.epfl.fmottier.studenthealthmonitoring;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mottier on 14.12.2017.
 */

public class HeartRate
{
    public List<DateTime> time = new ArrayList<DateTime>();
    public List<Double> heart_rate = new ArrayList<Double>();

    public HeartRate(List<DateTime> time, List<Double> heart_rate)
    {
        this.time = time;
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

    public List<Double> getHeartRate()
    {
        return heart_rate;
    }

    public void setHeartRate(List<Double> heart_rate)
    {
        this.heart_rate = heart_rate;
    }
}
