package com.doat.tracklocation.utils;

import java.util.Date;
import java.util.Map;
import java.util.TimerTask;

import android.content.Context;
import android.util.Log;

import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.service.TrackLocationService;

public class TrackingTimerJob extends TimerTask {

	private String className;
	private String methodName;
	private String logMessage;
	private Context context;
//	TrackLocationService trackLocationService;
//	
//	public void setTrackLocationServiceObject(TrackLocationService trackLocationService){
//		this.trackLocationService = trackLocationService;
//	}
	
	public TrackingTimerJob(Context context) {
		super();
		this.context = context;
	}
	
	@Override
	public void run() {
		className = this.getClass().getName();
		methodName = "run";
//		// Get start time (keep active request time)
//		long trackLocationServiceStartTime = trackLocationService.getTrackLocationServiceStartTime();
		// Get current time
		long currentTime = System.currentTimeMillis();
		logMessage = "TRACKING TIMER JOB: " + new Date().toString();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
		Map<String, String> map = Preferences.getAccountRegIdMap(context, 
			CommonConst.PREFERENCES_LOCATION_REQUESTER_MAP__ACCOUNT_AND_REG_ID);
		logMessage = "Return contacts to: " + map.toString();
		LogManager.LogInfoMsg(className, methodName, logMessage);
		Log.i(CommonConst.LOG_TAG, "[INFO] {" + className + "} -> " + logMessage);
		
//		if(currentTime - trackLocationServiceStartTime > CommonConst.REPEAT_PERIOD_DEFAULT){
//			trackLocationService.stopTrackLocationService();
//        	Log.i(CommonConst.LOG_TAG, "Timer with TimerJob stoped TrackLocationService");
//		}
	}

}
