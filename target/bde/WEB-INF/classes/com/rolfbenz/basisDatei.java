package com.rolfbenz;

import java.io.*;

public class basisDatei {
    // Eigenschaften der Datei
    public String pfad;
    public String dateiname;
    public String endung;
    public File datei;
    public int istOffen;

    public FileOutputStream   ausgabeStrom;
    public OutputStreamWriter ausgabe;
    
    // Konstruktoren
    public basisDatei() {
	istOffen=0;
    }
    public basisDatei(String iPfad) {
	pfad      = new String (iPfad.substring(0,iPfad.lastIndexOf("/")));
	dateiname = new String (iPfad.substring(iPfad.lastIndexOf("/")+1,iPfad.lastIndexOf(".")));
	endung    = new String (iPfad.substring(iPfad.lastIndexOf(".")+1));		
	istOffen=0;
    }
    public basisDatei(String iPfad, String iDateiname, String iEndung) {
	pfad      = new String (iPfad);
	dateiname = new String (iDateiname); 		
	endung    = new String (iEndung);
	istOffen=0;
    }
    
    public basisDatei(File iPfad) {
	pfad      = new String (iPfad.getPath().substring(0,iPfad.getPath().lastIndexOf("/")));
	dateiname = new String (iPfad.getPath().substring(iPfad.getPath().lastIndexOf("/")+1,iPfad.getPath().lastIndexOf(".")));
	endung    = new String (iPfad.getPath().substring(iPfad.getPath().lastIndexOf(".")+1));
	istOffen=0;		
    }	
    public int oeffnen() {
	if (istOffen==0) {
	    try {
		datei = new File(pfad + File.separator + dateiname + ".tmp"+endung);
		ausgabeStrom = new FileOutputStream(datei);
		ausgabe      = new OutputStreamWriter(ausgabeStrom,"ISO-8859-1");			
		istOffen=1;
	    }
	    catch(IOException ioe) {
		ioe.printStackTrace();
		return -1;
	    }		
	}
	return 0;
    }
    /* Kommentar eingefügt */
    public void schliessen() {
	try {
	    ausgabe.flush();
	    ausgabe.close();
	    ausgabeStrom.close();
	}
	catch(IOException ioe) {
	    ioe.printStackTrace();
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
    public void setNameTimestamp(String iFormat) {
	try {
	    dateiname = bdeZeit.getTimestamp(iFormat);
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
    public void loeschDatei() {
	datei.delete();
    }
}
