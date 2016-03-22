package edu.dartmouth.cs.MyRuns;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;


public class ExerciseEntry {
    private Long id;
    private final static String[] input_array = {"Manual Entry", "GPS" ,"Automatic"};
    private final static String[] activity_array = {"Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
    "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other"};

    private int mInputType;        // Manual, GPS or automatic
    private int mActivityType;     // Running, cycling etc.
    private String mDateTime;    // When does this entry happen
    private int mDuration;         // Exercise duration in seconds
    private Double mDistance;      // Distance traveled. Either in meters or feet.
    private Double mAvgPace;       // Average pace
    private Double mAvgSpeed;      // Average speed
    private Integer mCalorie;          // Calories burnt
    private Double mClimb;         // Climb. Either in meters or feet.
    private Double mCurSpeed;       // Current speed
    private Integer mHeartRate;        // Heart rate
    private String mComment;       // Comments
    private boolean isMetric;      //Unit, metric or imperial
    private ArrayList<LatLng> mLocationList ;// location information
    private final static double DISTANCE_UNIT = 1.609344;

    public ExerciseEntry(boolean isMetric){
        this.mInputType = 0;
        this.mActivityType = 0;
        this.mDateTime = "";
        this.mDuration = 0;
        this.mDistance = 0.0;
        this.mAvgPace = 0.0;
        this.mAvgSpeed = 0.0;
        this.mCalorie = 0;
        this.mClimb = 0.0;
        this.mCurSpeed = 0.0;
        this.mHeartRate = 0;
        this.mComment = "";
        this.isMetric = isMetric;
        this.mLocationList=new ArrayList<LatLng>();
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getmInputType(){
        return this.mInputType;
    }

    public void setmInputType(int inputType){
        this.mInputType = inputType;
    }

    public int getmActivityType(){
        return this.mActivityType;
    }

    public void setmActivityType(int activityType){
        this.mActivityType = activityType;
    }

    public String getmDateTime(){
        return this.mDateTime;
    }

    public void setmDateTime(String dateTime){
        this.mDateTime = dateTime;
    }

    public int getmDuration(){
        return this.mDuration;
    }

    public void setmDuration(int duration){
        this.mDuration = duration;
    }

    public Double getmDistance(){
        return this.mDistance;
    }

    public void setmDistance(double distance){
        this.mDistance = distance;
    }

    public Double getmAvgPace(){
        return this.mAvgPace;
    }

    public void setmAvgPace(double avgPace){
        this.mAvgPace = avgPace;
    }

    public Double getmAvgSpeed(){
        return this.mAvgSpeed;
    }

    public void setmAvgSpeed(double avgSpeed){
        this.mAvgSpeed = avgSpeed;
    }

    public Integer getmCalorie(){
        return this.mCalorie;
    }

    public void setmCalorie(int calorie){
        this.mCalorie = calorie;
    }

    public Double getmClimb(){
        return this.mClimb;
    }

    public void setmClimb(double climb){
        this.mClimb = climb;
    }

    public Double getmCurSpeed(){
        return this.mCurSpeed;
    }

    public void setmCurSpeed(double curSpeed){
        this.mCurSpeed = curSpeed;
    }

    public Integer getmHeartRate(){
        return this.mHeartRate;
    }

    public void setmHeartRate(int heartRate){
        this.mHeartRate = heartRate;
    }

    public String getmComment() {
        return mComment;
    }

    public void setmComment(String comment) {
        this.mComment = comment;
    }

    public void setisMetric(boolean isMetric){
        if (this.isMetric != isMetric){
            if (this.isMetric){
                this.mDistance = metricToImperial(this.mDistance);
            }else{
                this.mDistance = imperialToMetric(this.mDistance);
            }
        }
        this.isMetric = isMetric;
    }

    public boolean getisMetric(){
        return this.isMetric;
    }

    public ArrayList<LatLng> getLocationList() {
        return mLocationList;
    }

    public void setLocationList(ArrayList<LatLng> mLocationList) {
        this.mLocationList = mLocationList;
    }

    public String getDurationString(){
        StringBuffer sb = new StringBuffer();
        if (this.mInputType == 0){
            sb.append(this.mDuration);
            if (this.mDuration != 0){
                sb.append(" mins 0 secs");
            }else{
                sb.append(" secs");
            }
        }else{
            int seconds = this.mDuration % 60;
            sb.append(mDuration / 60);
            sb.append(" mins ");
            sb.append(seconds);
            sb.append(" secs");
        }
        return sb.toString();
    }

    public String getDistanceString(){
        NumberFormat formatter = new DecimalFormat("#0.00");
        StringBuffer sb = new StringBuffer();
        if (this.isMetric){
            sb.append(formatter.format(this.mDistance));
            sb.append(" Kilometers");
        }else{
            sb.append(formatter.format(this.mDistance));
            sb.append(" Miles");
        }
        return sb.toString();
    }

    public String getCaloriesString(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.mCalorie);
        sb.append(" cals");
        return sb.toString();
    }

    public String getHeartRateString(){
        StringBuffer sb = new StringBuffer();
        sb.append(this.mHeartRate);
        sb.append(" bpm");
        return sb.toString();
    }

    // line 1 in ArrayAdapter in the ListView
    public String toString1() {
        StringBuffer sb = new StringBuffer();
        sb.append(input_array[this.mInputType] + ": ");
        sb.append(activity_array[this.mActivityType] + ", ");
        sb.append(this.mDateTime);
        return sb.toString();
    }

    // line 2 in ArrayAdapter in the ListView
    public String toString2(boolean isMetric) {
        StringBuffer sb = new StringBuffer();
        sb.append(getDistanceString() + ", ");
        sb.append(getDurationString());
        return sb.toString();
    }

    //get the input type given id
    public String getmInputTypeString(){
        return input_array[this.mInputType];
    }

    //get the name of current activity given id
    public String getmActivityString(){
        return activity_array[this.mActivityType];
    }

    //transfer metric to imperial
    private double metricToImperial(double metric){
        return(metric / DISTANCE_UNIT);
    }

    //transfer imperial to metric
    private double imperialToMetric(double imperial){
        return imperial * DISTANCE_UNIT;
    }
    //Convert the Arraylist of location into byte array
    public byte[] getLocationByteArray() {
        int[] intArray = new int[mLocationList.size() * 2];

        for (int i = 0; i < mLocationList.size(); i++) {
            intArray[i * 2] = (int) (mLocationList.get(i).latitude * 1E6);
            intArray[(i * 2) + 1] = (int) (mLocationList.get(i).longitude * 1E6);
        }

        ByteBuffer byteBuffer = ByteBuffer.allocate(intArray.length
                * Integer.SIZE);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(intArray);

        return byteBuffer.array();
    }

    //insert the location information into the locationlist
    public void insertLocation(Location location){
        this.mLocationList.add(new LatLng(location.getLatitude(), location.getLongitude()));
    }

    //Convert Byte array to locationlist:
    public void setLocationListFromByteArray(byte[] bytePointArray) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytePointArray);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();

        int[] intArray = new int[bytePointArray.length / Integer.SIZE];
        intBuffer.get(intArray);

        int locationNum = intArray.length / 2;

        for (int i = 0; i < locationNum; i++) {
            LatLng latLng = new LatLng((double) intArray[i * 2] / 1E6F,
                    (double) intArray[i * 2 + 1] / 1E6F);
            this.mLocationList.add(latLng);
        }
    }
}
