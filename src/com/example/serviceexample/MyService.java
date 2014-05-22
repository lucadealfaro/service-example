// Copyright 2014 Luca de Alfaro

package com.example.serviceexample;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.example.serviceexample.MyServiceTask.ResultCallback;

public class MyService extends Service {

    private static final String LOG_TAG = "MyService";
    
    // Handle to notification manager.
    private NotificationManager notificationManager;
    private int ONGOING_NOTIFICATION_ID = 1; // This cannot be 0. So 1 is a good candidate.
    
    // Motion detector thread and runnable.
    private Thread myThread;
    private MyServiceTask myTask;
    
    // Binder given to clients
    private final IBinder myBinder = new MyBinder();
    
    // Binder class.
    public class MyBinder extends Binder {
    	MyService getService() {
    		// Returns the underlying service.
    		return MyService.this;
    	}
    }

    @Override
    public void onCreate() {

		Log.i(LOG_TAG, "Service is being created");
		
        // Display a notification about us starting.  We put an icon in the status bar.
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		showMyNotification();

		// Creates the thread running the service.
		myTask = new MyServiceTask(getApplicationContext());
        myThread = new Thread(myTask);
		myThread.start();
	}

    @Override
    public IBinder onBind(Intent intent) {
    	Log.i(LOG_TAG, "Service is being bound");
    	// Returns the binder to this service.
    	return myBinder;
    }
    
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
				
        Log.i(LOG_TAG, "Received start id " + startId + ": " + intent);
        // We start the task thread.
    	if (!myThread.isAlive()) {
    		myThread.start();
    	}
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
	}
	
    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        notificationManager.cancel(ONGOING_NOTIFICATION_ID);
        Log.i(LOG_TAG, "Stopping.");
        // Stops the motion detector.
        myTask.stopProcessing();
        Log.i(LOG_TAG, "Stopped.");
    }
    
    // Interface to be able to subscribe to the results by the service.
    
    public void releaseResult(ServiceResult result) {
        myTask.releaseResult(result);
    }

    public void addResultCallback(ResultCallback resultCallback) {
        myTask.addResultCallback(resultCallback);
    }

    public void removeResultCallback(ResultCallback resultCallback) {
        myTask.removeResultCallback(resultCallback);
    }
    
    // Interface which sets recording on/off.
    public void setTaskState(boolean b) {
    	myTask.setTaskState(b);
    }

    
    /**
     * Show a notification while this service is running.
     */
    @SuppressWarnings("deprecation")
	private void showMyNotification() {

        // Creates a notification.
		Notification notification = new Notification(
        		R.drawable.ic_launcher, 
        		getString(R.string.my_service_started),
                System.currentTimeMillis());
        
    	Intent notificationIntent = new Intent(this, MainActivity.class);
    	PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    	notification.setLatestEventInfo(this, getText(R.string.notification_title),
    	        getText(R.string.my_service_running), pendingIntent);
    	startForeground(ONGOING_NOTIFICATION_ID, notification);
    }
	
}
