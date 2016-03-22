package edu.dartmouth.cs.MyRuns;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class MapDisplayActivity extends FragmentActivity implements ServiceConnection {
    private final static String[] activity_array = {"Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
            "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other"};
    private final static String[] activity_auto_array = {"Standing", "Walking", "Running", "Other"};
    private final String TAG = "MAP";
    private final String LOCATIONLIST= "LocationList";


    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private TextView mType;
    private TextView mAvgSpeed;
    private TextView mCurSpeed;
    private TextView mClimb;
    private TextView mCalorie;
    private TextView mDistance;

    private int mActivityType;
    private int mInputType;
    private boolean mIsBind;

    private boolean mFromHistory = true;

    private ExerciseEntry mExerciseEntry;
    private ExercisesDataSource datasource;
    private AddExerciseEntryTask addExerciseTask;

    private Marker mStart;
    private Marker mEnd;
    private Polyline mTrace;

    private Intent mServiceIntent;
    private Service mTrackingService;
    private LocationChangedReceiver mLocationChangedReceiver;
    private IntentFilter mReceiverIntent;
    private ActivityChangedReceiver mActivityChangedReceiver;
    private IntentFilter mActivityReceiverIntent;
    private int position;
    private long id;
    private int[] activityCount = new int[4];
    private int maxCount = 0;
    private int maxActivity = 0;
    private boolean isAuto = false;
    private Bundle mBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        //get writable database
        datasource = new ExercisesDataSource(this);
        datasource.open();

        //get reference to textview objects
        mType = (TextView) findViewById(R.id.map_type);
        mAvgSpeed = (TextView) findViewById(R.id.map_avg_speed);
        mCalorie = (TextView) findViewById(R.id.map_calorie);
        mClimb = (TextView) findViewById(R.id.map_climb);
        mCurSpeed = (TextView) findViewById(R.id.map_cur_speed);
        mDistance = (TextView) findViewById(R.id.map_distance);

        //set up map
        setUpMapIfNeeded();

        Intent i = getIntent();
        // try to find where is the source comes from
        String fromClass = i.getStringExtra("from");
        position = i.getIntExtra(getString(R.string.position_key), 0);
        id=i.getLongExtra(getString(R.string.entry_id_key), 0);

        //if the source class is from startFragment
        if(fromClass.equals("StartFragment")) {
            mFromHistory = false;

            //get input type and activity type from start fragment
            mActivityType = i.getIntExtra(getString(R.string.activity_type_key), 0);
            mInputType = i.getIntExtra(getString(R.string.input_type_key), 0);

            if (mInputType == 2){
                isAuto = true;
            }

            //register receiver
            mLocationChangedReceiver = new LocationChangedReceiver();
            mReceiverIntent = new IntentFilter("Tracking");
            registerReceiver(mLocationChangedReceiver, mReceiverIntent);

            if (isAuto){
                mActivityChangedReceiver = new ActivityChangedReceiver();
                mActivityReceiverIntent = new IntentFilter("label_available");
                registerReceiver(mActivityChangedReceiver, mActivityReceiverIntent);
            }

            if (savedInstanceState != null){
                mBundle = savedInstanceState;
                if (isAuto){
                    activityCount = savedInstanceState.getIntArray("ActivityCount");
                    maxActivity = savedInstanceState.getInt("MaxActivity");
                    maxCount = savedInstanceState.getInt("MaxCount");
                }
            }

            startTrackingService();

            //update the textview fields
            updateTextView();
            drawTrack();
        }
        else if(fromClass.equals("HistoryFragment")){
            mFromHistory = true;
            //Get id of the exercise entry from intent
            // get the entry from the database according to row id.
            mExerciseEntry = datasource.getEntryById(id);

            //Make the save button and cancel button invisible
            Button save_button = (Button) findViewById(R.id.map_save_button);
            Button cancel_button= (Button) findViewById(R.id.map_cancel_button);

            //disable the buttons
            save_button.setVisibility(View.GONE);
            cancel_button.setVisibility(View.GONE);

            //draw the track
            drawTrack();
            updateTextView();
        }
    }

    //re-register the receiver
    @Override
    protected void onResume() {
//        setUpMapIfNeeded();
        if (!mFromHistory){
            registerReceiver(mLocationChangedReceiver, mReceiverIntent);
        }
        drawTrack();
        updateTextView();
        super.onResume();
    }

    //unregister the receiver
    @Override
    protected void onDestroy() {
        if (!mFromHistory){
            unregisterReceiver(mLocationChangedReceiver);
            if (mActivityChangedReceiver != null){
                unregisterReceiver(mActivityChangedReceiver);
            }
            stopTrackingService();
        }
        super.onDestroy();
    }


    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        //save the location list
        savedInstanceState.putParcelableArrayList(LOCATIONLIST, mExerciseEntry.getLocationList());
        savedInstanceState.putInt("Duration", mExerciseEntry.getmDuration());
        savedInstanceState.putString("mDateTime", mExerciseEntry.getmDateTime());
        savedInstanceState.putInt("Calorie", mExerciseEntry.getmCalorie());
        savedInstanceState.putDouble("Distance", mExerciseEntry.getmDistance());
        if (isAuto){
            savedInstanceState.putIntArray("ActivityCount", activityCount);
            savedInstanceState.putInt("MaxActivity", maxActivity);
            savedInstanceState.putInt("MaxCount", maxCount);
        }
    }

    /*<-- Map Location Section -->*/
    //set up value of texts
    private void updateTextView(){
        Intent i = getIntent();
        if (!isAuto){
            mType.setText("Type: " + activity_array[i.getIntExtra(getString(R.string.activity_type_key), 0)]);
        }

        if (mExerciseEntry == null){
            //default values for text views
            mCalorie.setText("Calorie: 0");
            if (HistoryFragment.isMetric){
                mAvgSpeed.setText("Avg speed: 0 km/h");
                mClimb.setText("Climb: 0 Kilometers");
                mCurSpeed.setText("Cur Speed: 0 km/h");
                mDistance.setText("Distance: 0 Kilometers");
            }else{
                mAvgSpeed.setText("Avg speed: 0 m/h");
                mClimb.setText("Climb: 0 Miles");
                mCurSpeed.setText("Cur Speed: 0 m/h");
                mDistance.setText("Distance: 0 Miles");
            }
        }else{
            mCalorie.setText("Calorie: " + mExerciseEntry.getmCalorie());
            NumberFormat formatter = new DecimalFormat("#0.00");
            if (HistoryFragment.isMetric){
                mAvgSpeed.setText("Avg speed: " + formatter.format(mExerciseEntry.getmAvgSpeed()) + " km/h");
                mClimb.setText("Climb: " + formatter.format(mExerciseEntry.getmClimb()) + " Kilometers");
                mCurSpeed.setText("Cur Speed: " + formatter.format(mExerciseEntry.getmCurSpeed()) + " km/h");
                mDistance.setText("Distance: " + formatter.format(mExerciseEntry.getmDistance()) + " Kilometers");
            }else{
                mAvgSpeed.setText("Avg speed: " + formatter.format(mExerciseEntry.getmAvgSpeed()) + " m/h");
                mClimb.setText("Climb: " + formatter.format(mExerciseEntry.getmClimb()) + " Miles");
                mCurSpeed.setText("Cur Speed: " + formatter.format(mExerciseEntry.getmCurSpeed()) + " m/h");
                mDistance.setText("Distance: " + formatter.format(mExerciseEntry.getmDistance())+ " Miles");
            }
        }
    }

    //setup the delete button
    public boolean onCreateOptionsMenu(Menu menu) {
        //only show when it is opened by history fragment
        if (mFromHistory){
            getMenuInflater().inflate(R.menu.deletemap, menu);
            return true;
        }
        return false;
    }
    public boolean onOptionsItemSelected(MenuItem Item){
        //If delete item is clicked
        if(Item.getItemId()== R.id.delete) {
            DeleteExerciseTask deleteExerciseTask = new DeleteExerciseTask();
            deleteExerciseTask.execute();
            finish();
        }
        return true;
    }

    //draw the  track based on the location arraylist
    private void drawTrack() {
        if ((mExerciseEntry == null) || (mExerciseEntry.getLocationList().size() == 0))
            return;

        ArrayList locaitonList = mExerciseEntry.getLocationList();
        LatLng startPt = (LatLng) locaitonList.get(0);
        LatLng endPt = (LatLng) locaitonList.get(locaitonList.size() - 1);

        //set the start marker
        if (mStart == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPt, 17));
            MarkerOptions markerOpt = new MarkerOptions();
            mStart = mMap.addMarker(markerOpt.position(startPt).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }

        //draw the ployline
        mTrace.setPoints(locaitonList);

        //set the end marker
        if (mEnd == null){
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(endPt, 17));
            MarkerOptions markerOpt = new MarkerOptions();
            mEnd = mMap.addMarker(markerOpt.position(endPt).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
        else {
            //change marker location if locaiton changes
            mEnd.setPosition(endPt);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
                // Configure the map display options
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mTrace = mMap.addPolyline(new PolylineOptions());
    }

    /*<-- EVENT HANDLERS -->*/
    //save location, activity information into database
    public void onMapSaveClick(View view){
        if (isAuto){
            //update activity type based on most frequent activity
            switch (maxActivity){
                case 0: mExerciseEntry.setmActivityType(2); break;
                case 1: mExerciseEntry.setmActivityType(1); break;
                case 2: mExerciseEntry.setmActivityType(0); break;
                case 3: mExerciseEntry.setmActivityType(activity_array.length - 1); break;
                default: mExerciseEntry.setmActivityType(0); break;
            }

        }
        addExerciseTask = new AddExerciseEntryTask();
        addExerciseTask.execute(mExerciseEntry);
        finish();
    }

    //exit map activity
    public void onMapCancelClick(View view){
        finish();
    }

    /*<-- Service Section -->*/
    //start tracking service
    private void startTrackingService(){
        mServiceIntent = new Intent(this, TrackingService.class);
        mServiceIntent.putExtra(getString(R.string.activity_type_key), mActivityType);
        mServiceIntent.putExtra(getString(R.string.input_type_key), mInputType);

        startService(mServiceIntent);
        bindService();
    }

    //bind the service
    private void bindService(){
        if (!mIsBind){
            bindService(new Intent(this, TrackingService.class), this, Context.BIND_AUTO_CREATE);
            mIsBind = true;
        }
    }

    //unbind the service
    private void unbindService() {
        if (mIsBind) {
            unbindService(this);
            mIsBind = false;
        }
    }


    //stop the tracking service
    private void stopTrackingService() {
        if (mTrackingService != null) {
            unbindService();
            stopService(mServiceIntent);
        }
    }

    //if service is connected, get the reference of the exercise entry class
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mExerciseEntry = ((TrackingService.TrackingServiceBinder)iBinder).getExerciseEntry();
        if (mBundle != null){
            ArrayList<LatLng> locationList = mBundle.getParcelableArrayList(LOCATIONLIST);
            if (locationList != null){
                mExerciseEntry.setLocationList(locationList);
            }
            mExerciseEntry.setmDuration(mBundle.getInt("Duration"));
            mExerciseEntry.setmDateTime(mBundle.getString("mDateTime"));
            mExerciseEntry.setmCalorie(mBundle.getInt("Calorie"));
            mExerciseEntry.setmDistance(mBundle.getDouble("Distance"));
        }
        mTrackingService = ((TrackingService.TrackingServiceBinder)iBinder).getService();
        updateTextView();
        drawTrack();
    }

    //set the mTrackingService to be null
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mTrackingService = null;
    }


    //update the track and text views
    public class LocationChangedReceiver extends BroadcastReceiver
    {
        public LocationChangedReceiver() {}
        public void onReceive(Context context, Intent intent){
            drawTrack();
            updateTextView();
        }
    }

    //update the track and text views
    public class ActivityChangedReceiver extends BroadcastReceiver
    {
        public ActivityChangedReceiver() {}
        public void onReceive(Context context, Intent intent){
            int activityType = (int)intent.getDoubleExtra("lable_result", 0.0);
            activityCount[activityType]++;
            if (activityCount[activityType] > maxCount){
                maxCount = activityCount[activityType];
                maxActivity = activityType;
            }
            mType.setText("Type: " + activity_auto_array[activityType]);
        }
    }

    //used to delete the record
    private class DeleteExerciseTask extends AsyncTask<Void,Void,Void>{

        @Override
        protected Void doInBackground(Void... unused) {
            ExercisesDataSource dataSource = new ExercisesDataSource(getApplicationContext());
            dataSource.open();
            dataSource.deleteExercise(id);
            return null;
        }
        @Override
        protected void onPostExecute(Void unused) {
            //delete entry from database according to the id
            HistoryFragment.actAdapter.remove(position);
            HistoryFragment.actAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Successful Delete!", Toast.LENGTH_SHORT).show();
        }
    }


    //Asynctask used to add entry
    private class AddExerciseEntryTask extends AsyncTask<ExerciseEntry, ExerciseEntry, Void> {
        //save the data to the database
        @Override
        public Void doInBackground(ExerciseEntry... exerciseEntry) {
            Log.d(TAG, "save, do in background");
            ExerciseEntry exercise = datasource.createExercise(exerciseEntry[0]);
            publishProgress(exercise);
            return null;
        }

        //update the adapter of the listview in the history tab
        @Override
        public void onProgressUpdate(ExerciseEntry... exercise) {
            HistoryFragment.actAdapter.add(exercise[0]);
            if (HistoryFragment.isMetric){
                HistoryFragment.actAdapter.setMetric();
            }else{
                HistoryFragment.actAdapter.setImperial();
            }
            HistoryFragment.actAdapter.notifyDataSetChanged();
        }

        //toast the message notifying users that entry has been saved.
        @Override
        public void onPostExecute(Void unused) {
            int count = HistoryFragment.actAdapter.size();
            Toast.makeText(getApplicationContext(), "Entry #" + count + " saved.", Toast.LENGTH_SHORT).show();
            addExerciseTask = null;
        }
    }
}
