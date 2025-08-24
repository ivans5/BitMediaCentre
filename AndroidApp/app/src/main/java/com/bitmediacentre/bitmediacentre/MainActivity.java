package com.bitmediacentre.bitmediacentre;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.widget.Toast;

import com.bitmediacentre.bitmediacentre.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {


    public SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        final CustomViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                //Toast.makeText(getApplicationContext(), "onPageSelected: position="+position, Toast.LENGTH_SHORT).show();

                if (position == 1) {  // Example: Disable swipe for Tab 2
                    viewPager.setSwipeEnabled(false);
                } else {
                    viewPager.setSwipeEnabled(true);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        /*
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
         */

        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    acquireWifiLock();  //they say it's okay to acquire the lock more than once, release only needs to be called once...
                } else if (tab.getPosition() == 1)  {
                    releaseWifiLock();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) { }
        });

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
    }


    @Override
    public void onStart() {
        super.onStart();
        TabLayout tabs = findViewById(R.id.tabs);

        //If tab1 is visible acquire lock:
        if (tabs.getSelectedTabPosition() == 0)  {
            acquireWifiLock();
        }

    }

    @Override
    public void onStop() {
        super.onStop();
        releaseWifiLock();
    }

    WifiManager.WifiLock wfl = null;

    public void acquireWifiLock()  {
        //Toast.makeText(getApplicationContext(), "acquiring wifi lock: "+wfl, Toast.LENGTH_LONG).show();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (wfl == null) {
                        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

                        wfl = wm.createWifiLock(WifiManager.WIFI_MODE_FULL_LOW_LATENCY, "mylock");
                    }
                    if (wfl != null) {
                        wfl.acquire();
                    }
                } catch (Exception e)  {
                    //TODO: do something...
                }
            }
        }.start();
    }

    public void releaseWifiLock()  {
        //Toast.makeText(getApplicationContext(), "releasing wifi lock: "+wfl, Toast.LENGTH_LONG).show();
        new Thread() {
            @Override
            public void run() {
                try {
                    if (wfl != null) {
                        wfl.release();
                    }
                } catch (Exception e)  {
                    //TODO: do something...
                }
            }
        }.start();
    }

}