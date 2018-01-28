package ch.epfl.fmottier.studenthealthmonitoring;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by Mottier on 20.11.2017.
 */

public class OfflineCapability extends Application
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        // Enable the Firebase offline capabilitiy //
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Enable the Picasso offline capabilitiy //
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = builder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);
    }
}
