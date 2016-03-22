package edu.dartmouth.cs.MyRuns.Server;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.logging.Level;
import java.util.logging.Logger;

import edu.dartmouth.cs.MyRuns.ExercisesDataSource;

public class GcmIntentService extends IntentService {

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (extras != null && !extras.isEmpty()) {  // has effect of unparcelling Bundle
            // Since we're not using two way messaging, this is all we really to check for
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Logger.getLogger("GCM_RECEIVED").log(Level.INFO, extras.toString());
                String msg = extras.getString("message");
                if (msg != null && !msg.isEmpty()){
                    if (!msg.startsWith("Added")){
                        //delete exercise entry based on exercise id
                        long id = Long.parseLong(msg);
                        ExercisesDataSource dataSource = new ExercisesDataSource(this);
                        dataSource.open();
                        dataSource.deleteExercise(id);

                        //set broadcast to history fragment
                        Intent i = new Intent("Adaptor");
                        i.putExtra("id", id);
                        sendBroadcast(i);
                    }
                }
            }
        }

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    protected void showToast(final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}