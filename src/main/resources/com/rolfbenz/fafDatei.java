package com.rolfbenz;

import java.io.*;

public class fafDatei extends basisDatei {
	public fafDatei() {
	}
    	public fafDatei(String iPfad) {
		super(iPfad);		
		
	}
	public fafDatei(String iPfad, String iDateiname, String iEndung) {
		super(iPfad,iDateiname,iEndung);
	}

	public void write(String iText) {
		try {
			ausgabe.write(iText);
		}
		catch(Exception e) {
			e.printStackTrace();
		}	
	}
	
}