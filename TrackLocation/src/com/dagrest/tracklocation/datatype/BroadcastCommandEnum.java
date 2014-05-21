package com.dagrest.tracklocation.datatype;

import java.util.HashMap;
import java.util.Map;

public enum BroadcastCommandEnum {
	location_updated("location_updated"), gcm_status("gcm_status"), location_service_status("location_service_status");
	
	private final String name;       
	private static Map<String, BroadcastCommandEnum> valueMap;
	
    private BroadcastCommandEnum(String s) {
        name = s;
    }

    public boolean equalsName(String otherName){
        return (otherName == null)? false:name.equals(otherName);
    } 

    public String toString(){
       return name;
    }
    
    public static BroadcastCommandEnum getValue(String value){
    	if (valueMap == null)
        {
            valueMap = new HashMap<String, BroadcastCommandEnum>();
            for(BroadcastCommandEnum provider: values())
                valueMap.put(provider.toString(), provider);
        }
    	
        return valueMap.get(value);
    }
}