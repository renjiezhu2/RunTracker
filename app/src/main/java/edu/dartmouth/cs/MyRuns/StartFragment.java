package edu.dartmouth.cs.MyRuns;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import edu.dartmouth.cs.MyRuns.Server.ServerUtilities;

/**
 * Created by LeoZhu on 1/15/16.
 */
public class StartFragment extends Fragment {
    static final String[] INPUT = new String[] { "Manual Entry",
            "GPS", "Automatic"};
    private static final String SERVER_ADD = "https://level-harbor-123220.appspot.com";
    private Spinner minputTypeList;
    private Spinner mactivityTypeList;
    private Button mStartButton;
    private Button mSyncButton;
    private JSONArray resultSet;
    private HashMap<String, String> map;

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.start_fragment, container, false);
        minputTypeList = (Spinner)rootView.findViewById(R.id.input_spinner);
        // Define a new adapter
        ArrayAdapter<CharSequence> inputAdapter = ArrayAdapter.createFromResource(rootView.getContext(), R.array.input_array, R.layout.activity_list);
        // Specify the layout to use when the list of choices appears
        inputAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Assign the adapter to Spinner
        minputTypeList.setAdapter(inputAdapter);

        
        mactivityTypeList = (Spinner)rootView.findViewById(R.id.activity_spinner);
        final ArrayAdapter<CharSequence> actAdapter = ArrayAdapter.createFromResource(rootView.getContext(), R.array.activity_array, R.layout.activity_list);
        actAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mactivityTypeList.setAdapter(actAdapter);


        mStartButton = (Button) rootView.findViewById(R.id.start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i;
                if (minputTypeList.getSelectedItem().toString().equals(INPUT[0])) {
                    i = new Intent(getActivity(), ManualInputActivity.class);
                } else {
                    i = new Intent(getActivity(), MapDisplayActivity.class);
                    //i.putExtra(getString(R.string.map_activity_type), mactivityTypeList.getSelectedItem().toString());
                    i.putExtra(getString(R.string.activity_type_key), mactivityTypeList.getSelectedItemPosition());
                    i.putExtra(getString(R.string.input_type_key), minputTypeList.getSelectedItemPosition());
                    i.putExtra(getString(R.string.from), "StartFragment");
                }
                startActivity(i);
            }
        });

        mSyncButton = (Button) rootView.findViewById(R.id.sync_button);
        mSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QueryDatabase queryTask = new QueryDatabase();
                queryTask.execute();
            }
        });
        return rootView;
    }
    class QueryDatabase extends AsyncTask<Void,Void, String> {

       // private static final String SENDER_ID = "123252165301";

        @Override
        protected String doInBackground(Void... params) {

            dbHelper = new MySQLiteHelper(getActivity());
            database = dbHelper.getWritableDatabase();

            Cursor cursor = database.query(MySQLiteHelper.TABLE_EXCERCISE, null, null, null, null, null, null);
            resultSet = new JSONArray();


            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {

                int totalColumn = cursor.getColumnCount();
                JSONObject rowObject = new JSONObject();

                for (int i = 0; i < totalColumn; i++) {
                    if (cursor.getColumnName(i) != null) {
                        try {
                            if (cursor.getString(i) != null) {
                                Log.d("TAG_NAME", cursor.getString(i));
                                rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                            } else {
                                rowObject.put(cursor.getColumnName(i), "");
                            }
                        } catch (Exception e) {
                            Log.d("TAG_NAME", e.getMessage());
                        }
                    }
                }
                resultSet.put(rowObject);
                cursor.moveToNext();
            }
            cursor.close();

            //put JsonArray into a map
            map = new HashMap<>();
            map.put("Key", resultSet.toString());

            // Upload the history of all entries using upload().
            String uploadState="";
            try {
                ServerUtilities.post(SERVER_ADD + "/add.do", map);
            } catch (IOException e1) {
                uploadState = "Sync failed: " + e1.getCause();
                Log.e("TAG", "data posting error " + e1);
                return uploadState;
            }

            Log.d("TAG_NAME", resultSet.toString());
            return "";
        }

        @Override
        protected void onPostExecute(String errString) {
            String resultString;
            if(errString.equals("")) {
                resultString =  " entry uploaded.";
            } else {
                resultString = errString;
            }

            Toast.makeText(getActivity(), resultString,
                    Toast.LENGTH_SHORT).show();
        }
    }

}
