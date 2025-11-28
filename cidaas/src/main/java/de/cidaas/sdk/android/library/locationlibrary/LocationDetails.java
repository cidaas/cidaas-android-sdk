package de.cidaas.sdk.android.library.locationlibrary;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import de.cidaas.sdk.android.helper.logger.LogFile;
import timber.log.Timber;

import static android.content.Context.LOCATION_SERVICE;

import de.cidaas.sdk.android.library.common.Privacy;

public class LocationDetails implements LocationListener {

    private final Context mContext;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    public static LocationDetails shared;

    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    float bearing; // bearing

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10 * 1; // 10 seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public LocationDetails(Context context) {
        this.mContext = context;
        getLocation();
    }

    public LocationDetails(Context context, String string) {
        this.mContext = context;
        getLocation();
    }


    // Create Shared instances
    public static LocationDetails getShared(Context contextfromcidaas) {

        if (shared == null) {
            shared = new LocationDetails(contextfromcidaas);
        } else {
            new LocationDetails(contextfromcidaas, "String");
        }


        return shared;
    }


    public Location getLocation() {
        try {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                getLocationPermissions();
            } else {
                getLocationAfterPermission();
            }

        } catch (Exception e) {
            Timber.e(e.getMessage());
            e.printStackTrace();
        }

        return location;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLocationPermissions() {

	if (!Privacy.isLocationEnabled()) {
   		 return false;
	}

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            getLocationAfterPermission();
        } else {
            Timber.i("Location permission Denied");
            LogFile.getShared(mContext).addFailureLog("Location Permission Denied");
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocationAfterPermission() {
        try {

            locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;

                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            bearing = location.getBearing();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                bearing = location.getBearing();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
    }


    /**
     * Stop using GPS listener
     * Calling this function will stop using GPS in your app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(LocationDetails.this);
        }
    }

    /**
     * Function to get latitude
     */
    public String getLatitude() {

        String Lat = "";

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (getLocation() != null) {
                latitude = getLocation().getLatitude();
                Lat = "" + latitude;
            }
        } else {
            Timber.i("Location permission Denied");
            LogFile.getShared(mContext).addFailureLog("Location Permission Denied");
        }
        // return latitude
        return Lat;
    }

    /**
     * Function to get longitude
     */
    public String getLongitude() {
        String Long = "";
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (getLocation() != null) {
                longitude = getLocation().getLongitude();
                Long = "" + longitude;
            }
        } else {
            Timber.i("Location permission Denied");
            LogFile.getShared(mContext).addFailureLog("Location Permission Denied");
        }

        // return longitude
        return Long;
    }

    public float getBearing() {
        if (location != null) {
            bearing = location.getBearing();
        }
        return bearing;
    }

    /**
     * Function to check GPS/wifi enabled
     *
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     */
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


}
