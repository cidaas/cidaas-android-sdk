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

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.os.CancellationSignal;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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

    private static final long CURRENT_LOCATION_TIMEOUT_SEC = 3;

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
            // Refresh on the same instance (do not create a throwaway listener).
            shared.getLocation();
        }

        return shared;
    }

    public Location getLocation() {
        try {
            if (!Privacy.isLocationEnabled()) {
                return null;
            }

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
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                    applyLastKnownIfPresent(LocationManager.NETWORK_PROVIDER);
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    if (location == null) {
                        applyLastKnownIfPresent(LocationManager.GPS_PROVIDER);
                    }
                }

                if (location == null) {
                    applyLastKnownIfPresent(LocationManager.PASSIVE_PROVIDER);
                }

                // First tap often has no lastKnown yet — fetch a fresh fix.
                // Callback uses a background executor so this is safe on the UI thread.
                if (location == null) {
                    resolveCurrentLocationIfNeeded();
                }
            }
        } catch (Exception e) {

        }
    }

    @SuppressLint("MissingPermission")
    private void applyLastKnownIfPresent(String provider) {
        if (locationManager == null) {
            return;
        }
        Location lastKnown = locationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
            applyLocation(lastKnown);
        }
    }

    private void applyLocation(Location newLocation) {
        if (newLocation == null) {
            return;
        }
        location = newLocation;
        latitude = newLocation.getLatitude();
        longitude = newLocation.getLongitude();
        bearing = newLocation.getBearing();
    }

    /**
     * Blocks briefly for a current fix. Uses a background executor for the callback
     * so it does not deadlock when invoked from the main thread.
     */
    @SuppressLint("MissingPermission")
    private void resolveCurrentLocationIfNeeded() {
        if (location != null || locationManager == null || !canGetLocation) {
            return;
        }

        // Network is usually faster for the first fix; fall back to GPS.
        if (isNetworkEnabled) {
            awaitCurrentLocation(LocationManager.NETWORK_PROVIDER);
        }
        if (location == null && isGPSEnabled) {
            awaitCurrentLocation(LocationManager.GPS_PROVIDER);
        }
    }

    @SuppressLint("MissingPermission")
    private void awaitCurrentLocation(String provider) {
        final CountDownLatch latch = new CountDownLatch(1);
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CancellationSignal cancellationSignal = new CancellationSignal();

        try {
            LocationManagerCompat.getCurrentLocation(
                    locationManager,
                    provider,
                    cancellationSignal,
                    executor,
                    loc -> {
                        if (loc != null) {
                            applyLocation(loc);
                        }
                        latch.countDown();
                    });

            if (!latch.await(CURRENT_LOCATION_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                cancellationSignal.cancel();
            }
        } catch (Exception e) {
            Timber.e(e.getMessage());
        } finally {
            executor.shutdownNow();
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
        if (!Privacy.isLocationEnabled()) {
            return "";
        }

        String Lat = "";

        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location current = getLocation();
            if (current != null) {
                latitude = current.getLatitude();
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
        if (!Privacy.isLocationEnabled()) {
            return "";
        }

        String Long = "";
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(mContext,
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Location current = getLocation();
            if (current != null) {
                longitude = current.getLongitude();
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
        if (!Privacy.isLocationEnabled()) {
            return 0f;
        }
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
        if (!Privacy.isLocationEnabled()) {
            return false;
        }
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
        applyLocation(location);
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
