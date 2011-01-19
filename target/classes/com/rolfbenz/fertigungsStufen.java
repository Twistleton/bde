package com.rolfbenz;

import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.*;
import java.util.*;

public class fertigungsStufen {
	private int     abNr        = 0;
	private int     prog        = 0;
	private int     fsStatus    = 0;
	private int     pNr         = 0;
	private int     aendIx      = 0;
	private bdeDb      iBdeDb;
	private Connection con;
	private String     appRoot;
	private String nachTddDir = new String(""); // Alle TDD-Dateien in ein Verzeichnis
	private String vomTddDir  = new String(""); // Alle TDD-DAteien in ein VErzeicfhnis
	private String errorDir   = new String(""); // Datei, in die Fehler protokolliert werden
	private String fafDir = new String();
	public fertigungsStufen (bdeDb iBdeDb,String iApplicationRoot) {
		con = iBdeDb.getConnection();
		appRoot=iApplicationRoot;
		this.iBdeDb=iBdeDb;
	}
	public void setNachTddDir(String iNachTddDir) {
		nachTddDir = iNachTddDir;
	}
	public void setVomTddDir(String iVomTddDir) {
		vomTddDir  = iVomTddDir;
	}	
	public void setErrorDir(String iErrorDir) {
		errorDir = iErrorDir;
	}
	public void setFafDir(String iFafDir) {
		fafDir = iFafDir;
	}
	/* *********************************************************************** */
	/* ***** Methode getVorselektion                                     ***** */
	/* ***** Rückgabewert: String mit HTML-Code der Maske                ***** */
	/* *********************************************************************** */
	/* ***** HTML-Darestellung der Auswahmaske zum Selektieren           ***** */
	/* ***** bestimmter Fertigungsstufen.                                ***** */
	/* ***** Übergeben wird nur der "Bereich". Das ist ein Char          ***** */
	/* ***** und kann die Werte A,P und N Annehmen                       ***** */
	/* ***** A ==> Buttons, Felder für alle Kostenstellen anzeigen       ***** */
	/* ***** P ==> Buttons, Felder für Polsterei anzeigen                ***** */
	/* ***** N ==> Buttons, Felder für Näherei anzeigen                  ***** */
	/* ***** Bereiche wurden eingeführt, damit die Mitarbeiter nur die   ***** */
	/* ***** für sie relevanten Buttons und Felder angezeigt bekommen    ***** */
	/* *********************************************************************** */
	public String getVorselektion (String bereich) {
		String spezSelect = new String();
		if (bereich.compareTo("A")==0) {
			spezSelect ="SELECT DISTINCT abf_fs a,abf_fs b "+
				"FROM plrv11.bde_ab_fs_ma "+
				"ORDER BY a";
		} else {
			spezSelect ="SELECT DISTINCT abf_fs a,abf_fs b "+
				"FROM plrv11.bde_ab_fs_ma,plrv11.plr_attrAttr WHERE "+
				"att_attr=substr(abf_fs,3,3) AND att_tabname='kstZuAbt'"+
				" and att_kennz=1 AND att_bez='"+bereich+"' ORDER BY a";
		}	
		String retString = new String();
		retString += "<CENTER>Vorselektion der zu verwaltenden Auftr&auml;ge</CENTER>"+
			"<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">"+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"     >"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"auswahl\"     >"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"     VALUE=\"allg\"        >"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\" >"+
			"<TABLE BORDER=1><TR><TD>Fertigungsstufen</TD><TD>Fert.-stufen<BR>Status</TD>"+
			"<TD>Split-Kennz.</TD></TD><TD>Puffer -<BR> status</TD><TD>Sortierung</TD></TR>"+
			"<TR><TD>" +
			iBdeDb.getHtmlSelectAllg("fs",spezSelect,"") + "</TD><TD>" +
			iBdeDb.getHtmlSelectOption("fsStatus","DISTINCT","plrv11.bde_ab_fs_ma","abf_fs_status","Alle unfertigen")  + "</TD><TD>" +
			iBdeDb.getHtmlSelectOption("splitt","DISTINCT","plrv11.bde_ab_fs_ma","abf_spl_kz","Alle")       + "</TD><TD>" +
			iBdeDb.getHtmlSelectOption("pfStatus","DISTINCT","plrv11.bde_ab_fs_ma","abf_pf_status","Alle")  + "</TD><TD>" +
			"<SELECT NAME=\"sort\">"+
			"<OPTION VALUE=\"abf_fabt_pps,abf_prio\">Termin"+
			"<OPTION VALUE=\"abf_prio\">Priorit&auml;t" +
			"<OPTION VALUE=\"23,24\">SSP Fabt  "+
//			"<OPTION VALUE=\"19,20\">MinML Fabt"+
			"<OPTION VALUE=\"23,24\">SSN Fabt (nur N&auml;herei) "+
			"<OPTION VALUE=\"25,26\">SEN Fabt (nur N&auml;herei) "+
			"<OPTION VALUE=\"abf_pnr\">Personalnummer"+
			"<OPTION VALUE=\"abf_abnr\">AB-Nr"+
			"<OPTION VALUE=\"abf_prog\">Programm"+
			"</SELECT></TD></TR>"+
			"<TR><TH COLSPAN=5><input type=submit value=\"OK\"> </TH></TR>"+
			"</TABLE></FORM>\n";
		retString += "<TABLE BORDER=0>";
		retString += "<TR><FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\"><TD BGCOLOR=#FFDDDD>\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"auswahl\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"    VALUE=\"abNr\"        >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+
			"Fertigungsstufen<BR>der AB-Nr:</TD><TD BGCOLOR=#FFDDDD align=right><INPUT TYPE=\"text\" NAME=\"abNr\" SIZE=6 MAXLENGTH=6></TD>\n"+
			"<TD BGCOLOR=#FFDDDD ALIGN=right><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM>\n"+
			"<FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\"><TD BGCOLOR=#FFAAAA>\n    "+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"auswahl\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"    VALUE=\"pNr\"         >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+
			"Fertigungsstufen<BR>der Pers.-Nr:</TD><TD BGCOLOR=#FFAAAA align=right><INPUT TYPE=\"text\" NAME=\"pNr\" SIZE=4 MAXLENGTH=4></TD>\n"+
			"<TD BGCOLOR=#FFAAAA><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n"+
			"<TR BGCOLOR=#FFBB88><FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\">	<TD>R&uuml;ckstand RB</TD><TD COLSPAN=2 ALIGN=right>\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"auswahl\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"    VALUE=\"rueckstand\"  >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"param\"    VALUE=\"rb\"          >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+	
			iBdeDb.getHtmlSelectAllg("fs",spezSelect,"") +
			"</TD><TD>Fabt</TD><TD ALIGN=right><INPUT TYPE=\"text\"   NAME=\"fabt\"     SIZE=4 MAXLENGTH=4 ></TD><TD>\n"+
			"<INPUT TYPE=\"submit\" VALUE=\"OK\">\n"+
			"</TD></FORM></TR>\n"+	
			"<TR BGCOLOR=#FFFFDD><FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\"><TD>Unverpl.<BR>Fert.-Stufen	</TD><TD COLSPAN=4 >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"    >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"auswahl\"    >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"     VALUE=\"unverplant\" >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+	
			iBdeDb.getHtmlSelectAllg("fs",spezSelect,"") +
			"</TD><TD><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n"+
		
			"<TR BGCOLOR=#DDFFDD><FORM ACTION=\""+appRoot    +"bde\"   METHOD=\"GET\"><TD>\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"auswahl\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"     VALUE=\"fsPlatz\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+
			"Fertigungsstufe</TD><TD COLSPAN=2>"+iBdeDb.getHtmlSelectAllg("fs",spezSelect,"")+"</TD><TD>von Arbeitspl.\n"+	
			"<INPUT TYPE=\"text\"   NAME=\"vonPlatz\" VALUE=\"0\" MAXLENGTH=3 SIZE=3></TD><TD ALIGN=right>bis\n"+
			"<INPUT TYPE=\"text\"   NAME=\"bisPlatz\" VALUE=\"0\" MAXLENGTH=3 SIZE=3></TD><TD>"+
			"<INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n"+
		
			"<TR BGCOLOR=#EEFFEE><FORM ACTION=\""+appRoot    +"bde\"   METHOD=\"GET\"><TD>\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"    >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"eilab\"      >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"eilKz\"     VALUE=\"J\"          >\n"+
			"Eil-Auftrag kennz.</TD><TD ALIGN=right>"+
			"<INPUT TYPE=\"text\"   NAME=\"abNr\" MAXLENGTH=6 SIZE=6></TD><TD ALIGN=right>"+
			"<INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM>\n"+
		
			"<FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\"><TD>\n    "+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"eilab\"       >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"eilKz\"     VALUE=\"N\"          >\n"+
			"Eil-Auftrag Storno</TD><TD ALIGN=right>"+
			"<INPUT TYPE=\"text\"   NAME=\"abNr\" MAXLENGTH=6 SIZE=6></TD><TD ALIGN=right>"+
			"<INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n"+
		
			"<TR BGCOLOR=#EEFFEE><FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\"><TD>\n    "+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"auswahl\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"    VALUE=\"eilab\"       >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n"+
			"Eil-Auftr&auml;ge</TD><TD ALIGN=right>"+
			iBdeDb.getHtmlSelectAllg("fs",spezSelect,"") +
			"</TD><TD><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM>\n"+
		
			"<FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\"><TD>\n      "+
			"<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"auswahl\"     >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"modus\"     VALUE=\"kdab\"        >\n"+
			"<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\" >\n"+
			"Kundendienst-Auftr.</TD><TD ALIGN=right>"+
			iBdeDb.getHtmlSelectAllg("fs",spezSelect,"") +
			"</TD><TD><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n";
		// Administratoren (A) und Mitarbeiter der Näherei (N) sehen auch diese Dialoge
		if ((bereich.compareTo("N")==0) || (bereich.compareTo("A")==0)) {
			retString += "<TR BGCOLOR=#EEEEFF>";
			retString += "<FORM ACTION=\""+appRoot + "bde\" METHOD=\"GET\"><TD><B>Näherei:</B></TD><TD>Dezi Null Setzen</TD><TD>&nbsp;</TD>";
			retString += "<TD ALIGN=right><TD ALIGN=right>AB-Nr.";
			retString += "<INPUT TYPE=\"text\"       NAME=\"abNr\" MAXLENGTH=\"6\" SIZE=\"6\" >\n";
			retString += "<INPUT TYPE=\"hidden\"     NAME=\"modul\"     VALUE=\"nae\"         >\n";
			retString += "<INPUT TYPE=\"hidden\"     NAME=\"formular\"  VALUE=\"deziNull\"    >\n";
			retString += "<INPUT TYPE=\"hidden\"     NAME=\"userSicht\" VALUE=\""+bereich+"\" >\n";
			retString += "</TD><TD ALIGN=right><INPUT TYPE=\"SUBMIT\" VALUE=\"OK\"></TD></FORM></TR>\n";
		}
		if ((bereich.compareTo("P")==0) || (bereich.compareTo("A")==0)) {
			retString += "<TR BGCOLOR=#EEEEFF>";
			retString += "<FORM NAME \"deziNullPol\" ACTION=\""+appRoot + "bde\" METHOD=\"GET\"><TD><B>Polsterei:</B></TD><TD> Dezi Null Setzen</TD><TD>&nbsp;</TD>";
			retString += "<TD>";
			retString += "<SELECT NAME=\"fs\"><OPTION VALUE=\"10144100000\">10144100000</OPTION>";
			retString += "<OPTION VALUE=\"10245100000\">10245100000</OPTION></SELECT></TD><TD  ALIGN=\"RIGHT\">AB-Nr.";
			retString += "<INPUT TYPE=\"text\"       NAME=\"abNr\" MAXLENGTH=\"6\" SIZE=\"6\" >\n";
			retString += "<INPUT TYPE=\"hidden\"     NAME=\"modul\"     VALUE=\"pol\"         >\n";
			retString += "<INPUT TYPE=\"hidden\"     NAME=\"formular\"  VALUE=\"deziNull\"    >\n";
			retString += "<INPUT TYPE=\"hidden\"     NAME=\"userSicht\" VALUE=\""+bereich+"\" >\n";
			retString += "</TD><TD ALIGN=right><INPUT TYPE=\"SUBMIT\" VALUE=\"OK\"></TD></FORM></TR>\n";
		}
		// Administratoren (A) und Mitarbeiter der Polsterei (P) sehen auch diese Dialoge
		if ((bereich.compareTo("A")==0) || (bereich.compareTo("P"))==0) {
			retString += "<TR bgcolor=#DDDDFF><FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">";
			retString += "<TD>Auftrag hoch priorisieren</TD><TD ALIGN=right>";
			retString += "<INPUT TYPE=\"text\"   NAME=\"abNr\" MAXLENGTH=\"6\" SIZE=\"6\" >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"     >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"hohePrio\"    >\n";
			retString += "</TD><TD ALIGN=right><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n";
			retString += "<TR bgcolor=#DDDDFF><FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">";
			retString += "<TD>Tour des Tages</TD><TD ALIGN=right>";
			retString += "<INPUT TYPE=\"text\"   NAME=\"fabt\" MAXLENGTH=\"4\" SIZE=\"4\" >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"     >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"auswahl\"     >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\" >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modus\"     VALUE=\"tourDesTages\">\n";
			retString += "</TD><TD ALIGN=right><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n";
			retString += "<TR BGCOLOR=#FF99FF>";
			retString += "<FORM ACTION=\"" + appRoot + "bde\"  METHOD=\"GET\"><TD>R&uuml;ckstand Navi</TD><TD COLSPAN=2>\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\"     >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"auswahl\"     >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modus\"    VALUE=\"rueckstand\"  >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"param\"    VALUE=\"ml\"          >\n";
			retString += iBdeDb.getHtmlSelect("fs",spezSelect,"10245100000");
			retString += "</TD><TD>Fabt</TD><TD align=right><INPUT TYPE=\"text\"   NAME=\"fabt\" SIZE=4 MAXLENGTH=4 ></TD>";
			retString += "<TD ALIGN=right>\n";
			retString += "<INPUT TYPE=\"submit\" VALUE=\"OK\">\n";
			retString += "</TD></FORM></TR>";
			retString += "<TR BGCOLOR=#DD99DD>";
			retString += "<FORM ACTION=\"" + appRoot +"bde\"       METHOD=\"GET\"><TD>\n    ";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"ausw\"        >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"auftrAusw\"   >\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">\n";
			retString += "Historie des Auftrags</TD><TD ALIGN=right><INPUT TYPE=\"text\"   NAME=\"abNr\" SIZE=6 MAXLENGTH=6></TD>";
			retString += "<TD ALIGN=right>";
			retString += "<INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM>\n";
			retString += "<FORM ACTION=\""+appRoot    +"bde\"   METHOD=\"GET\"><TD>\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"auftrBearb\">\n";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+bereich+"\">";
			retString += "<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"stextEin\">\n";
			retString += "Sonderfertigungstext</TD><TD ALIGN=right>AB-Nr<INPUT TYPE=\"text\" NAME=\"abNr\" MAXLENGTH=6 SIZE=6></TD>";
			retString += "<TD ALIGN=right><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></FORM></TR>\n";
		}
		retString +="</TABLE>";
		return retString;
	}
	/* *********************************************************************** */
	/* ***** Methode getSelektion                                        ***** */
	/* ***** Rückgabewert: String mit HTML-Code, Auswahlliste der        ***** */
	/* *****               selektierten Fertigungsstufen                 ***** */
	/* ***** Eingabeparameter;                                           ***** */
	/* *****                   iRequest: Request des HTTP-Servers        ***** */
	/* *****                   recht   ; Rechteklasse des angemeldeten Users * */
	/* *********************************************************************** */
	/* ***** je nachdem, welcher Button in vorselektion geklickt wird,   ***** */
	/* ***** wird hier die entsprechende Ausgabe erzeugt.                ***** */
	/* *********************************************************************** */

	public String getSelektion (HttpServletRequest iRequest,int recht) {
		String    retString    = new String();
		String    sqlString    = new String();
		String    puffer       = new String();
		String    sqlString2   = new String();
		String    sqlString3   = new String();
		String    tabelle      = new String();
		String    selectList   = new String();
		String    from	       = new String();
		String    where        = new String("WHERE ");
		String    pNrFeld      = new String();
		String    sort         = new String();
		String    split        = new String("");
		String    fsUndProg    = new String();
		String    platzNr      = new String();
		String	  fertOrt      = new String();
		String	  wechselFabt  = new String();
		String    wechselUhrzeit=new String();
		String    senFabt      = new String(); 
		String    senUhrzeit   = new String();
		String    senString    = new String();
		String    rbEndeUhrzeit= new String("");
		String    maxMlUhrzeit = new String("");
		String    hyperlink    = new String();
		textDatei errorDatei   = new textDatei(errorDir,"error.log");
		String	  fehlerFarbe  = new String("");
		Statement  stmt;
		ResultSet  rs;
		Statement stmt2;
		Statement stmt3;
		ResultSet  rs2;
		ResultSet  rs3;

		pen       farbe     = new pen(221,255,221,221,221,255);
		// In den Näherei-Masken muss der Näherei-Termin angezeigt werden
		try {
			fertOrt=iRequest.getParameter("fs").substring(2,5);
		}
		catch (NullPointerException npe) {
			fertOrt ="0";
		}
		// Allgemeiner SELECT 
		// Dieser passt für die meisten Anfragen
		selectList = "SELECT "+
			"abf_abnr,abf_prog,abf_prio,abf_fabt_pps,abf_ordnr," +		// 1-5
			"abf_fs,abf_te,abf_dezi,abf_pnr,abf_fs_status,"+		// 6-10
			"abf_pf_status,abf_spl_kz,ma_ap,ako_kunde,ako_fabt_vs,"+	// 11-15
			"ako_uhr_zeit,ako_tournr,as_aend_ix, "+				// 16 - 18
			"b.sko_fabt,b.sko_uhrzeit,"+					// 19 - 20 Für Subsystem-Kopftabellen zusätzliche Spalten
			"c.sko_fabt,c.sko_uhrzeit, "+					// 21 - 22
			"abf_pf_platz, ako_vlz";					// 23 - 24
		from = "FROM "+
			"plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status,plrv11.bde_madat,plrv11.plr_auftr_kopf, "+ /* Ab hier iss' neu */
			"plrv11.plr_subsys_kopf b, plrv11.plr_subsys_kopf c "; 
			
		// WHERE Klausel zusammenfummeln
		if (iRequest.getParameter("modus").compareTo("allg")==0) {	
			if (iRequest.getParameter("fs").compareTo("Alle")!=0)
				where+= " abf_fs='"+iRequest.getParameter("fs") + "' AND ";	
			if (iRequest.getParameter("fsStatus").compareTo("Alle unfertigen")==0)
				where+= " abf_fs_status<40 AND ";	
			else
				where+= " abf_fs_status="+iRequest.getParameter("fsStatus") + " AND ";
			if (iRequest.getParameter("splitt").compareTo("Alle")!=0)
				where+= " abf_spl_kz='"+iRequest.getParameter("splitt")     + "' AND ";
			if (iRequest.getParameter("pfStatus").compareTo("Alle")!=0)
				where+= " abf_pf_status='"+iRequest.getParameter("pfStatus")+ "' AND ";
			where+= "abf_abnr>-1 ";
			sort = iRequest.getParameter("sort");
		}
		if (iRequest.getParameter("modus").compareTo("abNr")==0) {
			where += " abf_abnr="+ iRequest.getParameter("abNr")+ " AND abf_fs_status<=40 ";
			sort   = "abf_ordnr";
		}
		if (iRequest.getParameter("modus").compareTo("pNr")==0) {
			where += " abf_pnr ="+ iRequest.getParameter("pNr") + " AND abf_fs_status<40 ";
			sort   = "abf_fs";
		}
		if (iRequest.getParameter("modus").compareTo("unverplant")==0) {
			String rumpf = new String();
			rumpf = iRequest.getParameter("fs");
			rumpf = rumpf.substring(0,rumpf.length()-3);
			String fsEinschraenkung = new String();	
			fsEinschraenkung = "abf_fs='"+iRequest.getParameter("fs")+"' AND";	
			where += " abf_fs_status=1 AND abf_pf_status >=40 AND "+fsEinschraenkung +" abf_fs_status<40  ";
			sort = "25,26"; // Grundsätzlich wird nach SSN sortiert
			if (rumpf.compareTo("10144100")==0) {
				sort   = " 25,26"; // Sortierung ssp
			}
			if (rumpf.compareTo("10245100")==0) {
				sort   = " 21,22"; // Sortierung maxMl
			}
			if (rumpf.compareTo("22245100")==0) {
				sort   = " 21,22"; // Sortierung maxML
			}
		}
		if (iRequest.getParameter("modus").compareTo("tourDesTages")==0) {
			where +=" ako_fabt_vs="+iRequest.getParameter("fabt") + " AND abf_fs<>10144100000";
			sort = " ako_fabt_vs,ako_tournr,ako_ladefolge";
		}
		if (iRequest.getParameter("modus").compareTo("rueckstand")==0) {
			String rumpf = new String();
			rumpf = iRequest.getParameter("fs");
			rumpf = rumpf.substring(0,rumpf.length()-3);
		
			String fsEinschraenkung = new String();
			sort =  "25,26"; // Grundsätzlich wird nach SSN sortiert
		
			if (iRequest.getParameter("fs").compareTo("Alle")!=0) {
				fsEinschraenkung = "abf_fs='"+iRequest.getParameter("fs")+"'";
			}
			if (rumpf.compareTo("10144100")==0) {
				sort   = " 25,26"; // Sortierung ssp
			}
			if (rumpf.compareTo("10245100")==0) {
				sort   = " 25,26"; // Sortierung ssp
			}
			if (rumpf.compareTo("22245100")==0) {
				sort   = " 25,26"; // Sortierung ssp
			}	
			if (iRequest.getParameter("param").compareTo("rb")==0) {
				where += " abf_fabt_pps<=" + iRequest.getParameter("fabt") +" AND abf_fs_status=1 AND "+fsEinschraenkung;
			} else if (iRequest.getParameter("param").compareTo("ml")==0) {
				where += " abf_fabt_pps<="   + iRequest.getParameter("fabt") +" AND abf_fs_status<40 AND "+fsEinschraenkung;
				sort   = " 4,1";
			}   else  {
			  	where += " c.sko_fabt<="   + iRequest.getParameter("fabt") +" AND abf_fs_status=1 AND "+fsEinschraenkung;
			}
		}

		// Ausgabe nach Fertigungsstufen eingeschraenkt ueber die Platz-Nummer
		if (iRequest.getParameter("modus").compareTo("fsPlatz")==0) {
			String rumpf = new String();
			rumpf = iRequest.getParameter("fs");
			rumpf = rumpf.substring(0,rumpf.length()-3);
			where += "abf_fs_status in (5,10,20,30) AND abf_pnr=ma_pnr AND to_number(ma_ap) between " +
				rumpf + rbTextFormat.format('0',3,iRequest.getParameter("vonPlatz")) +" AND " + rumpf +
				rbTextFormat.format('0',3,iRequest.getParameter("bisPlatz"));
			sort	= "25,26"; // Grundsätzlich wird nach SSN sortiert		
			if (rumpf.compareTo("10144100")==0) {
				sort   = " ma_ap,25,26"; // Sortierung ssp
			}
			if (rumpf.compareTo("10245100")==0) {
				sort   = " ma_ap,25,26"; // Sortierung maxMl
			}
			if (rumpf.compareTo("22245100")==0) {
				sort   = " 25,26"; // Sortierung maxML
			}	
		}
		// Ausgabe von Eil-Aufträgen
		if (iRequest.getParameter("modus").compareTo("eilab")==0) {
			where += " ako_kz_eilab='J' AND abf_fs='"+iRequest.getParameter("fs")+"' AND abf_fs_status<40 ";
			sort  = " 21,22" ;
		}
		if (iRequest.getParameter("modus").compareTo("kdab")==0) {
			where += " (ako_aart2=15 OR ako_aart2=16) AND abf_fs='"+iRequest.getParameter("fs")+"' AND abf_fs_status<40 ";
			sort  = " 21,22" ;
		}
		/* ************************** Paketierte Auftraege ************************************************************ */
		if (iRequest.getParameter("modus").compareTo("packet")==0) {
			// Hier Probieren wir mal einen anderen SELECT
			selectList = "SELECT abf_abnr,abf_prog,abf_prio,abf_fabt_pps,abf_ordnr,abf_fs,abf_te,abf_dezi,"+
				"abf_pnr,abf_fs_status,abf_pf_status,abf_spl_kz,ma_ap,pp_pf_platz,pp_zuteil_kz,ako_kunde ";
			from = " FROM "+
				"plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status,plrv11.bde_madat,plrv11.bde_pufpl,plrv11.plr_auftr_kopf ";
			where += "  abf_fs_status=1 AND abf_pf_platz=pp_pf_platz ";
			sort   = " abf_pf_platz";
		}
		tabelle +="<TABLE BORDER=1>";

		try {
			String wechselTermin = new String();
			stmt = con.createStatement();
			stmt2= con.createStatement();
			stmt3= con.createStatement();


			// Hier noch die WHERES einfuegen, die fuer alle gelten
			where += " AND abf_pnr=ma_pnr(+) AND as_abnr=abf_abnr AND as_status<>99 AND ";
			where += " abf_aend_ix=as_aend_ix AND ako_abnr=abf_abnr AND ako_aend_ix=as_aend_ix ";	
			// Diese Where einschraenkunden beziehen sich auf die Subsystem-Köpfe
			where += "AND " +
				" b.sko_abnr=abf_abnr AND b.sko_aend_ix=as_aend_ix AND b.sko_subsys=10 AND b.sko_logsys=11 AND " + // 10.09.2002, Min-ML statt RB-End
				" c.sko_abnr=abf_abnr AND c.sko_aend_ix=as_aend_ix AND c.sko_subsys=10 AND c.sko_logsys=19 ";
			

			if (fertOrt.compareTo("243")==0) {
				selectList += ",d.sko_fabt,d.sko_uhrzeit,e.sko_fabt,e.sko_uhrzeit ";  // 25 - 26
				from       += ",plrv11.plr_subsys_kopf d,plrv11.plr_subsys_kopf e ";  // 27 - 28
				where      += " AND d.sko_abnr=abf_abnr AND d.sko_aend_ix=as_aend_ix AND d.sko_subsys=4  AND d.sko_logsys=4 "; //Spaetester Start Naehen
				where      += " AND e.sko_abnr=abf_abnr AND e.sko_aend_ix=as_aend_ix AND e.sko_subsys=4  AND e.sko_logsys=5 "; //Spaetestes Ende Naehen
				wechselTermin = "<TD>SSN</TD><TD>SEN</TD>";
			} else if (fertOrt.compareTo("0")==0) {
				selectList += ",d.sko_fabt,d.sko_uhrzeit ";  // 25 - 26
				from       += ",plrv11.plr_subsys_kopf d ";
				where      += " AND d.sko_abnr=abf_abnr AND d.sko_aend_ix=as_aend_ix AND d.sko_subsys=10  AND d.sko_logsys=14 "; //RB-Ende
				wechselTermin = "<TD>RB-Ende</TD>";
			} else {
				selectList += ",a.sko_fabt,a.sko_uhrzeit ";  // 25 - 26
				from       += ",plrv11.plr_subsys_kopf a ";
				where      += " AND a.sko_abnr=abf_abnr AND a.sko_aend_ix=as_aend_ix AND a.sko_subsys=7 AND a.sko_logsys=7 "; //Spaetester Start Polstern
				wechselTermin ="<TD>SSP</TD>";
			}
			tabelle += "<TR><TD>AB-Nr<BR>KD-Nr</TD><TD ALIGN=CENTER>Pers.-Nr.<BR>Platz</TD>";
			tabelle += "<TD>Fert.-<BR>stufe<BR>Prog.</TD><TD>FS-<BR>Stat.</TD>";
			tabelle += "<TD>Pf-<BR>stat.</TD><TD>Dezi</TD><TD>P-Tag</TD>"+wechselTermin+"<TD>Puffer</TD><TD>Split</TD><TD>VLZ</TD>";
			tabelle += "<TD>Auftragsinfo</TD></TR>";
			// SQL-String zusammenbauen
			sqlString = selectList + from + where;
			// Sortierkriterien einfügen
			sqlString += "ORDER BY " +sort;	
			// errorDatei.write("getSelektion: "+sqlString+"\n\n");	
			stmt.executeQuery(sqlString);	
			rs = stmt.getResultSet();
			while (rs.next()) {
				farbe.change();
				// Ueberpruefen, ob diese Einheit gesplittet werden darf
				if ( (rs.getInt("abf_fs_status")>=5) && (rs.getInt("abf_fs_status")<40)
				   && (rs.getString("abf_spl_kz").compareTo("N")==0)  // ... nur bereits gestartete duerfen gesplittet werden
				   && (recht>20)
				   && (rs.getString("abf_fs").substring(2,5).compareTo("243")!=0)) {
					// Felder fuer das Splitting anzeigen
					split="<TD><FONT SIZE=-1><FORM ACTION=\""+appRoot+"bde\" METHOD=\"GET\">"   +
						"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\">"                +
						"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"vorSplit\">"               +
						"<INPUT TYPE=\"hidden\" NAME=\"fs\"       VALUE=\""+rs.getString("abf_fs")   + "\">" +
						"<INPUT TYPE=\"hidden\" NAME=\"abNr\"     VALUE=\""+rs.getString("abf_abnr") + "\">" +
						"<INPUT TYPE=\"hidden\" NAME=\"pNr\"      VALUE=\""+rs.getString("abf_pnr")  + "\">" +
						"<INPUT TYPE=\"text\"   NAME=\"splitAnz\" VALUE=\"2\" MAXLENGTH=\"2\"    SIZE=\"2\"> "+
						"<INPUT TYPE=\"SUBMIT\" VALUE=\"Split.\"></FORM></FONT></TD>";
				} else {
					split="<TD>kein<BR>Split.</TD>";
				}
				// Ueberpruefen, ob Arbeitsplatz-Nummer angezeigt werden kann
				if (rs.getString("ma_ap")!=null) {
					platzNr= rs.getString("ma_ap").substring(8,11);
				} else {
					platzNr="keiner";
				}
				fsUndProg="";	

				// Ueberpruefen, ob die Zuweisung als Link dargestellt werden darf (Teil 1)
				if ((rs.getInt("abf_fs_status")!=40) && (recht>20) && (rs.getString("abf_pf_platz").charAt(0)==' ') ) {
					fsUndProg+="<A HREF=\""+appRoot+"bde?modul=planung&formular=ma&fs=" + rs.getString("abf_fs") + "&pNr="+
					rs.getString("abf_pnr")+"&abNr="+
					rs.getString("abf_abnr")+"&prog="+rs.getString("abf_prog")+"\" target=list>";
				}
				fsUndProg += rs.getString("abf_fs")+ "<BR>" + rs.getString("abf_prog");
				// Ueberpruefen, ob die Zuweisung als Link dargestellt werden darf (Teil 2)
				if ((rs.getInt("abf_fs_status")!=40) && (recht>20) && (rs.getString("abf_pf_platz").charAt(0)==' ')) {
					fsUndProg += "</A>";
				} else {
					if (rs.getInt("abf_fs_status")==40) {
						fsUndProg +="<BR>Bereits fertig";
		    			}
					if (rs.getString("abf_pf_platz").charAt(0)!=' ') {
						fsUndProg +="<BR><B><FONT COLOR=\"red\">Paket</FONT></B>";
					}
				}

				try {
					// Subsystem-KopfZeiten
					wechselFabt    = rs.getString(25);
					wechselUhrzeit = rs.getString(26);
					if (fertOrt.compareTo("243")==0) {
						senFabt    = rs.getString(27);
						senUhrzeit = rs.getString(28);
						senUhrzeit = senUhrzeit.substring(0,senUhrzeit.length()-2) + ":" + senUhrzeit.substring(senUhrzeit.length()-2);
						senString  = "<TD>"+senFabt+"<BR>"+senUhrzeit+"</TD>";
					} else {
						senString ="";
					}
					wechselUhrzeit = wechselUhrzeit.substring(0,wechselUhrzeit.length()-2) + ":" + wechselUhrzeit.substring(wechselUhrzeit.length()-2);
					rbEndeUhrzeit  = rs.getString(20);
					rbEndeUhrzeit  = rbEndeUhrzeit.substring(0,rbEndeUhrzeit.length()-2)   + ":" + rbEndeUhrzeit.substring(rbEndeUhrzeit.length()-2);
					maxMlUhrzeit   = rs.getString(22);
					maxMlUhrzeit   = maxMlUhrzeit.substring(0,maxMlUhrzeit.length()-2)     + ":" + maxMlUhrzeit.substring(maxMlUhrzeit.length()-2);
					fehlerFarbe="";	
				}
				catch (StringIndexOutOfBoundsException sioobe) {
					// sioobe.printStackTrace();
					fehlerFarbe="BGCOLOR=#FFDDDD";
				}

				// Einfaerbung nach prioritaet
				String prioFarbe = new String();
				if ((rs.getInt("abf_prio")<30)) {
					prioFarbe =" BGCOLOR=\"#FF9999\" ";
				} else if ((rs.getInt("abf_prio")>=30) && (rs.getInt("abf_prio")<50)) {
					prioFarbe =" BGCOLOR=\"#99FF99\" "; 
				} else {
					prioFarbe = "";
				}

				sqlString3 = "select count(*) from plrv11.plr_auftr_info where ai_abnr = " + rs.getString("abf_abnr");
				try {		
		
					stmt3.executeQuery(sqlString3);
					rs3 = stmt3.getResultSet();
					rs3.next();

					if (rs3.getInt(1) > 0) {
						hyperlink = "<a href=\"http://10.85.68.16/auftragsinfo/auftrag/show/" + rs.getString("abf_abnr") + 
					   "\" target=\"_blank\"><img src=\"info_button.png\" alt=\"LINK\" width=\"25px\" height=\"25px\"" +
					   " border=\"0\" hspace=\"20\"></a>";
						
					} else                 {
						hyperlink = "";
					}
					rs3.close();
				}
				catch (SQLException sqlex) {	
					System.out.println(sqlString3);
					sqlex.printStackTrace();	
				}

				sqlString2  = "SELECT pf_bez,pp_pf_platz ";
				sqlString2 += " FROM plrv11.bde_pufpl";
				sqlString2 += " INNER JOIN plrv11.bde_pfdat ON pf_pfnr=pp_pfnr ";
				sqlString2 += " WHERE (pp_abnr="+rs.getString("abf_abnr");
				sqlString2 += " OR pp_ab2="+rs.getString("abf_abnr");
				sqlString2 += " OR pp_ab3="+rs.getString("abf_abnr");
				sqlString2 += ") AND (pp_kz='J' OR pp_kz='B') ORDER BY pp_pf_platz";
				stmt2.executeQuery(sqlString2);
				rs2 = stmt2.getResultSet();
				puffer ="<TABLE BORDER='0'>";
				while (rs2.next()) {
					puffer += "<TR ";

					if (rs2.getString("pp_pf_platz").substring(5,7).compareTo("30")==0) {
						puffer += " style=\"background-color:yellow\"";
					}
					puffer += "><TD>";
					puffer += rs2.getString("pf_bez")+":</TD><TD>";
					puffer += rs2.getString("pp_pf_platz").substring(2,5)+".";
					
					if (rs2.getString("pf_bez").compareTo("PKZ")!=0) {
						// Anzeige für Zonen
						puffer += rs2.getString("pp_pf_platz").substring(5,7)+".";
						puffer += rs2.getString("pp_pf_platz").substring(7,9)+".";
						puffer += rs2.getString("pp_pf_platz").substring(9)+"\n";
					} else {
						puffer += rs2.getString("pp_pf_platz").substring(5,8)+".";
						puffer += rs2.getString("pp_pf_platz").substring(8)+"\n";
					}
					puffer +="</TD><TR>\n";
				}
				puffer +="</TABLE>";
				rs2.close();


				// Eigentliche Ausgabe der Zeile
				tabelle +="<TR BGCOLOR=#"                    + farbe.getHtmlColor()+"><TD "+prioFarbe+">";
				tabelle +=rs.getString("abf_abnr")           + "<BR>" + rs.getString("ako_kunde")+"</TD><TD ALIGN=CENTER>"  ;
				tabelle +=rs.getString("abf_pnr")            + "<BR>Platz: "+platzNr+"</TD><TD>" ;
				tabelle +=fsUndProg                          + "</TD><TD>" ;
				tabelle +=rs.getString("abf_fs_status")      + "</TD><TD>" ;
				tabelle +=rs.getString("abf_pf_status")      + "</TD><TD>" ;
				tabelle +=rs.getString("abf_dezi")           + "</TD><TD>" ;
				tabelle +=rs.getString("abf_fabt_pps")       + "</TD><TD "+fehlerFarbe+">" ;
				tabelle +=wechselFabt + "<BR>"+wechselUhrzeit+ "</TD>";
				tabelle +=senString;
				tabelle +="<TD>"+puffer+"</TD>";
/*				tabelle +="<TD>" + rs.getString(19) + "<BR>" + rbEndeUhrzeit + "</TD><TD>" +
				tabelle +=rs.getString(21) + "<BR>" + maxMlUhrzeit  + "</TD>"+ */
				tabelle +=split;
				tabelle +="<TD>" + rs.getString("ako_vlz") + "</TD>"; 
				tabelle +="<TD>" + hyperlink + "</TD>";
				tabelle += "</TR>\n";

			}
			rs.close();
			stmt3.close();
			stmt2.close();
			stmt.close();
		}
		catch (SQLException sqlex) {	
			System.out.println(sqlString);
			sqlex.printStackTrace();	
		}
		tabelle+="</TABLE>"; // + sqlString;
		retString += "<CENTER>Selektion der zu verwaltenden Auftr&auml;ge</CENTER>"+
		tabelle +"</CENTER>";	
		return retString;
	}
	public String vorSplit(HttpServletRequest iRequest) {
		String retString           = new String();
		int    anzSplit;
		String sqlString           = new String();
		try {
			anzSplit=Integer.parseInt(iRequest.getParameter("splitAnz"));
			retString += "</HEAD><BODY bgcolor=\"#CCCCCC\">"+
				"<CENTER>Splitten einer Fertigungsstufe</CENTER>"+
				"<TABLE BORDER=1><FORM ACTION=\""+appRoot+"bde\">";
			for (int i=1;i<=anzSplit;i++) {
				if (i==1) {
					retString += "<TR><TD>"+ i + ". Bearbeiter</TD><TD>";
					retString += "<INPUT TYPE=\"text\" NAME=\"pNr"+i+"\" SIZE=4 MAXLENGTH=4 VALUE=\""+iRequest.getParameter("pNr")+"\"></TD></TR>\n";
				} else {
					retString += "<TR><TD>"+ i + ". Bearbeiter</TD><TD><INPUT TYPE=\"text\" NAME=\"pNr"+i+"\" SIZE=4 MAXLENGTH=4></TD></TR>\n";
				}
			}
			retString+="<INPUT TYPE=\"hidden\" NAME=\"abNr\" VALUE=\""+iRequest.getParameter("abNr")+"\">"+
				"<INPUT TYPE=\"hidden\" NAME=\"fs\"       VALUE=\""   +iRequest.getParameter("fs")+"\">"+
				"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"planung\">"+
				"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"split\">"+
				"<INPUT TYPE=\"hidden\" NAME=\"anzSplit\" VALUE=\""   +iRequest.getParameter("splitAnz")+"\">"+		
				"<TR><TD><INPUT TYPE=\"SUBMIT\" VALUE=\"Splitten\"></TD></TR></FORM></TABLE>";
		}
		catch(Exception ex) {
			System.out.println("vorSplit :");
			ex.printStackTrace();
		}
		return retString;
	}
	public String split(HttpServletRequest iRequest) {
		int i=0;
		Statement stmt;
		ResultSet rs;
		String retString   = new String();
		String sqlString   = new String();
		String error       = new String();
		int anzSplit;
		textDatei tddDatei = new textDatei();
		tddDatei.setNameTimestamp(nachTddDir,"AA",".tdd");
		try {
			anzSplit = Integer.parseInt(iRequest.getParameter("anzSplit"));
			stmt = con.createStatement();
			// Zur Sicherheit aenderungsindex erfragen
			sqlString = "SELECT as_aend_ix FROM plrv11.plr_auftr_status WHERE as_status<>99 AND as_abnr="+iRequest.getParameter("abNr");
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			rs.next();
			aendIx = rs.getInt("as_aend_ix");
			// error += "Bis zum eigentlichen Aufsplitten hat alles funktioniert<BR>";
			for(i=1;i<=anzSplit;i++) {
				error += i +"er split:<BR>";
				if (i==1) {
					// Null darf nicht eingegeben werden
					// Den ersten (bereits vorhandenen) Datensatz updaten (Personalnummer und Split-KZ)...
					// Wenn alte Personalnummer==neue Personalnummer :
					sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_dezi=0,abf_spl_kz='J',abf_pnr=" + iRequest.getParameter("pNr"+i) +
						" WHERE abf_abnr    =" + iRequest.getParameter("abNr") +
						" AND   abf_fs      ='" + iRequest.getParameter("fs") + "'"+
						" AND   abf_aend_ix ="+aendIx;
					// ==> nur Splitkennzeichen auf "J"
					// Wenn alte Personalnummer!=neue Personalnummer 
					// Wenn alte Personalnummer==0 : Einplanen
					// Wenn alte Personalnummer!=0 : Umplanen
				} else {
					// ... und den Rest neu einfuegen (Achtung: Was passiert wenn 2 gleiche PNR eingetragen werden)
					// Dies sind alles Einplanungen!
					sqlString = "SELECT abf_abnr,abf_aend_ix,abf_prog,abf_prio,abf_fabt_pps," +
						"abf_ordnr,abf_fs,abf_tr,abf_te,abf_dezi,abf_pnr,abf_fs_status,abf_pf_status,abf_spl_kz FROM " +
						"plrv11.bde_ab_fs_ma WHERE "+
						"abf_abnr="+iRequest.getParameter("abNr") + 
						" AND abf_fs='"+iRequest.getParameter("fs") + "'" +
						" AND abf_aend_ix="+aendIx;
					stmt.executeQuery(sqlString);	
					error += sqlString +"<BR>";
					rs = stmt.getResultSet();
					rs.next();
					//    error += "--- ok<BR>";
					retString += rs.getString("abf_abnr") +"<BR>";
					sqlString = "INSERT INTO plrv11.bde_ab_fs_ma (abf_abnr,abf_aend_ix,abf_prog,abf_prio,abf_fabt_pps,"+
						"abf_ordnr,abf_fs,abf_tr,abf_te,abf_dezi,abf_pnr,abf_fs_status,abf_pf_status,abf_spl_kz) VALUES ("+
						rs.getString("abf_abnr")    + ","  +
						rs.getString("abf_aend_ix") + ","  +
						rs.getString("abf_prog")    + ","  +
						rs.getString("abf_prio")    + ","  +
						rs.getString("abf_fabt_pps")+ ","  +
						rs.getString("abf_ordnr")   + ",'" +
						rs.getString("abf_fs")      + "'," +
						rs.getString("abf_tr")      + ","  +
						rs.getString("abf_te")      + ","  +
						"0"                         + ","  + // Dezi muessen gleich 0 sein
						iRequest.getParameter("pNr"+i)+"," +
						"5"+","  + // fs-Status wird fuer alle Splits  =5 gesetzt
						rs.getString("abf_pf_status") +",'J')";
				}
				abNr     = Integer.parseInt(iRequest.getParameter("abNr"));
				pNr      = Integer.parseInt(iRequest.getParameter("pNr"+i));
				fsStatus = 5; // Fs-Status soll nach Splitt immer = 5 sein
				// Meldung an TDD 'raushauen
				tddDatei.write("07"+"11"+"A001" + bdeZeit.getTimestamp("ddMMyyyyHHmmss") +
					"059" +"01"+ tddDatei.format('0',6,String.valueOf(abNr))+
					"AD"  +"11"+ tddDatei.format('0',4,String.valueOf(pNr))+
					"0000"+
					iRequest.getParameter("fs") + tddDatei.format('0',2,String.valueOf(fsStatus))+"J\r\n"); 
				//error += "Meldung geschrieben.<BR>";
				stmt.executeUpdate(sqlString);		
				// Meldezeiten schreiben
				sqlString = "INSERT INTO plrv11.bde_ab_mz (abm_abnr,abm_aend_ix,abm_fs,abm_fs_status,abm_sendtime,abm_pnr,abm_status,abm_kz_auto) "+
				"VALUES ("+abNr+
				"," +aendIx+
				",'"+iRequest.getParameter("fs") +
				"',"+fsStatus+
				",'"+bdeZeit.getTimestamp("yyMMddHHmm") +
				"',"+pNr+
				",10,'N')";// Meldungsstatus ist immer = 10
				stmt.executeUpdate(sqlString);
			}
			// Endesatz fuer TDD
			tddDatei.writeln("0702E999"+bdeZeit.getTimestamp("ddMMyyyyHHmmss")+"042999999999911"+tddDatei.format('0',4,String.valueOf(i-1)));
			retString +="</HEAD><BODY bgcolor=\"#CCCCCC\" >"+
				"<CENTER>Fertigungsstufe aufgesplittet</CENTER><BR>";
			rs.close();
			stmt.close();
		}
		catch (Exception ex) {	
			ex.printStackTrace();
		}
		return retString;
	}
	public String getPaketAuswahl() {
		Statement stmt;
		ResultSet rs;
		String retString  = new String();
		String sqlString  = new String();
		String altPfPl    = new String();
		String aktPfPl    = new String(); 
		String paketAbNr     = new String();
		String paketPfPl     = new String();	
		String paketFsPrg    = new String();
		String paketFsStatus = new String();
		String paketPfStatus = new String();
		String paketDezi     = new String();
		String paketPrio     = new String();
		String paketFabt     = new String();
		String paketZuteil   = new String();
		String paketAbNrn    = new String();
		pen    farbe     = new pen(221,255,221,221,221,255);
		sqlString  = "SELECT ";
		sqlString += "abf_abnr,abf_prog,abf_prio,abf_fabt_pps,";
		sqlString += "abf_ordnr,abf_fs,abf_te,abf_dezi,abf_pnr,";
		sqlString += " abf_fs_status,abf_pf_status,abf_spl_kz,";
		sqlString += "ma_ap,pp_pf_platz,pp_zuteil_kz,abf_pf_platz,ako_kunde ";
		sqlString += "FROM ";
		sqlString += "plrv11.bde_ab_fs_ma INNER JOIN ";
		sqlString += "plrv11.plr_auftr_status ON (as_abnr=abf_abnr AND as_aend_ix=abf_aend_ix) INNER JOIN ";
		sqlString += "plrv11.bde_pufpl ON (abf_pf_platz=pp_pf_platz) INNER JOIN ";
		sqlString += "plrv11.plr_auftr_kopf ON (ako_abnr=abf_abnr AND ako_aend_ix=abf_aend_ix) LEFT OUTER JOIN ";
		sqlString += "plrv11.bde_madat ON (abf_pnr=ma_pnr) ";
		sqlString += "WHERE ";
		sqlString += "abf_fs_status=1 AND ";
		sqlString += "as_status<>99 ";
		sqlString += "ORDER BY abf_pf_platz,abf_abnr ";
		retString += "<CENTER>Liste der paketierten Auftr&auml;ge";
		retString +="<TABLE BORDER=1>";
		retString +="<TR><TD>Ab-Nr / KD-Nr</TD><TD>FS<BR>Programm</TD><TD>FS-Status</TD>";
		retString +="<TD>Puffer- Status</TD><TD>Dezi</TD><TD>Prio</TD><TD>Polst- Tag</TD></TR>\n";
		try {
			stmt = con.createStatement();
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()) {
				aktPfPl = rs.getString("abf_pf_platz");
				if (aktPfPl.compareTo(altPfPl)==0) {
					paketAbNr     += rs.getString("abf_abnr") +" / "+ rs.getString("ako_kunde") +"<BR>";
					paketAbNrn    += "," +rs.getString("abf_abnr");
					// paketFsPrg    += "<A HREF=\"" + appRoot + "bde?modul=planung&formular=maPaket&fs=" + 
					//	rs.getString("abf_fs") + "&pNr=" + rs.getString("abf_pnr") + "&abNr="+
					//	paketAbNrn+"&prog="+rs.getString("abf_prog")+"\" target=list>";
					paketFsPrg    += rs.getString("abf_fs") + " / "+ rs.getString("abf_prog");
					//paketFsPrg    += "</A>";
					paketFsPrg    += "<BR>";
					paketFsStatus += rs.getString("abf_fs_status") +"<BR>";
					paketPfStatus += rs.getString("abf_pf_status") +"<BR>";
					paketDezi     += rs.getString("abf_dezi")      +"<BR>";
					paketPrio     += rs.getString("abf_prio")      +"<BR>";
					paketFabt     += rs.getString("abf_fabt_pps")  +"<BR>";
					paketZuteil   += rs.getString("pp_zuteil_kz")  +"<BR>";
				} else {
					if (altPfPl.compareTo("")!=0) {
						farbe.change();
						retString +="<TR BGCOLOR=#" + farbe.getHtmlColor()+"><TD>" + paketAbNr +"</TD><TD>"+ paketFsPrg + "</TD><TD>" + 
						paketFsStatus+"</TD><TD>"+paketPfStatus+"</TD><TD>"+paketDezi+"</TD><TD>"+paketPrio+"</TD><TD>"+
						paketFabt+"</TD><TD>"+paketZuteil +"</TD></TR>\n";
					}
					paketAbNr     = rs.getString("abf_abnr") +" / "+ rs.getString("ako_kunde") +"<BR>";
					paketAbNrn    = rs.getString("abf_abnr");
					// paketFsPrg    = "<A HREF=\"" + appRoot + "bde?modul=planung&formular=maPaket&fs=" ;
					// paketFsPrg    += rs.getString("abf_fs") + "&pNr=" + rs.getString("abf_pnr") + "&abNr="+
					// paketFsPrg    += paketAbNrn +"&prog="+rs.getString("abf_prog")+"\" target=list>" +
					paketFsPrg    = rs.getString("abf_fs") + " / "+ rs.getString("abf_prog") +"</A>";
					paketFsPrg    +=" <BR>";
					paketFsStatus = rs.getString("abf_fs_status") + "<BR>";
					paketPfStatus = rs.getString("abf_pf_status") + "<BR>";
					paketDezi     = rs.getString("abf_dezi")      + "<BR>";
					paketPrio     = rs.getString("abf_prio")      + "<BR>";
					paketFabt     = rs.getString("abf_fabt_pps")  + "<BR>";
					paketZuteil   = rs.getString("pp_zuteil_kz")  + "<BR>";
					}
				altPfPl=aktPfPl;
			}
			farbe.change();
			retString +="<TR BGCOLOR=#" + farbe.getHtmlColor()+"><TD>" + paketAbNr +"</TD><TD>"+ paketFsPrg + "</TD><TD>" +
				paketFsStatus + "</TD><TD>" + paketPfStatus +"</TD><TD>"+paketDezi+"</TD><TD>"+paketPrio +"</TD><TD>"+
				paketFabt+"</TD><TD>"+paketZuteil+"</TD></TR>\n";
			rs.close();
			stmt.close();
		}
		catch(SQLException sqlex) {
			sqlex.printStackTrace();
		}
		retString +="</TABLE></CENTER>";
		return retString;
	}
	public int paketPlanung(int[] iAbNr,long iFs, int iPNr, int iaPNr) {
		/* *********************************************************************************************** */
		/* überarbeitete Variante der manuelle Verplanungsfunktion für Werksneuordnung 2006                */
		/* *********************************************************************************************** */
		/* Rueckgabewert: -10 : Inkonsistente Daten, Mehr als ein Datensatz erhalten, wo unique erwartet   */
		/*                -16 : Inkonsistente Daten, Ab-Nr steht auf mehreren Pufferplaetzen               */
		/*                -17 : Inkonsistente Daten,                                                       */
		/*                -18 : Umplanung: Bisheriger Arbeitsplatz konnte nicht gefunden werden            */
		/*                -20 : Es Auftragskopfdaten nicht vollständig                                     */
		/*                -30 : alte Personalnummer stimmt nicht mehr, Autrag wurde anderweitig verplant   */
		/*                -1  : Algemeiner SQL-Fehler: SQLException wurde geworfen                         */
		/* Pakete koennen nicht umgeplant werden, nur eingeplant                                           */
		/* ----------------===============----------------------                                           */
		
		int i,j;
		int anzPowa = 0;
		String sqlString    = new String();
		String neuPKz	    = new String();
		String zielPKz	    = new String();
		String zielPPlatz   = new String();
		String vopo         = new String();
		String maAp         = new String("");
		String altMaAp      = new String("");
		String pfKz         = new String();
		String pfr          = new String();
		String poma         = new String();
		String errors       = new String();
		String splitKz      = new String();
		String fafArt       = new String();
		String pol          = new String();
		String deziNull     = new String("");
		String pufSqlString = new String("");
		String relevantePuffer= new String();
		int[] progFeld  = new int[iAbNr.length];
		int[] fabtFeld  = new int[iAbNr.length];
		String[] sfKzFeld = new String[iAbNr.length];
		String[] sTextFeld= new String[iAbNr.length];
		int fsStatus    = 0;
		int neuFsStatus = 1;
		int aendIx      = 0;
		int fertOrt     = 0;
		int sspFabt     = 0;
		int sspUhr      = 0;
		int mlMinFabt   = 0;
		int mlMinUhr    = 0;
		int mlMaxFabt   = 0;
		int mlMaxUhr    = 0;
		int pfNr =0;
		String pfPlatz  = new String();
		String pfZone   = new String();
		textDatei error            = new textDatei(errorDir,"error.log");
		try {
			error.write("-------------------------------------\n");
			error.write("Manuelle Planung\n");
			error.write("-------------------------------------\n");
			Statement stmt;
			ResultSet rs;
			stmt = con.createStatement();
			i=0;
			while (iAbNr[i]!=0) {
				// Hier zur Sicherheit Kontrollieren, ob die alte Personal-Nummer noch übereinstimmt
				sqlString  = "SELECT abf_abnr,abf_pnr ";
				sqlString += "FROM plrv11.bde_ab_fs_ma ";
				sqlString += "INNER JOIN plrv11.plr_auftr_status ON abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix ";
				sqlString += "WHERE as_status<>99 ";
				sqlString += " AND abf_abnr="+iAbNr[i];
				sqlString += " AND abf_fs="  +iFs;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					error.writeln("In DB:"+rs.getInt("abf_pnr")+" / In Formular:"+iaPNr);
					if (iaPNr!=rs.getInt("abf_pnr")) {
						error.writeln("Falsche alte Personalnummer!!!");
						return -30;
					}
					error.writeln("Alles ok!");
				}
				rs.close();
				i++;
			}
			stmt.close();
		} 
		catch (Exception ex) {
			error.writeln(ex.getMessage());
			error.writeln(sqlString);
			error.write("PaketPlanung: Veraltete Daten: Personalnummer geändert\n");
		}
		/* Dateiarbeit vorbereiten */
		fafDatei fahrAuftragDatei  = new fafDatei(vomTddDir+"/"+bdeZeit.getTimestamp("ddHHmmssSSS")+".faf");
		try {
			Statement stmt;
			ResultSet rs;
			Statement stmt2;
			stmt = con.createStatement();
			stmt2 = con.createStatement();
			// Suchen des Ziels fuer die Fertigungsstufen  -  Oder: Wo ist der Mitarbeiter gerade angemeldet?
			// bzw. welcher Pufferplatz soll angefahren werden
			sqlString  = "SELECT ma_ap,pp_pf_platz,pp_pfzone,pp_kz ";
			sqlString += "FROM plrv11.bde_madat ";
			sqlString += "INNER JOIN plrv11.bde_apdat ON ma_ap=ap_platznr ";
			sqlString += "INNER JOIN plrv11.bde_pufpl ON pp_fs=ap_fs ";
			sqlString += "WHERE ma_pnr=" + iPNr;
			sqlString += " AND ap_fs='"+iFs+"'";
			sqlString += " AND (pp_kz='U' OR pp_kz='N')";
			sqlString += " AND pp_pfnr='10" +String.valueOf(iFs).substring(2,5) + "300'";
			sqlString += " AND substr(pp_pf_platz,1,9)=CONCAT('10" +String.valueOf(iFs).substring(2,5) + "30',substr(ma_ap,10,2)) ";
			sqlString += " ORDER BY pp_kz,pp_pf_platz";

			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			j=0;
			while (rs.next() && (j<1)){
				zielPPlatz = rs.getString("pp_pf_platz");
				pol = rs.getString("pp_pfzone");
				altMaAp = rs.getString("ma_ap");
				zielPKz = rs.getString("pp_kz"); // Ziel ist hier ein Pufferplatz
				j++;
			} 
			rs.close();
			// Zusätzliche Termine suchen für diese Fertigungsstufe (25.03.02)
			sqlString = "SELECT a.sko_fabt,a.sko_uhrzeit,b.sko_fabt,b.sko_uhrzeit,c.sko_fabt,c.sko_uhrzeit " +
				" FROM  plrv11.plr_auftr_status, "+
				" plrv11.plr_subsys_kopf a, plrv11.plr_subsys_kopf b, plrv11.plr_subsys_kopf c " +
				" WHERE as_status<>99 AND" +
				" a.sko_abnr="+ iAbNr[0] +" AND a.sko_aend_ix=as_aend_ix AND a.sko_subsys=7  AND a.sko_logsys=7 AND " + // Fabt_polst_start
				" b.sko_abnr="+ iAbNr[0] +" AND b.sko_aend_ix=as_aend_ix AND b.sko_subsys=10 AND b.sko_logsys=11 AND "+ // Min ML
				" c.sko_abnr="+ iAbNr[0] +" AND c.sko_aend_ix=as_aend_ix AND c.sko_subsys=10 AND c.sko_logsys=19 ";     // Max ML
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()) {
				sspFabt  = rs.getInt(1);
				sspUhr   = rs.getInt(2);
				mlMinFabt= rs.getInt(3);
				mlMinUhr = rs.getInt(4);
				mlMaxFabt= rs.getInt(5);
				mlMaxUhr = rs.getInt(6);
			}
			rs.close();
			// Fuer alle Ab-Nummern im Paket
			i=0;
			while (iAbNr[i]!=0) {
				// Fertigungsstufe suchen, die geplant werden soll
				sqlString = "SELECT abf_abnr,as_aend_ix,abf_fs,abf_fs_status,abf_prog,abf_pnr,abf_spl_kz,abf_fabt_pps,abf_aend_ix,ako_sf_hw_pos,ako_anz_powa "+
				" FROM plrv11.bde_ab_fs_ma "+
				" INNER JOIN plrv11.plr_auftr_status ON abf_abnr=as_abnr  AND abf_aend_ix=as_aend_ix "+
				" INNER JOIN plrv11.plr_auftr_kopf   ON abf_abnr=ako_abnr AND abf_aend_ix=ako_aend_ix "+
				" WHERE as_status <> 99 "+
				" AND   abf_abnr=" + iAbNr[i] +
				" AND   abf_fs  ='"+ iFs +"'";
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				// Informationen, die direkt fuer das Schreiben in die Datei gespeichert werden
				error.write("Aktuelle Ab-Nr: "+iAbNr[i]+"\n");
				j=0;
				while (rs.next()) {
					if (j!=0) {
						return -10; // Es wurden mehrere Datensaetze gefunden. Inkonsistente Daten. 'raus mit Fehlermeldung
					} else {
						progFeld[i] = rs.getInt("abf_prog");
						fabtFeld[i] = rs.getInt("abf_fabt_pps");
						sfKzFeld[i] = rs.getString("ako_sf_hw_pos");
						fsStatus    = rs.getInt("abf_fs_status");
						splitKz     = rs.getString("abf_spl_kz");
						aendIx      = rs.getInt("abf_aend_ix");
						anzPowa     = rs.getInt("ako_anz_powa");
					}
					j++;
				}
				if (fsStatus>5) deziNull=", abf_dezi=0 ";
				rs.close();
				if (j==0) return -20; // Auftragskopfdaten sind nicht vollständig 
				// Evtl. vorhandene Sondertexte für den jeweiligen Auftrag suchen
				sqlString = "SELECT akt_text FROM plrv11.plr_ak_texte WHERE akt_abnr="+iAbNr[i]+
					" AND akt_text_pos=0 AND akt_text_art='X' AND akt_aend_ix="+aendIx;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				j=0;
				sTextFeld[i]=" ";
				while (rs.next()) {
					if (j!=0) {
						return -10; // Es wurden mehrere Datensaetze gefunden. Inkonsistente Daten. 'raus mit Fehlermeldung
					} else {
						sTextFeld[i] = rs.getString("akt_text");
					}
				}
				rs.close();
				// FAF-Datei Erstellung: Allgemeiner Fall ***************** Muss das für jede AB-Nr einzeln gemacht werden?
				// 144 oder 245 werden der Fahrauftragsart angehängt
				fertOrt = Integer.parseInt(String.valueOf(iFs).substring(2,5));
				// error.write("FS-Status:"+fsStatus+"\n");
				if (fsStatus<5) {
					// Zuweisung
					fafArt = "Z";
					error.writeln("Zuweisung : Relevante Puffer suchen\n");
					if (iFs==Long.parseLong("10144100000")) {
						// relevantePuffer = " (pp_pfnr=10144100 OR pp_pfnr=10245100 OR pp_pfnr=10144300)";
						relevantePuffer = " (pp_pfnr=10144100 OR pp_pfnr=10144300)";
					} else {
						relevantePuffer = " (pp_pfnr=10144300 OR pp_pfnr=10245100 OR pp_pfnr=10245200 OR pp_pfnr=10245300 OR pp_pfnr=10245500)";
					}
					error.writeln("Relevante Puffer: "+relevantePuffer+"\n");
				} else {
					// Umplanung
					fafArt = "U";
					error.writeln("Umplanung 1 : Relevante Puffer suchen \n");
					if (iFs==Long.parseLong("10144100000")) {
						relevantePuffer = " (pp_pfnr=10144300) ";
					} else {
						relevantePuffer = " (pp_pfnr=10245300) ";
						sqlString = "SELECT ma_ap FROM plrv11.bde_madat WHERE ma_pnr="+iaPNr;
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						while (rs.next()) {
							maAp=rs.getString("ma_ap").substring(9,11);
						}
						rs.close();
						pol  = altMaAp.substring(9,11);
						vopo = maAp;
					}
				}
				// Suche den Puffer, aus der die FS geholt werden soll
				sqlString  = "SELECT pp_abnr,pp_fs,pp_pf_platz,pp_pfzone,pp_kz,pp_pfnr ";
				sqlString += "FROM plrv11.bde_pufpl ";
				sqlString += "WHERE  "+relevantePuffer;
				sqlString += " AND (pp_abnr=" +iAbNr[i] + " OR pp_ab2=" + iAbNr[i] + " OR pp_ab3=" +iAbNr[i];
				sqlString += ")  AND (pp_kz='J' OR pp_kz='B')";
				// error.write("\n"+sqlString+"\n");
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				// ACHTUNG!!! PAKETIERTE AUFTRAEGE BISHER immer 10245
				j=0;
				while (rs.next()) {
					pfNr    = rs.getInt("pp_pfnr");
					pfPlatz = rs.getString("pp_pf_platz");
					pfZone  = rs.getString("pp_pfzone");
					pfKz    = rs.getString("pp_kz");
					if (fsStatus<5) {
						error.write("Zuweisung 2 : "+pfNr+" - " +pfPlatz+"\n");
						if (iFs==Long.parseLong("10144100000")) {
							if (pfNr==10144100) vopo = pfZone;
							if (pfNr==10144300) {
								fafArt="U"; // Umplanung vom einen Puffer in den anderen
								vopo = pfZone;
							}
							pfr  = "  ";
							error.write("\nFS:"+iFs+"- vopo:"+vopo+"\n");
						} else {
							if (pfNr==10144300) vopo = pfZone;
							if (pfNr==10245100) {
								poma = pfPlatz;
							}
							if (pfNr==10245200) {
								fafArt="P";
								pfr  = pfZone;
							}
							if (pfNr==10245300) {
								fafArt="U"; // Umplanung vom einen Puffer in den anderen
								vopo  = pfZone;
							}
							if (pfNr==10245500) {
								fafArt="F"; // Umplanung vom einen Puffer in den anderen
								vopo  = pfZone;
							}
							error.write("\n FS:"+iFs+"- vopo:"+vopo+"\n");
						}
						iPNr=0;
					} else {
						error.write("Umplanung 2 : ");
						if (iFs==Long.parseLong("10144100000")) {
							if (pfNr==10144300) vopo = pfZone;
							poma= "   ";
							pfr = "  ";
							error.write("FS:"+iFs+" - vopo:"+vopo+"\n");
						} else {
							// FS steht nicht mehr im Puffer sondern am MA-Platz
							// vopo wurde praeventiv auf maAp gesetzt
							poma = "   ";
							pfr  = "  ";
							error.write("FS:"+iFs+" - vopo:"+vopo+"\n");
						}
						neuFsStatus=5;
					}
					// SQLString für das leeren des Pufferplatzes
					if (pfKz.compareTo("J")==0) neuPKz="N";
					if (pfKz.compareTo("B")==0) neuPKz="U";
					pufSqlString = "UPDATE plrv11.bde_pufpl SET pp_kz='"+neuPKz+"',pp_zuteil_kz='N',pp_abnr=0,pp_ab2=0,pp_ab3=0 WHERE pp_pfnr="+pfNr;
					pufSqlString += " AND pp_pf_platz='"+pfPlatz+"'";
					error.write("SQL zur Freigabe der Puffer: "+pufSqlString+"\n");
					stmt2.executeUpdate(pufSqlString);
					j++;
				}
				if ((fsStatus>=5) && (iFs==Long.parseLong("10245100000"))) {
					// Das Ding
					neuFsStatus=5;
				}
				fafArt += fertOrt;
				/* *********************************************************************************** */
				// Fertigungsstufen-Status hochsetzen und Mitarbeiter eintragen in ab_fs_ma
				//  Auf gesplittete Auftraege muss hier keine Ruecksicht genommen werden.
				sqlString  = "UPDATE plrv11.bde_ab_fs_ma SET ";
				sqlString += " abf_pnr= "+iPNr;
				sqlString += ", abf_fs_status= "+neuFsStatus;
				sqlString += deziNull;
				sqlString += " WHERE abf_abnr=" +iAbNr[i];
				sqlString += " AND   abf_fs  ='"+iFs;
				sqlString += "' AND abf_aend_ix="+aendIx;
				stmt.executeUpdate(sqlString);
				// Eintrag in die Meldezeiten-Tabelle schreiben
				sqlString  = "INSERT INTO plrv11.bde_ab_mz "; 
				sqlString += " (abm_abnr,abm_aend_ix,abm_fs,abm_fs_status,abm_sendtime,abm_pnr,abm_status,abm_kz_auto) ";
				sqlString += "VALUES ("+iAbNr[i];
				sqlString += ","       +aendIx;
				sqlString += ",'"      +iFs;
				sqlString += "',1,'"   +bdeZeit.getTimestamp("yyMMddHHmm"); // FS-Status ist immer 1, weil es sich um eine Einplanung handel
				sqlString += "',"      +iPNr;
				sqlString += ",10,'N')";
				stmt.executeQuery(sqlString);
				if (fsStatus>=5) {
					error.write("BDE/TDD-Dateien werden geschrieben\n");
					// TDD- und BDE-Dateien (die im Prinzip die gleichen sind) schreiben. Fuer jede AB-Nr einen
					bdeDatei tddDat            = new bdeDatei(nachTddDir+"/AA"+bdeZeit.getTimestamp("yyMMddHHmmssSSS")+".tdd");
					bdeDatei bdeDat            = new bdeDatei(vomTddDir+"/AA"+bdeZeit.getTimestamp("ddHHmmssSSS")+".bde");
					tddDat.oeffnen();
					tddDat.setVonSubsystem(7);
					tddDat.setNachSubsystem(11);
					tddDat.setEreignisAktion("E001");
					tddDat.setPaketFolgeNr(90);
					tddDat.setAuftragsNr(iAbNr[i]);
					tddDat.setInternerVorgang("AD");
					tddDat.setBezogenesSubsystem(11);
					tddDat.setNeuPersonalNr(iPNr);
					tddDat.setAltPersonalNr(iaPNr);
					tddDat.setFertigungsstufe(iFs);
					tddDat.setFsStatus(5);
					tddDat.setFsSplit('N');
					tddDat.schreibeSatz();
					
					bdeDat.oeffnen();
					bdeDat.setVonSubsystem(7);
					bdeDat.setNachSubsystem(2);
					bdeDat.setEreignisAktion("A001");
					bdeDat.setPaketFolgeNr(90);
					bdeDat.setAuftragsNr(iAbNr[i]);
					bdeDat.setInternerVorgang("ZU");
					bdeDat.setBezogenesSubsystem(7);
					bdeDat.setNeuPersonalNr(iPNr);
					bdeDat.setAltPersonalNr(iaPNr);
					bdeDat.setFertigungsstufe(iFs);
					bdeDat.setFsStatus(5);
					bdeDat.setFsSplit('N');
					bdeDat.schreibeSatz();
					// Endesaetze fuer TDD- und BDE-Dateien
					tddDat.schreibeEndeSatz();
					bdeDat.schreibeEndeSatz();
					tddDat.schliessen();
					bdeDat.schliessen();
					tddDat.aktiviereDatei();
					bdeDat.aktiviereDatei();
				}
				i++;
			}
			if ((fsStatus>=5) && (iFs==Long.parseLong("10245100000"))) {
				error.write("Es wird kein MA-Puffer Belegt\n");
			} else {
				// der Ziel-Puffer der Fertigungsstufe muss als belegt markiert werden.
				sqlString   = "UPDATE plrv11.bde_pufpl SET pp_abnr="+iAbNr[0];
				if (iAbNr[1]!=0) sqlString  += ",pp_ab2="+iAbNr[1];
				if (iAbNr[2]!=0) sqlString  += ",pp_ab3="+iAbNr[2];
				if (zielPKz.compareTo("U")==0) {
					sqlString += ",pp_kz='B' WHERE pp_pf_platz='"+zielPPlatz+"'";
				} else {
					sqlString += ",pp_kz='J' WHERE pp_pf_platz='"+zielPPlatz+"'";
				}
				stmt.executeQuery(sqlString);
			}

			// Der folgende Teil ist fuer alle Ab-Nrn des Pakets gleich
			// Fahrauftrag-Datei schreiben
			fahrAuftragDatei.oeffnen();
			fahrAuftragDatei.write(fafArt);
			fahrAuftragDatei.write(rbTextFormat.format('0',6,String.valueOf(iAbNr[0])));
			fahrAuftragDatei.write(rbTextFormat.format('0',3,String.valueOf(progFeld[0])));
			fahrAuftragDatei.write(rbTextFormat.format(' ',1,sfKzFeld[0]));
			fahrAuftragDatei.write(rbTextFormat.linksb(' ',50,sTextFeld[0]));
			fahrAuftragDatei.write(rbTextFormat.format(' ',3,vopo));
			fahrAuftragDatei.write(rbTextFormat.format(' ',2,pfr));
			fahrAuftragDatei.write(rbTextFormat.format(' ',3,pol));
			fahrAuftragDatei.write(rbTextFormat.format(' ',3,poma));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(fabtFeld[0])));
			/* ***********************   Zusätzliche Termine aus Subsystem-Kopf-Daten   *************************** */
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(sspFabt)));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(sspUhr)));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(mlMinFabt)));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(mlMinUhr)));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(mlMaxFabt)));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(mlMaxUhr)));
			/* ***********************   Anzahl Polsterwagen *************************** */
			fahrAuftragDatei.write(rbTextFormat.format(' ',1,String.valueOf(anzPowa)));      // Anzahl Polsterwagen
			/* ***********************   Zusätzliche AB-Nummern bei Paketiertem Auftrag *************************** */
			fahrAuftragDatei.write(rbTextFormat.format(' ',6,String.valueOf(iAbNr[1])));   // 2. Ab-Nr	
			fahrAuftragDatei.write(rbTextFormat.format(' ',3,String.valueOf(progFeld[1])));
			fahrAuftragDatei.write(rbTextFormat.format(' ',1,sfKzFeld[1]));
			fahrAuftragDatei.write(rbTextFormat.linksb(' ',50,sTextFeld[1]));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(fabtFeld[1])));
			fahrAuftragDatei.write(rbTextFormat.format(' ',6,String.valueOf(iAbNr[2])));// 3. Ab-Nr	
			fahrAuftragDatei.write(rbTextFormat.format(' ',3,String.valueOf(progFeld[2])));
			fahrAuftragDatei.write(rbTextFormat.format(' ',1,sfKzFeld[2]));
			fahrAuftragDatei.write(rbTextFormat.linksb(' ',50,sTextFeld[2]));
			fahrAuftragDatei.write(rbTextFormat.format(' ',4,String.valueOf(fabtFeld[2])) + "\n");
			fahrAuftragDatei.schliessen();
			fahrAuftragDatei.aktiviereDatei();
			rs.close();
			stmt.close();
		}
		catch (Exception ex) {
			error.write(ex.getMessage());
			error.write("PaketPlanung: Allgemeiner Fehler\n");
			error.write(sqlString+"\n");
			ex.printStackTrace();
			return -1;	
		}
		return 0;
	}
	public String maPaketZuweis(HttpServletRequest iRequest) {
		String retString           = new String();
		int abNrFeld[] = new int[5];
		long fs  = 0;
		int pNr  = 0;
		int lauf = 0;
		int rueck= 0;
		try { 	
	    		StringTokenizer aufbrecher = new StringTokenizer(iRequest.getParameter("abNr"),new String(","));
			fs  = Long.parseLong(iRequest.getParameter("fs"));
			pNr = Integer.parseInt(iRequest.getParameter("pNr"));
			retString += "Abnr: "+ iRequest.getParameter("abNr") +"<BR>" +
				"FS: "  + fs                           +"<BR>"+
				"Prog: "+ iRequest.getParameter("prog")+"<BR>";
			while (aufbrecher.hasMoreTokens()) {
				abNrFeld[lauf] = Integer.parseInt(aufbrecher.nextToken());
				retString += abNrFeld[lauf] + "<BR>";
				lauf++;
			}
			rueck = paketPlanung(abNrFeld,fs,pNr,0);
			if (rueck==0) {
				retString += "<FONT COLOR='green'>Einplanung wurde erfolgreich vorgenommen</FONT>";
			} else if (rueck==-30) {
				retString += "<FONT COLOR='orange'>PNr nicht mehr aktuell.<BR>Der Auftrag wurde bereits von einem anderen Mitarbeiter oder der Automatik verplant</FONT>";
			} else {
				retString += "<FONT COLOR='red'>Fehler bei der Verplanung!</FONT><BR>Fehlernr:"+rueck;
			}
		}
		catch (NumberFormatException nfe) {
			System.out.println("Klasse: fertigungsStufen, Methode: maPaketZuweis");
			nfe.printStackTrace();
		}
		return (retString);
	}
	public String maZuweis(HttpServletRequest iRequest) {
		String retString           = new String();
		int rueck    = 0;
		int abNr     = 0;
		long fs      = 0;
		int pNr      = 0;
		int aPNr     = 0;
		int abNrFeld[] = new int[5];
		textDatei zuwLogDatei   = new textDatei(errorDir,"zuweis.log");
		try {
			abNr = Integer.parseInt(iRequest.getParameter("abNr"));
			abNrFeld[0] = abNr;
			fs   = Long.parseLong(iRequest.getParameter("fs"));
			pNr  = Integer.parseInt(iRequest.getParameter("pNr"));
			aPNr = Integer.parseInt(iRequest.getParameter("aPNr"));

			rueck = paketPlanung(abNrFeld ,fs , pNr, aPNr);
			if (rueck==0) {
				retString += "<FONT COLOR='green'>Einplanung wurde erfolgreich vorgenommen</FONT>";
			} else if (rueck==-30) {
				retString += "<FONT COLOR='red'><B>PNr nicht mehr aktuell.<BR>";
				retString += "Der Auftrag wurde bereits von einem anderen Mitarbeiter oder der Automatik verplant<BR><BR>";
				retString += "Eine aktualisierte Darstellung erhalten Sie... ";
				retString += "<FORM ACTION=\"" + appRoot + "bde\" TARGET=\"main\" METHOD=\"GET\"><TD BGCOLOR=#FFDDDD>\n";
				retString += "<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"planung\"    >\n";
				retString += "<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"auswahl\"    >\n";
				retString += "<INPUT TYPE=\"hidden\" NAME=\"modus\"     VALUE=\"abNr\"       >\n";
				retString += "<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\"P\"          >\n";
				retString += "<INPUT TYPE=\"hidden\" NAME=\"abNr\"      VALUE=\""+abNr   +"\">\n";
				retString += "<INPUT TYPE=\"submit\" VALUE=\"hier\"></FORM>";
			} else {
				retString += "<FONT COLOR='red'><B>Fehler bei der Verplanung!</B></FONT><BR>Fehlernr:"+rueck;
			}
			zuwLogDatei.write("User:"+iRequest.getRemoteUser()      +" fs: "+iRequest.getParameter("fs"));
			zuwLogDatei.write(" AbNr:"+iRequest.getParameter("abNr")+" "+iRequest.getParameter("aPNr")+"->"+iRequest.getParameter("pNr")+"\n");
		}
		catch (NumberFormatException nfe) {
			System.out.println("Klasse: fertigungsStufen, Methode: maZuweis");
			nfe.printStackTrace();
		}
		return (retString);
	}
	public String bezugMelden (HttpServletRequest iRequest) {
		String retString =  new String();
		Statement stmt;
		retString +="</HEAD><BODY BGCOLOR=\"#CCCCCC\">";
		try {
			try {
				stmt = con.createStatement();
				stmt.executeUpdate("UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status=abf_pf_status+10 WHERE abf_fs='10245100000' AND abf_pf_status<40 AND abf_abnr="+Integer.parseInt(iRequest.getParameter("abNr")));
				retString +="Bezug wurde gemeldet<BR>";
				stmt.close();
			}
			catch(SQLException sqle) {
				sqle.printStackTrace();
				retString +="Ein Datenbank-Fehler ist aufgetreten.<BR>";
			}
		}
		catch(NumberFormatException nfe){
			nfe.printStackTrace();
			retString +="Ihre Eingaben waren nicht korrekt. AB-Nr muss eine Zahl sein.<BR>";
		}
		return retString;
	}
	public String getPaketEinarbeit (String iFs,int iProg,int[] iAbNr,int iaPNr) {
		String    retString = new String();
		Statement stmt;
		Statement stmtEinarb;
		Statement stmtVorrat;
		ResultSet rsEinarb;
		ResultSet rsVorrat;
		ResultSet rs;
		String    abNrListe= new String();
		int i=1;
		pen farbe = new pen(221,255,221,221,221,255);
		abNrListe = String.valueOf(iAbNr[0]);
		while (iAbNr[i]!=0) {
			abNrListe += "," + iAbNr[i];
			i++;
		}	
		retString += "M&ouml;gliche Mitarbeiter  f&uuml;r die paketierten Auftraege</BR>"+
			"<TABLE BORDER=0><TR><TD>"+
			"<TR><TD><B>AB-Nr</B></TD><TD>: "+abNrListe;
		
		retString += "</TD></TR><TR><TD><B>Programm</B></TD><TD>: "+ iProg +
		"</TD></TR><TR><TD><B>FS</B></TD><TD>: "+ iFs   +
		"</TD></TR></TABLE><TABLE BORDER=1><TR><TD>Platz</TD><TD>PNr</TD><TD>Name</TD><TD>EA</TD><TD>Vorrat</TD></TR>\n";
		try {
			stmt       = con.createStatement();
			stmtEinarb = con.createStatement();
			stmtVorrat = con.createStatement();
			stmt.executeQuery("SELECT map_pnr,ma_vname,ma_nname,ma_ap FROM plrv11.bde_madat,plrv11.bde_apdat,plrv11.bde_ma_prog "+
					"WHERE  map_pnr   = ma_pnr "+
					"AND    ma_ap     = ap_platznr "+
					"AND    map_fs    = ap_fs "+
					"AND    ma_anmeld ='J'  "+
					"AND    map_fs    = '"+iFs+"' "+
					"AND    map_prog  =  "+iProg+" ORDER BY ma_ap");
			rs=stmt.getResultSet();
			while (rs.next()) {
				farbe.change();
				stmtVorrat.executeQuery("SELECT COUNT(*) FROM plrv11.bde_ab_fs_ma WHERE abf_pnr="+rs.getString("map_pnr")+
							" AND (abf_fs_status=5 OR abf_fs_status=10 OR abf_fs_status=30)");
				stmtEinarb.executeQuery("SELECT COUNT(*) FROM plrv11.bde_ma_prog  WHERE map_pnr="+rs.getString("map_pnr"));
				rsVorrat=stmtVorrat.getResultSet();
				rsEinarb=stmtEinarb.getResultSet();
				rsVorrat.next();
				rsEinarb.next();
				if (rs.getInt("map_pnr")!=iaPNr) {
				retString += "<TR BGCOLOR=#"+farbe.getHtmlColor()+"><FORM ACTION=\"" + appRoot +
					"bde\" METHOD=\"GET\"><INPUT TYPE=\"HIDDEN\" NAME=\"modul\" VALUE=\"planung\">"+
					"<INPUT TYPE=\"HIDDEN\" NAME=\"formular\" VALUE=\"maPaketZuweisung\">"+
					"<INPUT TYPE=\"HIDDEN\" NAME=\"abNr\"     VALUE=\"" + abNrListe + "\">"+
					"<INPUT TYPE=\"HIDDEN\" NAME=\"fs\"       VALUE=\"" + iFs       + "\">"+
					"<INPUT TYPE=\"HIDDEN\" NAME=\"prog\"     VALUE=\"" + iProg     + "\">"+
					"<INPUT TYPE=\"HIDDEN\" NAME=\"aPNr\"     VALUE=\"" + iaPNr     + "\">" +
					"<INPUT TYPE=\"HIDDEN\" NAME=\"pNr\"      VALUE=\"" + rs.getString("map_pnr")+"\">\n"+
					"<TD>"+ rs.getString("ma_ap").substring(8,11) +"</TD><TD>" + rs.getString("map_pnr") + "</TD><TD>" +
					rs.getString("ma_nname")+", "+rs.getString("ma_vname") + "</TD><TD>"+
					rsEinarb.getString(1) + "</TD><TD>"+rsVorrat.getString(1) +
					"</TD><TD><INPUT TYPE=\"SUBMIT\" VALUE=\"Zuweisen\"></TD></FORM></TR>\n";
				}
				rsVorrat.close();
				rsEinarb.close();
			}
			rs.close();
			stmtVorrat.close();
			stmtEinarb.close();
			stmt.close();
		}
		catch (SQLException sqlex) {
			sqlex.printStackTrace();
			retString += "<TR><TD><B>Schwerwiegender Fehler bei der Mitarbeiter-Auswahl<BR><FONT COLOR=red>"+
				"</FONT></B></TD></TR>";
		}
		retString +="</TABLE>";
		return retString;
	}
	
	public String abteilung (int kst) {
		String ret = new String();
		String sql = new String();
		sql = "SELECT att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='kstZuAbt' AND att_attr="+kst;
		Statement stmt;
		ResultSet rs;
		try {
			stmt = con.createStatement();
			stmt.executeQuery(sql);
			rs = stmt.getResultSet();
			while (rs.next()) {
				ret = rs.getString("att_bez");
			}
			rs.close();
			stmt.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return "-1";
		}
		return ret;
		
	}
	public String getEinarbeit (String iFs,int iProg,int iAbNr,int iaPNr) {
		String    retString = new String();
		Statement stmt;
		Statement stmtEinarb;
		Statement stmtVorrat;
		ResultSet rs;
		ResultSet rsEinarb;
		ResultSet rsVorrat;
		String    sql = new String();
		String    abtlg = new String();
		pen farbe = new pen(221,255,221,221,221,255);
		abtlg = abteilung(Integer.parseInt(iFs.substring(2,5)));
		retString += "M&ouml;gliche Mitarbeiter  f&uuml;r</BR>";
		retString += "<TABLE BORDER=0>\n<TR><TD>";
		retString += "<TR><TD><B>AB-Nr</B></TD><TD>: "               + iAbNr;
		retString += "</TD></TR>\n<TR><TD><B>Programm</B></TD><TD>: "+ iProg;
		retString += "</TD></TR>\n<TR><TD><B>FS</B></TD><TD>: "      + iFs;
		retString += "</TD></TR>\n<TR><TD><B>Kst.</B></TD><TD>: "    + iFs.substring(2,5);
		retString += "</TD></TR>\n<TR><TD><B>Abtl.</B></TD><TD>: "    + abtlg;
		retString += "</TD></TR>\n</TABLE><TABLE BORDER=1><TR><TD>Platz</TD>";
		retString += "<TD>PNr</TD><TD>Name</TD><TD>EA</TD><TD>Vorrat</TD><TD>FPP</TD></TR>\n";
		try {
			stmt       = con.createStatement();
			stmtEinarb = con.createStatement();
			stmtVorrat = con.createStatement();
			if (abtlg.compareTo("P")!=0) {
				sql = "SELECT map_pnr,ma_vname,ma_nname,ma_ap,att_kennz,0 "+
					"FROM plrv11.bde_madat "+
					"INNER JOIN plrv11.bde_ma_prog ON ma_pnr=map_pnr "+
					"INNER JOIN plrv11.bde_apdat ON ma_ap=ap_platznr AND map_fs=ap_fs "+
					"INNER JOIN plrv11.plr_attrAttr ON att_attr=substr(map_fs,3,3) "+
					"WHERE  ma_anmeld  = 'J' "+
					"AND    map_fs     = '"+iFs+"' "+
					"AND    att_tabname= 'abtlg' "+
					"AND    map_prog   =  "+iProg+" ORDER BY ma_ap";
			} else {
				// Es wird die Anzahl freier Pufferplätze angezeigt
				sql = "SELECT map_pnr,ma_vname,ma_nname,ma_ap,att_kennz,count(*) "+
					"FROM plrv11.bde_madat "+
					"INNER JOIN plrv11.bde_ma_prog ON ma_pnr=map_pnr "+
					"INNER JOIN plrv11.bde_apdat ON ma_ap=ap_platznr AND map_fs=ap_fs "+
					"INNER JOIN plrv11.plr_attrAttr ON att_attr=substr(map_fs,3,3) "+
					"INNER JOIN plrv11.bde_pufpl ON pp_fs=ap_fs "+
					"WHERE  ma_anmeld  = 'J' "+
					"AND    map_fs     = '"+iFs+"' "+
					"AND    att_tabname= 'abtlg' "+
					"AND    (pp_kz='U' OR pp_kz='N') "+
					"AND    pp_pfnr='10" +iFs.substring(2,5) + "300' " +
					"AND    substr(pp_pf_platz,1,9)=CONCAT('10" +iFs.substring(2,5) + "30',substr(ma_ap,10,2)) " +
					"AND    map_prog   =  "+iProg+
					" GROUP BY  map_pnr,ma_vname,ma_nname,ma_ap,att_kennz ORDER BY ma_ap";
				// retString +="<BR>"+sql+"<BR>";
			}
			stmt.executeQuery(sql);
			rs=stmt.getResultSet();	
			while (rs.next()) {
				farbe.change();
				stmtVorrat.executeQuery("SELECT COUNT(*) FROM plrv11.bde_ab_fs_ma WHERE abf_pnr="+rs.getString("map_pnr")+
					" AND (abf_fs_status=5 OR abf_fs_status=10 OR abf_fs_status=30)");
				stmtEinarb.executeQuery("SELECT COUNT(*) FROM plrv11.bde_ma_prog  WHERE map_pnr="+rs.getString("map_pnr"));
				rsVorrat=stmtVorrat.getResultSet();
				rsEinarb=stmtEinarb.getResultSet();
				rsVorrat.next();
				rsEinarb.next();
				if (rs.getInt("map_pnr")!=iaPNr) {
					retString += "<TR BGCOLOR=#"+farbe.getHtmlColor()+"><FORM ACTION=\"" + appRoot +
						"bde\" METHOD=\"GET\">";
					if (rs.getInt("att_kennz")==1) {
						retString += "<INPUT TYPE=\"HIDDEN\" NAME=\"modul\" VALUE=\"planung\">\n";
						retString += "<INPUT TYPE=\"HIDDEN\" NAME=\"formular\" VALUE=\"maZuweisung\">\n";
					} else {
						retString += "<INPUT TYPE=\"HIDDEN\" NAME=\"modul\" VALUE=\"nae\">\n";	
						retString += "<INPUT TYPE=\"HIDDEN\" NAME=\"formular\" VALUE=\"naeZuweisung\">\n";
					}
					retString += "<INPUT TYPE=\"HIDDEN\" NAME=\"abNr\"     VALUE=\""+iAbNr+"\">"+
						"<INPUT TYPE=\"HIDDEN\" NAME=\"fs\"       VALUE=\""+iFs  +"\">\n"+
						"<INPUT TYPE=\"HIDDEN\" NAME=\"prog\"     VALUE=\""+iProg+"\">\n"+
						"<INPUT TYPE=\"HIDDEN\" NAME=\"aPNr\"     VALUE=\""+iaPNr+"\">\n" +
						"<INPUT TYPE=\"HIDDEN\" NAME=\"pNr\"      VALUE=\""+ rs.getString("map_pnr")+"\">\n"+
						"<TD>"+ rs.getString("ma_ap").substring(8,11) +"</TD><TD>" +
						rs.getString("map_pnr") + "</TD><TD>" +
						rs.getString("ma_nname").trim()+", "+
						rs.getString("ma_vname").trim()+"</TD><TD>"+
						rsEinarb.getString(1) + "</TD><TD>"+rsVorrat.getString(1) +
						"</TD><TD>"+rs.getString(6)+"</TD><TD><INPUT TYPE=\"SUBMIT\" VALUE=\"Zuweisen\"></TD></FORM></TR>\n";
				}
				rsVorrat.close();
				rsEinarb.close();
			}
			rs.close();
			stmtVorrat.close();
			stmtEinarb.close();
			stmt.close();
		}
		catch (SQLException sqlex) {
			sqlex.printStackTrace();
			retString += "<TR><TD><B>Schwerwiegender Fehler bei der Mitarbeiter-Auswahl<BR><FONT COLOR=red>"+
				"</FONT></B></TD></TR>";
		}
		retString +="</TABLE>";
		return retString;
    	}
	private  String stati(int iSelected, String iTyp) {
		String retString = new String();
		int stati[]={0,0},pufferStati[]={10,20,30,40},fsStati[]={1,5,10,20,30,40};
		if (iTyp.compareTo("fs")==0) stati=fsStati;
		if (iTyp.compareTo("puffer")==0) stati=pufferStati;
		for (int i=0;i<stati.length;i++) {
			if (stati[i]==iSelected) {
				retString += "<OPTION SELECTED>"+stati[i];
			} else {
				retString += "<OPTION>"+stati[i];
			}
		}
		return retString;
	}
}
