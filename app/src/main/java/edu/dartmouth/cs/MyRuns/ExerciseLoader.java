package edu.dartmouth.cs.MyRuns;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * Created by makunkun on 2/1/16.
 */
//This class is the AsynctaskLoader.
public class ExerciseLoader extends AsyncTaskLoader<List<ExerciseEntry>> {

    private ExercisesDataSource datasource;

    //Constructor
    public  ExerciseLoader(Context context){
        super(context);
        datasource = new ExercisesDataSource(context);
        datasource.open();
    }
    @Override
    //Retrieve data from database
    public List<ExerciseEntry> loadInBackground() {
        return datasource.getAllExercises();
    }
}
