package net.alexandroid.testgpsproviders;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ZAQ-MainActivity";
    private LocationHelper mLocationHelper;
    private LocationHelper.LocationResult mLocationResult;
    private ToggleButton tbGps, tbNetwork, tbPassive;
    private Location tempLocation;
    private TextView tv_result;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();
        mLocationHelper = new LocationHelper(getApplicationContext());
        refreshProvidersStatus(null);

        mLocationResult = new LocationHelper.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if (location != null) {
                    tempLocation = location;
                } else {
                    tempLocation = null;
                }
                runOnUiThread(showResults);
            }
        };
        mLocationHelper.getLocation(mLocationResult);
    }

    Runnable showResults = new Runnable() {
        @Override
        public void run() {
            if (tempLocation != null) {
                String provider = "Provider used: " + tempLocation.getProvider();
                String accuracy = "Accuracy: " + tempLocation.getAccuracy() + " meters";
                String time = "Time: " + ((System.currentTimeMillis() - tempLocation.getTime()) / 1000 / 60);
                tv_result.setText(provider + "\n" + accuracy + "\n" + time);
            } else {
                tv_result.setText("location == null");
            }
        }
    };

    private void setViews() {
        tbGps = (ToggleButton) findViewById(R.id.tb_gps_provider);
        tbNetwork = (ToggleButton) findViewById(R.id.tb_network_provider);
        tbPassive = (ToggleButton) findViewById(R.id.tb_passive_provider);
        tv_result = (TextView) findViewById(R.id.tv_result);
    }

    public void refreshProvidersStatus(View view) {
        mLocationHelper.refreshStatus();
        tbGps.setChecked(mLocationHelper.isGpsProviderEnabled());
        tbNetwork.setChecked(mLocationHelper.isNetworkProviderEnabled());
        tbPassive.setChecked(mLocationHelper.isPassiveProviderEnabled());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void refreshResult(View view) {
        tv_result.setText("...........");
        mLocationHelper.getLocation(mLocationResult);
    }
}
