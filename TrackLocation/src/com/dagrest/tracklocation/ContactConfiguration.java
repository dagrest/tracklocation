package com.dagrest.tracklocation;

import java.util.ArrayList;
import java.util.List;

import com.dagrest.tracklocation.datatype.CommandEnum;
import com.dagrest.tracklocation.datatype.ContactData;
import com.dagrest.tracklocation.datatype.ContactDeviceData;
import com.dagrest.tracklocation.datatype.ContactDeviceDataList;
import com.dagrest.tracklocation.datatype.DeviceData;
import com.dagrest.tracklocation.datatype.NotificationCommandEnum;
import com.dagrest.tracklocation.datatype.PushNotificationServiceStatusEnum;
import com.dagrest.tracklocation.datatype.TrackLocationServiceStatusEnum;
import com.dagrest.tracklocation.http.HttpUtils;
import com.dagrest.tracklocation.log.LogManager;
import com.dagrest.tracklocation.utils.CommonConst;
import com.dagrest.tracklocation.utils.Preferences;
import com.dagrest.tracklocation.utils.Utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class ContactConfiguration extends Activity {

	private static final String DEVICE_STATUS_DEFAULT = "N/A";
	private static final String NOTIFICATION_DEFAULT = "N/A";
	private static final String LAT_DEFAULT = "N/A";
	private static final String LNG_DEFAULT = "N/A";
	
	private TextView mUserName;
	private TextView mEmail;
	private TextView mDeviceName;
	private TextView mDeviceType;
	private TextView mStatus;
	private TextView mNotification;
	private TextView mLat;
	private TextView mLng;
	
	private String deviceStatus;
	private String notification;
	private String lat;
	private String lng;

	private ContactDeviceDataList contactDeviceDataList;
	private ContactDeviceData contactDeviceData;
	private ContactData contactData;
	private DeviceData deviceData;
	
	private BroadcastReceiver GcmIntentServiceChangeWatcher;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.contact_config);
		initGcmIntentServiceWatcher();
		
		Intent intent = getIntent();
		String jsonStringContactDeviceData = intent.getExtras().getString(CommonConst.JSON_STRING_CONTACT_DEVICE_DATA);
		String userName = intent.getExtras().getString(CommonConst.CONTACT_LIST_SELECTED_VALUE);

		contactDeviceDataList = Utils.fillContactDeviceDataFromJSON(jsonStringContactDeviceData);
		if(contactDeviceDataList == null){
			return;
		}
		contactDeviceData = Utils.getContactDeviceDataByUsername(contactDeviceDataList, userName);
		if(contactDeviceData == null){
			return;
		}
		
		contactData = contactDeviceData.getContactData();
		if(contactData == null){
			return;
		}
		deviceData = contactDeviceData.getDeviceData();
		if(deviceData == null){
			return;
		}
		
		mUserName = (TextView) findViewById(R.id.username);
		mEmail = (TextView) findViewById(R.id.email);
		mDeviceName = (TextView) findViewById(R.id.devicename);
		mDeviceType = (TextView) findViewById(R.id.devicetype);
		mStatus = (TextView) findViewById(R.id.status);
		mNotification = (TextView) findViewById(R.id.notification);
		mLat = (TextView) findViewById(R.id.lat);
		mLng = (TextView) findViewById(R.id.lng);

		deviceStatus = mStatus.getText().toString();
		notification = mNotification.getText().toString();
		lat = mLat.getText().toString();
		lng = mLng.getText().toString();

		if(deviceStatus == null || deviceStatus.isEmpty()){
			mStatus.setText(DEVICE_STATUS_DEFAULT);
		}
//		} else {
//			mStatus.setText(deviceStatus);
//		}
		
		if(notification == null || notification.isEmpty()){
			mNotification.setText(NOTIFICATION_DEFAULT);
		}
//		} else {
//			mNotification.setText(notification);
//		}
		
		if(lat == null || lat.isEmpty()){
			mLat.setText(LAT_DEFAULT);
		}
//		} else {
//			mLat.setText(lat);
//		}

		if(lng == null || lng.isEmpty()){
			mLng.setText(LNG_DEFAULT);
		}
//		} else {
//			mLng.setText(lng);
//		}
		
		mUserName.setText(contactData.getUsername());
		mEmail.setText(contactData.getEmail());
		mDeviceName.setText(deviceData.getDeviceName());
		mDeviceType.setText(deviceData.getDeviceTypeEnum().toString());
		
	}

    public void onClick(final View view) {

    	String jsonMessage = "";
    	Controller controller = new Controller();
    	switch(view.getId()) {
        	case R.id.check_status:
        		String regIDToReturnMessageTo = Controller.getRegistrationId(getApplicationContext());
        		List<String> listRegIDs = new ArrayList<String>();
        		listRegIDs.add(contactData.getRegistration_id());
        		String time = "";
        		String messageString = "";
//        		TrackLocationServiceStatusEnum trackLocationServiceStatus = null;
//        		PushNotificationServiceStatusEnum pushNotificationServiceStatus = null;
        		jsonMessage = controller.createJsonMessage(listRegIDs, 
        	    		regIDToReturnMessageTo, 
        	    		CommandEnum.status_request, 
        	    		"", // messageString, 
        	    		Controller.getCurrentDate(), // time,
//        	    		trackLocationServiceStatus,
//        	    		pushNotificationServiceStatus,
        	    		null, //NotificationCommandEnum.pushNotificationServiceStatus.toString(),
        	    		null //PushNotificationServiceStatusEnum.available.toString()
        				);
        		//HttpUtils.sendRegistrationIdToBackend(jsonMessage);
        		controller.sendCommand(jsonMessage);
        	break;
        	case R.id.start:
        		//String toId = getRegistrationId(getApplicationContext());
        	break;
        	case R.id.stop:
        		//String toId = getRegistrationId(getApplicationContext());
        	break;
        	case R.id.show_map:
        		//String toId = getRegistrationId(getApplicationContext());
        	break;
    	}
    	
    }
    
    private void initGcmIntentServiceWatcher()
    {
    	LogManager.LogFunctionCall("ContactConfiguration", "initGcmIntentServiceWatcher");
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction("com.dagrest.tracklocation.service.GcmIntentService.GCM_UPDATED");
	    GcmIntentServiceChangeWatcher = new BroadcastReceiver() 
	    {
	    	@Override
    		public void onReceive(Context context, Intent intent) {
    			// TODO Auto-generated method stub
	    		LogManager.LogInfoMsg("ContactConfiguration", "initGcmIntentServiceWatcher->onReceive", "WORK");
	    		String result = intent.getExtras().getString("updated");
	    		mNotification.setText(result);
    		}
	    };
	    registerReceiver(GcmIntentServiceChangeWatcher, intentFilter);
	    LogManager.LogFunctionExit("ContactConfiguration", "initGcmIntentServiceWatcher");
    }
    
}

