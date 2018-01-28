package ch.epfl.fmottier.studenthealthmonitoring;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
{
    private Menu globalMenuItem;

    // Toolbar //
    private Toolbar toolbarMain;

    // ViewPager //
    private ViewPager viewPagerMain;
    private SectionsPagerAdapter sectPagerAdaptMain;

    private TabLayout tabLayoutMain;

    // FireBase Authentication //
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar //
        toolbarMain = (Toolbar) findViewById(R.id.toolbarMain);
        setSupportActionBar(toolbarMain);
        getSupportActionBar().setTitle(R.string.toolbar_main);

        // ViewPager //
        viewPagerMain = (ViewPager) findViewById(R.id.viewPagerMain) ;
        sectPagerAdaptMain = new SectionsPagerAdapter(getSupportFragmentManager(),getApplicationContext());

        viewPagerMain.setAdapter(sectPagerAdaptMain);

        tabLayoutMain = (TabLayout) findViewById(R.id.tabLayoutMain);
        tabLayoutMain.setupWithViewPager(viewPagerMain);

        // FireBase Authentication //
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        // FireBase: Check if user is signed in(non-null) and update UI accordingly //
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if(currentUser == null)
        {
            intentToStart();
        }
    }

    // Creation of Options Menu //
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        //menu.findItem(R.menu.menu_main).setIcon(R.drawable.menu);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    // Options Menu click //
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.menuSync)
        {
            intentToSync();
        }
        else if(item.getItemId() == R.id.menuitemMainAccountSettings)
        {
            intentToSettings();
        }
        else if(item.getItemId() == R.id.menuitemMainAllUsers)
        {
            intentToUsers();
        }
        else if(item.getItemId() == R.id.menuitemMainLogOut)
        {
            // User Sign out //

            FirebaseAuth.getInstance().signOut();
            intentToStart();
        }
        return true;
    }

    // Intent MainActivity to StartActivity //
    private void intentToStart()
    {
        Intent intentStart = new Intent(MainActivity.this, StartActivity.class);
        startActivity(intentStart);
        finish();
    }

    // Intent MainActivity to Users //
    private void intentToUsers()
    {
        Intent intentUsers = new Intent(MainActivity.this, UsersActivity.class);
        startActivity(intentUsers);
    }

    // Intent MainActivity to Settings //
    private void intentToSettings()
    {
        Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(intentSettings);
    }

    // Intent MainActivity to Sync //
    private void intentToSync()
    {
        Intent intentSync = new Intent(MainActivity.this, SyncActivity.class);
        startActivity(intentSync);
        //finish();
    }
}