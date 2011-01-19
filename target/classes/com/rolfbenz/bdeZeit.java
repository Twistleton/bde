package com.rolfbenz;

import java.text.SimpleDateFormat;

public class bdeZeit {
    public bdeZeit() {	 
    }    
    public static String  getTimestamp(String iFormat) {
       	java.util.Date datum = new java.util.Date();
	String     timestamp = new String();	
	timestamp = new  SimpleDateFormat(iFormat).format(datum);      
	return timestamp;
    }
}
