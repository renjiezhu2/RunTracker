package edu.dartmouth.cs.myruns.backend.data;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class ExerciseEntry {
    private final static String[] input_array = {"Manual Entry", "GPS" ,"Automatic"};
    private final static String[] activity_array = {"Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing",
            "Cross-Country Skiing", "Snowboarding", "Skating", "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other"};

    public static final String EXERCISE_PARENT_ENTITY_NAME = "ExerciseParent";
    public static final String EXERCISE_PARENT_KEY_NAME = "ExerciseParent";

    public static final String EXERCISE_ENTRY_ENTITY_NAME = "ExerciseEntry";
    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_INPUT_TYPE = "input_type";
    public static final String FIELD_NAME_ACTIVITY_TYPE = "activity_type";
    public static final String FIELD_NAME_DATE_AND_TIME = "date_and_time";
    public static final String FIELD_NAME_DURATION = "duration";
    public static final String FIELD_NAME_DISTANCE = "distance";
    public static final String FIELD_NAME_AVG_SPEED = "avg_speed";
    public static final String FIELD_NAME_CALORIE = "calorie";
    public static final String FIELD_NAME_CLIMB = "climb";
    public static final String FIELD_NAME_HEARTRATE = "heart_rate";
    public static final String FIELD_NAME_COMMENT = "comment";
    public static final String FIELD_NAME_IS_METRIC = "is_metric";


    private Long id;                // Unique identifier for exercise entry
    private int mInputType;        // Manual, GPS or automatic
    private int mActivityType;     // Running, cycling etc.
    private String mDateTime;    // When does this entry happen
    private int mDuration;         // Exercise duration in seconds
    private double mDistance;      // Distance traveled. Either in meters or feet.
    private double mAvgSpeed;      // Average speed
    private String mCalorie;          // Calories burnt
    private double mClimb;         // Climb. Either in meters or feet
    private String mHeartRate;        // Heart rate
    private String mComment;       // Comments
    private int mIsMetric;      // Unit: meters or feet

    public ExerciseEntry(Long id, int mInputType, int mActivityType, String mDateTime, int mDuration, double mDistance,
                         double mAvgSpeed, String mCalorie, double mClimb, String mHeartRate, String mComment, int mIsMetric){
        this.id = id;
        this.mInputType = mInputType;
        this.mActivityType = mActivityType;
        this.mDateTime = mDateTime;
        this.mDuration = mDuration;
        this.mDistance = mDistance;
        this.mAvgSpeed = mAvgSpeed;
        this.mCalorie = mCalorie;
        this.mClimb = mClimb;
        this.mHeartRate = mHeartRate;
        this.mComment = mComment;
        this.mIsMetric = mIsMetric;
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

    public double getmDistance(){
        return this.mDistance;
    }

    public void setmDistance(double distance){
        this.mDistance = distance;
    }

    public double getmAvgSpeed(){
        return this.mAvgSpeed;
    }

    public void setmAvgSpeed(double avgSpeed){
        this.mAvgSpeed = avgSpeed;
    }

    public String getmCalorie(){
        return this.mCalorie;
    }

    public void setmCalorie(String calorie){
        this.mCalorie = calorie;
    }

    public double getmClimb(){
        return this.mClimb;
    }

    public void setmClimb(double climb){
        this.mClimb = climb;
    }

    public String getmHeartRate(){
        return this.mHeartRate;
    }

    public void setmHeartRate(String heartRate){
        this.mHeartRate = heartRate;
    }

    public String getmComment() {
        return mComment;
    }

    public void setmComment(String comment) {
        this.mComment = comment;
    }

    public int getmIsMetric(){
        return this.mIsMetric;
    }

    public void setmIsMetric(int mIsMetric){
        this.mIsMetric = mIsMetric;
    }

    public String getmInputTypeString(){
        return input_array[this.mInputType];
    }

    public String getmActivityTypeString(){
        return activity_array[this.mActivityType];
    }

    public String getmDurationString(){
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

    public String getmDistanceString(){
        NumberFormat formatter = new DecimalFormat("#0.00");
        StringBuffer sb = new StringBuffer();
        if (this.mIsMetric == 1){
            sb.append(formatter.format(this.mDistance));
            sb.append(" Kilometers");
        }else{
            sb.append(formatter.format(this.mDistance));
            sb.append(" Miles");
        }
        return sb.toString();
    }

    public String getmClimbString(){
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(this.mClimb);
    }

    public String getmAvgSpeedString(){
        NumberFormat formatter = new DecimalFormat("#0.00");
        return formatter.format(this.mAvgSpeed);
    }
}
