package edu.dartmouth.cs.MyRuns;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import edu.dartmouth.cs.MyRuns.R;

public class EntryActivity extends Activity {
    private EditText mInputType;
    private EditText mActivityType;
    private EditText mDateAndTime;
    private EditText mDuration;
    private EditText mDistance;
    private EditText mCalories;
    private EditText mHeartRate;
    private int position;
    private long id;
    private DeleteExerciseTask deleteExerciseTask;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        //get the reference to the edittext fields
        mInputType = (EditText)findViewById(R.id.input_type);
        mActivityType = (EditText)findViewById(R.id.activity_type);
        mDateAndTime = (EditText) findViewById(R.id.date_time);
        mDuration = (EditText) findViewById(R.id.duration);
        mDistance = (EditText) findViewById(R.id.distance);
        mCalories = (EditText) findViewById(R.id.calories);
        mHeartRate = (EditText) findViewById(R.id.heartRate);

        //set the text values of each edittext field
        Intent intent = getIntent();
        id = intent.getLongExtra(getString(R.string.entry_id_key), 0);
        position = intent.getIntExtra(getString(R.string.position_key), 0);
        mInputType.setText(intent.getStringExtra(getString(R.string.input_type_key)));
        mActivityType.setText(intent.getStringExtra(getString(R.string.activity_type_key)));
        mDateAndTime.setText(intent.getStringExtra(getString(R.string.date_and_time_key)));
        mDuration.setText(intent.getStringExtra(getString(R.string.duration_key)));
        mDistance.setText(intent.getStringExtra(getString(R.string.distance_key)));
        mCalories.setText(intent.getStringExtra(getString(R.string.calories_key)));
        mHeartRate.setText(intent.getStringExtra(getString(R.string.heart_rate_key)));
    }

    //create the delete button
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.items, menu);
        return true;
    }

    @Override
    //This method will be called when the action bar is clicked
    public boolean onOptionsItemSelected(MenuItem Item){
        //If delete item is clicked
        if(Item.getItemId()== R.id.delete) {
            deleteExerciseTask = new DeleteExerciseTask();
            deleteExerciseTask.execute();
            finish();
        }
        return true;
    }

    // unload the delete operation by asynctask
    class DeleteExerciseTask extends AsyncTask<Void, Void, Void> {
        //save the data to the database
        @Override
        protected Void doInBackground(Void... unused) {
            ExercisesDataSource dataSource = new ExercisesDataSource(getApplicationContext());
            dataSource.open();
            dataSource.deleteExercise(id);
            return null;
        }

        //toast the message notifying users that entry has been saved.
        @Override
        protected void onPostExecute(Void unused) {
            //delete entry from database according to the id
            HistoryFragment.actAdapter.remove(position);
            HistoryFragment.actAdapter.notifyDataSetChanged();
            Toast.makeText(getApplicationContext(), "Successful Delete!", Toast.LENGTH_SHORT).show();
        }
    }
}
