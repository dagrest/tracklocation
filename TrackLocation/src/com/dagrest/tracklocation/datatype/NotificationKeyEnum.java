package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum NotificationKeyEnum {
	pushNotificationServiceStatus("pushNotificationServiceStatus"), 
	trackLocationServiceStatus("trackLocationServiceStatus"),
	joinRequestApprovalMutualId("joinRequestApprovalMutualId");
	
	private final String name;       
	private static Map<String, NotificationKeyEnum> valueMap;
	
    private NotificationKeyEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static NotificationKeyEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, NotificationKeyEnum>();
            for(NotificationKeyEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}