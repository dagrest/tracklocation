package com.doat.tracklocation.service;

import java.util.List;

import com.doat.tracklocation.Controller;
import com.doat.tracklocation.datatype.CommandData;
import com.doat.tracklocation.datatype.CommandDataBasic;
import com.doat.tracklocation.datatype.CommandEnum;
import com.doat.tracklocation.datatype.CommandKeyEnum;
import com.doat.tracklocation.datatype.CommandValueEnum;
import com.doat.tracklocation.datatype.ContactDeviceDataList;
import com.doat.tracklocation.datatype.MessageDataContactDetails;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;
import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

// This service notifies location automatically once in certain period of time ("Tracking" button) when configured
public class TrackingService extends TrackLocationServiceBasic {

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
                
                String objectName = TrackingService.className.toString();
                if (containsGPS && forceGps) {
                	LogManager.LogInfoMsg(className, "requestLocation", "GPS_PROVIDER selected.");
                
	            	locationListenerGPS = new LocationListenerBasic(context, this, "LocationListenerGPS", CommonConst.GPS, objectName);
	            	locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK, objectName);

                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerGPS);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                } else if (containsNetwork) {
                	LogManager.LogInfoMsg(className, "requestLocation", "NETWORK_PROVIDER selected.");
                	
            		locationListenerNetwork = new LocationListenerBasic(context, this, "LocationListenerNetwork", CommonConst.NETWORK, objectName);

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

}
