package com.rolfbenz;

import java.io.*;
public class configDatei { 
	protected String datei  = new String();
	protected String inhalt = new String();
	
	public configDatei() {
	}
	public configDatei(String dname) {
		this.datei = dname;	
	}
	public int einlesen() {
		//Einlesen der Config-Datei
                try {
                        File configFile = new File(datei);
                        FileReader in = new FileReader(configFile);
                        for (int i=0;i<configFile.length();i++){
                                inhalt += (char) in.read();
                        }
                }
                catch(IOException ioe) {
                        ioe.printStackTrace();
			return -1;
                }
		return 0;
	}
}
