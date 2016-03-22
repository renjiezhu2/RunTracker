package edu.dartmouth.cs.MyRuns;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class HistoryFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<ExerciseEntry>>{
    public static ListView mEntryList;
    private static List<ExerciseEntry> exerciseList = new ArrayList<ExerciseEntry>();
    public static ExerciseEntryArrayAdapter actAdapter;
    public static boolean isMetric = true;
    public static LoaderManager loaderManager;

    private HistoryChangedReceiver mHistoryChangedReceiver;
    private IntentFilter mReceiverIntent;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.history_fragment, container, false);
        mEntryList = (ListView)view.findViewById(R.id.entryListView);
        actAdapter = new ExerciseEntryArrayAdapter(getActivity(), exerciseList, false);
        //initiate the AsyncTaskLoader
        loaderManager = getLoaderManager();
        loaderManager.initLoader(1, null, this).forceLoad();


        //register receiver
        mHistoryChangedReceiver = new HistoryChangedReceiver();
        mReceiverIntent = new IntentFilter("Adaptor");
        this.getActivity().registerReceiver(mHistoryChangedReceiver, mReceiverIntent);

        //when item is clicked, enter entry activity screen
        mEntryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                ExerciseEntry currentEntry = exerciseList.get(position);

                //Check the input type of this entry
                switch(currentEntry.getmInputType()) {
                    //If the input type is Manual Entry
                    case(0):
                        //open entry activity, and pass the selected ExerciseEntry to it
                        Intent intent = new Intent(getActivity(), EntryActivity.class);

                        intent.putExtra(getString(R.string.position_key), position);
                        intent.putExtra(getString(R.string.entry_id_key), currentEntry.getId());
                        intent.putExtra(getString(R.string.input_type_key), currentEntry.getmInputTypeString());
                        intent.putExtra(getString(R.string.activity_type_key), currentEntry.getmActivityString());
                        intent.putExtra(getString(R.string.date_and_time_key), currentEntry.getmDateTime());
                        intent.putExtra(getString(R.string.duration_key), currentEntry.getDurationString());
                        intent.putExtra(getString(R.string.distance_key), currentEntry.getDistanceString());
                        intent.putExtra(getString(R.string.calories_key), currentEntry.getCaloriesString());
                        intent.putExtra(getString(R.string.heart_rate_key), currentEntry.getHeartRateString());
                        startActivity(intent);

                        break;
                    //if the input type is GPS / Automatic
                    case(1):case(2):
                        Intent intentGPS=new Intent(getActivity(),MapDisplayActivity.class);

                        intentGPS.putExtra(getString(R.string.position_key), position);
                        intentGPS.putExtra(getString(R.string.entry_id_key), currentEntry.getId());
                        intentGPS.putExtra(getString(R.string.input_type_key), currentEntry.getmInputTypeString());
                        intentGPS.putExtra(getString(R.string.activity_type_key), currentEntry.getmActivityType());
                        intentGPS.putExtra(getString(R.string.date_and_time_key), currentEntry.getmDateTime());
                        intentGPS.putExtra(getString(R.string.duration_key), currentEntry.getDurationString());
                        intentGPS.putExtra(getString(R.string.distance_key), currentEntry.getDistanceString());
                        intentGPS.putExtra(getString(R.string.calories_key), currentEntry.getCaloriesString());
                        intentGPS.putExtra(getString(R.string.heart_rate_key), currentEntry.getHeartRateString());
                        intentGPS.putExtra(getString(R.string.from), "HistoryFragment");
                        startActivity(intentGPS);

                        break;
                    default:
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public Loader<List<ExerciseEntry>> onCreateLoader(int id, Bundle args) {
        return new ExerciseLoader(getActivity());
    }

    @Override
    //Load the exercise data into adapter
    public void onLoadFinished(Loader<List<ExerciseEntry>> loader, List<ExerciseEntry> exerciseList) {
        this.exerciseList = exerciseList;
        //initiate adapter for history frag
        actAdapter = new ExerciseEntryArrayAdapter(getActivity(), exerciseList, false);
        //set up the adapter
        mEntryList.setAdapter(actAdapter);

        //check the unit preference
        if (isMetric){
            actAdapter.setMetric();
        }else{
            actAdapter.setImperial();
        }
    }

    @Override
    public void onLoaderReset(Loader<List<ExerciseEntry>> loader) {
        actAdapter.setExercises(new ArrayList<ExerciseEntry>() {
        });
    }

    //update the track and text views
    public class HistoryChangedReceiver extends BroadcastReceiver
    {
        public HistoryChangedReceiver() {}
        public void onReceive(Context context, Intent intent){
            long id = intent.getLongExtra("id", 0);
            actAdapter.removeById(id);
        }
    }

    @Override
    public void onDestroy(){
        this.getActivity().unregisterReceiver(mHistoryChangedReceiver);
        super.onDestroy();
    }
}
