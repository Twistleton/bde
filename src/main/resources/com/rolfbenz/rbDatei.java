package com.rolfbenz;

import java.io.FileOutputStream;
import java.io.PrintWriter;

public class rbDatei {
    private String pfad;
    private String dateiname;
    private FileOutputStream ausgabeStrom;
    private PrintWriter      ausgabe;

    public rbDatei() {
	
    }    
    public rbDatei(String iPfad,String iDateiName) {
	pfad      = new String (iPfad);
	dateiname = new String (iDateiName); 		   
    }
}
