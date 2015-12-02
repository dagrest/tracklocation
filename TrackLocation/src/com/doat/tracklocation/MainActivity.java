package com.doat.tracklocation;

import com.doat.tracklocation.R;
import com.doat.tracklocation.controller.MainActivityController;
import com.doat.tracklocation.datatype.BackupDataOperations;
import com.doat.tracklocation.db.DBHelper;
import com.doat.tracklocation.db.DBLayer;
import com.doat.tracklocation.db.DBManager;
import com.doat.tracklocation.dialog.InfoDialog;
import com.doat.tracklocation.log.LogManager;
import com.doat.tracklocation.model.MainModel;
import com.doat.tracklocation.utils.CommonConst;
import com.doat.tracklocation.utils.Preferences;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends BaseActivity {
    private final static int JOIN_REQUEST = 1;      
    protected MainActivityController mainActivityController;
    protected MainModel mainModel;
    
    public static volatile boolean isTrackLocationRunning; // Used in SMSReceiver.class

    
    public MainActivityController getMainActivityController() {
		return mainActivityController;
	}

	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		className = this.getClass().getName();
		methodName = "onCreate";
		
		LogManager.LogActivityCreate(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_CREATE] {" + className + "} -> " + methodName);
		
		setContentView(R.layout.activity_main);
				
		isTrackLocationRunning = true;
		
		Context context = getApplicationContext();
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
			.setSmallIcon(R.drawable.main_icon_96)
			.setContentTitle(getResources().getString(R.string.app_name));      

		Intent intent = new Intent( context, MainActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(context, 1 , intent, 0);
		builder.setContentIntent(pIntent);
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notif = builder.build();
		mNotificationManager.notify(1, notif);
    }

	@Override
	protected void onStart() {
		super.onStart();
		if(broadcastReceiver == null){
			broadcastReceiver = new BroadcastReceiverBase(MainActivity.this);
		}
		initNotificationBroadcastReceiver(broadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		DBManager.initDBManagerInstance(new DBHelper(context));
		if(mainModel == null){
			mainModel = new MainModel();
		}
		if(mainActivityController == null){
			mainActivityController = new MainActivityController(this, context);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		methodName = "onPause";

        BackupDataOperations backupData = new BackupDataOperations();
		boolean isBackUpSuccess = backupData.backUp();
		if(isBackUpSuccess != true){
			logMessage = methodName + " -> Backup process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + methodName + ": " + logMessage);
		}
    }

    @Override
	protected void onStop() {
		super.onStop();
		
        if(broadcastReceiver != null){
    		unregisterReceiver(broadcastReceiver);
    	}
        
     	Thread registerToGCMInBackgroundThread = 
         	mainActivityController.getRegisterToGCMInBackgroundThread();
    	if(registerToGCMInBackgroundThread != null){
    		registerToGCMInBackgroundThread.interrupt();
    	}

		BackupDataOperations backupData = new BackupDataOperations();
		boolean isBackUpSuccess = backupData.backUp();
		if(isBackUpSuccess != true){
			logMessage = methodName + " -> Backup process failed.";
			LogManager.LogErrorMsg(className, methodName, logMessage);
			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
		}
	}

	@Override
    protected void onDestroy() {
        super.onDestroy();
        methodName = "onDestroy";
        
        isTrackLocationRunning = false;
		
		
		LogManager.LogActivityDestroy(className, methodName);
		Log.i(CommonConst.LOG_TAG, "[ACTIVITY_DESTROY] {" + className + "} -> " + methodName);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    public void onClick(final View view) {
    	if (view == findViewById(R.id.btnLocate) || view == findViewById(R.id.btnLocationSharing) || view == findViewById(R.id.btnTracking) ){
    		mainModel.setContactDeviceDataList(DBLayer.getInstance().getContactDeviceDataList(null));
    	}
    	
    	// ========================================
    	// ABOUT button
    	// ========================================
        if (view == findViewById(R.id.btnAbout)) {        	
//        	showAboutDialog();
        	String title = "About";
        	String dialogMessage = String.format(getResources().getString(R.string.about_dialog_text), 
        		Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME));
        	new InfoDialog(this, context, title, dialogMessage, null);
        	
    	// ========================================
    	// JOIN button
    	// ========================================
        } else if (view == findViewById(R.id.btnJoin)) {
        	String account = Preferences.getPreferencesString(context, CommonConst.PREFERENCES_PHONE_ACCOUNT);
        	if(account == null || account.isEmpty()){
    	    	Toast.makeText(MainActivity.this, "Please register your application.\nPress Locate button at first.", 
        	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogErrorMsg(className, "onClick -> JOIN button", 
            		"Unable to join contacts - application is not registred yet.");
        	} else {
        		Intent joinContactListIntent = new Intent(this, JoinContactListActivity.class);
        		//Intent joinContactListIntent = new Intent(this, JoinContactListActivity.class);
        		startActivityForResult(joinContactListIntent, JOIN_REQUEST);
        	}

// 			*********************************************************************************        	
//		    // Start an activity for the user to pick a phone number from contacts
//		    Intent intent = new Intent(Intent.ACTION_PICK);
//		    intent.setType(CommonDataKinds.Phone.CONTENT_TYPE);
//		    if (intent.resolveActivity(getPackageManager()) != null) {
//		        startActivityForResult(intent, CommonConst.REQUEST_SELECT_PHONE_NUMBER);
//		    }
// 			*********************************************************************************        	
			
    	// ========================================
    	// SETTINGS button
    	// ========================================
        } else if (view == findViewById(R.id.btnSettings)) {	
    		Intent settingsIntent = new Intent(this, SettingsActivity.class);
    		startActivityForResult(settingsIntent, 2); 

    	// ========================================
    	// LOCATE button (CONTACT_LIST)
    	// ========================================
        } else if (view == findViewById(R.id.btnLocate)) {
    		LogManager.LogInfoMsg(className, "onClick -> Locate button", 
    			"ContactList activity started.");
    		
    		if(mainModel.getContactDeviceDataList() != null){
	    		Intent intentContactList = new Intent(this, ContactListActivity.class);
	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, mainModel.getContactDeviceDataList());
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogInfoMsg(className, "onClick -> LOCATE button", 
                	"There is no any contact. Some contact must be joined at first.");
    		}
    	// ========================================
    	// LOCATION SHARING MANAGEMENT button
    	// ========================================
        } else if (view == findViewById(R.id.btnLocationSharing)) {
    		LogManager.LogInfoMsg(className, "onClick -> Location Sharing Management button", 
    			"ContactList activity started.");
    		    		
    		if(mainModel.getContactDeviceDataList() != null){
	    		Intent intentContactList = new Intent(this, LocationSharingListActivity.class);
	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, mainModel.getContactDeviceDataList());
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogInfoMsg(className, "onClick -> LOCATION SHARING MANAGEMENT button", 
                    "There is no any contact. Some contact must be joined at first.");
    		}
    	// ========================================
    	// TRACKING button (TRACKING_CONTACT_LIST)
    	// ========================================
        } else if (view == findViewById(R.id.btnTracking)) {
    		LogManager.LogInfoMsg(className, "onClick -> Tracking button", 
    			"TrackingList activity started.");
    		    		
    		if(mainModel.getContactDeviceDataList() != null){
	    		Intent intentContactList = new Intent(this, TrackingListActivity.class);
	    		intentContactList.putParcelableArrayListExtra(CommonConst.CONTACT_DEVICE_DATA_LIST, mainModel.getContactDeviceDataList());
	    		startActivity(intentContactList);
    		} else {
    	    	Toast.makeText(MainActivity.this, "There is no any contact.\nJoin some contact at first.", 
    	    		Toast.LENGTH_SHORT).show();
        		LogManager.LogInfoMsg(className, "onClick -> TRACKING button", 
                    "There is no any contact. Some contact must be joined at first.");
    		}
        }
    }
    
//	// Initialize BROADCAST_MESSAGE broadcast receiver
//	private void initNotificationBroadcastReceiver() {
//		methodName = "initNotificationBroadcastReceiver";
//		LogManager.LogFunctionCall(className, methodName);
//	    IntentFilter intentFilter = new IntentFilter();
//	    intentFilter.addAction(BroadcastActionEnum.BROADCAST_MESSAGE.toString());
//	    notificationBroadcastReceiver = new BroadcastReceiver() {
//
//	    	Gson gson = new Gson();
//	    	
//			@Override
//			public void onReceive(Context context, Intent intent) {
//				methodName = "onReceive";
//				Bundle bundle = intent.getExtras();
//	    		if(bundle != null && bundle.containsKey(BroadcastConstEnum.data.toString())){
//	    			String jsonNotificationData = bundle.getString(BroadcastConstEnum.data.toString());
//	    			if(jsonNotificationData == null || jsonNotificationData.isEmpty()){
//	    				return;
//	    			}
//	    			NotificationBroadcastData broadcastData = gson.fromJson(jsonNotificationData, NotificationBroadcastData.class);
//	    			if(broadcastData == null){
//	    				return;
//	    			}
//	    			
//	    			String key  = broadcastData.getKey();
//
//    				// Notification about command: bring to top - to foreground
//	    			// bring MainActivity to foreground
//	    			if(BroadcastKeyEnum.join_sms.toString().equals(key)) {
//	    				showApproveJoinRequestDialog(broadcastData); // bring to foreground
//
//	    				SMSUtils.checkJoinRequestBySMSInBackground(context, MainActivity.this, true);
//	    			}
//	    		}
//			}
//	    };
//	    
//	    registerReceiver(notificationBroadcastReceiver, intentFilter);
//	    
//		LogManager.LogFunctionExit(className, methodName);
//	}
//
//	private void showApproveJoinRequestDialog(NotificationBroadcastData broadcastData){
//		methodName = "showApproveJoinRequestDialog";
//		
//		//  SMS Message values:
//		//	[0] - smsMessageKey - "JOIN_TRACK_LOCATION"
//		//	[1] - regIdFromSMS
//		//	[2] - mutualIdFromSMS
//		//	[3] - phoneNumberFromSMS
//		//	[4] - accountFromSMS
//		//	[5] - macAddressFromSMS
//		String smsMessageText = broadcastData.getValue();
//		
////		List<String> listSmsVals = new ArrayList<String>();
////		StringTokenizer st = new StringTokenizer(smsMessage, ",");
////		while(st.hasMoreElements()){
////			listSmsVals.add(st.nextToken().trim());
////		}
//		String smsVals[] = smsMessageText.split(",");
//		if(smsVals.length == CommonConst.JOIN_SMS_PARAMS_NUMBER){ // should be exactly 6 values
//			//Controller.showApproveJoinRequestDialog(this, 
////			mainActivityController.showApproveJoinRequestDialog(this,
////				context, 
////				smsVals[4].trim(), 	// accountFromSMS
////				smsVals[3].trim(), 	// phoneNumberFromSMS
////				smsVals[2].trim(), 	// mutualIdFromSMS 
////				smsVals[1].trim(), 	// regIdFromSMS
////				smsVals[5].trim(),	// macAddressFromSMS
////				null
////			);
//			
//			ApproveJoinRequestContext approveJoinRequestContext = 
//				new ApproveJoinRequestContext(context, smsVals[2].trim());
//			
//			ApproveJoinRequestDialogListener approveJoinRequestDialogListener = 
//				new ApproveJoinRequestDialogListener(approveJoinRequestContext, null);
//			
//			ApproveJoinRequestDialog approveJoinRequestDialog = 
//				new ApproveJoinRequestDialog(MainActivity.this, context, approveJoinRequestDialogListener);
//			approveJoinRequestDialog.showApproveJoinRequestDialog(
//					this, 
//					context, 
//					smsVals[4].trim(), 	// accountFromSMS
//					smsVals[3].trim(), 	// phoneNumberFromSMS 
//					smsVals[2].trim(), 	// mutualIdFromSMS  
//					smsVals[1].trim(), 	// regIdFromSMS 
//					smsVals[5].trim(),	// macAddressFromSMS 
//					null);
//		} else {
//			logMessage = "JOIN SMS Message has incorrect parameters number" +
//				" - supposed to be: " + CommonConst.JOIN_SMS_PARAMS_NUMBER;
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		
//			logMessage = methodName + " -> Backup process failed.";
//			LogManager.LogErrorMsg(className, methodName, logMessage);
//			Log.e(CommonConst.LOG_TAG, "[ERROR] {" + className + "} -> " + logMessage);
//		}
//	}

/*	
	private void showAboutDialog() {
    	String dialogMessage = 
    		String.format(getResources().getString(R.string.about_dialog_text), 
    			Preferences.getPreferencesString(context, CommonConst.PREFERENCES_VERSION_NAME));
    	
		CommonDialog aboutDialog = new CommonDialog(this, dialogActionsAboutDialog);
		aboutDialog.setDialogMessage(dialogMessage);
		aboutDialog.setDialogTitle("About");
		aboutDialog.setPositiveButtonText("OK");
		aboutDialog.setStyle(CommonConst.STYLE_NORMAL, 0);
		aboutDialog.showDialog();
		aboutDialog.setCancelable(true);
    }
*/	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==2){
			// Make sure the request was successful
	        if (resultCode == RESULT_OK) {	 
	        	if (data != null && data.getExtras().getBoolean(CommonConst.THEME_CHANGED)){
	        		Intent i = new Intent(this, MainActivity.class);
	                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
	                startActivity(i);
	        	}
	       }
		}
	}

/*	
	IDialogOnClickAction dialogActionsAboutDialog = new IDialogOnClickAction() {
		@Override
		public void doOnPositiveButton() {
		}
		@Override
		public void doOnNegativeButton() {
		}
		@Override
		public void setActivity(Activity activity) {
		}
		@Override
		public void setContext(Context context) {
		}
		@Override
		public void setParams(Object[]... objects) {
		}
		@Override
		public void doOnChooseItem(int which) {
		}
	};
*/	
}
