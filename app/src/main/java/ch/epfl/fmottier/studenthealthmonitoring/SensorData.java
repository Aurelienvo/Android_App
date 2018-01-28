package ch.epfl.fmottier.studenthealthmonitoring;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;

import java.io.Serializable;

@Entity
public class SensorData implements Cloneable, Serializable
{
    //Different types of sensors
    public final static int HEART_RATE = 0;
    public final static int LATITUDE = 1;
    public final static int LONGITUDE = 2;
    public final static int ALTITUDE = 3;

    @PrimaryKey(autoGenerate = true)
    public int uid;
    @ColumnInfo
    public String timestamp;
    @ColumnInfo
    public int type;
    @ColumnInfo
    public double value;

    public SensorData() {
    }

    public SensorData clone() {
        SensorData sensorData = null;
        try {
            sensorData = (SensorData) super.clone();

        } catch (CloneNotSupportedException cnse) {
            cnse.printStackTrace(System.err);
        }

        if (sensorData == null)
            Log.i("DEBUG", "Sensor Data Clone FAILED ********************");
        return sensorData;
    }

    public SensorData(DataMap map) {
        // Construct instance from the datamap
        uid = map.getInt("uid", uid);
        timestamp = map.getString("timestamp", timestamp);
        type = map.getInt("type", type);
        value = map.getDouble("value", value);
    }

    public DataMap toDataMap() {
        DataMap map = new DataMap();
        map.putInt("uid", uid);
        map.putString("timestamp", timestamp);
        map.putInt("type", type);
        map.putDouble("value", value);

        return map;
    }
}