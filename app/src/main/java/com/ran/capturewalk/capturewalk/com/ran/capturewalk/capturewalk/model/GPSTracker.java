package com.ran.capturewalk.capturewalk.com.ran.capturewalk.capturewalk.model;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.ran.capturewalk.capturewalk.onLocationCallbackListener;

public final class GPSTracker implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 60000;
    public boolean canGetLocation;
    public boolean isGPSEnabled;
    boolean isNetworkEnabled;
    double latitude;
    Location location;
    protected LocationManager locationManager;
    double longitude;
    private final Context mContext;
    private onLocationCallbackListener mOnLocationCallbackListener;

    /* renamed from: com.ran.capturewalk.capturewalk.com.ran.capturewalk.capturewalk.model.GPSTracker.1 */
    class C05951 implements OnClickListener {
        C05951() {
        }

        public void onClick(DialogInterface dialog, int which) {
            GPSTracker.this.mContext.startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
        }
    }

    /* renamed from: com.ran.capturewalk.capturewalk.com.ran.capturewalk.capturewalk.model.GPSTracker.2 */
    class C05962 implements OnClickListener {
        C05962() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
        }
    }

    public GPSTracker(Context context, onLocationCallbackListener mOnLocationCallbackListener) {
        this.isGPSEnabled = false;
        this.isNetworkEnabled = false;
        this.canGetLocation = false;
        this.mContext = context;
        this.mOnLocationCallbackListener = mOnLocationCallbackListener;
        getLocation();
    }

    public Location getLocation() {
        try {
            this.locationManager = (LocationManager) this.mContext.getSystemService("location");
            Log.v("locationManager", "=" + this.locationManager);
            this.isGPSEnabled = this.locationManager.isProviderEnabled("gps");
            Log.v("isGPSEnabled", "=" + this.isGPSEnabled);
            this.isNetworkEnabled = this.locationManager.isProviderEnabled("network");
            Log.v("isNetworkEnabled", "=" + this.isNetworkEnabled);
            if (this.isGPSEnabled || this.isNetworkEnabled) {
                if (this.isNetworkEnabled) {
                    this.canGetLocation = true;
                    if (ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_FINE_LOCATION") != 0 && ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_COARSE_LOCATION") != 0) {
                        return null;
                    }
                    this.locationManager.requestLocationUpdates("network", MIN_TIME_BW_UPDATES, 10.0f, this);
                    Log.d("Network", "Network");
                    if (this.locationManager != null) {
                        this.location = this.locationManager.getLastKnownLocation("network");
                        if (this.location != null) {
                            this.latitude = this.location.getLatitude();
                            this.longitude = this.location.getLongitude();
                        }
                    }
                }
                if (this.isGPSEnabled) {
                    this.canGetLocation = true;
                    if (this.location == null) {
                        this.locationManager.requestLocationUpdates("gps", MIN_TIME_BW_UPDATES, 10.0f, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (this.locationManager != null) {
                            this.location = this.locationManager.getLastKnownLocation("gps");
                            if (this.location != null) {
                                this.latitude = this.location.getLatitude();
                                this.longitude = this.location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this.location;
    }

    public void stopUsingGPS() {
        if (this.locationManager == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_FINE_LOCATION") == 0 || ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_COARSE_LOCATION") == 0) {
            this.locationManager.removeUpdates(this);
        }
    }

    public double getLatitude() {
        if (this.location != null) {
            this.latitude = this.location.getLatitude();
        }
        return this.latitude;
    }

    public double getLongitude() {
        if (this.location != null) {
            this.longitude = this.location.getLongitude();
        }
        return this.longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    public void showSettingsAlert() {
        Builder alertDialog = new Builder(this.mContext);
        alertDialog.setTitle("GPS settings");
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings", new C05951());
        alertDialog.setNegativeButton("Cancel", new C05962());
        alertDialog.show();
    }

    public void onLocationChanged(Location location) {
        this.mOnLocationCallbackListener.onLocationUpdateResponse(location);
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
