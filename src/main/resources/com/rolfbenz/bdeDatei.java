package com.rolfbenz;

import java.io.*;

public class bdeDatei extends basisDatei {

	// Inhalt des Datensatzes
	private int vonSubsystem;
	private int nachSubsystem;
	private String ereignisAktion;
	private int paketFolgeNr;
	private int auftragsNr;
	private String internerVorgang;
	private int bezogenesSubsystem;
	private int neuPersonalNr;
	private int altPersonalNr;
	private long fertigungsstufe;
	private int fsStatus;
	private char fsSplit;
	private int datensaetze=0;

	// Konstruktoren
	public bdeDatei() {
	}
    	public bdeDatei(String iPfad) {
		super(iPfad);		
		
	}
	public bdeDatei(String iPfad, String iDateiname, String iEndung) {
		super(iPfad,iDateiname,iEndung);
	}

	public bdeDatei(File iPfad) {
		super(iPfad);
	}

	// Methoden
	public void schreibeEndeSatz() {
		try {
			ausgabe.write(rbTextFormat.format('0',2,vonSubsystem));
			ausgabe.write(rbTextFormat.format('0',2,nachSubsystem));
			ausgabe.write("E999"+bdeZeit.getTimestamp("ddMMyyyyHHmmss") + "0429999999999"+rbTextFormat.format('0',2,bezogenesSubsystem)+rbTextFormat.format('0',4,String.valueOf(datensaetze)));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void schreibeSatz() {
		try {
			ausgabe.write(rbTextFormat.format('0',2,vonSubsystem));
			ausgabe.write(rbTextFormat.format('0',2,nachSubsystem));
			ausgabe.write(rbTextFormat.format(' ',4,ereignisAktion));
			ausgabe.write(bdeZeit.getTimestamp("yyyyMMddHHmmss"));
			ausgabe.write(rbTextFormat.format('0',3,"59"));
			ausgabe.write(rbTextFormat.format('0',2,paketFolgeNr));
			ausgabe.write(rbTextFormat.format('0',6,auftragsNr));
			ausgabe.write(rbTextFormat.format('0',2,internerVorgang));
			ausgabe.write(rbTextFormat.format('0',2,bezogenesSubsystem));
			ausgabe.write(rbTextFormat.format('0',4,neuPersonalNr));              // Verändert 26.11.2002
			ausgabe.write(rbTextFormat.format('0',4,altPersonalNr));			// Neu dazu  26.11.2002
			ausgabe.write(rbTextFormat.format('0',11,fertigungsstufe));
			ausgabe.write(rbTextFormat.format('0',2,fsStatus));
			ausgabe.write(fsSplit+"\r\n");
			datensaetze++;
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}

	public String getName(){
		return datei.getName();
	}

	public void setNameTimestamp() {
		try {
			dateiname = bdeZeit.getTimestamp("yyMMddHHmmssSSS");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public void aktiviereDatei() {
          // Endesatz angängen und Datei umbenennen.
		File temp = new File(pfad+File.separator+dateiname+"."+endung) ;
	     datei.renameTo(temp);	
		datei = temp;
	}


	// SetMethoden zum überwachten Füllen der Variablen
	public int setVonSubsystem(int iVonSubsystem) {					
		if ((iVonSubsystem>99) || (iVonSubsystem<0)) {
			return -1;
		} else {
			this.vonSubsystem = iVonSubsystem;
			return 0;
		}
	}
	public int setNachSubsystem(int iNachSubsystem) {
		if ((iNachSubsystem>99)|| (iNachSubsystem<0)) {
			return -1;
		} else {
			this.nachSubsystem = iNachSubsystem;
			return 0;
		}	
	}
	public int setEreignisAktion(String iEreignisAktion) {
		if (iEreignisAktion.length()>4) {
			return -1;
		} else {
			this.ereignisAktion = new String(iEreignisAktion);
			return 0;
		}
	}
	public int setPaketFolgeNr(int iPaketFolgeNr) {
		if ((iPaketFolgeNr>99)|| (iPaketFolgeNr<0)) {
			return -1;
		} else {
			this.paketFolgeNr=iPaketFolgeNr;
			return 0;
		}
	}
	public int setAuftragsNr(int iAuftragsNr) {
		if ((iAuftragsNr<=0)||(iAuftragsNr>999999)) {
			return -1;
		} else {
			this.auftragsNr= iAuftragsNr;
			return 0;
		}		
	}
	public int setInternerVorgang(String iInternerVorgang) {
		if (iInternerVorgang.length()>2) {
			return -1;
		} else {
			this.internerVorgang = new String(iInternerVorgang);
			return 0;
		}				
	}
	public int setBezogenesSubsystem(int iBezogenesSubsystem) {		
		if ((iBezogenesSubsystem>99) || (iBezogenesSubsystem<0)) {
			return -1;
		} else {
			this.bezogenesSubsystem = iBezogenesSubsystem;
			return 0;
		}

	}

	// Überholt, funktioniert aber immer noch. Siehe setNeuPersonalNummer
	public int setPersonalNr(int iPersonalNr) {
		return setNeuPersonalNr(iPersonalNr);
	}

	public int setNeuPersonalNr(int iNeuPersonalNr) {
		if ((iNeuPersonalNr>9999) || (iNeuPersonalNr<1)) {
			return -1;
		} else {
			this.neuPersonalNr = iNeuPersonalNr;
			return 0;
		}
	}
	public int setAltPersonalNr(int iAltPersonalNr) {
		if ((iAltPersonalNr>9999) || (iAltPersonalNr<1)) {
			return -1;
		} else {
			this.altPersonalNr = iAltPersonalNr;
			return 0;
		}
	}

	public int setFertigungsstufe(long iFertigungsstufe) {
		this.fertigungsstufe=iFertigungsstufe;
		return 0;
	}
	public int setFsStatus(int iFsStatus) {
		if ((iFsStatus>99) || (iFsStatus<0)) {
			return -1;
		} else {
			this.fsStatus = iFsStatus;
			return 0;
		}
	}
	public int setFsSplit(char iFsSplit) {
		this.fsSplit=iFsSplit;
		return 0;
	}

}

	                                                  	
