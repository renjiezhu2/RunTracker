package edu.dartmouth.cs.MyRuns;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class TrackingService extends Service implements LocationListener, SensorEventListener {
    private static final String LABEL_AVAILABLE = "label_available";
    private final String TAG = "TrackingService";
    private final Binder IBinder = new TrackingServiceBinder();
    private final Calendar mDateAndTime = Calendar.getInstance();
    private final static double DISTANCE_UNIT = 1.609344;


    private LocationManager mLocationManager;
    private String mProvider;
    private ExerciseEntry mEntry;
    private NotificationManager mNotificationManager;
    private int mDuration;
    private long mStartTime;
    private Location mLastLocation;
    private Location mCurLocation;
    private boolean mIsStarted;
    private boolean mIsMetric;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ArrayBlockingQueue<Double> mAccBuffer;
    private onSensorChangedTask mSensorTask;
    private List<Double> features;
    private Intent intent;
    private Context mContext;


    /*Notification Area*/
    public void setupNotification() {
        String title = "MyRuns";
        String notificationText = "Recording your path now";

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapDisplayActivity.class).addCategory(Intent.CATEGORY_LAUNCHER)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP).setAction(title), 0);
        Notification notification = new Notification.Builder(this)
                .setContentTitle(title)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(contentIntent).build();
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notification.flags = notification.flags| Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(0, notification);
    }

    /*Location Listener Area*/
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = mCurLocation;
        mCurLocation = location;
        updateWithNewLocation();
    }

    private void updateWithNewLocation() {
        if (mCurLocation != null) {
            //update the data fields
            mEntry.insertLocation(mCurLocation);
            if (mLastLocation != null){
                if (mIsMetric){
                    mEntry.setmDistance(mEntry.getmDistance() + Math.abs(mCurLocation.distanceTo(mLastLocation) / 1000.0));
                    mEntry.setmClimb(mEntry.getmClimb() + Math.abs(mCurLocation.getAltitude() - mLastLocation.getAltitude()) / 1000.0);
                }else{
                    mEntry.setmDistance(mEntry.getmDistance() + Math.abs(mCurLocation.distanceTo(mLastLocation) * DISTANCE_UNIT / 1000.0));
                    mEntry.setmClimb(mEntry.getmClimb() + Math.abs(mCurLocation.getAltitude() - mLastLocation.getAltitude()) * DISTANCE_UNIT / 1000.0);
                }
            }

            mEntry.setmCalorie(mEntry.getmCalorie());
            mEntry.setmCurSpeed(mCurLocation.getSpeed());
            mDuration = (int) (System.currentTimeMillis() - mStartTime) / 1000;
            mEntry.setmDuration(mDuration);
            mEntry.setmAvgSpeed(mEntry.getmDistance() / (mDuration / 360.0));

            if (mIsMetric){
                mEntry.setmCalorie((int) (mEntry.getmDistance() / 15.0));
            }else{
                mEntry.setmCalorie((int) (mEntry.getmDistance() * DISTANCE_UNIT / 15.0) );
            }

            //set broadcast
            Intent intent = new Intent("Tracking");
            sendBroadcast(intent);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            double m = Math.sqrt(event.values[0] * event.values[0]
                    + event.values[1] * event.values[1] + event.values[2]
                    * event.values[2]);

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.

            try {
                mAccBuffer.add(new Double(m));
            } catch (IllegalStateException e) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                ArrayBlockingQueue<Double> newBuf = new ArrayBlockingQueue<Double>(
                        mAccBuffer.size() * 2);

                mAccBuffer.drainTo(newBuf);
                mAccBuffer = newBuf;
                mAccBuffer.add(new Double(m));
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    //set up the tracking binder
    public class TrackingServiceBinder extends Binder {
        public ExerciseEntry getExerciseEntry(){
            return mEntry;
        }
        public TrackingService getService(){
            return TrackingService.this;
        }
    }

    /*Service Related Methods*/
    @Override
    public void onCreate(){
        mIsStarted = true;
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        mContext=this;
        mAccBuffer = new ArrayBlockingQueue<Double>(
                Globals.ACCELEROMETER_BUFFER_CAPACITY);
        features = new ArrayList<Double>();

        //set up criteria for location providers
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        mProvider = mLocationManager.getBestProvider(criteria, true);

        mStartTime = mDateAndTime.getTimeInMillis();
        mIsMetric = HistoryFragment.isMetric;

        //Get the sensor manager of the system
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Instantiate and start async sensing task
        mSensorTask = new onSensorChangedTask();
        mSensorTask.execute();
        super.onCreate();
    }

    public void onDestroy() {
        mNotificationManager.cancelAll();
//        mLocationManager.removeUpdates(this);
//        mEntry = null;
        mSensorTask.cancel(true);
        mIsStarted = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(getClass().getName(), "onBind()");
        if (!mIsStarted){
            mIsStarted = true;
            initExerciseEntry(intent);
        }
        return (IBinder);
    }

    //set up mEntry Values
    //request location updates
    //set notification service
    //set up sensor request
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLocationManager.requestLocationUpdates(mProvider, 1000, 1, this);

        initExerciseEntry(intent);
        setupNotification();
        //Get the accelerometer sensor from the sensor manager
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        //register listener to update the sensor data
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

        return START_NOT_STICKY;
    }

    //initialize the exercise entry
    private void initExerciseEntry(Intent intent){
        mEntry = new ExerciseEntry(HistoryFragment.isMetric);
        mEntry.setmActivityType(intent.getIntExtra(getString(R.string.activity_type_key), 0));
        mEntry.setmInputType(intent.getIntExtra(getString(R.string.input_type_key), 0));
        SimpleDateFormat ft = new SimpleDateFormat ("kk:mm:ss EEE MMM dd yyyy");
        mEntry.setmDateTime(ft.format(mDateAndTime.getTime()).toString());
    }

    private class onSensorChangedTask extends AsyncTask<Void,Void,Void>{
        @Override
        protected Void doInBackground(Void... params) {
            int blockSize = 0;
            FFT fft = new FFT(Globals.ACCELEROMETER_BLOCK_CAPACITY);
            double[] accBlock = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];
            //Define real part and imaginary part
            double[] re = accBlock;
            double[] im = new double[Globals.ACCELEROMETER_BLOCK_CAPACITY];

            double max = Double.MIN_VALUE;

            while (true) {
                try {
                    // need to check if the AsyncTask is cancelled or not in the while loop
                    if (isCancelled () == true)
                    {
                        return null;
                    }

                    // Dumping buffer
                    accBlock[blockSize++] = mAccBuffer.take().doubleValue();

                    //When the block size reaches the limit 64
                    if (blockSize == Globals.ACCELEROMETER_BLOCK_CAPACITY) {
                        blockSize = 0;


                        max = .0;
                        //Find the maximum value in accBlock
                        for (double val : accBlock) {
                            if (max < val) {
                                max = val;
                            }
                        }
                        //Fourier transform
                        fft.fft(re, im);

                        // add the magnitude of each feature into the features array
                        for (int i = 0; i < re.length; i++) {
                            double mag = Math.sqrt(re[i] * re[i] + im[i]
                                    * im[i]);

                            features.add(Double.valueOf(mag));
                            im[i] = .0; // Clear the field
                        }


                        //add the maximum value into feature vecture
                        features.add(Double.valueOf(max));

                        //Classify the feature vector using weka classifier
                        double type = WekaClassifier.classify(features.toArray());
                        Log.d(TAG, "doInBackground: sensor service type = "+ type);
                        features.clear();

                        //put the return label into the intent
                        Intent intent = new Intent();
                        intent.setAction(LABEL_AVAILABLE);
                        intent.putExtra("lable_result", type);

                        //Broadcast the intent to all intent receiver
                        mContext.sendBroadcast(intent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
