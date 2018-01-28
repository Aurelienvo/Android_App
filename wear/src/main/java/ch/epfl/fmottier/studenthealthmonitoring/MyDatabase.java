package ch.epfl.fmottier.studenthealthmonitoring;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SensorData.class}, version = 1, exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    //Abstract class for inheritance: you don't implement the methods but you can extend this class and implement them and add other features

    //Instance of the database that will be used later
    public static MyDatabase INSTANCE;

    //Dao to associate to the database and use the queries implemented
    public abstract SensorDataInterface sensorDataDao();

    //Constructor of the class. It's "synchronized" to avoid that concurrent threads corrupts the instance.
    public static synchronized MyDatabase getDatabase(Context context){
        //Singleton pattern
        if(INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context,MyDatabase.class,"SensorDB").build();

        }
        return INSTANCE;
    }

    //Method to destroy the instance of the database
    public static void destroyInstance(){
        INSTANCE = null;
    }
}


