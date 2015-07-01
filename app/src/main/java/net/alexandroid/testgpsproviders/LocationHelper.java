package net.alexandroid.testgpsproviders;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class LocationHelper {
    private static final String TAG = "ZAQ-LocationHelper";
    public static final int LAST_UPDATE_THRESHOLD = 5;
    public static final int ACCURACY_THRESHOLD = 100;
    public static final int SEARCH_TIME_LIMIT = 7000;

    LocationManager mLocationManager;
    LocationResult mLocationResult;
    boolean gpsProviderEnabled = false;
    boolean networkProviderEnabled = false;
    boolean passiveProviderEnabled = false;
    Handler mHandler;

    // Provider priority
    String[] providers = {
            LocationManager.PASSIVE_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER
    };

    public LocationHelper(Context context) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        refreshStatus();
    }

    // Providers status
    public void refreshStatus() {
        gpsProviderEnabled = getProviderStatus(LocationManager.GPS_PROVIDER);
        networkProviderEnabled = getProviderStatus(LocationManager.NETWORK_PROVIDER);
        passiveProviderEnabled = getProviderStatus(LocationManager.PASSIVE_PROVIDER);

        Location gpsLastKnownLocation = getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (gpsLastKnownLocation != null) {
            Log.d(TAG, "GPS - Accuracy: " + gpsLastKnownLocation.getAccuracy() + " meters");
            Log.d(TAG, "GPS - Time: " + ((System.currentTimeMillis() - gpsLastKnownLocation.getTime()) / 1000 / 60));
        }

        Location networkLastKnownLocation = getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (networkLastKnownLocation != null) {
            Log.d(TAG, "Network - Accuracy: " + networkLastKnownLocation.getAccuracy() + " meters");
            Log.d(TAG, "Network - Time: " + ((System.currentTimeMillis() - networkLastKnownLocation.getTime()) / 1000 / 60));
        }

        Location passiveLastKnownLocation = getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (passiveLastKnownLocation != null) {
            Log.d(TAG, "Passive - Accuracy: " + passiveLastKnownLocation.getAccuracy() + " meters");
            Log.d(TAG, "Passive - Time: " + ((System.currentTimeMillis() - passiveLastKnownLocation.getTime()) / 1000 / 60));
        }
    }

    private boolean getProviderStatus(String provider) {
        try {
            if (mLocationManager.isProviderEnabled(provider)) {
                return true;
            }
        } catch (Exception ex) {
            return false;  // Provider is null -  exceptions will be thrown if provider is not permitted.
        }
        return false;
    }

    public boolean isGpsProviderEnabled() {
        return gpsProviderEnabled;
    }

    public boolean isNetworkProviderEnabled() {
        return networkProviderEnabled;
    }

    public boolean isPassiveProviderEnabled() {
        return passiveProviderEnabled;
    }

    // Last known location
    public Location getLastKnownLocation(String provider) {
        if (getProviderStatus(provider)) {
            return mLocationManager.getLastKnownLocation(provider);
        } else {
            return null;
        }
    }

    public void getLocation(LocationResult result) {
        mLocationResult = result;

        for (String provider : providers) {
            if (getProviderStatus(provider)) {
                Location tempLastKnownlocation = getLastKnownLocation(provider);
                if (tempLastKnownlocation != null) {
                    long lastUpdateInMinutes = (System.currentTimeMillis() - tempLastKnownlocation.getTime()) / 1000 / 60;
                    Log.d(TAG, "lastUpdateInMinutes: " + lastUpdateInMinutes);
                    if (lastUpdateInMinutes < LAST_UPDATE_THRESHOLD && tempLastKnownlocation.getAccuracy() < ACCURACY_THRESHOLD) {
                        mLocationResult.gotLocation(tempLastKnownlocation);
                        return;
                    }
                }
            }
        }

        if (getProviderStatus(LocationManager.NETWORK_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
        }  if (getProviderStatus(LocationManager.GPS_PROVIDER)) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
        }

        mHandler = new Handler();
        mHandler.postDelayed(runnable, SEARCH_TIME_LIMIT);
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            mHandler.removeCallbacks(runnable);
            mLocationResult.gotLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            mHandler.removeCallbacks(runnable);
            mLocationResult.gotLocation(location);
            mLocationManager.removeUpdates(this);
            mLocationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mLocationManager.removeUpdates(locationListenerGps);
            mLocationManager.removeUpdates(locationListenerNetwork);

            Location gps_loc = getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location net_loc = getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gps_loc != null) {
                mLocationResult.gotLocation(gps_loc);
                return;
            }
            if (net_loc != null) {
                mLocationResult.gotLocation(net_loc);
                return;
            }
            mLocationResult.gotLocation(null);
        }
    };

    public static abstract class LocationResult {
        public abstract void gotLocation(Location location);
    }

    public void cancelTimer() {
        if (mHandler != null) mHandler.removeCallbacks(runnable);
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(locationListenerGps);
            mLocationManager.removeUpdates(locationListenerNetwork);
        }
    }
}