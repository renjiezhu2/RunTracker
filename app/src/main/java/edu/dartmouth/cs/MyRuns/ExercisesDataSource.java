package edu.dartmouth.cs.MyRuns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by LeoZhu on 1/29/16.
 */
public class ExercisesDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    //schema of the database
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_INPUT_TYPE,
            MySQLiteHelper.COLUMN_ACTIVITY_TYPE,
            MySQLiteHelper.COLUMN_DATE_TIME,
            MySQLiteHelper.COLUMN_DURATION,
            MySQLiteHelper.COLUMN_DISTANCE,
            MySQLiteHelper.COLUMN_AVG_PACE,
            MySQLiteHelper.COLUMN_AVG_SPEED,
            MySQLiteHelper.COLUMN_CALORIE,
            MySQLiteHelper.COLUMN_CLIMB,
            MySQLiteHelper.COLUMN_HEART_RATE,
            MySQLiteHelper.COLUMN_COMMENT,
            MySQLiteHelper.COLUMN_ISMETRIC,
            MySQLiteHelper.COLUMN_GPS_DATA};

    private static final String TAG = "DBDEMO";

    //set context
    public ExercisesDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    //open the connection to the database
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    //close the connection to the database
    public void close() {
        dbHelper.close();
    }

    //create an exercise entry in database given a exerciseentry object
    public ExerciseEntry createExercise(ExerciseEntry exercise) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_INPUT_TYPE, exercise.getmInputType());
        values.put(MySQLiteHelper.COLUMN_ACTIVITY_TYPE, exercise.getmActivityType());
        values.put(MySQLiteHelper.COLUMN_DATE_TIME, exercise.getmDateTime());
        values.put(MySQLiteHelper.COLUMN_DURATION, exercise.getmDuration());
        values.put(MySQLiteHelper.COLUMN_DISTANCE, exercise.getmDistance());
        values.put(MySQLiteHelper.COLUMN_AVG_PACE, exercise.getmAvgPace());
        values.put(MySQLiteHelper.COLUMN_AVG_SPEED, exercise.getmAvgSpeed());
        values.put(MySQLiteHelper.COLUMN_CALORIE, exercise.getmCalorie());
        values.put(MySQLiteHelper.COLUMN_CLIMB, exercise.getmClimb());
        values.put(MySQLiteHelper.COLUMN_HEART_RATE, exercise.getmHeartRate());
        values.put(MySQLiteHelper.COLUMN_COMMENT, exercise.getmComment());
        //values.put(MySQLiteHelper.COLUMN_GPS_DATA,getLocationByteArray(exercise.getLocationList()));
        //Convert ArrayList into byte array and store in the values
        byte[] byteLocations = exercise.getLocationByteArray();

        if(byteLocations.length>0)
            values.put(MySQLiteHelper.COLUMN_GPS_DATA,byteLocations);

        if (exercise.getisMetric()){
            values.put(MySQLiteHelper.COLUMN_ISMETRIC, 1);
        }else{
            values.put(MySQLiteHelper.COLUMN_ISMETRIC, 0);
        }

        long insertId = database.insert(MySQLiteHelper.TABLE_EXCERCISE, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXCERCISE,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        ExerciseEntry newExercise = cursorToExercise(cursor);

        // Log the exercise stored
        Log.d(TAG, "exercise = " + cursorToExercise(cursor).toString()
                + " insert ID = " + insertId);

        cursor.close();
        return newExercise;
    }


    //delete an exercise entry
    public void deleteExercise(long id){
        // long id = exercise.getId();
        database.delete(MySQLiteHelper.TABLE_EXCERCISE, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    //delete all exercises records in the database
    public void deleteAllExercises() {
        System.out.println("Exercises deleted all");
        Log.d(TAG, "delete all = ");
        database.delete(MySQLiteHelper.TABLE_EXCERCISE, null, null);
    }


    //get all exercises entry in the database
    public List<ExerciseEntry> getAllExercises() {
        List<ExerciseEntry> exercises = new ArrayList<ExerciseEntry>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXCERCISE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ExerciseEntry exercise = cursorToExercise(cursor);
            Log.d(TAG, "get exercise = " + cursorToExercise(cursor).toString());
            exercises.add(exercise);
            cursor.moveToNext();
        }
        // Make sure to close the cursor
        cursor.close();
        return exercises;
    }

    //transfer the cursor into exerciseEntry element
    private ExerciseEntry cursorToExercise(Cursor cursor) {
        int metric = cursor.getInt(12);
        boolean isMetric;
        if (metric == 1){
            isMetric = true;
        }else{
            isMetric = false;
        }

        ExerciseEntry exercise = new ExerciseEntry(isMetric);
        exercise.setId(cursor.getLong(0));
        exercise.setmInputType(cursor.getInt(1));
        exercise.setmActivityType(cursor.getInt(2));
        exercise.setmDateTime(cursor.getString(3));
        exercise.setmDuration(cursor.getInt(4));
        exercise.setmDistance(cursor.getDouble(5));
        exercise.setmAvgPace(cursor.getDouble(6));
        exercise.setmAvgSpeed(cursor.getDouble(7));
        exercise.setmCalorie(cursor.getInt(8));
        exercise.setmClimb(cursor.getDouble(9));
        exercise.setmHeartRate(cursor.getInt(10));
        exercise.setmComment(cursor.getString(11));
        if (cursor.getBlob(13) != null){
            exercise.setLocationListFromByteArray(cursor.getBlob(13));
        }
        return exercise;
    }

    public ExerciseEntry getEntryById(long id) {
        //Construct a new entry
        ExerciseEntry row = new ExerciseEntry(true);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_EXCERCISE, allColumns,
                MySQLiteHelper.COLUMN_ID + "=" + id, null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()) {
            row = cursorToExercise(cursor);
            cursor.moveToNext();
        }
        cursor.close();
        return row;
    }
}
