package com.rolfbenz;

import java.sql.*;

public class bdeDb {
    private Connection bdeConnection;
    private String     benutzer;
    private String     passwort;
    private String     host;
    private String     database;

    public bdeDb() {
    }
    // Konstruktor fuer den Fall, dass noch gar nichts da ist
    public bdeDb(String pfadZuConfigDatei) {
    
    }    
    public bdeDb(String iHost, String iDatabase, String iBenutzer, String iPasswort, int iPortNr) {
	benutzer = iBenutzer;
	passwort = iPasswort;
	host     = iHost;
	database = iDatabase;
	try {
        Class.forName("oracle.jdbc.driver.OracleDriver");
//	    DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        bdeConnection = DriverManager.getConnection("jdbc:oracle:thin:@" + host + ":" + iPortNr + ":" + database, benutzer, passwort);
    }
	catch (Exception e) {
	    e.printStackTrace();
	}
    }    
    public void close() {
	try {
	    bdeConnection.close();
	}
	catch (Exception e) {
	    e.printStackTrace();
	}
    }
    public Connection getConnection() {
	return bdeConnection;
    }
    public String getHtmlSelectAllg(String iName,String iSql,String iOption) {
	Statement stmt;
	ResultSet rs;
	ResultSetMetaData  rsmd;
	String retString = new String();
	
	retString = "\n<SELECT NAME=\"" + iName + "\">";
	retString += iOption;
	try {
	    stmt = bdeConnection.createStatement();
	    stmt.executeQuery(iSql);
	    rs = stmt.getResultSet();
	    rsmd = rs.getMetaData();
	    while (rs.next()) {
		// Der Wert des Auswahlfeldes wird durch das Letzte Feld des Selektierten Datensatzes bestimmt
		retString += "\n<OPTION VALUE=\"" + rs.getString(rsmd.getColumnCount()) +"\">";
		// Alle Felder bis auf das Letzte werden im Auswahlfeld angezeigt...
		for  (int lauf=1;lauf<rsmd.getColumnCount();lauf++) {
		    retString += rs.getString(lauf) + " ";
		}
	    }
	    rs.close();
	    stmt.close();
	}
	catch (SQLException sqlEx) {
	    sqlEx.printStackTrace();
	    retString += "<OPTION> Methode getHtmlSelectAllg: Fehler beim Datenbankzugriff";
	}	
	retString += "</SELECT>\n";
	return retString;
    }
    public String getHtmlValueSelect(String iName, String iZusatz, String iTable, String iFeldValue, String iFeldShow) {
	String sql = "SELECT " + iZusatz +" " + iFeldValue + "," + iFeldShow + " FROM " + iTable + " ORDER BY " + iFeldShow;
	Statement stmt;
	ResultSet rs;
	String retString = new String();
	retString = "<SELECT NAME=\"" + iName +"\">";
	
	try {	    
	    stmt = bdeConnection.createStatement();
	    stmt.executeQuery(sql);
	    rs = stmt.getResultSet();	    
	    while (rs.next()) {
		retString = retString + "<OPTION VALUE=\"" + rs.getString(iFeldValue) + "\">" + rs.getString(iFeldShow);		
	    }
	    rs.close();
	    stmt.close();
	}
	catch (SQLException sqlEx){
	    sqlEx.printStackTrace();
	    retString = retString +"<OPTION>Methode getHtmlValueSelect: Fehler bei Datenbankzugriff";
	}
	retString = retString + "</SELECT>";
	return retString ;
    }
    
    public String getHtmlSelect(String iName, String iZusatz, String iTable, String iFeldShow) {
	String sql = "SELECT " + iZusatz +" " + iFeldShow + " FROM " + iTable + " ORDER BY " + iFeldShow;
	Statement stmt;
	ResultSet rs;
	String retString = new String();
	retString = "<SELECT NAME=\"" + iName +"\">";
	try {
	    stmt = bdeConnection.createStatement();
	    stmt.executeQuery(sql);
	    rs = stmt.getResultSet();	    
	    while (rs.next()) {
		retString = retString + "<OPTION>" + rs.getString(iFeldShow);		
	    }
	    rs.close();
	    stmt.close();
	}
	catch (SQLException sqlEx){
	    sqlEx.printStackTrace();
	    retString = retString +"<OPTION>Methode getHtmlSelect: Fehler bei Datenbankzugriff";
	}
	retString = retString + "</SELECT>";
	return retString ;
    }
    public String getHtmlSelectOption(String iName, String iZusatz, String iTable, String iFeldShow,String iOption) {
	String sql = "SELECT " + iZusatz +" " + iFeldShow + " FROM " + iTable + " ORDER BY " + iFeldShow;
	Statement stmt;
	ResultSet rs;
	String retString = new String();
	retString = "<SELECT NAME=\"" + iName +"\">";
	retString =  retString + "<OPTION>" + iOption;
	try {
	    stmt = bdeConnection.createStatement();
	    stmt.executeQuery(sql);
	    rs = stmt.getResultSet();	    
	    while (rs.next()) {
		retString = retString + "<OPTION>" + rs.getString(iFeldShow);		
	    }
	    rs.close();
	    stmt.close();
	}
	catch (SQLException sqlEx){
	    sqlEx.printStackTrace();
	    retString = retString +"<OPTION>Methode getHtmlSelect: Fehler bei Datenbankzugriff";
	}	
	retString = retString + "</SELECT>";
	return retString ;
    }
    public String getHtmlSelect(String iName,String sql,String initWert) {
	Statement stmt;
	ResultSet rs;
	String retString = new String();
	retString = "<SELECT NAME=\"" + iName +"\">";
	try {
	    stmt = bdeConnection.createStatement();	    
	    stmt.executeQuery(sql);
	    rs = stmt.getResultSet(); 
	    while (rs.next()) {
		if (initWert.compareTo(rs.getString(1))==0) {
		    retString += "\n<OPTION SELECTED VALUE="+rs.getString(1)+" >"+rs.getString(2);
		} else {
		    retString += "\n<OPTION VALUE="+rs.getString(1)+" >"+rs.getString(2);
		}
	    }
	    rs.close();
	    stmt.close();
	}
	catch (SQLException sqlEx) {
	    sqlEx.printStackTrace();
	    retString = retString +"<OPTION>Methode getHtmlSelect: Fehler beim Datenbankzugriff";
	}
	retString +="</SELECT>";
	return retString;
    }
    public String getHtmlSelectInit(String iName, String iZusatz, String iTable, String iFeldShow, String iInit) {
	String sql = "SELECT " + iZusatz +" " + iFeldShow + " FROM " + iTable + " ORDER BY " + iFeldShow;
	Statement stmt;
	ResultSet rs;
	String retString = new String();
	retString = "<SELECT NAME=\"" + iName +"\">";      
	try {
	    stmt = bdeConnection.createStatement();
	    stmt.executeQuery(sql);
	    rs = stmt.getResultSet();	    
	    while (rs.next()) {
		if (rs.getString(iFeldShow).compareTo(iInit)==0) 
		    retString += "<OPTION>" + rs.getString(iFeldShow);
		else
		    retString += "<OPTION SELECTED>" + rs.getString(iFeldShow);		
	    }
	    rs.close();
	    stmt.close();
	}
	catch (SQLException sqlEx){
	    sqlEx.printStackTrace();
	    retString = retString +"<OPTION>Methode getHtmlSelect: Fehler bei Datenbankzugriff";
	}	
	retString = retString + "</SELECT>";
	return retString ;
    }

}
