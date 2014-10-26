package com.dagrest.tracklocation.service;

import java.util.List;
import java.util.Timer;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.AppInfo;
import com.dagrest.tracklocation.datatype.BroadcastActionEnum;
import com.dagrest.tracklocation.datatype.BroadcastConstEnum;
import com.dagrest.tracklocation.datatype.BroadcastKeyEnum;
import com.dagrest.tracklocation.datatype.CommandData;
import com.dagrest.tracklocation.datatype.CommandDataBasic;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.CommandKeyEnum;
import com.dagrest.tracklocation.datatype.CommandValueEnum;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.MessageDataContactDetails;
import com.dagrest.tracklocation.datatype.NotificationBroadcastData;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.TimerJob;
import com.dagrest.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class TrackingService extends TrackLocationServiceBasic {

	protected static String className;
	protected static Context context;
	protected LocationManager locationManager;
	protected List<String> locationProviders;
	protected LocationListener locationListenerGPS = null;
	protected LocationListener locationListenerNetwork = null;
//	protected TimerJob timerJob;
//	protected Timer timer;
//	protected long repeatPeriod;
//	protected long trackLocationServiceStartTime;
//	protected String trackLocationKeepAliveRequester;
//	protected BroadcastReceiver gcmKeepAliveBroadcastReceiver;
	
	protected String clientAccount;
	protected String clientMacAddress;
	protected String clientPhoneNumber;
	protected String clientRegId;
	protected int clientBatteryLevel;
	protected String logMessage;
	protected AppInfo appInfo;
	protected String methodName;

	@Override
	public IBinder onBind(Intent intent) {
        LogManager.LogFunctionCall(className, "onBind");
        LogManager.LogFunctionExit(className, "onBind");
		return null;
	}
	
	@Override          
    public void onCreate()          
    {             
        super.onCreate();
        methodName = "onCreate";
        className = this.getClass().getName();
//        timer = null;
		LogManager.LogFunctionCall(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
        
		
		// initBroadcastReceiver(BroadcastActionEnum.BROADCAST_LOCATION_KEEP_ALIVE.toString(), "ContactConfiguration");
        
        try{
            LogManager.LogFunctionCall(className, "onCreate");
            if(context == null){
            	context = getApplicationContext();
            }
            if(locationManager == null){
            	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
           
            appInfo = Controller.getAppInfo(context);

            // prepareTrackLocationServiceStopTimer();
            
        	// Collect client details
    		context = getApplicationContext();
    		clientAccount = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
    		clientMacAddress = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_MAC_ADDRESS);
    		clientPhoneNumber = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_NUMBER);
    		clientRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
    		
    		LogManager.LogFunctionExit(className, methodName);
    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
    		
        } catch (Exception e) {
        	LogManager.LogException(e, className, methodName);
        	Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + methodName, e);
        }
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        try{
        	LogManager.LogFunctionCall(className, "onDestroy");
        	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> onDestroy - Start");
        	
//        	// Stop TrackLocationServiceStopTimer
//        	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> Stop TrackLocationService TimerJob");
//        	timerJob.cancel();
//        	Log.i(CommonConst.LOG_TAG, "[INFO]  {" + className + "} -> Timer with TimerJob that stops TrackLocationService - stopped");

            if(locationManager != null){
            	if( locationListenerGPS != null){
	                locationManager.removeUpdates(locationListenerGPS);
	                LogManager.LogInfoMsg(className, "onDestroy", "locationListenerGPS - Updates removed");
            	}
            	if( locationListenerNetwork != null){
	                locationManager.removeUpdates(locationListenerNetwork);
	                LogManager.LogInfoMsg(className, "onDestroy", "locationListenerNetwork - Updates removed");
            	}
            }
            
//    		if(gcmKeepAliveBroadcastReceiver != null) {
//    			unregisterReceiver(gcmKeepAliveBroadcastReceiver);
//    		}
    		
			// sendTrackLocationServiceStopped();
			
            LogManager.LogFunctionExit(className, "onDestroy");
            Log.i(CommonConst.LOG_TAG, "onDestroy - End");
            
        } catch (Exception e) {
            LogManager.LogException(e, className, "onDestroy");
            Log.e(CommonConst.LOG_TAG, "onDestroy", e);
        }
    }  

    @Override          
	public void onStart(Intent intent, int startId)           
	{                  
    	methodName = "onStart";
		try{
			LogManager.LogFunctionCall(className, "onStart");
            Log.i(CommonConst.LOG_TAG, "{" + className + "} onStart - Start");
            
            Gson gson = new Gson();
            Bundle extras = intent.getExtras();
            String jsonSenderMessageDataContactDetails = null;
            MessageDataContactDetails senderMessageDataContactDetails = null;
    		if(extras.containsKey(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS)){
    			jsonSenderMessageDataContactDetails = extras.getString(CommonConst.START_CMD_SENDER_MESSAGE_DATA_CONTACT_DETAILS);
	            senderMessageDataContactDetails = 
	            	gson.fromJson(jsonSenderMessageDataContactDetails, MessageDataContactDetails.class);
	            if(senderMessageDataContactDetails != null){
	            	String trackingServiceStartRequester = senderMessageDataContactDetails.getAccount();
	            	logMessage = "Tracking service has been started by [" + trackingServiceStartRequester + "]";
	        		LogManager.LogInfoMsg(className, methodName, logMessage);
	        		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
	            }
    		}
            
//            // Start TrackLocationServiceStopTimer
//        	Log.i(CommonConst.LOG_TAG, "{" + className + "} Start TrackLocationService TimerJob with repeat period = " + 
//        		repeatPeriod/1000/60 + " min");
//            try {
//            	if(timer != null){
//            		timer.schedule(timerJob, 0, repeatPeriod);
//            	}
//			} catch (IllegalStateException e) {
//				String ecxeptionMessage = "TimerTask is scheduled already";
//				logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
//				if(!ecxeptionMessage.equals(e.getMessage())){
//					LogManager.LogException(e, className, methodName);
//					Log.e(CommonConst.LOG_TAG, "[EXCEPTION] {" + className + "} -> " + logMessage, e);
//				} else {
//					LogManager.LogInfoMsg(className, methodName, ecxeptionMessage);
//					Log.e(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + ecxeptionMessage);
//				}
//			} catch (IllegalArgumentException e) {
//				logMessage = "[EXCEPTION] {" + className + "} Failed to Start TrackLocationService TimerJob";
//				LogManager.LogException(e, className, methodName);
//				Log.e(CommonConst.LOG_TAG, logMessage, e);
//			}
//    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> Timer with TimerJob that stops TrackLocationService - started");

            requestLocation(true);

            // Notify to caller by GCM (push notification)
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
            // Notify caller by GCM (push notification)
            
            String msgServiceStarted = "{" + className + "} TrackingService was started by [" + senderMessageDataContactDetails.getAccount() + "]";
            String notificationKey = CommandKeyEnum.start_tracking_status.toString();
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
            
            Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} TrackingService - send NOTIFICATION Command performed");

            LogManager.LogFunctionExit(className, "onStart");
            Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} onStart - End");
		} catch (Exception e) {
			LogManager.LogException(e, className, "onStart");
		}
	}
	
	public void requestLocation(boolean forceGps) {
        try{
        	LogManager.LogFunctionCall(className, "requestLocation");
        	if(locationListenerGPS != null){
        		locationManager.removeUpdates(locationListenerGPS);
        	}
        	if(locationListenerNetwork != null){
        		locationManager.removeUpdates(locationListenerNetwork);
        	}
			locationManager = null;
			locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locationProviders = locationManager.getProviders(true);
			LogManager.LogInfoMsg(className, "requestLocation", "Providers list: " + locationProviders.toString());

	        if (providerAvailable(locationProviders)) {
	        	boolean containsGPS = locationProviders.contains(LocationManager.GPS_PROVIDER);
                LogManager.LogInfoMsg(className, "requestLocation", "containsGPS: " + containsGPS);

                boolean containsNetwork = locationProviders.contains(LocationManager.NETWORK_PROVIDER);
                LogManager.LogInfoMsg(className, "requestLocation", "containsNetwork: " + containsNetwork);

                String intervalString = Preferences.getPreferencesString(context, CommonConst.LOCATION_SERVICE_INTERVAL);
                if(intervalString == null || intervalString.isEmpty()){
                	intervalString = CommonConst.LOCATION_DEFAULT_UPDATE_INTERVAL; // time in milliseconds
                }
                
                if (containsGPS && forceGps) {
                	LogManager.LogInfoMsg(className, "requestLocation", "GPS_PROVIDER selected.");
                
	            	locationListenerGPS = new LocationListenerBasic(context, this, command, "LocationListenerGPS", CommonConst.GPS, objectName);
	            	locationListenerNetwork = new LocationListenerBasic(context, this, command, "LocationListenerNetwork", CommonConst.NETWORK, objectName);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerGPS);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                } else if (containsNetwork) {
                	LogManager.LogInfoMsg(className, "requestLocation", "NETWORK_PROVIDER selected.");
                	
            		locationListenerNetwork = new LocationListenerBasic(context, this, command, "LocationListenerNetwork", CommonConst.NETWORK, objectName);

            		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                }
	        } else {
		        LogManager.LogInfoMsg(className, "requestLocation", "No location providers available.");
	        }
        LogManager.LogFunctionExit(className, "requestLocation");
        } catch (Exception e) {
        	LogManager.LogException(e, className, "requestLocation");
        }
    }
    
    protected boolean providerAvailable(List<String> providers) {
        if (providers.size() < 1) {
        	return false;
        }
        return true;
    }

//    public void stopTrackLocationService(){
//    	Log.i(CommonConst.LOG_TAG, "Stop TrackLocationService");
//    	stopSelf();
//    }
//
//	public long getTrackLocationServiceStartTime() {
//		return trackLocationServiceStartTime;
//	}
//
//	public void setTrackLocationServiceStartTime(long trackLocationServiceStartTime) {
//		this.trackLocationServiceStartTime = trackLocationServiceStartTime;
//	}
    
//    protected void initBroadcastReceiver(final String action, final String actionDescription)
//    {
//    	methodName = "initBroadcastReceiver";
//		LogManager.LogFunctionCall(className, methodName);
//		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
//		
//		logMessage = "Broadcast action: " + action + ". Broadcast action description: " + actionDescription;
//		LogManager.LogInfoMsg(className, methodName, logMessage);
//		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//		
//	    IntentFilter intentFilter = new IntentFilter();
//	    intentFilter.addAction(action);
//	    gcmKeepAliveBroadcastReceiver = new BroadcastReceiver() 
//	    {
//	    	@Override
//    		public void onReceive(Context context, Intent intent) {
//	    		methodName = "BroadcastReceiver->onReceive";
//	    		LogManager.LogFunctionCall(className, methodName);
//	    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_CALL] {" + className + "} -> " + methodName);
//	    		
//	    		Gson gson = new Gson();
//	    		Bundle bundle = intent.getExtras();
//	    		// ===========================================
//	    		// broadcast key = keep_alive
//	    		// ===========================================
//	    		if(bundle != null  && bundle.containsKey(BroadcastConstEnum.data.toString())){
//	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
//	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
//	    				return;
//	    			}
//	    			NotificationBroadcastData broadcastData = 
//	    				gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
//	    			if(broadcastData == null){
//	    				return;
//	    			}
//	    			
//	    			String key = broadcastData.getKey();
//	    			
//	    			String currentTime = broadcastData.getValue();
//	    			if(currentTime == null || currentTime.isEmpty()){
//	    				logMessage = "Keep alive delay is empty.";
//	    				LogManager.LogErrorMsg(className, methodName, logMessage);
//	    				Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//	    				return;
//	    			}
//	    			MessageDataContactDetails messageDataContactDetails = broadcastData.getContactDetails();
//	    			String accountRequestedKeepAlive = messageDataContactDetails.getAccount();
//
//	    			logMessage = "Broadcast action key: " + key + " in: " + 
//		    			(Long.parseLong(currentTime, 10) - System.currentTimeMillis()) + " sec. " +
//		    			"Requested by [" + accountRequestedKeepAlive + "]";
//		    		LogManager.LogInfoMsg(className, methodName, logMessage);
//		    		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//
//	    			if(BroadcastKeyEnum.keep_alive.toString().equals(key)){
//		    			logMessage = "Broadcast action key: " + key +
//				    		"Requested by [" + accountRequestedKeepAlive + "]";
//		    			LogManager.LogInfoMsg(className, methodName, logMessage);
//		    			Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//		    			trackLocationServiceStartTime = Long.parseLong(currentTime, 10);   
//		    			trackLocationKeepAliveRequester = accountRequestedKeepAlive;
//		   			}
//	    		}
//	    		LogManager.LogFunctionExit(className, methodName);
//	    		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
//	    	}
//	    };
//	    registerReceiver(gcmKeepAliveBroadcastReceiver, intentFilter);
//	    
//		logMessage = "Broadcast action: " + action + ". Broadcast action description: " + actionDescription;
//		LogManager.LogInfoMsg(className, methodName, logMessage);
//		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
//	    
//		LogManager.LogFunctionExit(className, methodName);
//		Log.i(CommonConst.LOG_TAG, "[FUNCTION_EXIT] {" + className + "} -> " + methodName);
//    }

    public void prepareTrackLocationServiceStopTimer(){
//        timer = new Timer();
//        timerJob = new TimerJob();
//        timerJob.setTrackingServiceObject(this);
//        repeatPeriod = CommonConst.REPEAT_PERIOD_...; // each 12 hours
//        trackLocationServiceStartTime = System.currentTimeMillis();
    }

}

