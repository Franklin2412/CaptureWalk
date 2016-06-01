package com.ran.capturewalk.capturewalk;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.internal.view.SupportMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.Builder;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.events.CompletionEvent;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.wearable.WearableStatusCodes;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class WalkMapFragment extends Fragment implements OnMapReadyCallback, OnClickListener, LocationListener, ConnectionCallbacks, OnConnectionFailedListener {
    private static final long FASTEST_INTERVAL = 5000;
    private static final long INTERVAL = 10000;
    private static final String TAG = "MainActivity";
    private static double mPreviousLatitude;
    private static double mPreviousLongitude;
    private static double mStartLatitude;
    private static double mStartLongitude;
    private final int GET_LOCATION_PERMISSION;
    private AnyDBAdapter db;
    private GoogleMap googleMap;
    private Button mBtnStart;
    private MainActivity mContext;
    private Location mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private boolean mRequestingLocationUpdates;
    private boolean mStartButtonPressed;
    private long mStartTime;
    private long mStopTime;
    private float[] result;

    /* renamed from: com.ran.capturewalk.capturewalk.WalkMapFragment.1 */
    class C05931 implements OnClickListener {
        final /* synthetic */ Dialog val$dialog;
        final /* synthetic */ String val$timeTaken;

        C05931(String str, Dialog dialog) {
            this.val$timeTaken = str;
            this.val$dialog = dialog;
        }

        public void onClick(View v) {
            WalkMapFragment.this.db.open();
            WalkMapFragment.this.db.InsertEntry((double) WalkMapFragment.this.result[0], new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), this.val$timeTaken);
            WalkMapFragment.this.startActivity(new Intent(WalkMapFragment.this.mContext, AllDistancesTravelled.class));
            this.val$dialog.dismiss();
        }
    }

    /* renamed from: com.ran.capturewalk.capturewalk.WalkMapFragment.2 */
    class C05942 implements OnClickListener {
        final /* synthetic */ Dialog val$dialog;

        C05942(Dialog dialog) {
            this.val$dialog = dialog;
        }

        public void onClick(View v) {
            this.val$dialog.dismiss();
        }
    }

    public WalkMapFragment() {
        this.GET_LOCATION_PERMISSION = 1;
        this.mRequestingLocationUpdates = false;
        this.mStartButtonPressed = false;
        this.result = new float[1];
    }

    static {
        mStartLatitude = 0.0d;
        mStartLongitude = 0.0d;
        mPreviousLatitude = 0.0d;
        mPreviousLongitude = 0.0d;
    }

    protected void createLocationRequest() {
        this.mLocationRequest = new LocationRequest();
        this.mLocationRequest.setInterval(INTERVAL);
        this.mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        this.mLocationRequest.setPriority(100);
    }

    @Nullable
    @TargetApi(17)
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_main, container, false);
        if (isGooglePlayServicesAvailable()) {
            buildGoogleApiClient();
            createLocationRequest();
        } else {
            this.mContext.finish();
        }
        this.mBtnStart = (Button) view.findViewById(R.id.btn_start);
        this.mBtnStart.setOnClickListener(this);
        this.db = new AnyDBAdapter(this.mContext);
        ((MapFragment) getChildFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
        return view;
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = (MainActivity) activity;
    }

    protected synchronized void buildGoogleApiClient() {
        this.mGoogleApiClient = new Builder(this.mContext).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    public void onStart() {
        super.onStart();
        if (this.mGoogleApiClient != null) {
            Log.d(TAG, "onStart fired ..............");
            this.mGoogleApiClient.connect();
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mGoogleApiClient != null) {
            Log.d(TAG, "onStop fired ..............");
            this.mGoogleApiClient.disconnect();
            Log.d(TAG, "isConnected ...............: " + this.mGoogleApiClient.isConnected());
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this.mContext);
        if (status == 0) {
            return true;
        }
        GooglePlayServicesUtil.getErrorDialog(status, this.mContext, 0).show();
        return false;
    }

    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        this.googleMap.setMyLocationEnabled(true);
        this.googleMap.setBuildingsEnabled(true);
        if (this.mCurrentLocation != null) {
            LatLng latlng = new LatLng(this.mCurrentLocation.getLatitude(), this.mCurrentLocation.getLongitude());
            this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
            this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 11.0f), WearableStatusCodes.TARGET_NODE_NOT_CONNECTED, null);
        }
    }

    public void onClick(View v) {
        if (v.getId() == R.id.btn_start) {
            togglePeriodicLocationUpdates();
        }
    }

    public long getCurrentTimeinMilliseconds() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + this.mGoogleApiClient.isConnected());
        if (ContextCompat.checkSelfPermission(this.mContext, "android.permission.ACCESS_FINE_LOCATION") == 0) {
            updateCurrentLocation();
            if (this.mRequestingLocationUpdates) {
                startLocationUpdates();
                return;
            }
            return;
        }
        ActivityCompat.requestPermissions(this.mContext, new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 1);
    }

    public void updateCurrentLocation() {
        this.mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(this.mGoogleApiClient);
        LatLng latlng = new LatLng(this.mCurrentLocation.getLatitude(), this.mCurrentLocation.getLongitude());
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        this.googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15.0f), WearableStatusCodes.TARGET_NODE_NOT_CONNECTED, null);
    }

    public void togglePeriodicLocationUpdates() {
        if (this.mRequestingLocationUpdates) {
            Toast.makeText(this.mContext, "Done location capturing...", 0).show();
            this.mStopTime = getCurrentTimeinMilliseconds();
            this.mBtnStart.setText("Start");
            this.mStartButtonPressed = false;
            this.mRequestingLocationUpdates = false;
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(mPreviousLatitude, mPreviousLongitude));
            markerOptions.title("Destination").draggable(false);
            this.googleMap.addMarker(markerOptions);
            showAlertDialog();
            stopLocationUpdates();
            return;
        }
        Toast.makeText(this.mContext, "Started Capturing...", 0).show();
        this.mStartTime = getCurrentTimeinMilliseconds();
        this.mBtnStart.setText("Stop");
        this.mStartButtonPressed = true;
        this.mRequestingLocationUpdates = true;
        startLocationUpdates();
    }

    public void resetCapture() {
        this.googleMap.clear();
        mStartLatitude = 0.0d;
        mStartLongitude = 0.0d;
        mPreviousLatitude = 0.0d;
        mPreviousLongitude = 0.0d;
        this.result = new float[1];
    }

    public void showAlertDialog() {
        Dialog dialog = new Dialog(this.mContext);
        View view = this.mContext.getLayoutInflater().inflate(R.layout.layout_alert_dialog, null);
        TextView distanceTextview = (TextView) view.findViewById(R.id.alert_textview_distace);
        TextView timeTextview = (TextView) view.findViewById(R.id.alert_textview_time);
        Button viewHistoryButton = (Button) view.findViewById(R.id.alert_btn_view_history);
        Button startAgainButton = (Button) view.findViewById(R.id.alert_btn_start_again);
        String distanceString = reduceToTwoDecimalPlaces(this.result[0]) + " m";
        if (this.result[0] > 1000.0f) {
            distanceString = reduceToTwoDecimalPlaces(this.result[0] / 1000.0f) + " km";
        }
        String timeTaken = getTimeDifference(this.mStartTime, this.mStopTime);
        timeTextview.setText(timeTaken);
        distanceTextview.setText(distanceString);
        viewHistoryButton.setOnClickListener(new C05931(timeTaken, dialog));
        startAgainButton.setOnClickListener(new C05942(dialog));
        dialog.requestWindowFeature(1);
        dialog.setContentView(view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public String getTimeDifference(long starttime, long stoptime) {
        return getDurationBreakdown(stoptime - starttime);
    }

    public static String getDurationBreakdown(long milliseconds) {
        if (milliseconds < 0) {
            throw new IllegalArgumentException("Duration must be greater than zero!");
        }
        return String.format("%02d:%02d:%02d", new Object[]{Long.valueOf(TimeUnit.MILLISECONDS.toHours(milliseconds)), Long.valueOf(TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliseconds))), Long.valueOf(TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))});
    }

    public String reduceToTwoDecimalPlaces(float value) {
        NumberFormat formatter = NumberFormat.getNumberInstance();
        formatter.setMinimumFractionDigits(2);
        formatter.setMaximumFractionDigits(2);
        return formatter.format((double) value);
    }

    protected void startLocationUpdates() {
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(this.mGoogleApiClient, this.mLocationRequest, (LocationListener) this);
        Log.d(TAG, "Location update started ..............: ");
    }

    public void onConnectionSuspended(int i) {
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    public void onLocationChanged(Location location) {
        if (this.mStartButtonPressed) {
            mStartLatitude = location.getLatitude();
            mStartLongitude = location.getLongitude();
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(location.getLatitude(), location.getLongitude()));
            markerOptions.title("Source").draggable(true);
            this.googleMap.addMarker(markerOptions);
            this.mStartButtonPressed = false;
            mPreviousLatitude = location.getLatitude();
            mPreviousLongitude = location.getLongitude();
            return;
        }
        this.googleMap.addPolyline(new PolylineOptions().color(SupportMenu.CATEGORY_MASK).add(new LatLng(mPreviousLatitude, mPreviousLongitude), new LatLng(location.getLatitude(), location.getLongitude())));
        System.out.println("new PolylineOptions().add(new LatLng(" + mPreviousLatitude + "," + mPreviousLongitude + ",new LatLng(" + location.getLatitude() + "," + location.getLongitude());
        Toast.makeText(this.mContext, "Location has been updated.New Loaction is " + Double.toString(location.getLatitude()) + " and " + Double.toString(location.getLongitude()), 0).show();
        Location.distanceBetween(mStartLatitude, mStartLongitude, location.getLatitude(), location.getLongitude(), this.result);
        mPreviousLatitude = location.getLatitude();
        mPreviousLongitude = location.getLongitude();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(this.mGoogleApiClient, (LocationListener) this);
        Log.d(TAG, "Location update stopped .......................");
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case CompletionEvent.STATUS_FAILURE /*1*/:
                if (grantResults.length > 0 && grantResults[0] == 0) {
                    updateCurrentLocation();
                    if (this.mRequestingLocationUpdates) {
                        startLocationUpdates();
                    }
                }
            default:
        }
    }
}
