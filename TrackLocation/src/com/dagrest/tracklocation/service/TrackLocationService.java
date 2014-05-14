package com.dagrest.tracklocation.service;

import java.util.Date;
import java.util.List;

import com.dagrest.tracklocation.Controller;
import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class TrackLocationService extends Service {

	private static String className;
	private static Context context;
	private LocationManager locationManager;
	private PowerManager.WakeLock wl;
	private PowerManager pm;
	private Boolean toReleaseWakeLock;
	private List<String> locationProviders;
	private Boolean isLocationProviderAvailable;
	
	@Override
	public IBinder onBind(Intent intent) {
        LogManager.LogFunctionCall(className, "onBind");
        LogManager.LogFunctionExit(className, "onBind");
		return null;
	}
	
	public void broadcastLocationUpdatedGps()
	{
		LogManager.LogFunctionCall(className, "broadcastLocationUpdatedGps");
		Intent intent = new Intent();
		intent.setAction("com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_GPS");
		sendBroadcast(intent);
		LogManager.LogFunctionExit(className, "broadcastLocationUpdatedGps");
	}

	public void broadcastLocationUpdatedNetwork()
	{
		LogManager.LogFunctionCall(className, "broadcastLocationUpdatedNetwork");
		Intent intent = new Intent();
		intent.setAction("com.dagrest.tracklocation.service.TrackLocationService.LOCATION_UPDATED_NETWORK");
		sendBroadcast(intent);
		LogManager.LogFunctionExit(className, "broadcastLocationUpdatedNetwork");
	}

	@Override          
    public void onCreate()          
    {             
        super.onCreate();
        className = this.getClass().getName();
        
    	LogManager.LogFunctionCall(className, "onCreate");
    	Log.i(LOCATION_SERVICE, "onCreate - Start");
    	Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
       
        try{
            LogManager.LogFunctionCall(className, "onCreate");
            if(context == null){
            	context = getApplicationContext();
            }
            if(locationManager == null){
            	locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            toReleaseWakeLock = false;
            
            LogManager.LogFunctionExit(className, "onCreate");
            Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
            Log.i(LOCATION_SERVICE, "onCreate - End");
            
        } catch (Exception e) {
        	LogManager.LogException(e, className, "onCreate");
        	Log.e(LOCATION_SERVICE, "onCreate", e);
        }
	}   
    
    @Override          
    public void onDestroy()           
    {                  
        super.onDestroy();
        try{
        	LogManager.LogFunctionCall(className, "onDestroy");
        	Log.i(LOCATION_SERVICE, "onDestroy - Start");
        	Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
        	
        	//LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
            if(wl != null){ 
            	boolean isHeld = wl.isHeld();
            	Log.i(LOCATION_SERVICE, "WAKE_LOCK - IS HELD: " + isHeld + ";");
                wl.release();
                Log.i(LOCATION_SERVICE, "WAKE_LOCK - IS HELD: " + isHeld + ";");
                LogManager.LogInfoMsg(className, "onDestroy", "WAKE LOCK - HAS BEEN REMOVED.");
                Log.i(LOCATION_SERVICE, "RELEASE: WAKE_LOCK = " + wl + ";");
            }
            
            LogManager.LogFunctionExit(className, "onDestroy");
            Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
            Log.i(LOCATION_SERVICE, "onDestroy - End");
            
        } catch (Exception e) {
            LogManager.LogException(e, className, "onDestroy");
            Log.e(LOCATION_SERVICE, "onDestroy", e);
        }
    }  

//    LocationListener locationListenerGPS = new LocationListenerBasic("LocationListenerGPS", CommonConst.GPS, wl, pm, toReleaseWakeLock);
//	LocationListener locationListenerNetwork = new LocationListenerBasic("LocationListenerNetwork", CommonConst.NETWORK, wl, pm, toReleaseWakeLock);
    
	LocationListener locationListenerGPS = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			onLocationChangedBasic(location, "LocationListenerGPS", CommonConst.GPS, wl, pm, toReleaseWakeLock);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	
	};
	
	LocationListener locationListenerNetwork = new LocationListener() {

		@Override
		public void onLocationChanged(Location location) {
			onLocationChangedBasic(location, "locationListenerNetwork", CommonConst.NETWORK, wl, pm, toReleaseWakeLock);
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		@Override
		public void onProviderEnabled(String provider) {
		}

		@Override
		public void onProviderDisabled(String provider) {
		}
	
	};

	private void onLocationChangedBasic(Location location, String className, String locationProviderType, PowerManager.WakeLock wl, PowerManager pm, boolean toReleaseWakeLock){
        try{
        	
            LogManager.LogFunctionCall(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
            	locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
            Log.i(CommonConst.LOG_TAG, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
            	locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
            Log.i(CommonConst.LOG_TAG, "WAKE_LOCK = " + wl + ";");
            
            // TODO: check if the next key,value is needed...
            // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, locationProviderType);

            double latitude = 0, longitude = 0;
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            if(latitude == 0 || longitude == 0){
            	return;
            }
            float accuracy = location.getAccuracy();
            String locationProviderName = location.getProvider();
            float speed = location.getSpeed();

            // TODO: check if the next key,value is needed...
            // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, locationProviderName);
            
            // Create string = "latitude,longitude,accuracy,speed,time" if a location is provided
            String locationInfo = latitude + CommonConst.DELIMITER_COMMA + 
            	longitude + CommonConst.DELIMITER_COMMA + 
            	accuracy + CommonConst.DELIMITER_COMMA + 
            	speed + CommonConst.DELIMITER_COMMA + 
            	Utils.getCurrentTime();
                    
            LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
            	locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", 
            	CommonConst.LOCATION_INFO_ + locationProviderType + CommonConst.DELIMITER_COLON + locationInfo);
            Preferences.setPreferencesString(context, CommonConst.LOCATION_INFO_ + locationProviderType, locationInfo);
    
            // NOT NEEDED - used the following function controller.sendCommand(jsonMessage); 
            // broadcastLocationUpdatedGps();
            
            // ==========================================
            // send GCM (push notification) to requester
            // ==========================================
			List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(context, 
    				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST); 

			String time = new Date().toString(); 
    		Controller controller = new Controller();

    		// Get current registration ID
    		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
    		String jsonMessage = controller.createJsonMessage(listRegIDs, 
	    		senderRegId, 
	    		CommandEnum.location, 
	    		null, // TODO: send device UUID in the message 
	    		time,
	    		locationProviderType, // key
	    		locationInfo// value	
    		);
    		// send message back with PushNotificationServiceStatusEnum.available
    		controller.sendCommand(jsonMessage);
            // ==============================
            // send GCM to requester
            // ==============================
    		
    		// For very OLD version
            //sendLocationByMail(latlong);

            if(wl != null && wl.isHeld()){
            	LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", "WAKE LOCK - READY TO BE RELEASED.");
            	Log.i(CommonConst.LOG_TAG, "WAKE LOCK - READY TO BE RELEASED.");
//                if(!laDeviceId.equals(preferences.getStringSettingsValue("deviceUid", deviceUid))){
            		toReleaseWakeLock = true;
//                }
    				
            	LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", "BEFORE RELEASE WAKE_LOCK = " + wl + ";");
            	LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", "BEFORE RELEASE WAKE LOCK isHeld: " + wl.isHeld() + ";");
	            Log.i(CommonConst.LOG_TAG, "BEFORE RELEASE WAKE_LOCK = " + wl + ";");
				Log.i(CommonConst.LOG_TAG, "BEFORE RELEASE WAKE LOCK isHeld: " + wl.isHeld() + ";");
            	
				wl.release();
				
				LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", "WAKE LOCK - HAS BEEN RELEASED.");
				Log.i(CommonConst.LOG_TAG, "WAKE LOCK - HAS BEEN RELEASED.");

				LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", "AFTER RELEASE WAKE_LOCK = " + wl + ";");
            	LogManager.LogInfoMsg(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", "AFTER RELEASE WAKE LOCK isHeld: " + wl.isHeld() + ";");
	            Log.i(CommonConst.LOG_TAG, "AFTER RELEASE WAKE_LOCK = " + wl + ";");
				Log.i(CommonConst.LOG_TAG, "AFTER RELEASE WAKE LOCK isHeld: " + wl.isHeld() + ";");
           }
            
            LogManager.LogFunctionCall(className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
            Log.i(CommonConst.LOG_TAG, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");

        } catch (Exception e) {
                LogManager.LogException(e, className, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged");
                Log.e(CommonConst.LOG_TAG, CommonConst.LOCATION_LISTENER + CommonConst.DELIMITER_ARROW + 
                    locationProviderType + CommonConst.DELIMITER_ARROW + "onLocationChanged", e);
        }      
	}
	
	@Override          
	public void onStart(Intent intent, int startId)           
	{                  
		try{
              LogManager.LogFunctionCall(className, "onStart");
              Log.i(LOCATION_SERVICE, "onStart - Start");
              Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
              
              pm = (PowerManager) getSystemService(Context.POWER_SERVICE); 
              wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, CommonConst.TRACK_LOCATION_WAKE_LOCK);
              wl.setReferenceCounted(false);
              Log.i(LOCATION_SERVICE, "PARTIAL_WAKE_LOCK: WAKE_LOCK = " + wl + ";");
              Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
              
              LogManager.LogInfoMsg("LocationNotifierService", "onStart()", "Before if - WAKE LOCK isHeld: " + wl.isHeld());
//              String locProvName = null; 
//              locProvName = Preferences.getPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME);
//              LogManager.LogInfoMsg(className, "onStart", "Location provider name: " + locProvName);
              //if(wl.isHeld() == false && locProvName != null && locProvName.equalsIgnoreCase("gps")){
              if(wl != null && wl.isHeld() == false) {
            	  wl.acquire(); 
            	  LogManager.LogInfoMsg(className, "onStart", "WAKE LOCK - HAS BEEN ACUIRED.");
            	  Log.i(LOCATION_SERVICE, "WAKE LOCK - HAS BEEN ACUIRED = " + wl + ";");
              }
              
              Log.i(LOCATION_SERVICE, "WAKE LOCK - TO RELEASE WAKE LOCK = " + toReleaseWakeLock + ";");
              if(toReleaseWakeLock){
            	  if(wl != null && wl.isHeld()){
            		  wl.release();
            		  Log.i(LOCATION_SERVICE, "RELEASE; WAKE_LOCK = " + wl + ";");
            		  LogManager.LogInfoMsg(className, "onStart", "WAKE LOCK - HAS BEEN RELEASED.");
            		  Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
                      toReleaseWakeLock = false;
            	  }
              }
              Log.i(LOCATION_SERVICE, "WAKE LOCK - HAS BEEN ACUIRED = " + wl + ";");
              
              requestLocation(true);
              isLocationProviderAvailable = Preferences.getPreferencesBoolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE);
              if(isLocationProviderAvailable){
            	  String locationStringGPS = Preferences.getPreferencesString(context, CommonConst.LOCATION_INFO_GPS);
            	  String locationStringNETWORK = Preferences.getPreferencesString(context, CommonConst.LOCATION_INFO_NETWORK);
      
                  if(!locationStringGPS.equals("initial")){
                      LogManager.LogInfoMsg("LocationNotifierService", "onStart()", "locationGPS: " + locationStringGPS);
                      //sendLocationByMail(locationStringGPS, locationProvider);
                      // TODO: send notification
                  } else if(!locationStringNETWORK.equals("initial")){
                      LogManager.LogInfoMsg("LocationNotifierService", "onStart()", "locationNETWORK: " + locationStringNETWORK);
                      //sendLocationByMail(locationStringNETWORK, locationProvider);
                      // TODO: send notification
                  }
              }
              
              LogManager.LogFunctionExit(className, "onStart");
              Log.i(LOCATION_SERVICE, "onStart - End");
		} catch (Exception e) {
			LogManager.LogException(e, className, "onStart");
			LogManager.LogInfoMsg(className, "onStart", e.toString());
		}
	}
	
	private void requestLocation(boolean forceGps) {
        try{
        	LogManager.LogFunctionCall(className, "requestLocation");
			locationManager.removeUpdates(locationListenerGPS);
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerGPS);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                    Preferences.setPreferencesBooolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE, true);
                    //preferences.setStringSettingsValue("locationProviderName", "GPS");
                } else if (containsNetwork) {
                LogManager.LogInfoMsg(className, "requestLocation", "NETWORK_PROVIDER selected.");
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Integer.parseInt(intervalString), 0, locationListenerNetwork);
                    Preferences.setPreferencesBooolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE, true);
                    //preferences.setStringSettingsValue("locationProviderName", "NETWORK");
                }
	        } else {
		        LogManager.LogInfoMsg(className, "requestLocation", "No location providers available.");
		        Preferences.setPreferencesBooolean(context, CommonConst.IS_LOCATION_PROVIDER_AVAILABLE, false);
	        }
        LogManager.LogFunctionExit(className, "requestLocation");
        } catch (Exception e) {
        	LogManager.LogException(e, className, "requestLocation");
        }
    }
    
    private boolean providerAvailable(List<String> providers) {
        if (providers.isEmpty()) {
        	return false;
        }
        return true;
    }
    
//    // Define a listener that responds to location updates
//    LocationListener locationListenerGPS_OLD = new LocationListener() {
//        public void onLocationChanged(Location location) {
//	        try{
//	        	
//                LogManager.LogFunctionCall(className, "locationListenerGPS->onLocationChanged:Start");
//                Log.i(LOCATION_SERVICE, "locationListenerGPS->onLocationChanged:Start");
//                Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
//                
//                // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, CommonConst.GPS);
//
//                double latitude = 0, longitude = 0;
//                latitude = location.getLatitude();
//                longitude = location.getLongitude();
//                if(latitude == 0 || longitude == 0){
//                	return;
//                }
//                float accuracy = location.getAccuracy();
//                String locationProvider = location.getProvider();
//                float speed = location.getSpeed();
//
//                // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, locationProvider);
//                
//                //sets latitude/longitude when a location is provided
//                String locationInfo = latitude + CommonConst.DELIMITER_COMMA + 
//                	longitude + CommonConst.DELIMITER_COMMA + 
//                	accuracy + CommonConst.DELIMITER_COMMA + 
//                	speed + CommonConst.DELIMITER_COMMA + 
//                	Utils.getCurrentTime();
//                        
//                LogManager.LogInfoMsg(className, "locationListenerGPS->onLocationChanged", CommonConst.LOCATION_INFO_GPS + locationInfo);
//                Preferences.setPreferencesString(context, CommonConst.LOCATION_INFO_GPS, locationInfo);
//        
//                broadcastLocationUpdatedGps();
//                
//                // ==============================
//                // send GCM to requester
//                // ==============================
//    			List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(getApplicationContext(), 
//        				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST); 
//
//    			String time = new Date().toString(); 
//        		Controller controller = new Controller();
//
//        		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//        		String jsonMessage = controller.createJsonMessage(listRegIDs, 
//    	    		senderRegId, 
//    	    		CommandEnum.location, 
//    	    		null, 
//    	    		time,
//    	    		"GPS", // key
//    	    		locationInfo// value	
//        		);
//        		// send message back with PushNotificationServiceStatusEnum.available
//        		controller.sendCommand(jsonMessage);
//                // ==============================
//                // send GCM to requester
//                // ==============================
//        		
//                //sendLocationByMail(latlong);
//
////                String laDeviceId = preferences.getStringSettingsValue("laDeviceId", "004999010640000");
////                String deviceUid = null;
//                if(wl != null && wl.isHeld()){
//                	LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "WAKE LOCK - READY TO BE RELEASED.");
//                	Log.i(LOCATION_SERVICE, "WAKE LOCK - READY TO BE RELEASED.");
////                    if(!laDeviceId.equals(preferences.getStringSettingsValue("deviceUid", deviceUid))){
//                		toReleaseWakeLock = true;
////                    }
//                	LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "WAKE LOCK isHeld: " + wl.isHeld());
//					wl.release();
//					Log.i(LOCATION_SERVICE, "WAKE LOCK - RELEASE.");
//					Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
//					LogManager.LogInfoMsg("locationListenerGPS", "onLocationChanged()", "WAKE LOCK - HAS BEEN RELEASED.");
//                }
//                
//                LogManager.LogFunctionCall(className, "locationListenerGPS->onLocationChanged:End");
//                Log.i(LOCATION_SERVICE, "locationListenerGPS->onLocationChanged:End");
//                Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
//
//	        } catch (Exception e) {
//	                LogManager.LogException(e, className, "locationListenerGPS->onLocationChanged");
//	                Log.e(LOCATION_SERVICE, "locationListenerGPS->onLocationChanged", e);
//	        }      
//        }
//
//	    public void onStatusChanged(String provider, int status, Bundle extras) {}
//	    public void onProviderEnabled(String provider) {}
//	    public void onProviderDisabled(String provider) {}
//    };
//
//    // Define a listener that responds to location updates
//	LocationListener locationListenerNetwork_OLD = new LocationListener() {
//        public void onLocationChanged(Location location) {
//			try{
//                
//				LogManager.LogFunctionCall(className, "locationListenerNetwork->onLocationChanged:Start");
//                Log.i(LOCATION_SERVICE, "locationListenerNetwork->onLocationChanged:Start");
//                Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
//
//                // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, CommonConst.NETWORK);
//                
//                double latitude = 0, longitude = 0;
//                latitude = location.getLatitude();
//                longitude = location.getLongitude();
//                if(latitude == 0 || longitude == 0){
//                        return;
//                }
//                float accuracy = location.getAccuracy();
//                String locationProvider = location.getProvider();
//                float speed = location.getSpeed();
//                
//                // Preferences.setPreferencesString(context, CommonConst.LOCATION_PROVIDER_NAME, locationProvider);
//
//                //sets latitude/longitude when a location is provided
//                String locationInfo = latitude + CommonConst.DELIMITER_COMMA + 
//                	longitude + CommonConst.DELIMITER_COMMA + 
//                	accuracy + CommonConst.DELIMITER_COMMA + 
//                	speed + CommonConst.DELIMITER_COMMA + 
//                	Utils.getCurrentTime();
//                        
//                LogManager.LogInfoMsg(className, "locationListenerNetwork->onLocationChanged", CommonConst.LOCATION_INFO_NETWORK + locationInfo);
//                Preferences.setPreferencesString(context, CommonConst.LOCATION_INFO_NETWORK, locationInfo);
//                broadcastLocationUpdatedNetwork();
//                
//                // ==============================
//                // send GCM to requester
//                // ==============================
//    			List<String> listRegIDs = Preferences.getPreferencesReturnToRegIDList(getApplicationContext(), 
//        				CommonConst.PREFERENCES_RETURN_TO_REG_ID_LIST); 
//
//    			String time = new Date().toString(); 
//        		Controller controller = new Controller();
//
//        		String senderRegId = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_REG_ID);
//        		String jsonMessage = controller.createJsonMessage(listRegIDs, 
//    	    		senderRegId, 
//    	    		CommandEnum.location, 
//    	    		null, 
//    	    		time,
//    	    		"NETWORK", // key
//    	    		locationInfo// value	
//        		);
//        		// send message back with PushNotificationServiceStatusEnum.available
//        		controller.sendCommand(jsonMessage);
//                // ==============================
//                // send GCM to requester
//                // ==============================
//
//        		
//        		if(wl != null && wl.isHeld()){
//                	LogManager.LogInfoMsg("locationListenerNetwork", "onLocationChanged()", "WAKE LOCK - READY TO BE RELEASED.");
//                	Log.i(LOCATION_SERVICE, "WAKE LOCK - READY TO BE RELEASED.");
////                    if(!laDeviceId.equals(preferences.getStringSettingsValue("deviceUid", deviceUid))){
//                		toReleaseWakeLock = true;
////                    }
//                	LogManager.LogInfoMsg("locationListenerNetwork", "onLocationChanged()", "WAKE LOCK isHeld: " + wl.isHeld());
//					wl.release();
//					Log.i(LOCATION_SERVICE, "WAKE LOCK - RELEASE.");
//					Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
//					LogManager.LogInfoMsg("locationListenerNetwork", "onLocationChanged()", "WAKE LOCK - HAS BEEN RELEASED.");
//                }
//
//                LogManager.LogFunctionCall(className, "locationListenerNetwork->onLocationChanged:End");
//                Log.i(LOCATION_SERVICE, "locationListenerNetwork->onLocationChanged:End");
//                Log.i(LOCATION_SERVICE, "WAKE_LOCK = " + wl + ";");
//
//            } catch (Exception e) {
//                LogManager.LogException(e, className, "locationListenerNetwork->onLocationChanged");
//                Log.e(LOCATION_SERVICE, "locationListenerNetwork->onLocationChanged", e);
//            }
//        }
//        public void onStatusChanged(String provider, int status, Bundle extras) {}
//        public void onProviderEnabled(String provider) {}
//        public void onProviderDisabled(String provider) {}
//      };

}

