package com.rolfbenz;

public class rbTextFormat {
    public static String format(char iPaddChar,int iLength,String iString) {
	String retString = new String();
	if (iString==null) iString="";	 
	int i=0;
	while( i < iLength) {
	    if (i < (iLength-iString.length()) ) {
		retString += iPaddChar;
	    } else {
		retString += iString.charAt(i-(iLength-iString.length()));
	    }
	    i++;				      				
	}
	return (retString);
    }
    public static String format(char iPaddChar,int iLength,int iInt) {		
	return (format(iPaddChar,iLength,String.valueOf(iInt)));	
    }
    public static String format(char iPaddChar,int iLength,long iLong) {		
	return (format(iPaddChar,iLength,String.valueOf(iLong)));	
    }
    public static String format(char iPaddChar,int iLength,char iChar) {		
	return (format(iPaddChar,iLength,String.valueOf(iChar)));	
    }
    public static String linksb(char iPaddChar,int iLength,String iString) {
	String retString = new String();
	if (iString==null) iString="";	 
	int i = iLength-iString.length();
	retString = iString;
	while (i>0) {
	    retString += iPaddChar;
	    i--;
	}				
	return retString;
    }
}
	
