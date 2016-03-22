package edu.dartmouth.cs.MyRuns;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.dartmouth.cs.MyRuns.view.SlidingTabLayout;
import edu.dartmouth.cs.myruns.backend.registration.Registration;

public class MainActivity extends Activity {
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewPager;
    private ArrayList<Fragment> fragments;
    private ActionTabsViewPagerAdapter myViewPageAdapter;

    //server address
//    public static String SERVER_ADDR = "http://10.31.186.205:8080";


    /** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

        // Define SlidingTabLayout (shown at top)
        // and ViewPager (shown at bottom) in the layout.
        // Get their instances.
        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tab);
        viewPager = (ViewPager) findViewById(R.id.viewpager);

        // create a fragment list in order.
        fragments = new ArrayList<Fragment>();
        fragments.add(new StartFragment());
        fragments.add(new HistoryFragment());
        fragments.add(new PrefsFragment());

        // use FragmentPagerAdapter to bind the slidingTabLayout (tabs with different titles)
        // and ViewPager (different pages of fragment) together.
        myViewPageAdapter =new ActionTabsViewPagerAdapter(getFragmentManager(),
                fragments);
        viewPager.setAdapter(myViewPageAdapter);

        // make sure the tabs are equally spaced.
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(viewPager);

        // get the initial preference unit
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String unit = SP.getString("unit_preference", "metric");
        if (unit.equals("metric")) {
            ManualInputActivity.isMetric = true;
            HistoryFragment.isMetric = true;
        }else{
            ManualInputActivity.isMetric = false;
            HistoryFragment.isMetric = false;
        }

        //register for GCM Server
        new GcmRegistrationAsyncTask(this).execute();
	}

    //fragment class for preference tab
    public static class PrefsFragment extends PreferenceFragment {
        public void onCreate(Bundle savedInstance) {
            //Inherited method from superclass
            super.onCreate(savedInstance);
            //add preference.xml to be the layout
            addPreferencesFromResource(R.xml.preferences);
            final ListPreference listPreference = (ListPreference)findPreference("unit_preference");

            //if unit preference change, reset the all the elements in the adapter
            listPreference.setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object o) {
                            int position = listPreference.findIndexOfValue(o.toString());
                            if (position == 0){
                                ManualInputActivity.isMetric = true;
                                HistoryFragment.actAdapter.setMetric();
                                HistoryFragment.actAdapter.notifyDataSetChanged();
                            }
                            else {
                                ManualInputActivity.isMetric = false;
                                HistoryFragment.actAdapter.setImperial();
                                HistoryFragment.actAdapter.notifyDataSetChanged();
                            }
                            return true;
                        }
                    }
            );
        }
    }

    //AsyncTask for GCM registration
    class GcmRegistrationAsyncTask extends AsyncTask<Void, Void, String> {
        private Registration regService = null;
        private GoogleCloudMessaging gcm;
        private Context context;

        // TODO: change to your own sender ID to Google Developers Console project number, as per instructions above
        private static final String SENDER_ID = "813841849249";

        public GcmRegistrationAsyncTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(Void... params) {
            if (regService == null) {
                Registration.Builder builder = new Registration.Builder(AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), null)
                        .setRootUrl("https://level-harbor-123220.appspot.com/_ah/api/");
                regService = builder.build();
            }

            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(context);
                }
                String regId = gcm.register(SENDER_ID);
                msg = "Device registered, registration ID=" + regId;

                // You should send the registration ID to your server over HTTP,
                // so it can use GCM/HTTP or CCS to send messages to your app.
                // The request to your server should be authenticated if your app
                // is using accounts.
                regService.register(regId).execute();

            } catch (IOException ex) {
                ex.printStackTrace();
                msg = "Error: " + ex.getMessage();
            }
            return msg;
        }

        @Override
        protected void onPostExecute(String msg) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            Logger.getLogger("REGISTRATION").log(Level.INFO, msg);
        }
    }
}