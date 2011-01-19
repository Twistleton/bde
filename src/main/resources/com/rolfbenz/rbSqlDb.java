package com.rolfbenz;

/**
 * rbSqlDb.java
 *
 *
 * Created: Thu Jul 12 14:18:11 2001
 *
 * @author Alexander Neubauer
 * @version 0.1
 */

/*Diese Klasse stellt Methoden zur Datenmanipulation auf SQL Datenbanken zur Verfügung*/

import java.sql.*;

public abstract class rbSqlDb {
    /*Variablendeklaration*/
    protected String sIpAdr;
    protected String sDbVersion;
    protected String sDbName;
    protected String sUser;
    protected String sPassWd;
    protected Connection cConn;

    /*Abstrakte Methoden*/
    public abstract void Connect();

    /*private Methoden*/
    //************************Daten in Tabelle ändern****************************
    private int Execute(String sQuery){
	Statement stmt = null;
	int iAnzZeil = 0;
	try {
	    /*Private Variablen der Superklasse auslesen*/
	    Connection cConn = getcConn();

	    /*Lokale Variablen anlegen und initialisieren*/
	    stmt = cConn.createStatement();
	    stmt.executeUpdate(sQuery);
	}
        catch(Exception e){
	    e.printStackTrace();
	}
        try {
	    iAnzZeil = stmt.getUpdateCount();
	}
	catch (SQLException se){
	    se.printStackTrace();
	}
	return iAnzZeil;
    }

    /*public Methoden*/
    //************************Daten aus Tabellen auslesen************************
    public ResultSet Select(String sQuery){
	/*Private Variablen der Superklasse auslesen*/
	Connection cConn = this.getcConn();

	/*Lokale Variablen anlegen*/
	Statement stmt = null;
	ResultSet rs = null;

	try {
	    stmt = cConn.createStatement();
	    rs = stmt.executeQuery(sQuery);
	    /*Ergebnis speichern*/
	}
	catch(Exception e){
	    e.printStackTrace();
	}
	return rs;
    }

    //************************Daten in Tabelle ändern****************************
    public int Update(String sQuery){
	return this.Execute(sQuery);
    }

    //************************Daten aus Tabelle löschen**************************
    public int Delete(String sQuery){
	return this.Execute(sQuery);
    }

    //************************Daten in Tabelle einfügen**************************
    public int Insert(String sQuery){
	return this.Execute(sQuery);
    }

    //************************Verbindung zur Datenbank beenden******************
    public void DisConnect(){
	/*Private Variablen der Superklasse auslesen*/
	Connection cConn = this.getcConn();
	try {
	    cConn.close();
	}
	catch(Exception e) {
	    e.printStackTrace();
	}
    }

    //************************Änderungen in Datenbank nachführen*****************
    //Nur return "0" implementiert da MySQL kein Commit versteht. 
    //Muß in Unterklasse implementiert werden
    public void Commit(){}

    //************************Änderungen in Datenbank rückgängig machen**********
    //Nur return "0" implementiert da MySQL kein Rollback versteht. 
    //Muß in Unterklasse implementiert werden
    public void Rollback(){}

    /*Set Methoden*/
    //************************IP - Adresse des Zielrechner speichern*************
    public void setsIpAdr(String sSetIpAdr){
	sIpAdr = sSetIpAdr;
    }
    //************************Datenbank - Versionsnummer speichern***************
    public void setsDbVersion(String sSetDbVersion){
	sDbVersion = sSetDbVersion;
    }
    //************************Datenbankname speichern****************************
    public void setsDbName(String sSetDbName){
	sDbName = sSetDbName;
    }
    //************************Usernamen für den Datenbankzugriff speichern*******
    public void setsUser(String sSetUser){
	sUser = sSetUser;
    }
    //************************Password des Users für DB Zugriff speichern********
    public void setsPassWd(String sSetPassWd){
	sPassWd = sSetPassWd;
    }
    //************************Verbindung speichern*******************************
    public void setcConn(Connection cSetConn){
	cConn = cSetConn;
    }

    /*Get Methoden*/
    //************************IP - Adresse des Zielrechners auslesen*************
    public String getsIpAdr(){
	return sIpAdr;
    }
    //************************Datenbank - Versionsnummer auslesen****************
    public String getsDbVersion(){
	return sDbVersion;
    }
    //************************Datenbankname auslesen*****************************
    public String getsDbName(){
	return sDbName;
    }
    //************************Usernamen für den Datenbankzugriff auslesen********
    public String getsUser(){
	return sUser;
    }
    //************************Password des Users für DB Zugriff auslesen*********
    public String getsPassWd(){
	return sPassWd;
    }
    //************************Verbindung auslesen********************************
    public Connection getcConn(){
	return cConn;
    }
}
