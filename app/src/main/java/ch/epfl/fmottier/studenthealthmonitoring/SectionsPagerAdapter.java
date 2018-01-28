package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Mottier on 10.11.2017.
 */

class SectionsPagerAdapter extends FragmentPagerAdapter
{
    Context context;

    public SectionsPagerAdapter(FragmentManager fm, Context c)
    {
        super(fm);
        context = c;
    }

    @Override
    public Fragment getItem(int position)
    {
        switch(position)
        {
            case 0:
                GPSFragment GPSFragment = new GPSFragment();
                return GPSFragment;

            case 1:
                HeartRateFragment heartRateFragment = new HeartRateFragment();
                return heartRateFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch(position)
        {
            case 0:
                return context.getString(R.string.section_gps);

            case 1:
                return context.getString(R.string.section_heart_rate);

            case 2:
                return context.getString(R.string.section_friends);

            default:
                return null;
        }
    }
}