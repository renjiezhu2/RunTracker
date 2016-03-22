package edu.dartmouth.cs.MyRuns;

import android.content.Context;

import java.util.List;

/**
 * Created by LeoZhu on 1/30/16.
 */
public class ExerciseEntryArrayAdapter extends TwoLineArrayAdapter<ExerciseEntry> {
    private List<ExerciseEntry> exercises;
    private boolean isMetric;

    public ExerciseEntryArrayAdapter(Context context, List<ExerciseEntry> exercises, boolean isMetric) {
        super(context, exercises);
        this.exercises = exercises;
        this.isMetric = isMetric;
    }

    //write to first line
    @Override
    public String lineOneText(ExerciseEntry e) {
        return e.toString1();
    }

    //write to second line
    @Override
    public String lineTwoText(ExerciseEntry e) {
        return e.toString2(this.isMetric);
    }

    //add new ExerciseEntry to the adaptor
    @Override
    public void add(ExerciseEntry e){
        this.exercises.add(e);
    }

    //remove an ExerciseEntry in the adaptor by index
    public void remove(int index){
        this.exercises.remove(index);
    }

    //remove an ExerciseEntry in the adaptor by id
    public void removeById(long id){
        if (this.exercises != null) {
            int length = exercises.size();
            for (int i = 0; i < length; i++){
                if (this.exercises.get(i).getId() == id){
                    this.exercises.remove(i);
                    this.notifyDataSetChanged();
                    return;
                }
            }

        }
    }

    //return the size of adapter
    public int size(){
        return this.exercises.size();
    }

    //set all elements in the adapter to user metric unit
    public void setMetric(){
        for (ExerciseEntry exerciseEntry:this.exercises){
            exerciseEntry.setisMetric(true);
        }
    }

    //set all elements in the adapter to user imperial unit
    public void setImperial(){
        for (ExerciseEntry exerciseEntry:this.exercises){
            exerciseEntry.setisMetric(false);
        }
    }

    public void setExercises(List<ExerciseEntry> data) {
        exercises.addAll(data);
        notifyDataSetChanged();
    }
}