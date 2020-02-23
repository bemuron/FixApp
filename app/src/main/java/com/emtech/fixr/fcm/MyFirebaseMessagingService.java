package com.emtech.fixr.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.emtech.fixr.R;
import com.emtech.fixr.app.Config;
import com.emtech.fixr.app.MyApplication;
import com.emtech.fixr.data.network.Result;
import com.emtech.fixr.data.network.api.APIService;
import com.emtech.fixr.data.network.api.LocalRetrofitApi;
import com.emtech.fixr.helpers.MyPreferenceManager;
import com.emtech.fixr.models.NotificationMessage;
import com.emtech.fixr.models.NotificationUser;
import com.emtech.fixr.presentation.ui.activity.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;*/

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private MyPreferenceManager preferenceManager;
    private MyNotificationManager myNotificationManager;

    public static final String KEY = "key";
    public static final String TOPIC = "topic";
    public static final String SUBSCRIBE = "subscribe";
    public static final String UNSUBSCRIBE = "unsubscribe";
    public static final String USER_ID = "userId";


    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            /*if (*//* Check if data needs to be processed by long running job *//* true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();*/
                try {

                    JSONObject json = new JSONObject(remoteMessage.getData().toString());
                    sendPushNotification(json);
                } catch (Exception e) {
                    Log.e(TAG, "Exception: " + e.getMessage());
                }

        }else {
        // Handle message within 10 seconds
        handleNow();
    }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    //method to handle notification received
    private void handleNotification(String message) {
        if (!MyNotificationManager.isAppInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
            pushNotification.putExtra("message", message);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

            // play notification sound
            MyNotificationManager myNotificationManager = new MyNotificationManager(getApplicationContext());
            myNotificationManager.playNotificationSound();
        }else{
            // If the app is in background, firebase itself handles the notification
        }
    }


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        //super.onNewToken(token);

        //save the token in sharedpreferences
        MyApplication.getInstance().getPrefManager().saveDeviceToken(token);

        /*
         LocalBroadcastManager is used to broadcast the message to all the activities which are
          registered for the broadcast receiver.
         */
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent();
        registrationComplete.setAction(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", token);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    /*private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                //.setService(MyJobService.class)
                .setTag("my-job-tag")
                .build();
        dispatcher.schedule(myJob);
        // [END dispatch_job]
    }*/

    /**
     * Subscribe to a topic
     */
    public void subscribeToTopic(String topic) {
        Log.e(TAG, "Subscribing to weather topic");
        // [START subscribe_topics]
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d(TAG, msg);
                        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });
// [END subscribe_topics]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    //this method will display the notification
    //We are passing the JSONObject that is received from
    //firebase cloud messaging
    private void sendPushNotification(JSONObject json) {
        //optionally we can display the json into log
        Log.e(TAG, "Notification JSON " + json.toString());
        try {
            //getting the json data
            JSONObject data = json.getJSONObject("data");

            //parsing json data
            //optString returns the value mapped by name if it exists,
            // coercing it if necessary. Returns the empty string if no such mapping exists.
            String imageUrl = data.optString("image");

            String activityToLaunch = data.getString("activity_to_launch");

            //float time_taken = 0.0f;
            //time_taken = (float) data.getDouble("time_taken");

            String time_taken = data.getString("time_taken");

            JSONObject mObj  = data.getJSONObject("message");
            NotificationMessage notificationMessage = new NotificationMessage();
            notificationMessage.setMeeting_id(mObj.getInt("meeting_id"));
            //variable to hold our created message
            String newMessage = null;
            if(mObj.getString("meeting_time") != null){
                newMessage = "Meeting date: " + mObj.getString("meeting_date") +
                "Meeting time: " + mObj.getString("meeting_time") +
                "Meeting location: " + mObj.getString("meeting_location");
            }

            notificationMessage.setMessage(newMessage);
            //notificationMessage.setId(mObj.getString("meeting_id"));
            //notificationMessage.setCreatedAt(mObj.getString("created_at"));

            JSONObject uObj = data.getJSONObject("user");
            NotificationUser user = new NotificationUser();
            user.setId(uObj.getInt("user_id"));
            //user.setEmail(uObj.getString("email"));
            user.setName(uObj.getString("name"));
            notificationMessage.setNotificationUser(user);

            String title = data.getString("title");
            boolean isBackground = data.getBoolean("is_background");
            String timestamp = "1:00";

            if (!MyNotificationManager.isAppInBackground(getApplicationContext())) {
                switch (activityToLaunch) {
                    case "HomeActivity": {
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);

                        //get the user id too, to be compared with the logged in users id
                        //so that if the requester is a tutor too we can still open the right activity
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("user_id", user.getId());
                        pushNotification.putExtra("isSessionOn", true);
                        pushNotification.putExtra("message", title);

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        Log.e(TAG, "fcm: session_in_progress");


                        break;
                    }
                    case "payment_activity": {

                        //time_taken = (float) data.getDouble("time_taken");
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);

                        //get the user id too, to be compared with the logged in users id
                        //so that if the requester is a tutor too we can still open the right activity
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("user_id", user.getId());
                        pushNotification.putExtra("time_taken", time_taken);
                        pushNotification.putExtra("session_finished", true);
                        Log.e(TAG, "fcm: payment activity");

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                        break;
                    }
                    case "rating_activity": {
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);

                        //get the user id too, to be compared with the logged in users id
                        //so that if the requester is a tutor too we can still open the right activity
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("user_id", user.getId());
                        pushNotification.putExtra("rating_activity", true);
                        Log.e(TAG, "fcm: rating activity");

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        break;
                    }
                    case "tutor_pending_requests": {
                        //creating an intent for the notification
                        //timestamp = data.getString("timestamp");
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                        pushNotification.putExtra("fragment_id", 5);
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("requester_name", data.getString("user_name"));
                        pushNotification.putExtra("tutor_pending_requests", true);
                        Log.e(TAG, "fcm: tutor_pending_requests");

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;
                    }
                    case "tutor_confirmed_requests": {
                        //creating an intent for the notification
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                        pushNotification.putExtra("fragment_id", 7);
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("requester_name", data.getString("user_name"));
                        pushNotification.putExtra("tutor_confirmed_requests", true);
                        Log.e(TAG, "fcm: tutor_confirmed_requests");

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());
                        break;
                    }
                    case "user_confirmed_requests": {
                        //creating an intent for the notification
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                        pushNotification.putExtra("fragment_id", 8);
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("tutor_name", data.getString("tutor_name"));
                        pushNotification.putExtra("user_confirmed_requests", true);
                        Log.e(TAG, "fcm: user_confirmed_requests");

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;
                    }
                    case "user_pending_requests": {
                        timestamp = data.getString("timestamp");
                        //creating an intent for the notification
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                        pushNotification.putExtra("fragment_id", 6);
                        pushNotification.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        pushNotification.putExtra("tutor_name", data.getString("tutor_name"));
                        pushNotification.putExtra("user_pending_requests", true);
                        Log.e(TAG, "fcm: user_pending_requests");

                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;
                    }

                    default: {
                        // app is in foreground, broadcast the push message
                        Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                        pushNotification.putExtra("message", title);
                        LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                        Log.e(TAG, "fcm:" + title);

                        // play notification sound
                        MyNotificationManager notificationManager = new MyNotificationManager(getApplicationContext());
                        notificationManager.playNotificationSound();
                        break;
                    }
                }

            }else {

                // app is in background. show the message in notification tray
            Intent intent = null;
                switch (activityToLaunch) {
                    case "HomeActivity":
                        timestamp = data.getString("timestamp");
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("fragment_id", 6);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        intent.putExtra("tutor_name", data.getString("tutor_name"));
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;
                    case "tutor_pending_requests":
                        timestamp = data.getString("timestamp");
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("fragment_id", 5);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        intent.putExtra("requester_name", data.getString("user_name"));
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;
                    case "tutor_confirmed_requests":
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("fragment_id", 7);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        intent.putExtra("requester_name", data.getString("user_name"));
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());
                        break;
                    case "user_confirmed_requests":
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("fragment_id", 8);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        intent.putExtra("tutor_name", data.getString("tutor_name"));
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;

                    case "session_in_progress":
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        intent.putExtra("user_id", user.getId());
                        //get the user id too, to be compared with the logged in users id
                        //so that if the requester is a tutor too we can still open the right activity
                        intent.putExtra("start_session", true);
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;

                    case "payment_activity":
                        //time_taken = (float) data.getDouble("time_taken");
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        //get the user id too, to be compared with the logged in users id
                        //so that if the requester is a tutor too we can still open the right activity
                        intent.putExtra("user_id", user.getId());
                        intent.putExtra("time_taken", time_taken);
                        intent.putExtra("session_finished", true);
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;

                    case "rating_activity":
                        //creating an intent for the notification
                        intent = new Intent(getApplicationContext(), HomeActivity.class);
                        intent.putExtra("meeting_id", notificationMessage.getMeeting_id());
                        //get the user id too, to be compared with the logged in users id
                        //so that if the requester is a tutor too we can still open the right activity
                        intent.putExtra("student_id", user.getId());
                        Log.e(TAG, "meeting id from notification " + notificationMessage.getMeeting_id());

                        break;
                }

                //if there is no image
                if (imageUrl.equals("null")) {
                    //displaying small notification
                    //myNotificationManager.showNotificationMessage(title, notificationMessage.getMessage(), intent);
                    showNotificationMessage(getApplicationContext(), title, notificationMessage.getMessage(), timestamp, intent);
                    //playing notification sound
                    myNotificationManager.playNotificationSound();
                } else {
                    //if there is an image
                    //displaying a big notification
                    //myNotificationManager.showNotificationMessage(title, notificationMessage.getMessage(), intent);
                    // image is present, show notification with image
                    showNotificationMessageWithBigImage(getApplicationContext(), title, notificationMessage.getMessage(), timestamp, intent, imageUrl);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        myNotificationManager = new MyNotificationManager(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myNotificationManager.showNotificationMessage(title, message, timeStamp, intent);
    }

    /**
     * Showing notification with text and image
     */
    private void showNotificationMessageWithBigImage(Context context, String title, String message, String timeStamp, Intent intent, String imageUrl) {
        myNotificationManager = new MyNotificationManager(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myNotificationManager.showNotificationMessage(title, message, timeStamp, intent, imageUrl);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */
    private void sendNotification(String messageBody) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(getString(R.string.app_name))
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

}
