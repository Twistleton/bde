package com.rolfbenz;

import java.util.*;

public class bdeConfigDatei extends configDatei {

	private String vomTddDir  = new String();
	private String zumHostDir = new String();
	private String hostEndung = new String();
	private String zumMLDir   = new String();
	private String mlEndung   = new String();
	private String dbname     = new String("");
	private String dbip       = new String("");
	private int    dbport     = 0;
	private String dbuser     = new String("");
	private String dbpass     = new String("");
	private String errorDir   = new String("");
	private String nachTddDir = new String("");
	private String fafDir     = new String("");
	private String naeDrucker = new String("");
	private String vomNaeDir  = new String("");
	private String napDir     = new String("");
	private String sesselDrucker= new String("");

	public bdeConfigDatei() {
	}
	public bdeConfigDatei(String dname) {
		super(dname);
	}
	public String getNaeDrucker() {
		return naeDrucker;
	}
	public String getVomNaeDir() {
		return vomNaeDir;
	}
	public String getNapDir() {
		return napDir;
	}
	public String getVomTddDir() {
		return vomTddDir;
	}
	public String getZumHostDir() {
		return zumHostDir;
	}
	public String getHostEndung() {
		return hostEndung;
	}
	public String getZumMLDir() {
		return zumMLDir;
	}
	public String getMlEndung() {
		return mlEndung;
	}
	public String getDbName() {
		return dbname;
	}
	public String getDbIp() {
		return dbip;
	}
	public String getDbUser() {
		return dbuser;
	}
	public int getDbPort() {
		return dbport;
	}
	public String getDbPass() {
		return dbpass;
	}
	public String getErrorDir() {
		return errorDir;
	}
	public String getNachTddDir() {
		return nachTddDir;
	}
	public String getFafDir() {
		return fafDir;
	}
	public String getSesselDrucker() {
		return sesselDrucker;
	}	
	public String verarbeiten() {
		super.einlesen();
		String retString = new String();
		
		// Auswerten der Config-Datei
		StringTokenizer st = new StringTokenizer(inhalt,"\n");
		String aktToken = new String();
		hostEndung = ".P01";
		zumMLDir   = "/tmp/bde";
		mlEndung   = ".00";
		while (st.hasMoreTokens()) {
			aktToken=st.nextToken();
			if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("dbname")==0) {
				dbname = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("dbip")==0) {
				dbip =  aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("dbport")==0) {
				try {
					dbport = Integer.parseInt(aktToken.substring(aktToken.indexOf(":")+1));
				}
				catch(NumberFormatException nfe) {
					retString += "DB-Port muss vom Typ Integer sein\n";
				}
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("dbuser")==0) {
                                dbuser = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("dbpass")==0) {
				dbpass = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("zumhostdir")==0) {
				zumHostDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("vomtdddir")==0) {
				vomTddDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("errordir")==0) {
				errorDir  = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("zumhostdir")==0) {
				nachTddDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("hostendung")==0) {
				hostEndung = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("zummldir")==0) {
				zumMLDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("mlendung")==0) {
				mlEndung = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("fafdir")==0) {
				fafDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("naedrucker")==0) {
				naeDrucker = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("nachtdddir")==0) {
				nachTddDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("vomnaedir")==0) {
				vomNaeDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("napdir")==0) {
				napDir = aktToken.substring(aktToken.indexOf(":")+1);
			} else if (aktToken.substring(0,aktToken.indexOf(":")).compareTo("sesselDrucker")==0) {
				sesselDrucker = aktToken.substring(aktToken.indexOf(":")+1);
			} else {
				retString +="!! - Unbekannter Parameter:"+aktToken.substring(0,aktToken.indexOf(":"))+"\n";
			}
		}
		return retString;
	}
}
