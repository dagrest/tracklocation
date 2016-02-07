package com.doat.tracklocation.service;

import java.util.List;
import java.util.Timer;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.broadcast.BroadcastReceiverTrackLocationService;
import com.doat.tracklocation.datatype.BroadcastActionEnum;
import com.doat.tracklocation.datatype.BroadcastConstEnum;
import com.doat.tracklocation.datatype.BroadcastKeyEnum;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.datatype.NotificationBroadcastData;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.doat.tracklocation.utils.TrackLocationServiceStopTimerJob;
import com.google.gson.Gson;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

// This service notifies location while tracking contact/s on map ("Locate" button)
public class TrackLocationService extends TrackLocationServiceBasic {

	protected TrackLocationServiceStopTimerJob trackLocationServiceStopTimerJob;
	protected Timer timer;
	protected long repeatPeriod;
	protected long trackLocationServiceStartTime;
	protected String trackLocationKeepAliveRequester;
	protected BroadcastReceiver gcmKeepAliveBroadcastReceiver;
//	protected boolean isUpdateRunnungService;
	protected MessageDataContactDetails senderMessageDataContactDetails;
	protected BroadcastReceiver notificationBroadcastReceiver;

	@Override
	public IBinder onBind(Intent intent) {
		super.onBind(intent);
		return null;
	}
	
	@Override          
    public void onCreate()          
    {             
        super.onCreate();
        methodName = "onCreate";
        className = this.getClass().getName();
        timer = null;
//        isUpdateRunnungService = false;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
        
		
		initBroadcastReceiver(BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(), "ContactConfiguration");
		
		if(notificationBroadcastReceiver == null){
			notificationBroadcastReceiver = new BroadcastReceiverTrackLocationService(this);
		}
		initNotificationBroadcastReceiver(notificationBroadcastReceiver);
        
        prepareTrackLocationServiceStopTimer();
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        methodName = "onDestroy";
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> onDestroy - Start");

    	// Stop TrackLocationServiceStopTimer
    	logMessage = "Stopping TrackLocationService TimerJob...";
    	LogManager.LogInfoMsg(className, methodName, logMessage);
    	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

    	trackLocationServiceStopTimerJob.cancel();

    	logMessage = "Stopped TrackLocationService TimerJob";
    	LogManager.LogInfoMsg(className, methodName, logMessage);
    	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

    	LogManager.LogFunctionExit(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }  

    public void updateService(MessageDataContactDetails senderMessageDataContactDetails){
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

//    	isUpdateRunnungService = true;
    	this.senderMessageDataContactDetails = senderMessageDataContactDetails;
    	onStartCommand(null, 0, START_STICKY);

    	LogManager.LogFunctionExit(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }
    
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId)
	{             
    	methodName = "onStartCommand";
		try{
			LogManager.LogFunctionCall(className, methodName);
			Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);
            
            if(intent == null){
            	logMessage = "TrackLocation service - has been restarted.";
        		LogManager.LogInfoMsg(className, methodName, logMessage);
        		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            	// return START_STICKY;
            } else {
	            Bundle extras = intent.getExtras();
	            String jsonSenderMessageDataContactDetails = null;
//	            if(isUpdateRunnungService == false){
	    		if(extras.containsKey(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS)){
	    			jsonSenderMessageDataContactDetails = extras.getString(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS);
		            senderMessageDataContactDetails = 
		            	gson.fromJson(jsonSenderMessageDataContactDetails, MessageDataContactDetails.class);
		            String senderAccount = senderMessageDataContactDetails == null ? "NOT PROVIDED SENDER DETAILS..." : senderMessageDataContactDetails.getAccount();
	            	logMessage = "TrackLocation service - first start requested by  [" + senderAccount + "]";
	        		LogManager.LogInfoMsg(className, methodName, logMessage);
	        		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    		}
            }
//            }
            if(senderMessageDataContactDetails != null){
            	trackLocationKeepAliveRequester = senderMessageDataContactDetails.getAccount();
            	logMessage = "TrackLocation service - start requested by [" + trackLocationKeepAliveRequester + "]";
        		LogManager.LogInfoMsg(className, methodName, logMessage);
        		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            } else {
            	logMessage = "Unable to start TrackLocation service - no SenderMessageDataContactDetails has been provided.";
        		LogManager.LogErrorMsg(className, methodName, logMessage);
        		Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            	return START_STICKY;
            }
            
            Controller.addAccountToList(context, CommonConst.PREFERENCES_SEND_LOCATION_TO_ACCOUNTS, trackLocationKeepAliveRequester);
            String jsonListAccounts = Preferences.getPreferencesString(context, 
            	CommonConst.PREFERENCES_SEND_LOCATION_TO_ACCOUNTS);
            logMessage = "Updated accounts to send Location updates: " + jsonListAccounts;
    		LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            
            
            // Start TrackLocationServiceStopTimer
        	logMessage = "Start TrackLocationService TimerJob with repeat period = " + 
            		repeatPeriod/1000 + " seconds.";
        	LogManager.LogInfoMsg(className, methodName, logMessage);
        	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            try {
            	if(timer != null){
            		timer.schedule(trackLocationServiceStopTimerJob, 0, repeatPeriod);
                	logMessage = "Started TrackLocationService TimerJob with repeat period = " + 
                    		repeatPeriod/1000 + " seconds.";
                	LogManager.LogInfoMsg(className, methodName, logMessage);
                	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
            	}
			} catch (IllegalStateException e) {
				String ecxeptionMessage = "TimerTask is scheduled already";
				logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
				if(!ecxeptionMessage.equals(e.getMessage())){
					LogManager.LogException(e, className, methodName);
					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
				} else {
					LogManager.LogInfoMsg(className, methodName, ecxeptionMessage);
					Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + ecxeptionMessage);
				}
			} catch (IllegalArgumentException e) {
				logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
				LogManager.LogException(e, className, methodName);
				Log.e(CommonConst.LOG_TAG, logMessage, e);
			}
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> Timer with TimerJob that stops TrackLocationService - started");

    		// **********************************************
    		// *             REQUEST LOCATION				*
    		// *											*
    		requestLocation(true); //						*
    		// *											*
    		// *											*					
    		// **********************************************

            // Notify to caller by GCM (push notification) - TrackLocationServiceStarted
    		clientBatteryLevel = Controller.getBatteryLevel(context);
            MessageDataContactDetails messageDataContactDetails = new MessageDataContactDetails(clientAccount, 
                clientMacAddress, clientPhoneNumber, clientRegId, clientBatteryLevel);
            ContactDeviceDataList contactDeviceDataToSendNotificationTo = 
            	new ContactDeviceDataList (
            		senderMessageDataContactDetails.getAccount(), 
            		senderMessageDataContactDetails.getMacAddress(), 
            		senderMessageDataContactDetails.getPhoneNumber(), 
            		senderMessageDataContactDetails.getRegId(), 
            		null);
            
            String msgServiceStarted = "{" + className + "} TrackLocationService was started by [" + senderMessageDataContactDetails.getAccount() + "]";
            String notificationKey = CommandKeyEnum.start_status.toString();
            String notificationValue = CommandValueEnum.success.toString();		

            CommandDataBasic commandDataBasic = new CommandData(
				getApplicationContext(), 
				contactDeviceDataToSendNotificationTo, 
    			CommandEnum.notification, 
    			msgServiceStarted, 
    			messageDataContactDetails, 
    			null, 					// location
    			notificationKey, 		// key
    			notificationValue,  	// value
    			appInfo
    		);
            commandDataBasic.sendCommand();
            
            logMessage = "TrackLocationService - send NOTIFICATION TrackLocation Strat succeeded from [" + senderMessageDataContactDetails.getAccount() + "]";
            LogManager.LogInfoMsg(className, methodName, logMessage);
    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
		} catch (Exception e) {
			LogManager.LogException(e, className, methodName);
			Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage);
		}
		return START_STICKY;
	}

    protected boolean providerAvailable(List<String> providers) {
        if (providers.size() < 1) {
        	return false;
        }
        return true;
    }

    public void stopTrackLocationService(){
    	methodName = "stopTrackLocationService";
    	LogManager.LogFunctionCall(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_ENTRY] {" + className + "} -> " + methodName);

	    unregisterReceiver(gcmKeepAliveBroadcastReceiver);
	    unregisterReceiver(notificationBroadcastReceiver);
    	stopSelf();

		Preferences.clearAccountRegIdMap(context, CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID);
    	
    	logMessage = "Track Location Service has been stopped.";
    	LogManager.LogInfoMsg(className, methodName, logMessage);
    	Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
    	
    	LogManager.LogFunctionExit(className, methodName);
    	Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }

	public long getTrackLocationServiceStartTime() {
		return trackLocationServiceStartTime;
	}

	public void setTrackLocationServiceStartTime(long trackLocationServiceStartTime) {
		this.trackLocationServiceStartTime = trackLocationServiceStartTime;
	}
    
    protected void initBroadcastReceiver(final String action, final String actionDescription)
    {
    	methodName = "initBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
		
		logMessage = "Broadcast action: " + action + ". Broadcast action description: " + actionDescription;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(action);
	    gcmKeepAliveBroadcastReceiver = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
	    		methodName = "BroadcastReceiver->onReceive";
	    		LogManager.LogFunctionCall(className, methodName);
	    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
	    		
	    		Gson gson = new Gson();
	    		Bundle bundle = intent.getExtras();
	    		// ===========================================
	    		// broadcast key = keep_alive
	    		// ===========================================
	    		if(bundle != null  && bundle.containsKey(BroadcastConstEnum.data.toString())){
	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
	    				return;
	    			}
	    			NotificationBroadcastData broadcastData = 
	    				gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
	    			if(broadcastData == null){
	    				return;
	    			}
	    			
	    			String key = broadcastData.getKey();
	    			
	    			String currentTime = broadcastData.getValue();
	    			if(currentTime == null || currentTime.isEmpty()){
	    				logMessage = "Keep alive delay is empty.";
	    				LogManager.LogErrorMsg(className, methodName, logMessage);
	    				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
	    				return;
	    			}
	    			MessageDataContactDetails messageDataContactDetails = broadcastData.getContactDetails();
	    			String accountRequestedKeepAlive = messageDataContactDetails.getAccount();

	    			logMessage = "Broadcast action key: " + key + " in: " + 
		    			(Long.parseLong(currentTime, 10) - System.currentTimeMillis()) + " sec. " +
		    			"Requested by [" + accountRequestedKeepAlive + "]";
		    		LogManager.LogInfoMsg(className, methodName, logMessage);
		    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);

	    			if(BroadcastKeyEnum.keep_alive.toString().equals(key)){
		    			logMessage = "Broadcast action key: " + key +
				    		"Requested by [" + accountRequestedKeepAlive + "]";
		    			LogManager.LogInfoMsg(className, methodName, logMessage);
		    			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		    			trackLocationServiceStartTime = Long.parseLong(currentTime, 10);   
		    			trackLocationKeepAliveRequester = accountRequestedKeepAlive;
		   			}
	    		}
	    		LogManager.LogFunctionExit(className, methodName);
	    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
	    	}
	    };
	    registerReceiver(gcmKeepAliveBroadcastReceiver, intentFilter);
	    
		logMessage = "Broadcast action: " + action + ". Broadcast action description: " + actionDescription;
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	    
		LogManager.LogFunctionExit(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    }

    public void prepareTrackLocationServiceStopTimer(){
        timer = new Timer();
        trackLocationServiceStopTimerJob = new TrackLocationServiceStopTimerJob();
        trackLocationServiceStopTimerJob.setTrackLocationServiceObject(this);
        repeatPeriod = CommonConst.REPEAT_PERIOD_DEFAULT; // 2 minutes
        trackLocationServiceStartTime = System.currentTimeMillis();
    }

	// Initialize BROADCAST_MESSAGE broadcast receiver
	private void initNotificationBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
		methodName = "initNotificationBroadcastReceiver";
		LogManager.LogFunctionCall(className, methodName);
		
		IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_MESSAGE.toString());
	    
	    registerReceiver(broadcastReceiver, intentFilter);
	    
		LogManager.LogFunctionExit(className, methodName);
	}

}

