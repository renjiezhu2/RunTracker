package edu.dartmouth.cs.MyRuns;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class ManualInputActivity extends Activity {
    private final Calendar mDateAndTime = Calendar.getInstance();

    public static boolean isMetric = true;

    private ExerciseEntry exerciseEntry;
    private ExercisesDataSource datasource;
    private AddExerciseTask addExerciseTask;
    private ListView mManualInputList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_input);

        //get writable database
        datasource = new ExercisesDataSource(this);
        datasource.open();

        //use this exercise entry to store all the dialog data
        exerciseEntry = new ExerciseEntry(isMetric);

        //set the element of manual list
        mManualInputList = (ListView)findViewById(R.id.manual_input_list_view);
        ArrayAdapter<CharSequence> actAdapter = ArrayAdapter.createFromResource(this, R.array.manual_input_array, R.layout.manual_input_list);
        mManualInputList.setAdapter(actAdapter);

        mManualInputList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // selected item
                createDialog(position);
            }
        });
    }

    //close the activity if save button is clicked
    public void onManualSaveClick(View view) {
        SimpleDateFormat ft =
                new SimpleDateFormat ("kk:mm:ss EEE MMM dd yyyy");
        exerciseEntry.setmDateTime(ft.format(mDateAndTime.getTime()).toString());
        //exerciseEntry.setmInputType(0);
        addExerciseTask = new AddExerciseTask();
        addExerciseTask.execute(exerciseEntry);
        finish();
    }

    //close the activity if cancel button is clicked
    public void onManualCancelClick(View view) {
        Toast.makeText(this, R.string.manual_discard_entry, Toast.LENGTH_SHORT).show();
        finish();
    }

    //create data picker dialog
    private void onDateClicked(){
        DatePickerDialog.OnDateSetListener mDateListener = new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                mDateAndTime.set(Calendar.YEAR, year);
                mDateAndTime.set(Calendar.MONTH, monthOfYear);
                mDateAndTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        };

        new DatePickerDialog(ManualInputActivity.this, mDateListener,
                mDateAndTime.get(Calendar.YEAR),
                mDateAndTime.get(Calendar.MONTH),
                mDateAndTime.get(Calendar.DAY_OF_MONTH)).show();
    }

    //create time picker dialog
    public void onTimeClicked() {
        TimePickerDialog.OnTimeSetListener mTimeListener = new TimePickerDialog.OnTimeSetListener() {
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mDateAndTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mDateAndTime.set(Calendar.MINUTE, minute);
            }
        };

        new TimePickerDialog(ManualInputActivity.this, mTimeListener,
                mDateAndTime.get(Calendar.HOUR_OF_DAY),
                mDateAndTime.get(Calendar.MINUTE), true).show();

    }

    //alert message for edittext
    public void onEditTextClicked(String message, int start_id, final int item_id){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManualInputActivity.this);
        alertDialog.setMessage(message);
        final EditText input = new EditText(ManualInputActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        if (start_id == 0){
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
        }else{
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setHint(R.string.manual_comment_hint);
        }

        input.setLayoutParams(lp);
        alertDialog.setView(input);

        //set the value of corresponding field of exercise entry after clicking the ok button
        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (input.getText().toString().length() > 0){
                            switch (item_id){
                                case 0:
                                    exerciseEntry.setmDuration(Integer.parseInt(input.getText().toString()));
                                    break;
                                case 1:
                                    exerciseEntry.setmDistance(Double.parseDouble(input.getText().toString()));
                                    break;
                                case 2:
                                    exerciseEntry.setmCalorie(Integer.parseInt(input.getText().toString()));
                                    break;
                                case 3:
                                    exerciseEntry.setmHeartRate(Integer.parseInt(input.getText().toString()));
                                    break;
                                case 4:
                                    exerciseEntry.setmComment(input.getText().toString());
                                    break;
                                default:break;
                            }
                        }

                        return;
                    }
                });

        alertDialog.setNegativeButton("CANCEL",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                });
        alertDialog.show();
    }

    //Open corresponding dialog based on the clicked item id
    private void createDialog(int dialogId){
        switch (dialogId){
            case 0:
                onDateClicked();
                break;
            case 1:
                onTimeClicked();
                break;
            case 2:
                onEditTextClicked(getString(R.string.manual_duration_message), 0, 0);
                break;
            case 3:
                onEditTextClicked(getString(R.string.manual_distance_message), 0, 1);
                break;
            case 4:
                onEditTextClicked(getString(R.string.manual_calories_message), 0, 2);
                break;
            case 5:
                onEditTextClicked(getString(R.string.manual_heart_message), 0, 3);
                break;
            case 6:
                onEditTextClicked(getString(R.string.manual_comment_message), 1, 4);
                break;
            default:break;
        }
    }

    //Asynctask used to add entry
    private class AddExerciseTask extends AsyncTask<ExerciseEntry, ExerciseEntry, Void> {
        //save the data to the database
        @Override
        public Void doInBackground(ExerciseEntry... exerciseEntry) {
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
