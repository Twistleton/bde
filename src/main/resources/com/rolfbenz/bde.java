package com.rolfbenz;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.StringTokenizer;
import java.util.Vector;


public class bde extends HttpServlet {
	private String appRoot = "";
	private bdeZeit zeit = new bdeZeit();
	private String aktColor   = new String();
	private bdeConfigDatei bcd = new bdeConfigDatei("/etc/bdeServlet.conf");
	private String changeColor() {
		if (aktColor.compareTo("ddffdd")==0) {
			aktColor="ddddff";
		} else {
			aktColor="ddffdd";
		}
		return(aktColor);
	}
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		reInit();
	}
	public void reInit() {
		aktColor   = "ddddff";
		// Auswerten der Config-Datei
		log(bcd.verarbeiten());
		log("Servlet wird mit Config-Daten neu initialisiert.");
	}
	public void destroy() {
		log("Servlet beendet\n");
	}
	public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		bdeDb oraBde;
		fertigungsStufen fertStuf;
		Connection con;
		oraBde = new bdeDb(bcd.getDbIp(),bcd.getDbName(),bcd.getDbUser(),bcd.getDbPass(),bcd.getDbPort());
		con = oraBde.getConnection();
		String formParameter;
		String modulParameter;
		fertStuf = new fertigungsStufen(oraBde,appRoot);
		fertStuf.setVomTddDir(bcd.getVomTddDir());
		fertStuf.setErrorDir(bcd.getErrorDir());
		fertStuf.setNachTddDir(bcd.getNachTddDir());
		fertStuf.setFafDir(bcd.getFafDir());
		try {
			String      user             = new String();
			int         userRecht        = 01;
			int         userWerk         = 00;
			int         userKst          = 0;
			char        userSicht        = '0';
			// ************ Mitarbeiter ***************
			int         maPNr            = 0;
			String      maNName          = new String();
			String      maVName          = new String();
			String      maUser           = new String();
			int         maKst            = 0;
			int         maTel            = 0 ;
			String      maAP             = new String();
			char        maAnmeld         = 'N';
			int         maRecht          = 10;
			int         maPIN            = 0;
			char        maProd           = 'J';
			int         maMaxAnzAuftr    = 2 ;
			int         maWerk           = 10;
			int         maTeam           = 0;
			char        maTeamKz         = 'N';
			String      maDrucker	     = new String();
			// ************ Arbeitsplatz ***********
			String      apPlatzNr     = new String();
			int         apKst         = 0;
			String      apBe          = new String();
			String      apFs          = new String();
			String      apKz          = new String();
			String      apBez         = new String();
			// ************ FS-Parameter ***********
			int         fspWerkPo     = 10;
			int         fspMaGru      = 0;
			int         fspOrdNr      = 0;
			String      fspFs         = new String("");
			String      fspBez        = new String("");
			int         fspPfIni      = 0;
			// ************ Puffer *****************
			int         pfPfNr        = 0;
			String      pfBez         = new String("");
			char        pfKz          = 'A';
			int         pfStatus      = 0;
			String      pfFs          = new String("");
			// ************ Pufferplaetze **********
			String      ppPfPlatz     = new String();
			int         ppPfNr        = 0;
			int         ppPfZone      = 0;
			int         ppAbNr        = 0;
			String      ppFs          = new String();
			char        ppKz          = 'N';
			PrintWriter out;
			String      sqlError      = new String();
			String      meldung       = new String();
			String      sqlString     = new String();
			int         rsZaehl       = 0;
			String      backFormular  = new String();
			String      backModul     = new String();
			String      actionForm    = new String();
			String      actionModul   = new String();
			String      buttonValue   = new String();
			String      loeschButton  = new String();
			String      params        = new String();
			String      javaScripts   = new String();
			String 	    aktSql        = new String();
			ResultSet   rs;
			Statement   stmt;
			out = response.getWriter();
			javaScripts="<SCRIPT TYPE=\"text/javascript\">"+
				"function setFrame(frameName, frameSrc){"+
				"parent[frameName].location=(frameSrc);}"+
				"</SCRIPT>";
			response.setContentType("text/html");
			stmt = con.createStatement();
			out.println("<HTML><HEAD><TITLE>Die Rolf Benz Betriebsdatenerfassung</TITLE>");
			out.println("<link rel=stylesheet type=\"text/css\" href=\"/bde.css\">");
			modulParameter = request.getParameter("modul");
			formParameter  = request.getParameter("formular");
			if (formParameter==null) formParameter="";
			try {
				// Zur Generierung eines Benutzer-Abhaengigen Menues werden Benutzer-Daten aus Datenbank gelesen
				user = request.getRemoteUser();
				sqlString = "SELECT ma_user,ma_recht,ma_werk,ma_kst,att_Bez ";
				sqlString += "FROM plrv11.bde_madat,plrv11.plr_attrAttr WHERE att_tabname='kstZuAbt' and att_attr=ma_kst and ma_user='"+user+"'";
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				rs.next();
				userRecht = rs.getInt("ma_recht");
				userWerk  = rs.getInt("ma_werk");
				userKst   = rs.getInt("ma_kst");
				userSicht = rs.getString("att_bez").charAt(0);
			}
			catch(SQLException sqlex) {
				log("Fehler beim Generieren des benutzerabhaengigen Menues fuer "+request.getRemoteUser()+ " - "+sqlex.getMessage());
			}
			// ************************************************  N A V I G A T I O N S L E I S T E  ***********************************************
			if (modulParameter.compareTo("navi")==0) {
				String pfListe = new String();
				if (userSicht=='A') {
					sqlString = "SELECT pf_pfnr,pf_bez FROM plrv11.bde_pfdat ORDER BY pf_pfnr";
				} else {
					sqlString = "SELECT pf_pfnr,pf_bez FROM plrv11.bde_pfdat,plrv11.plr_attrAttr ";
					sqlString += "WHERE att_attr=substr(pf_pfnr,3,3) AND att_kennz=1 AND att_bez='";
					sqlString += userSicht+"' ORDER BY pf_pfnr";
				}
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					pfListe += "&nbsp;&nbsp;&nbsp;<A HREF=\""+appRoot;
					pfListe += "bde?modul=anlegen&formular=pufferPl&pfNr="+rs.getString("PF_PFNR");
					pfListe += "\" TARGET=\"main\">"+rs.getString("PF_BEZ")+"</A><BR>";
				}
				rs.close();
				out.println(javaScripts);
				out.println("</HEAD><BODY BGCOLOR=#BBBBBB><CENTER><H1><IMG SRC=\"/rb-logo.gif\"><BR><BR>BDE</H1></CENTER>");
				out.println("User:"+user+"-"+ userSicht+"<BR>\n");
				if (userRecht>=30) {
					out.println("<B>Stammdaten</B><BR>");
					// Hier sollen alle Kostenstellen, für die der Mitarbeiter zuständig ist aufgelistet werden
					if (userSicht=='A') {
						stmt.executeQuery("SELECT att_Attr FROM plrv11.plr_attrAttr WHERE att_tabname='kstZuAbt' AND att_aktiv=1  ORDER BY 1");
					} else {
						stmt.executeQuery("SELECT att_Attr FROM plrv11.plr_attrAttr WHERE att_tabname='kstZuAbt' AND att_Bez='"+userSicht+"' AND att_aktiv=1 ORDER BY 1");
					}
					rs = stmt.getResultSet();
					while (rs.next()) {
						out.println(rs.getString("att_Attr") +"<BR>\n");
						out.println("&nbsp;<A HREF=\""+appRoot+"bde?modul=anlegen&formular=ma&klass=user&kst=");
						out.println(rs.getString("att_Attr") +"\" target=\"main\">Mitarbeiter</A><BR>");
						out.println("&nbsp;<A HREF=\""+appRoot+"bde?modul=anlegen&formular=ma&klass=admin&kst=");
						out.println(rs.getString("att_Attr") +"\" target=\"main\">Admins</A><BR>");
						out.println("&nbsp;<A HREF=\""+appRoot+"bde?modul=anlegen&formular=ap&kst=");
						out.println(rs.getString("att_Attr") +"\" target=\"main\">Arbeitspl&auml;tze</A><BR>\n");
						out.println("&nbsp;&nbsp;<A HREF=\""+appRoot+"bde?modul=akt_inakt&formular=ap&kst=");
						out.println(rs.getString("att_Attr")+"\" target=\"main\">akt./deakt. </A><BR>\n");
					}
					rs.close();
				}
				if (userRecht>=40) {
					out.println("<BR>&nbsp;<A HREF=\""+appRoot+"bde?modul=anlegen&formular=fsParam\" target=\"main\">FS-Parameter</A><BR><BR>");
				}
				if (userRecht>=30) {
					out.println("Pufferpl&auml;tze<BR>"+pfListe +"<BR>\n");
				}
				if (userRecht>=40) {
					out.println("<A HREF=\""+appRoot+"bde?modul=anlegen&formular=puffer\"   target=\"main\">Puffer</A><BR><BR>\n");
				}
				if (userRecht>=20) {
					out.println("<B>Auswertung</B><BR>");
					out.println("&nbsp;&nbsp;<A HREF=\""+appRoot+"bde?modul=ausw&formular=eing\" target=\"main\">Termine</A><BR><BR>\n");
					out.println("<B>Verwaltung</B><BR>");
					out.println("&nbsp;<A HREF=\""+appRoot+"bde?modul=planung&formular=vorauswahl&userSicht="+userSicht);
					out.println("\" target=\"main\">Auftr&auml;ge</A><BR>\n");
					if ((userSicht=='A') || (userSicht=='P')) {
						out.println("&nbsp;<A HREF=\""+appRoot);
						out.println("bde?modul=planung&formular=paketAuswahl\" target=\"main\">Pakete</A><BR>\n");
					}
				}
				out.println("<BR><BR><B>Sonstiges</B><BR>\n");
				out.println("&nbsp;<A HREF=\""+appRoot+"bde?modul=stat\" target=\"main\">Statistik</A><BR><BR>\n");
			}
			// ********************************************** P O L S T E R E I ********************************
			// *************** Dezi auf 0 Setzen für Aufträge der Polsterei
			if ((modulParameter.compareTo("pol"))==0) {
				if (formParameter.compareTo("deziNull")==0) {
					int abNr=0;
					int fsStatus = 0;
					int aendIx = 0;
					try {
						abNr  = Integer.parseInt(request.getParameter("abNr"));
						sqlString  = "SELECT abf_fs_status,abf_aend_ix FROM  ";
						sqlString += "plrv11.bde_ab_fs_ma ";
						sqlString += "INNER JOIN plrv11.plr_auftr_status ON abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix ";
						sqlString += "WHERE abf_abnr="+ abNr;
						sqlString += " AND as_status<>99 AND abf_fs='"+request.getParameter("fs")+"'";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						int i=0;
						while (rs.next()) {
							fsStatus = rs.getInt("abf_fs_status");
							aendIx   = rs.getInt("abf_aend_ix");
							i++;
						}
						if (i==0) {
							out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
							out.println("<FONT COLOR=red>Achtung!</FONT>Auftrag nicht in Bestand:"+abNr);
						} else {
							if (fsStatus>10) {
								out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
								out.println("<FONT COLOR=red>Achtung!</FONT> Fertigungsstufenstatus 10 ist bereits überschritten bei "+abNr);
							} else {
								out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC >");
								// onLoad=\"setFrame('main','"+
								// appRoot+"bde?modul=planung&formular=vorauswahl&userSicht="+request.getParameter("userSicht")+"');\">");
								sqlString  = "UPDATE plrv11.bde_ab_fs_ma ";
								sqlString += "SET abf_dezi=0 ";
								sqlString += "WHERE abf_fs='"+request.getParameter("fs")+"' AND abf_abnr="+abNr+" AND abf_aend_ix="+aendIx;
								stmt.executeQuery(sqlString);
								out.println("PO: Dezi wurden für auf Null gesesetzt für <B>"+abNr+"</B><BR>");
								out.println("<A HREF=\""+appRoot+"bde?");
								out.println("modul=planung&formular=vorauswahl&userSicht="+request.getParameter("userSicht"));
								out.println("\">Weiter</A>");
							}
						}
					}
					catch (Exception e) {
						out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
						out.println(e.getMessage()+"<BR>\nKeine gültige Auftragsnummer\n");
					}
				}
			}
			// ********************************************** N A E H E R E I *********************************
			if ((modulParameter.compareTo("nae"))==0) {
				int abNr=0;
				int j=0;
				int fsStatus = 0;
				int aendIx = 0;
				long fs =0;
				String fehlerMeldung = new String();
				String ausgabe = new String();
				String backURL = new String("");
				/* *************************** Hier prüfen, ob diese Anwendungen überhaupt noch benötigt werden ******************************** */
				/* Wird inzwischen normalerweise über ein gesondertes JSP-File gemacht ********************************************************* */
				if(formParameter.compareTo("deziLogin")==0) {
					out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC onLoad=\"document.form.pNr.focus();\"><CENTER>");
					try {
						if (request.getParameter("fehlerMeldung").compareTo("")!=0) {
						out.println("<font color=red>" + request.getParameter("fehlerMeldung")+"</font>");
						}
					}
						catch (NullPointerException npe) {
					}
					out.println("<H2>Anmeldung Sondernaht/Einschlaufen</H2>");
					out.println("<TABLE BORDER=1>");
					out.println("<FORM ACTION=\""+appRoot+"bde\" NAME=\"form\" METHOD=\"POST\"  >");
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"modul\"    VALUE=\"nae\">");
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"formular\" VALUE=\"deziEin\">");
					out.println("<TR><TD>Personal-Nr.   </TD><TD ALIGN=right><INPUT TYPE=\"TEXT\"     NAME=\"pNr\"  SIZE=4  MAXLENGTH=4 ></TD></TR>");
					out.println("<TR><TD>PIN            </TD><TD ALIGN=right><INPUT TYPE=\"password\" NAME=\"pin\"  SIZE=4  MAXLENGTH=4 ");
					out.println("onChange=\"document.form.submit();\"></TD></TR>");
					out.println("<TR><TD></TD><TD ALIGN=right><INPUT TYPE=\"SUBMIT\" VALUE=\"OK\" name=\"enter\"></TD></TR></FORM>");
					out.println("</TABLE>");
				}
				/* *************************** Hier prüfen, ob diese Anwendungen überhaupt noch benötigt werden ******************************** */
				/* Wird inzwischen normalerweise über ein gesondertes JSP-File gemacht ********************************************************* */
				/* Stand 29.02.2008: Wird noch verwendet bzw. ist noch verlinkt auf NTS12031 *************************************************** */

				if (formParameter.compareTo("deziEin")==0) {
					// Anmeldung überprüfen, wenn fehlerhaft angemeldet, dann Zurück zu Eingabemaske
					try {
						maPNr = Integer.parseInt(request.getParameter("pNr")); // Das schafft er halt manchmal nicht
					} catch(NumberFormatException nfe) {
						maPNr =0;
					}
					sqlString = "SELECT ma_pnr,ma_pin,ma_recht FROM plrv11.bde_madat WHERE ma_pnr=" + maPNr;
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					j=0;
					while (rs.next()) {
						maPIN = rs.getInt("ma_pin");
						j++;
					}
					rs.close();
					if ((j!=1) || (request.getParameter("pin").compareTo(String.valueOf(maPIN))!=0) ) {
						fehlerMeldung = "<IMG SRC=\"/warndreieck.gif\" align=\"middle\">Falscher Benutzername/Passwort";
						backURL       = " onLoad=\"top.location.href = '"+appRoot+"bde?modul=nae&formular=deziLogin&fehlerMeldung=";
						backURL      += URLEncoder.encode(fehlerMeldung,"ISO-8859-1")+"';\" ";
					} else {
						backURL       = " onLoad=\"document.form.abNrFs.focus();\"";
					}
					if (request.getParameter("fehlerMeldung")!=null) {
						out.println("<font color=red>" + request.getParameter("fehlerMeldung")+"</font>");
					}
					out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC "+backURL+"><CENTER>");
					// Überprüfen, ob gültige Auftragsnummer
					try {
						String abNrFs= new String(request.getParameter("abNrFs"));
						abNr = Integer.parseInt(abNrFs.substring(0,7));
						fs   = Long.parseLong(abNrFs.substring(7));
					}
					catch (Exception e) {
						log("ungueltige AB");
					}
					if ((abNr!=0) && (fs!=0)) {
						sqlString = "SELECT abf_abnr,abf_fs,abf_fs_status,abf_pf_status,as_aend_ix FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status "+
						" WHERE abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix AND as_status<>99 AND "+
						" abf_abnr="+abNr+" AND abf_fs='"+fs+"' AND abf_fs_status<40 ";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						int k=0;
						while (rs.next()) {
							k++;
							fsStatus = rs.getInt("abf_fs_status");
							pfStatus = rs.getInt("abf_pf_status");
							aendIx   = rs.getInt("as_aend_ix");
						}
						rs.close();
						if (k==0) {
							log("keine gültige Meldung");
							// Keine gültige Meldung
							out.println("<IMG SRC=\"/warndreieck.gif\" align=\"middle\"><BR>");
							out.println("Gemeldete Auftrags-Nr <B><FONT SIZE=+2 COLOR=\"#FF0000\">");
							out.println(abNr+"</FONT></B> konnte nicht gefunden werden.<BR>");
							out.println("Meldung wurde nicht verarbeitet!<BR><BR>");
						} else {
							log("gueltige Meldung");
							// gültige Meldung
							// In ab_fs_ma: fsStatus auf 40 setzen
							sqlString  = "UPDATE plrv11.bde_ab_fs_ma SET abf_fs_status=40,abf_pnr="+maPNr;
							sqlString += " WHERE abf_abnr=" + abNr;
							sqlString += " AND abf_fs='"+fs+"' AND abf_aend_ix="+aendIx;
							stmt.execute(sqlString);
							// Eintrag in Meldezeiten vornehmen
							sqlString  = "INSERT INTO plrv11.bde_ab_mz ";
							sqlString += "(abm_abnr,abm_aend_ix,abm_fs,abm_fs_status,abm_sendtime,abm_pnr,abm_status,abm_kz_auto) ";
							sqlString += "VALUES ("+abNr;
							sqlString += ","       +aendIx;
							sqlString += ",'"      +fs;
							sqlString += "',40";
							sqlString += ",'"      +bdeZeit.getTimestamp("yyMMddHHmm");
							sqlString += "',"      +maPNr;
							sqlString += ",10,'N')";
							stmt.executeQuery(sqlString);
							// Datei schreiben für Uwe
							// Bemelden des Auftrags (alt)
							bdeDatei bdeDat = new bdeDatei(bcd.getVomNaeDir()+"/timestamp.bde");
							int i=0;
							i = bdeDat.oeffnen();
							i+= bdeDat.setVonSubsystem(5);
							i+= bdeDat.setNachSubsystem(2);
							i+= bdeDat.setEreignisAktion("E001");
							i+= bdeDat.setPaketFolgeNr(90);
							i+= bdeDat.setAuftragsNr(abNr);
							i+= bdeDat.setInternerVorgang("AD");
							i+= bdeDat.setBezogenesSubsystem(5);
							i+= bdeDat.setPersonalNr(maPNr);
							i+= bdeDat.setFertigungsstufe(fs);
							i+= bdeDat.setFsStatus(40);
							i+= bdeDat.setFsSplit('N');
							if (i!=0) {
								out.println("Fehler bei Erzeugen von Melde-Datei!");
							}
							bdeDat.setNameTimestamp();
							bdeDat.schreibeSatz();
							bdeDat.schreibeEndeSatz();
							bdeDat.schliessen();
							bdeDat.aktiviereDatei();
							out.println("<B>Meldung Erfolgreich!</B><BR>");
						}
					}
					// Verarbeiten der Meldungen
					out.println("Gelesene Auftrags-Nr: <B>" + abNr);
					out.println("</B>\n<BR>Gelesene FS: <B>" + fs);
					out.println("</B>\n<BR><H2>Statusmeldung Sondernaht/Einschlaufen</H2>");
					out.println("<TABLE BORDER=1>");
					out.println("<FORM ACTION=\""+appRoot+"bde\" name=\"form\" METHOD=\"POST\">");
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"pNr\"      VALUE=\""+request.getParameter("pNr")+"\">");
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"pin\"      VALUE=\""+request.getParameter("pin")+"\">");
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"modul\"    VALUE=\"nae\">");
					out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"formular\" VALUE=\"deziEin\">");
					out.println("<TR BGCOLOR=#DDDDFF><TD>Auftrags-Nr und<BR>Fertigungsstufe</TD><TD ALIGN=right><INPUT TYPE=\"TEXT\"");
					out.println(" NAME=\"abNrFs\" SIZE=18  MAXLENGTH=18 ></TD>");
					out.println("<TD ALIGN=right><INPUT TYPE=\"SUBMIT\" name=\"enter\" VALUE=\"OK\"></TD></TR></FORM>");
					out.println("<FORM ACTION=\""+appRoot+"bde\" METHOD=\"POST\">");
					out.println("<TR BGCOLOR=#FFDDDD><TD COLSPAN=2>Abmelden       </TD>");
					out.println("<TD ALIGN=right><INPUT TYPE=\"hidden\" NAME=\"modul\" VALUE=\"nae\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"deziLogin\">");
					out.println("<INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></TR></FORM>");
					out.println("</TABLE>");
				}
				if (formParameter.compareTo("naeZuweisung")==0) {
					String bgColor = new String("");
					out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC >");
					out.println("<H2>Zuweisung Näherei</H2>");
					out.println("<FORM ACTION=\""+appRoot+"bde\" NAME=\"form\" METHOD=\"POST\">");
					// Aenderungs-Index abfragen und status (bisher) ermitteln
					sqlString = "SELECT abf_abnr,abf_fs_status,as_aend_ix "+
						" FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status "+
						" WHERE abf_abnr="+request.getParameter("abNr")+
						" AND abf_aend_ix=as_aend_ix AND as_status<>99 AND abf_fs='"+request.getParameter("fs")+"'";
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						aendIx = rs.getInt("as_aend_ix");
						fsStatus = rs.getInt("abf_fs_status");
					}
					out.println("<TABLE BORDER=1><TR><TD>AktuellerFS-Status</TD><TD ALIGN=CENTER>"+fsStatus+"</TD>");
					if (fsStatus>5) {
						out.println("<TR><TD>Vorgang</TD><TD ALIGN=CENTER>Umplanung</TD></TR>");
						bgColor="#FFDDDD";
					} else {
						out.println("<TR><TD>Vorgang</TD><TD ALIGN=CENTER>Manuelle Zuweisung</TD></TR>");
						bgColor="#DDFFDD";
					}
					out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"nae\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"naeZuweisungSich\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"pNr\"      VALUE=\""+request.getParameter("pNr") + "\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"abNr\"     VALUE=\""+request.getParameter("abNr")+ "\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"fs\"       VALUE=\""+request.getParameter("fs")  + "\">");
					out.println("<TR BGCOLOR="+bgColor+"><TD>Sind Sie sicher ?&nbsp;&nbsp;</TD>");
					out.println("<TD ALIGN=right><INPUT TYPE=\"submit\" VALUE=\"OK\"></TD></TR></TABLE></FORM>");
				}
				if (formParameter.compareTo("naeZuweisungSich")==0){
					out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC >");
					// Aenderungs-Index erneut abfragen und status (bisher) ermitteln
					sqlString  = "SELECT abf_abnr,abf_fs_status,as_aend_ix ";
					sqlString += "FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status ";
					sqlString += "WHERE abf_abnr="+request.getParameter("abNr");
					sqlString += "AND abf_aend_ix=as_aend_ix ";
					sqlString += "AND as_status<>99 ";
					sqlString += "AND abf_fs='"+request.getParameter("fs")+"'";
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						aendIx = rs.getInt("as_aend_ix");
						fsStatus = rs.getInt("abf_fs_status");
					}
					// Sicherheitsabfrage wurde bestätigt
					// Hier einfach nur Mitarbeiter-Nummer eintragen und Status auf 5 setzen
					if (fsStatus>5) {
						out.println("<H3><FONT COLOR=#FF0000>Auftrag bereits gestartet.<BR>Umplanung wird vorgenommen.</FONT></H3>\n");
						// Bei Umplanung (fsStatus>5) Dezi auf 0 setzen
						sqlString  = "UPDATE plrv11.bde_ab_fs_ma SET ";
						sqlString += " abf_pnr=" + request.getParameter("pNr");
						sqlString += ",abf_fs_status=5";
						sqlString += ",abf_dezi=0 ";
						sqlString += "WHERE abf_abnr=" + request.getParameter("abNr");
						sqlString += " AND abf_fs='"+request.getParameter("fs")+"'";
					} else {
						out.println("<H3><FONT COLOR=#00AA00>Auftrag noch nicht gestartet.<BR>Manuelle Zuweisung wird vorgenommen.</FONT></H3>\n");
						sqlString  = "UPDATE plrv11.bde_ab_fs_ma ";
						sqlString += "SET abf_pnr=" + request.getParameter("pNr");
						sqlString += ",abf_fs_status=5 ";
						sqlString += "WHERE abf_abnr=" + request.getParameter("abNr");
						sqlString += " AND abf_fs='"+request.getParameter("fs")+"'";
					}
					stmt.executeQuery(sqlString);
					// Eintrag in Meldezeiten-Tabelle vornehmen
					sqlString  = "INSERT INTO plrv11.bde_ab_mz ";
					sqlString += "(abm_abnr,abm_aend_ix,abm_fs,abm_fs_status,abm_sendtime,abm_pnr,abm_status,abm_kz_auto) ";
					sqlString += "VALUES ("+request.getParameter("abNr");
					sqlString += ","       +aendIx;
					sqlString += ",'"      +request.getParameter("fs");
					sqlString += "',5,'"   +bdeZeit.getTimestamp("yyMMddHHmm");
					sqlString += "',"      +request.getParameter("pNr");
					sqlString += ",10,'N')";
					stmt.executeQuery(sqlString);
				}
				if (formParameter.compareTo("deziNull")==0) {
					try {
						abNr  = Integer.parseInt(request.getParameter("abNr"));
						sqlString = "SELECT abf_fs_status,abf_aend_ix FROM  plrv11.bde_ab_fs_ma, plrv11.plr_auftr_status WHERE "+
							"abf_abnr="+ abNr +
							" AND abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix AND as_status<>99 AND abf_fs='10243100000'";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						int i=0;;
						while (rs.next()) {
							fsStatus = rs.getInt("abf_fs_status");
							aendIx   = rs.getInt("abf_aend_ix");
							i++;
						}
						if (i==0) {
							out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
							out.println("<FONT COLOR=red>Achtung!</FONT> Auftrag nicht in Bestand:"+abNr);
						} else {
							if (fsStatus>10) {
								out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
								out.println("<FONT COLOR=red>Achtung!</FONT> Fertigungsstufe 10 ist bereits überschritten bei "+abNr);
							} else {
								out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
								sqlString  = "UPDATE plrv11.bde_ab_fs_ma SET abf_dezi=0 ";
								sqlString += "WHERE abf_fs='10243100000' AND abf_abnr="+abNr+" AND abf_aend_ix="+aendIx;
								stmt.executeQuery(sqlString);
									out.println("NA: Dezi wurden für auf Null gesesetzt für <B>"+abNr+"</B><BR>");
									out.println("<A HREF=\""+appRoot+"bde?modul=planung&formular=vorauswahl&");
									out.println("userSicht="+request.getParameter("userSicht"));
									out.println("\">Weiter</A>");
							}
						}
					}
					catch (NumberFormatException nfe) {
						out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC>");
						out.println("\nKeine gültige Auftragsnummer\n");
					}
				}
			}
			// ************************************************************ A U S W E R T U N G *********************************************************
			if ((modulParameter.compareTo("ausw")==0) && (userRecht>=20)) {
				out.println(javaScripts + "</HEAD><BODY BGCOLOR=#CCCCCC onLoad=\"setFrame('list','/blank.html');\"><CENTER>");
				if (formParameter.compareTo("auftrAusw")==0) {
					out.println("<CENTER><H2>Auftragsverfolgung</H2></CENTER>");
					try {
						String proUhrzeit   = new String();
						String proTimestamp = new String();
						String gueltFarbe   = new String();
						sqlString =  "SELECT pro_werk,pro_timestamp,a.att_bez,pro_vgfabt,pro_vgtime,b.att_bez,pro_gueltkz "+
						"FROM plrv11.ml_protokoll,plrv11.plr_attrAttr a,plrv11.plr_attrAttr b WHERE "+
						"a.att_tabname='vgart' and a.att_attr=pro_vgart and b.att_tabname='urheber' and b.att_attr=pro_urheber and pro_abnr=" + request.getParameter("abNr") +
						" ORDER BY 2";
						stmt.executeQuery(sqlString);
						out.println("<TABLE BORDER=1>");
						out.println("<TR><TD>Werk</TD><TD>Zeitpunkt der Meldung</TD><TD>Art</TD><TD>Plan-Tag</TD><TD>Plan-Zeit</TD><TD>Urheber</TD></TR>");
						rs = stmt.getResultSet();
						while (rs.next()) {
						proUhrzeit = rs.getString("pro_vgtime");
						proUhrzeit = proUhrzeit.substring(0,proUhrzeit.length()-2) + ":" + proUhrzeit.substring(proUhrzeit.length()-2);
						proTimestamp = rs.getString("pro_timestamp");
						if (proTimestamp.length()==14) {
							proTimestamp = proTimestamp.substring(6,8) + "." + proTimestamp.substring(4,6) + "." + proTimestamp.substring(0,4) + "  " +
							proTimestamp.substring(8,10) +":"+proTimestamp.substring(10,12) +":"+proTimestamp.substring(12,14);
						}
						if (rs.getInt("pro_gueltkz")==1) {
							gueltFarbe = "ddddff";
						} else {
							gueltFarbe = "ffdddd";
						}
						out.println("<TR BGCOLOR=#"+gueltFarbe+"><TD>"+ rs.getString("pro_werk") + "</TD><TD>" +proTimestamp + "</TD><TD>" +
								rs.getString(3) +"</TD><TD>" + rs.getString("pro_vgfabt")  +"</TD><TD ALIGN=right>" + proUhrzeit + "</TD><TD>" +
								rs.getString(6) +"</TD></TR>");
						}
						out.println("</TABLE>");
						rs.close();
					}
					catch(SQLException sqlex) {
						sqlex.printStackTrace();
					}
				}
				if (formParameter.compareTo("eing")==0) {
					out.println("<CENTER><H2>Auswertung P&uuml;nktlichkeit &uuml;ber alle Werke</H2><BR>");
					out.println("<FORM ACTION=\""+appRoot+"bde?ausw=planung&formular=erg\" METHOD=\"GET\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"erg\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"ausw\">");
					out.println("Datum<INPUT TYPE=\"text\"   NAME=\"datum\"   SIZE=10 MAXLENGTH=10>");
					out.println("<INPUT TYPE=\"submit\" VALUE=\"OK\"></FORM>");
				}
				if (formParameter.compareTo("erg")==0) {
					int alle=0;
					int zuFrueh=0;
					int zuSpaet=0;
					int puenktlich=0;
					int n=0;
					Vector werke    = new Vector();
					Vector werkeBez = new Vector();
					Vector datumVec = new Vector();
					String datum = new String();
					StringTokenizer datumsZerhacker = new StringTokenizer(request.getParameter("datum"),".");
					DecimalFormat df = new DecimalFormat("##.##");
					werke.addElement((Object) new String("a.pro_werk=10 AND "));
					// Werk 30 will niemand mehr sehen			     werke.addElement((Object) new String("a.pro_werk=30 AND "));
					// Gesamt-Auswertung nicht mehr nötig                   werke.addElement((Object) new String(""));
					werkeBez.addElement((Object) new String("10"));
					// Werk 30 will niemand mehr sehen                      werkeBez.addElement((Object) new String("30"));
					// Gesamt-Auswertung nicht mehr nötig                   werkeBez.addElement((Object) new String("Gesamt"));
					out.println("<CENTER><H2>Auswertung der Protokoll-Tabelle</H2></CENTER><TABLE BGCOLOR=#DDDDDD BORDER=1> ");
					while (datumsZerhacker.hasMoreElements()) {
						datumVec.addElement((Object) datumsZerhacker.nextToken());
					}
					datum = rbTextFormat.format('0',4,(String) datumVec.elementAt(2)) +
						rbTextFormat.format('0',2,(String) datumVec.elementAt(1)) +
						rbTextFormat.format('0',2,(String) datumVec.elementAt(0));
					for (n=0;n<=2;n++) {
						out.println("<TR><TD VALIGN=CENTER ALIGN=CENTER>"+werkeBez.elementAt(n)+"</TD><TD><TABLE BORDER=1>");
						/* Alle Aufträge */
						try {
						sqlString = "select count(*) "+
							"FROM plrv11.ml_protokoll a, plrv11.ml_protokoll b,plrv11.ml_protokoll c,plrv11.ml_protokoll d "+
							"WHERE "+
							"a.pro_abnr   = b.pro_abnr AND "+
							"a.pro_abnr   = c.pro_abnr AND "+
							"a.pro_abnr   = d.pro_abnr AND "+ // Neu 04.07.2002
							"a.pro_vgart  = 7 AND " +
							"b.pro_vgart  = 3 AND " +
							"c.pro_vgart  = 2 AND " +
							"d.pro_vgart  = 1 AND " +
							"a.pro_gueltkz= 1 AND " +
							"b.pro_gueltkz= 1 AND " +
							"c.pro_gueltkz= 1 AND " +
							"d.pro_gueltkz= 1 AND " +
							"a.pro_vgfabt > 0 AND " +
							werke.elementAt(n) +
							" substr(a.pro_timestamp,1,8)='"+datum+"' ORDER BY a.pro_abnr";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						alle = rs.getInt(1);
						/* Zu späte Aufträge */
						sqlString = "SELECT "+
							"COUNT(*) "+
							"FROM plrv11.ml_protokoll a, plrv11.ml_protokoll b,plrv11.ml_protokoll c "+
							"WHERE " +
							"a.pro_abnr   = b.pro_abnr AND "+
							"a.pro_abnr   = c.pro_abnr AND "+
							"a.pro_vgart  = 7 and "+
							"b.pro_vgart  = 3 and "+
							"c.pro_vgart  = 1 and "+
							"a.pro_gueltkz= 1 and "+
							"b.pro_gueltkz= 1 and "+
							"c.pro_gueltkz= 1 and "+
							werke.elementAt(n) +
							"a.pro_vgfabt>0 and "+
							" substr(a.pro_timestamp,1,8)='"+datum+"' AND "+
							"((a.pro_vgfabt > b.pro_vgfabt) OR " +
							"(a.pro_vgfabt = b.pro_vgfabt AND a.pro_vgtime > b.pro_vgtime))";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						zuSpaet = rs.getInt(1);
						/* Zu früh */
						sqlString = "SELECT COUNT(*) "+
							"FROM  plrv11.ml_protokoll a, plrv11.ml_protokoll b,plrv11.ml_protokoll c "+
							"WHERE "+
							"a.pro_abnr   = b.pro_abnr AND " +
							"a.pro_abnr   = c.pro_abnr AND " +
							"a.pro_vgart  = 7 and "+
							"b.pro_vgart  = 2 and "+
							"c.pro_vgart  = 1 and "+
							"a.pro_gueltkz= 1 and "+
							"b.pro_gueltkz= 1 AND "+
							"c.pro_gueltkz= 1 and "+
							werke.elementAt(n) +
							"a.pro_vgfabt>0 AND "+
							"substr(a.pro_timestamp,1,8)='"+datum+"' AND "+
							"((a.pro_vgfabt < b.pro_vgfabt ) OR " +
							"(a.pro_vgfabt = b.pro_vgfabt AND a.pro_vgtime< b.pro_vgtime))";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						zuFrueh =rs.getInt(1);
						/* Pünktlich */
						sqlString = "SELECT COUNT(*) "+
							"FROM  plrv11.ml_protokoll a, plrv11.ml_protokoll b,plrv11.ml_protokoll c,plrv11.ml_protokoll d "+
							"WHERE a.pro_abnr   = b.pro_abnr AND "+
							"a.pro_abnr   = c.pro_abnr AND "+
							"a.pro_abnr   = d.pro_abnr AND "+
							"a.pro_vgart  = 7 AND "+
							"b.pro_vgart  = 3 AND "+
							"c.pro_vgart  = 2 AND "+
							"d.pro_vgart  = 1 AND "+
							"a.pro_gueltkz= 1 AND "+
							"b.pro_gueltkz= 1 AND "+
							"c.pro_gueltkz= 1 AND "+
							"d.pro_gueltkz= 1 AND "+
							werke.elementAt(n) +
							"a.pro_vgfabt>0 and "+
							"substr(a.pro_timestamp,1,8)='"+datum+"' AND "+
							"(((a.pro_vgfabt > c.pro_vgfabt) OR (a.pro_vgfabt = c.pro_vgfabt AND a.pro_vgtime >= c.pro_vgtime)) AND "+
							" ((a.pro_vgfabt < b.pro_vgfabt) OR (a.pro_vgfabt = b.pro_vgfabt AND a.pro_vgtime <= b.pro_vgtime)))";
						//					out.println("<TR><TD>"+sqlString+ "</TD></TR>");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						puenktlich =rs.getInt(1);
						}
						catch (SQLException sqlex) {
						out.println("<TR><TD>"+sqlString+"</TD></TR>");
						sqlex.printStackTrace();
						}
						out.println("<TR BGCOLOR=#DDDDFF><TD ALIGN=CENTER>&nbsp;</TD><TD>Absolut</TD><TD>Prozentual</TD></TR>");
						out.println("<TR BGCOLOR=#DDFFDD><TD>Alle</TD><TD>" + alle       + "</TD><TD>100</TD></TR>\n");
						out.println("<TR><TD>p&uuml;nktlich</TD><TD>" + puenktlich + "</TD><TD>"+df.format( (double)puenktlich/(double)alle*100 ) +"</TD></TR>\n");
						out.println("<TR><TD>zu fr&uuml;h</TD><TD>" + zuFrueh    + "</TD><TD>"  +df.format( (double)zuFrueh/(double)alle*100)+"</TD></TR>\n");
						out.println("<TR><TD>zu sp&auml;t</TD><TD>" + zuSpaet    + "</TD><TD>"  +df.format( (float )zuSpaet/(float)alle*100)+"</TD></TR>\n");
						out.println("</TABLE></TD></TR>");
					}
					out.println("</TABLE>");
				}
			}
			// ************************************************************************* A N L E G E N ******************************************************
			if ((modulParameter.compareTo("anlegen")==0) && (userRecht>=30)) {
				int modus=0; // 0 - Anlegen ; 1 - Aendern
				String schluesselFeld = new String();
				String schluesselFeld2 = new String();
				if (formParameter.compareTo("ma")==0) {
					String fsListe     = new String();
					String fsProgListe = new String();
					long ID = 0;
					try {
						sqlString="SELECT DISTINCT fsp_fs FROM plrv11.bde_fs_param order by fsp_fs";
						stmt.executeQuery(sqlString);
						rs =stmt.getResultSet();
						while (rs.next()) {
						fsListe = fsListe+"<OPTION>" + rs.getString("fsp_fs");
						}
					}
					catch(SQLException sqlEx) {
						meldung = "DatenbankFehler";
						sqlEx.printStackTrace();
					}
					if (request.getParameter("maPNr")!=null) {
						maPNr =  Integer.parseInt(request.getParameter("maPNr"));
						sqlString  = "SELECT ";
						sqlString += "MA_PNR,MA_NNAME,MA_VNAME,MA_KST,MA_TEL,";
						sqlString += "MA_AP,MA_ANMELD,MA_RECHT,MA_PIN,MA_PROD,";
						sqlString += "MA_MAX_AB,MA_USER,MA_WERK,ma_team,ma_team_kz,";
						sqlString += "MA_DRUCKER ";
						sqlString += "FROM PLRV11.BDE_MADAT ";
						sqlString += "WHERE MA_PNR=" + maPNr;
						schluesselFeld = maPNr        +"<INPUT TYPE=\"hidden\" NAME=\"maPNr\" VALUE=\"" + maPNr + "\"></TD>";
						// Vorbelegung der Variablen
						try {
							actionModul="aendern";
							buttonValue="&Auml;ndern";
							stmt.executeQuery(sqlString);
							rs = stmt.getResultSet();
							rs.next();
							maNName      = rs.getString("MA_NNAME");
							maVName      = rs.getString("MA_VNAME");
							maKst        = rs.getInt("MA_KST");
							maTel        = rs.getInt("MA_TEL");
							maAP         = rs.getString("MA_AP");
							maAnmeld     = rs.getString("MA_ANMELD").charAt(0);
							maRecht      = rs.getInt("MA_RECHT");
							maPIN        = rs.getInt("MA_PIN");
							maProd       = rs.getString("MA_PROD").charAt(0);
							maMaxAnzAuftr= rs.getInt("MA_MAX_AB");
							maUser       = rs.getString("MA_USER");
							maWerk       = rs.getInt("MA_WERK");
							maTeam       = rs.getInt("ma_team");
							maTeamKz     = rs.getString("ma_team_kz").charAt(0);
							maDrucker    = rs.getString("ma_drucker");
							if (maDrucker==null) {
								maDrucker="";
							}
							// Programme der aktuellen Fertigungsstufe rauskramen
							sqlString = " SELECT map_prog,map_fs FROM plrv11.bde_ma_prog WHERE map_pnr=" + maPNr + " ORDER BY map_fs,map_prog";
							stmt.executeQuery(sqlString);
							rs = stmt.getResultSet();
							while (rs.next()) {
								fsProgListe=fsProgListe + "<OPTION VALUE=\""+maPNr+","+rs.getString("map_fs") + "," +
								rs.getString("map_prog") + "\">" +rs.getString("map_fs") +"&nbsp;-&nbsp;"+ rs.getString("map_prog");
							}
						}
						catch(SQLException sqlEx) {
							meldung = "DatenbankFehler";
							sqlEx.printStackTrace();
						}
						meldung = meldung + "&Auml;ndern<BR>";
						modus = 1;
					} else {
						meldung = meldung +"Neuen Datensatz anlegen<BR>";
						actionModul="einfuegen";
						buttonValue="Speichern";
						schluesselFeld = "<INPUT TYPE=\"text\" NAME=\"maPNr\"         VALUE=\""+ maPNr        +"\" SIZE=4  MAXLENGTH=4 ></TD>";
						maKst = Integer.parseInt(request.getParameter("kst"));
						modus = 0;
					}
					out.print(javaScripts);
					out.println("</HEAD><BODY bgcolor=\"#CCCCCC\" onLoad=\"");
					out.println("setFrame('list','" + appRoot +"bde?modul=liste&formular=ma&klass="+request.getParameter("klass")+"&kst="+request.getParameter("kst")+"');");
					out.println("setFrame('navi','" + appRoot +"bde?modul=navi')\">");
					out.println("<CENTER><H2>Mitarbeiter-Daten</H2>");
					out.println("<TABLE BORDER=1><TR><TD><TABLE BORDER=0>");
					out.println("<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\" VALUE=\""+actionModul+"\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"ma\">\n");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"kst\" VALUE=\""+request.getParameter("kst")+"\">\n");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"klass\" VALUE=\""+request.getParameter("klass")+"\">\n");
					out.println("<TR><TD>Status</TD><TD>" +meldung + "</TD></TR>\n");
					out.println("<TR><TD>Personal-Nr. </TD><TD>" + schluesselFeld);
					out.println("    <TD>Kostenstelle </TD><TD>");
					aktSql = "SELECT att_attr,att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='bdeGueltKst' ORDER BY att_attr";
					out.println(oraBde.getHtmlSelect("maKst",aktSql,String.valueOf(maKst)));
					out.println("</TD></TR>");
					out.println("<TR><TD>Nachname     </TD><TD COLSPAN=3><INPUT TYPE=\"text\" NAME=\"maNName\"       VALUE=\""+ maNName      +"\" SIZE=15 MAXLENGTH=25></TD></TR>");
					out.println("<TR><TD>Vorname      </TD><TD><INPUT TYPE=\"text\" NAME=\"maVName\"       VALUE=\""+ maVName      +"\" SIZE=15 MAXLENGTH=25></TD>");
					out.println("<TD>Team-KZ</TD><TD>");
					aktSql = "SELECT att_bez,att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='janein'";
					out.println(oraBde.getHtmlSelect("teamKz",aktSql,String.valueOf(maTeamKz))+"</TD></TR>");
					out.println("<TR><TD>Benutzername </TD><TD><INPUT TYPE=\"text\" NAME=\"maUser\"        VALUE=\""+ maUser       +"\" SIZE=5  MAXLENGTH=5 ></TD><TD>Team</TD><TD>");
					aktSql = "SELECT ma_pnr,ma_nname FROM plrv11.bde_madat WHERE ma_team_kz='J'";
					out.println(oraBde.getHtmlSelect("team",aktSql,String.valueOf(maTeam))+"</TD></TR>");
					out.println("<TR><TD>Telefon-Nr.  </TD><TD><INPUT TYPE=\"text\" NAME=\"maTel\"         VALUE=\""+ maTel        +"\" SIZE=4  MAXLENGTH=4 ></TD>");
					out.println("    <TD>Platz-Nr.    </TD><TD>");
					aktSql = "select ap_platznr,ap_platznr from plrv11.bde_apdat where ap_kst="+String.valueOf(maKst)+" ORDER BY 1";
					out.println(oraBde.getHtmlSelect("maAP",aktSql,maAP)+"</TD></TR>");
					out.println("<TR><TD>Anmelde-KZ.  </TD><TD>");
					aktSql = "SELECT att_bez,att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='bdeGueltAnmKz' ORDER BY 1";
					out.println(oraBde.getHtmlSelect("maAnmeld",aktSql,String.valueOf(maAnmeld))+"</TD>");
					out.println("<TD>Rechteklasse     </TD><TD>");
					aktSql = "SELECT att_Attr,att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='bdeGueltRecht' ORDER BY att_attr";
					out.println(oraBde.getHtmlSelect("maRecht",aktSql,String.valueOf(maRecht)));
					out.println("<TD></TR>");
					out.println("<TR><TD>PIN          </TD><TD><INPUT TYPE=\"password\" NAME=\"maPIN\"     VALUE=\""+ maPIN        +"\" SIZE=4  MAXLENGTH=4 ></TD>");
					out.println("    <TD>Prod.-KZ     </TD><TD>");
					aktSql = "SELECT att_bez,att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='bdeGueltProdKz' ORDER BY 1";
					out.println(oraBde.getHtmlSelect("maProd",aktSql,String.valueOf(maProd)));
					out.println("<TD></TR>");
					out.println("<TR><TD>Max. Anz. Auftr.</TD><TD>       <INPUT TYPE=\"text\" NAME=\"maMaxAnzAuftr\" VALUE=\""+ maMaxAnzAuftr+"\" SIZE=2  MAXLENGTH=2 ></TD>");
					out.println("<TD>Werk         </TD><TD>");
					out.println(oraBde.getHtmlSelect("maWerk","SELECT att_attr,att_Bez FROM plrv11.plr_attrAttr WHERE att_tabname='bdeGueltWerk' ORDER BY 1",String.valueOf(maWerk)));
					out.println("</TD></TR>");
					out.println("<TR><TD>Drucker</TD><TD><INPUT TYPE=\"text\" NAME=\"maDrucker\" VALUE=\""+ maDrucker+"\" SIZE=15  MAXLENGTH=30 ></TD>");
					out.println("</TD></TR>");
					out.println("<TR><TD>&nbsp;</TD><TD><INPUT TYPE =\"SUBMIT\" VALUE=\"" + buttonValue);
					out.println("\"></FORM></TD></TR></TABLE></TD>\n");
					if (modus == 1) {
						out.println("<TD valign=top><TABLE BORDER=0>");
						out.println("<FORM ACTION=\"http://"+request.getServerName()+":8080/zusbemeldung/einarbListe.jsp\" METHOD=\"POST\">");
						out.println("<INPUT TYPE=\"HIDDEN\" NAME=\"pNr\" VALUE=\""+maPNr+"\">");
						out.println("<TR><TD COLSPAN=\"2\" ALIGN=\"CENTER\">");
						out.println("<INPUT TYPE=\"submit\" VALUE=\"Einarbeit Druckansicht\"></TD></TR></FORM>");
						out.println("<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"einfuegen\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"einarb\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"kst\" VALUE=\""+maKst+"\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"klass\" VALUE=\""+request.getParameter("klass") +"\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"maPNr\" VALUE=\"" + maPNr + "\">");
						out.println("<TR><TD>Fert.-Stufe</TD><TD>Programm</TD></TR>");
						out.println("<TR><TD><SELECT NAME=\"fs\">" + fsListe + "</SELECT></TD><TD><INPUT TYPE=\"text\" NAME=\"progInput\" SIZE=3 MAXLENGTH=3></TD></TR>");
						out.println("<TR><TD>&nbsp;</TD><TD><INPUT TYPE=\"SUBMIT\" VALUE=\"Hinzuf&uuml;gen\"></TD><TR>");
						out.println("</FORM>");
						out.println("<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"loeschen\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"einarb\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"kst\" VALUE=\""+maKst+"\">");
						out.println("<INPUT TYPE=\"hidden\" NAME=\"klass\" VALUE=\""+request.getParameter("klass") +"\">");
						out.println("<TR><TD><SELECT NAME=\"loeschID\" SIZE=20>"+fsProgListe+"</SELECT></TD>");
						out.println("    <TD><INPUT TYPE=\"SUBMIT\" VALUE=\"L&ouml;schen\">  </TD></TR>");
						out.println("</FORM>");
						out.println("</TABLE>"); //<TD>
						// Formular fuer das loeschen des Mitarbeiters
						out.println("</TR><TR><FORM ACTION=\""+appRoot+"bde\" METHOD=\"GET\"><TD COLSPAN=2 ALIGN=CENTER>"+
							"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"loeschSicher\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"ma\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"loeschID\" VALUE=\""+maPNr+"\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"kst\"      VALUE=\""+request.getParameter("kst")+"\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"klass\"    VALUE=\""+request.getParameter("klass")+"\">"+
							"<INPUT TYPE=\"submit\"                   VALUE=\"L&ouml;schen\">"+
							"</TD></FORM>");
					}
					out.println("</TR></TABLE></CENTER>");
				}
				if (formParameter.compareTo("ap")==0) {
					if (request.getParameter("apPlatzNr")!=null) {
						actionModul="aendern";
						actionForm ="ap1";
						buttonValue="&Auml;ndern";
						modus=1;
						sqlString = "SELECT ap_platznr,ap_kst,ap_fs,ap_bez,ap_kz FROM plrv11.bde_apdat WHERE ap_platznr=" + request.getParameter("apPlatzNr");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						apPlatzNr = rs.getString("ap_platzNr");
						apKst     = Integer.parseInt(rs.getString("ap_kst"));
						apFs      = rs.getString("ap_fs");
						apBez     = rs.getString("ap_bez");
						apKz      = rs.getString("ap_kz");
						schluesselFeld="<INPUT TYPE=\"hidden\" NAME=\"apPlatzNr\" VALUE=\""+apPlatzNr+"\">"+apPlatzNr;
					} else {
						modus=0;
						actionModul="einfuegen";
						actionForm ="ap1";
						buttonValue="Speichern";
						schluesselFeld="<INPUT TYPE=\"text\" NAME=\"apPlatzNr\" VALUE=\""+apPlatzNr+"\" SIZE=11 MAXLENGTH=11>";
					}
					out.println(javaScripts +
							"</HEAD><BODY bgcolor=\"#CCCCCC\"onLoad=\"setFrame('list','" +
							appRoot +"bde?modul=liste&formular=ap&kst="+request.getParameter("kst")+"')\">"+
							"<H1><CENTER>Arbeitsplatz</CENTER><H1>"+
							"<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"" + actionModul + "\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"" + actionForm  + "\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"kst\" VALUE=\""      + request.getParameter("kst")  + "\">"+
							"<TABLE BORDER=0>"+
							"<TR><TD>Platz-Nr.      </TD><TD>"+schluesselFeld+"</TD></TR>"+
							"<TR><TD>Kostenstelle   </TD><TD><INPUT TYPE=\"text\" NAME=\"apKst\"     SIZE=3  MAXLENGTH=3   VALUE=\""+apKst     +"\"></TD></TR>"+
							"<TR><TD>Fertigungsstufe</TD><TD><INPUT TYPE=\"text\" NAME=\"apFs\"      SIZE=11 MAXLENGTH=11  VALUE=\""+apFs      +"\"></TD></TR>"+
							"<TR><TD>Bezeichnung    </TD><TD><INPUT TYPE=\"text\" NAME=\"apBez\"     SIZE=24 MAXLENGTH=24  VALUE=\""+apBez     +"\"></TD></TR>"+
							"<TR><TD>Kennzeichen    </TD><TD><INPUT TYPE=\"text\" NAME=\"apKz\"      SIZE=1  MAXLENGTH=1   VALUE=\""+apKz      +"\"></TD></TR>"+
							"<TR><TD>&nbsp;         </TD><TD><INPUT TYPE =\"SUBMIT\" VALUE=\""+ buttonValue +"\"> </TD></TR></TABLE></FORM>");
				}
				if (formParameter.compareTo("fsParam")==0) {
					if (request.getParameter("fs")!=null) {
						actionModul="aendern";
						actionForm ="fsParam";
						buttonValue="&Auml;ndern";
						modus      =1;
						sqlString  = "SELECT fsp_werk_po,fsp_magru,fsp_ordnr,fsp_fs,fsp_bez,fsp_pf_ini FROM plrv11.bde_fs_param WHERE fsp_fs="+request.getParameter("fs")+" AND fsp_magru="+
						request.getParameter("fspMaGru");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						fspWerkPo = Integer.parseInt(rs.getString("fsp_werk_po"));
						fspMaGru  = Integer.parseInt(rs.getString("fsp_magru"));
						fspOrdNr  = Integer.parseInt(rs.getString("fsp_ordnr"));
						fspFs     = rs.getString("fsp_fs");
						fspBez    = rs.getString("fsp_bez");
						fspPfIni  = Integer.parseInt(rs.getString("fsp_pf_ini"));
						schluesselFeld  ="<INPUT TYPE=\"hidden\" NAME=\"fspFs\"     VALUE=\""+fspFs    +"\">" + fspFs;
						schluesselFeld2 ="<INPUT TYPE=\"hidden\" NAME=\"fspMaGru\"  VALUE=\""+fspMaGru +"\">" + fspMaGru;
					} else {
						actionModul="einfuegen";
						actionForm ="fsParam";
						buttonValue="Speichern";
						schluesselFeld  ="<INPUT TYPE=\"text\"   NAME=\"fspFs\"     VALUE=\""+fspFs    +"\"       SIZE=11 MAXLENGTH=11>";
						schluesselFeld2 ="<INPUT TYPE=\"text\" NAME=\"fspMaGru\"  VALUE=\""+fspMaGru   +"\"       SIZE=9  MAXLENGTH=9 >";
						modus=0;
					}
					out.println(javaScripts +
							"</HEAD><BODY bgcolor=\"#CCCCCC\"onLoad=\"setFrame('list','" + appRoot +"bde?modul=liste&formular=fsParam')\">"+
							"<H1><CENTER>Fertigungsstufen-Parameter</CENTER><H1>"+
							"<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\""+actionModul+"\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\""+actionForm+"\">"+
							"<TABLE BORDER=0>"+
							"<TR><TD>Fertigungsstufe   </TD><TD>"+schluesselFeld +"</TD></TR>"+
							"<TR><TD>AGSNR             </TD><TD>"+schluesselFeld2+"</TD></TR>"+
							"<TR><TD>Polsterwerk       </TD><TD><INPUT TYPE=\"text\" NAME=\"fspWerkPo\" VALUE=\""+fspWerkPo+"\"       SIZE=2  MAXLENGTH=2 ></TD></TR>"+
							"<TR><TD>FS-Positionsnummer</TD><TD><INPUT TYPE=\"text\" NAME=\"fspOrdNr\"  VALUE=\""+fspOrdNr +"\"       SIZE=3  MAXLENGTH=3 ></TD></TR>"+
							"<TR><TD>FS-Bezeichnung    </TD><TD><INPUT TYPE=\"text\" NAME=\"fspBez\"    VALUE=\""+fspBez   +"\"       SIZE=24 MAXLENGTH=24></TD></TR>"+
							"<TR><TD>Init-Wert Pf-Status</TD><TD><INPUT TYPE=\"text\" NAME=\"fspPfIni\" VALUE=\""+fspPfIni +"\"       SIZE=2  MAXLENGTH=2 ></TD></TR>"+
							"<TR><TD>&nbsp;            </TD><TD><INPUT TYPE =\"SUBMIT\" VALUE=\""+buttonValue+"\">                        </TD></TR></TABLE></FORM>");
				}
				if (formParameter.compareTo("puffer")==0) {
					if (request.getParameter("pfNr")!=null ) {
						actionModul= "aendern";
						actionForm = "puffer";
						buttonValue= "&Auml;ndern";
						sqlString  = "SELECT pf_pfnr,pf_bez,pf_kz,pf_status,pf_fs FROM plrv11.bde_pfdat WHERE pf_pfnr=" + request.getParameter("pfNr");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rs.next();
						pfBez    = rs.getString("pf_bez");
						pfKz     = rs.getString("pf_kz").charAt(0);
						pfStatus = Integer.parseInt(rs.getString("pf_status"));
						pfFs     = rs.getString("pf_fs");
						schluesselFeld =  "<INPUT TYPE=\"hidden\" NAME=\"pfNr\" VALUE=\""+rs.getString("pf_pfnr")+"\">"+rs.getString("pf_pfnr");
					} else {
						actionModul= "einfuegen";
						actionForm = "puffer";
						buttonValue= "Speichern";
						schluesselFeld =  "<INPUT TYPE=\"text\" NAME=\"pfNr\"   SIZE=8  MAXLENGTH=8 >";
					}
					out.println(javaScripts+
							"</HEAD><BODY bgcolor=\"#CCCCCC\"onLoad=\"setFrame('list','" + appRoot +"bde?modul=liste&formular=puffer')\">"+
							"<H1><CENTER>Puffer</CENTER><H1>"+
							"<FORM ACTION=\"" + appRoot + "bde\"      METHOD=\"GET\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\""+actionModul+"\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\""+actionForm +"\">"+
							"<TABLE BORDER=0>"+
							"<TR><TD>Puffer-Nr.          </TD><TD>" + schluesselFeld                                         +"</TD></TR>"+
							"<TR><TD>Bezeichnung         </TD><TD><INPUT TYPE=\"text\" NAME=\"pfBez\"    VALUE=\""+ pfBez    +"\" SIZE=24 MAXLENGTH=24></TD></TR>"+
							"<TR><TD>Automatische Vergabe</TD><TD><INPUT TYPE=\"text\" NAME=\"pfKz\"     VALUE=\""+ pfKz     +"\" SIZE=1  MAXLENGTH=11></TD></TR>"+
							"<TR><TD>Pufferstatus        </TD><TD><INPUT TYPE=\"text\" NAME=\"pfStatus\" VALUE=\""+ pfStatus +"\" SIZE=2  MAXLENGTH=2 ></TD></TR>"+
							"<TR><TD>Fertigungsstufe     </TD><TD><INPUT TYPE=\"text\" NAME=\"pfFs\"     VALUE=\""+ pfFs     +"\" SIZE=11 MAXLENGTH=11></TD></TR>"+
							"<TR><TD>&nbsp;              </TD><TD><INPUT TYPE =\"SUBMIT\" VALUE=\""+buttonValue     +"\"></TD></TR></TABLE></FORM>");
				}
				if (formParameter.compareTo("pufferPl")==0) {
					String aender   = new String();
					String eingabe  = new String();
					if (request.getParameter("pfPlNr")!=null) {
						actionModul = "aendern";
						buttonValue = "&Auml;ndern";
						aender ="&Auml;ndern";
						sqlString = "SELECT pp_pf_platz,pp_pfnr,pp_pfzone,pp_abnr,pp_pfnr,pp_pfzone,pp_abnr,pp_ab2,pp_ab3,pp_fs,pp_kz,pp_zuteil_kz FROM "+
						"plrv11.bde_pufpl WHERE pp_pf_platz="+request.getParameter("pfPlNr")+" AND pp_pfnr="+request.getParameter("pfNr");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						rsZaehl=0;
						while (rs.next()) {
							rsZaehl++;
							ppPfPlatz = rs.getString("pp_pf_platz");
							ppPfNr    = rs.getInt("pp_pfnr");
							ppPfZone  = rs.getInt("pp_pfzone");
							ppAbNr    = rs.getInt("pp_abnr");
							ppFs      = rs.getString("pp_fs");
							ppKz      = rs.getString("pp_kz").charAt(0);
							eingabe = "Pufferplatz-Nr.   </TD><TD>"+ ppPfPlatz +"</TD></TR>\n"+
								"    <INPUT TYPE=\"hidden\" NAME=\"pfNr\"      SIZE=8  MAXLENGTH=8  VALUE=\""+ ppPfNr    +"\">\n"+
								"    <INPUT TYPE=\"hidden\" NAME=\"ppPfPlatz\" VALUE=\""+ppPfPlatz+"\">"+
								"<TR><TD>Pufferzone</TD><TD>"+ ppPfZone  +"</TD></TR>\n"+
								"<TR><TD>Kennzeichen</TD><TD>";
							if (ppKz=='J') {
								eingabe += "<SELECT NAME=\"ppKz\"><OPTION VALUE=\"J\" SELECTED>Belegt"+
									"<OPTION VALUE=\"N\">Nicht belegt"+
									"<OPTION VALUE=\"U\">&Uuml;berlauf - nicht belegt"+
									"<OPTION VALUE=\"B\">&Uuml;berlauf - belegt"+
									"<OPTION VALUE=\"D\">Deaktiviert</SELECT>";
							} else if (ppKz=='N') {
								eingabe += "<SELECT NAME=\"ppKz\"><OPTION VALUE=\"J\">Belegt"+
									"<OPTION VALUE=\"N\" SELECTED>Nicht belegt"+
									"<OPTION VALUE=\"U\">&Uuml;berlauf - nicht belegt"+
									"<OPTION VALUE=\"B\">&Uuml;berlauf - belegt"+
									"<OPTION VALUE=\"D\">Deaktiviert</SELECT>";
							} else if(ppKz=='D') {
								eingabe += "<SELECT NAME=\"ppKz\"><OPTION VALUE=\"J\">Belegt"+
									"<OPTION VALUE=\"N\">Nicht belegt"+
									"<OPTION VALUE=\"U\">&Uuml;berlauf - nicht belegt"+
									"<OPTION VALUE=\"B\">&Uuml;berlauf - belegt"+
									"<OPTION VALUE=\"D\" SELECTED>Deaktiviert</SELECT>";
							} else if(ppKz=='U') {
								eingabe += "<SELECT NAME=\"ppKz\"><OPTION VALUE=\"J\">Belegt"+
									"<OPTION VALUE=\"N\">Nicht belegt"+
									"<OPTION VALUE=\"U\" SELECTED >&Uuml;berlauf - nicht belegt"+
									"<OPTION VALUE=\"B\">&Uuml;berlauf - belegt"+
									"<OPTION VALUE=\"D\">Deaktiviert</SELECT>";
							} else if(ppKz=='B') {
								eingabe += "<SELECT NAME=\"ppKz\"><OPTION VALUE=\"J\">Belegt"+
									"<OPTION VALUE=\"N\">Nicht belegt"+
									"<OPTION VALUE=\"U\">&Uuml;berlauf - nicht belegt"+
									"<OPTION VALUE=\"B\" SELECTED >&Uuml;berlauf - belegt"+
									"<OPTION VALUE=\"D\">Deaktiviert</SELECT>";
							} else {
								eingabe += "Achtung! Inkonsistente Daten! Bitte EDV benachrichtigen!";
							}
							// Loesch-Button nur einblenden wenn Puffer nicht belegt ist
							if ((ppKz=='n') || (ppKz=='N') || (ppKz=='d') || (ppKz=='D')) {
								loeschButton = "<FORM ACTION=\""+appRoot+"bde\" METHOD=\"GET\">"+
								"<INPUT TYPE=\"hidden\" NAME=\"modul\" VALUE=\"loeschen\">"+
								"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"pufferPl\">"+
								"<INPUT TYPE=\"hidden\" NAME=\"loeschID\" VALUE=\""+request.getParameter("pfPlNr")+","+request.getParameter("pfNr")+"\">"+
								"<INPUT TYPE=\"submit\" VALUE=\"L&ouml;schen\"></FORM>";
							} else {
								loeschButton="<FONT COLOR=red>Pufferplatz ist belegt!</FONT>";
							}
						}
					} else {
						actionModul="einfuegen";
						buttonValue="Einf&uuml;gen";
						aender    ="Neuanlage";
						ppPfPlatz = request.getParameter("pfNr");
						ppPfNr    = Integer.parseInt(request.getParameter("pfNr"));
						eingabe   = "Pufferplatz-Nr.   </TD><TD><INPUT TYPE=\"text\"   NAME=\"ppPfPlatz\" SIZE=11 MAXLENGTH=11 VALUE=\""+ ppPfPlatz +"\"></TD></TR>\n"+
						"                           <INPUT TYPE=\"hidden\" NAME=\"pfNr\"      SIZE=8  MAXLENGTH=8  VALUE=\""+ ppPfNr    +"\">\n"+
						"<TR><TD>Pufferzone</TD><TD><INPUT TYPE=\"text\"   NAME=\"ppPfZone\"  SIZE=2  MAXLENGTH=2  VALUE=\""+ ppPfZone  +"\"></TD></TR>\n";
						sqlString="SELECT pf_fs FROM plrv11.bde_pfdat WHERE pf_pfnr="+request.getParameter("pfNr");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						while (rs.next()) {
							eingabe +="<TR><TD>Fertigungsstufe </TD><TD><INPUT TYPE=\"text\"  NAME=\"ppFs\" SIZE=11 MAXLENGTH=11 VALUE=\""+rs.getString("pf_fs")+"\"></TD></TR>\n";
						}
					}
					out.println(javaScripts+
							"</HEAD><BODY bgcolor=\"#CCCCCC\"onLoad=\"setFrame('list','" + appRoot +
							"bde?modul=liste&formular=pufferPl&pfNr="+request.getParameter("pfNr")+"')\">"+
							"<H1><CENTER>Pufferplatz-Daten pflegen</CENTER></H1>\n"+
							aender +"<BR>\n"+
							"f&uuml;r den Puffer "+request.getParameter("pfNr")+"\n"+
							"<TABLE BORDER=0>\n"+
							"<TR><TD> "+
							"<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">\n"+
							"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\""+actionModul+"\">\n"+
							"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"pufferPl\">\n"+
							eingabe);
					out.println("<TR><TD><INPUT TYPE =\"SUBMIT\" VALUE=\""+buttonValue+"\"></FORM></TD><TD>"+loeschButton+"</TD></TR>"+
							"</TABLE>\n");
				}
				if (formParameter.compareTo("fsStufe")==0) {
					out.println(javaScripts+
							"</HEAD><BODY bgcolor=\"#CCCCCC\">"+
							"<FORM ACTION=\"" + appRoot + "bde\" METHOD=\"GET\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"einfuegen\">"+
							"<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"fsStufe\">"+
							"<CENTER><H1>Manuell eine Fertigungsstufe einf&uuml;gen</h1></CENTER>"+
							"<TABLE BORDER=0>"+
							"<TR><TD>AB-Nr.                </TD><TD><INPUT TYPE=\"text\"   NAME=\"abNr\"     SIZE=6  MAXLENGTH=6            ></TD>"+
							"    <TD>Polsterprogramm       </TD><TD><INPUT TYPE=\"text\"   NAME=\"prog\"     SIZE=3  MAXLENGTH=3            ></TD></TR>"+
							"    <TD>Polstertag            </TD><TD><INPUT TYPE=\"text\"   NAME=\"fabtPPS\"  SIZE=4  MAXLENGTH=4            ></TD></TR>"+
							"<TR><TD>FS-Positions-Nr.      </TD><TD><INPUT TYPE=\"text\"   NAME=\"ordNr\"    SIZE=3  MAXLENGTH=3            ></TD>"+
							"    <TD>Fertigungsstufe       </TD><TD><INPUT TYPE=\"text\"   NAME=\"fs\"       SIZE=11 MAXLENGTH=11           ></TD></TR>"+
							"<TR><TD>R&uuml;stzeit         </TD><TD><INPUT TYPE=\"text\"   NAME=\"tr\"       SIZE=4  MAXLENGTH=4            ></TD>"+
							"    <TD>Bearbeitungszeit      </TD><TD><INPUT TYPE=\"text\"   NAME=\"te\"       SIZE=7  MAXLENGTH=7            ></TD></TR>"+
							"<TR><TD>DEZI                  </TD><TD><INPUT TYPE=\"text\"   NAME=\"dezi\"     SIZE=7  MAXLENGTH=7            ></TD>"+
							"    <TD>Personal-Nr.          </TD><TD><INPUT TYPE=\"text\"   NAME=\"pnr\"      SIZE=4  MAXLENGTH=4            ></TD></TR>"+
							"<TR><TD>FS-Status             </TD><TD><INPUT TYPE=\"text\"   NAME=\"fsStatus\" SIZE=2  MAXLENGTH=2            ></TD>"+
							"    <TD>Puffer-Status         </TD><TD><INPUT TYPE=\"text\"   NAME=\"pfStatus\" SIZE=2  MAXLENGTH=2            ></TD></TR>"+
							"<INPUT TYPE=hidden                                            NAME=\"splKz\"                        VALUE=\"N\">"+
							"<INPUT TYPE=hidden                                            NAME=\"aendIx\"                       VALUE=\"1\">"+
							"<INPUT TYPE=hidden                                            NAME=\"prio\"                         VALUE=\"20\">"+
							"<TR><TD>&nbsp;              </TD><TD><INPUT TYPE =\"SUBMIT\" VALUE=\"Speichern\">                 </TD></TR></TABLE></FORM>");
				}
			}
			// ******************************************************* A K T I V I E R E N    U N D    I N A K T I V I E R EN **************************************
			if ( (modulParameter.compareTo("akt_inakt")==0) && (userRecht>=30) ) {
				if (formParameter.compareTo("ap")==0) {
					String apListe = new String();
					String aktField= new String();
					try {
						sqlString="SELECT ap_platznr,ap_kst,ap_fs,ap_bez,ap_kz FROM plrv11.bde_apdat WHERE ap_kst='"+request.getParameter("kst")+"' ORDER BY ap_platznr";
						stmt.executeQuery(sqlString);
						rs =stmt.getResultSet();
						while (rs.next()) {
							if (rs.getString("ap_kz").compareTo("J")==0) {
								aktField="<B><FONT COLOR=#00ff00>Aktiv  </FONT></B><INPUT TYPE=\"hidden\" name=\"apKz\" value=\"N\">";
							} else {
								aktField="<B><FONT COLOR=#ff0000>Inaktiv</FONT></B><INPUT TYPE=\"hidden\" name=\"apKz\" value=\"J\">";
							}
							apListe = apListe+"<TR bgcolor=#"+changeColor()+"><FORM ACTION=\""+appRoot+"bde\" METHOD=\"GET\">"+
								"<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"aendern\">"+
								"<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"ap2\">"+
								"<INPUT TYPE=\"hidden\" NAME=\"apPlatzNr\" VALUE=\""+rs.getString("ap_platznr")+"\">"+
								"<TD>"+rs.getString("ap_platznr")+"</TD><TD>"+
								rs.getString("ap_kst")+"</TD><TD>"+rs.getString("ap_fs")+"</TD><TD>"+rs.getString("ap_bez")+"</TD><TD>"+
								aktField+"</TD><TD><INPUT TYPE=\"SUBMIT\" VALUE=\"&Auml;ndern\"></TD></FORM></TR>";
						}
					}
					catch(SQLException sqlEx) {
						meldung = "DatenbankFehler";
						sqlEx.printStackTrace();
					}
					out.println(javaScripts+
							"</HEAD><BODY bgcolor=\"#CCCCCC\"onLoad=\"setFrame('list','" + appRoot +"bde?modul=liste&formular=ap')\">"+
							"<CENTER><H1>Arbeitspl&auml;tze</H1><TABLE BORDER=1>"+
							"<TR><TD><FONT SIZE=+1>Platz-Nr.</FONT></TD><TD><FONT SIZE=+1>Kostenstelle</FONT></TD><TD><FONT SIZE=+1>Fertigungsstufe</FONT></TD>"+
							"<TD><FONT SIZE=+1>Abt-Bezeichnung</FONT></TD><TD><FONT SIZE=+1>KZ-Aktiv</FONT></TD><TD><FONT SIZE=+1>&Auml;ndern</FONT></TD></TR>"+
							apListe+"</TABLE></CENTER>");
				}
			}
			// ********************************************************************** E I N F U E G E N  *******************************************************
			if ((modulParameter.compareTo("einfuegen")==0) && (userRecht>=30)){
				int    error=0;
				if (formParameter.compareTo("ma")==0) {
					maNName  = request.getParameter("maNName");
					maVName  = request.getParameter("maVName");
					maAnmeld = request.getParameter("maAnmeld").charAt(0);
					maAP     = request.getParameter("maAP");
					maProd   = request.getParameter("maProd").charAt(0);
					maUser   = request.getParameter("maUser");
					maTeamKz = request.getParameter("teamKz").charAt(0);
					if (maUser.compareTo("")==0) {
						maUser=" ";
					}
					try {
						maPNr         = Integer.parseInt(request.getParameter("maPNr"));
						maKst         = Integer.parseInt(request.getParameter("maKst"));
						maTel         = Integer.parseInt(request.getParameter("maTel"));
						maRecht       = Integer.parseInt(request.getParameter("maRecht"));
						maPIN         = Integer.parseInt(request.getParameter("maPIN"));
						maMaxAnzAuftr = Integer.parseInt(request.getParameter("maMaxAnzAuftr"));
						maRecht       = Integer.parseInt(request.getParameter("maRecht"));
						maTeam        = Integer.parseInt(request.getParameter("team"));
						maWerk        = Integer.parseInt(request.getParameter("maWerk"));
						maDrucker     = request.getParameter("maDrucker");
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						error = 1;
						log("Ein Fehler ist aufgetreten beim Umwandeln von Zahlen im Modul einfuegen, Formuler ma...");
					}
					if (maPNr==0) error=3;
					sqlString  = "INSERT INTO PLRV11.BDE_MADAT ";
					sqlString += "(MA_PNR,MA_NNAME,MA_VNAME,MA_KST,MA_TEL,MA_AP,MA_ANMELD,MA_RECHT,MA_PIN,MA_PROD,MA_MAX_AB,MA_USER,ma_team,ma_team_kz,ma_werk,ma_drucker) ";
					sqlString += "VALUES (";
					sqlString += maPNr         + ",'"  ;
					sqlString += maNName       + "','" ;
					sqlString += maVName       + "',"  ;
					sqlString += maKst         + ","   ;
					sqlString += maTel         + ","   ;
					sqlString += maAP          + ",'"  ;
					sqlString += String.valueOf(maAnmeld).toUpperCase() + "',";
					sqlString += maRecht       + ","   ;
					sqlString += maPIN         + ",'"  ;
					sqlString += String.valueOf(maProd).toUpperCase()   + "',";
					sqlString += maMaxAnzAuftr + ",'"  ;
					sqlString += maUser        + "',"  ;
					sqlString += maTeam        + ",'"  ;
					sqlString += maTeamKz      + "',"  ;
					sqlString += maWerk        + ",'";
					sqlString += maDrucker     + "')";
					backFormular="ma";
					params="&maPNr="+request.getParameter("maPNr")+"&klass="+request.getParameter("klass")+"&kst="+request.getParameter("kst");
				}
				// *******************************************************************
				if (formParameter.compareTo("ap1")==0) {
					apPlatzNr = request.getParameter("apPlatzNr");
					apFs      = request.getParameter("apFs");
					apBez     = request.getParameter("apBez");
					apKz      = request.getParameter("apKz");
					try {
						apKst = Integer.parseInt(request.getParameter("apKst"));
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						error = 1;
						out.println("Ein Fehler ist aufgetreten beim Umwandelnd von Zahlen im Modul einfuegen, Formular ap...");
					}
					sqlString = "INSERT INTO plrv11.bde_apdat (ap_platznr,ap_kst,ap_fs,ap_bez,ap_kz) "+
						"VALUES ('"+
						apPlatzNr          + "',"  +
						apKst              + ",'"  +
						apFs               + "','" +
						apBez              + "','" +
						apKz.toUpperCase() + "')";
					params="&kst="+request.getParameter("kst");
					backFormular="ap";
				}
				// ********************************************************************
				if (formParameter.compareTo("einarb")==0) {
					maNName  = request.getParameter("maNName");
					String fs =new String();
					long prog =0;
					fs        = request.getParameter("fs");
					try {
						maPNr     = Integer.parseInt(request.getParameter("maPNr"));
						prog      = Integer.parseInt(request.getParameter("progInput"));
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						error = 1;
						log("Fehler beim Einfügen einer MA-Einarbeit");
					}
					sqlString = "INSERT INTO plrv11.bde_ma_prog (map_pnr,map_prog,map_fs) VALUES ("+
						maPNr + "," +
						prog  + ",'" +
						fs    + "')";
					backFormular="ma&maPNr="+maPNr+"&klass="+request.getParameter("klass")+"&kst="+request.getParameter("kst");
				}
				// ********************************************************************
				if (formParameter.compareTo("fsParam")==0) {
					fspBez     = request.getParameter("fspBez");
					fspFs      = request.getParameter("fspFs");
					try {
						fspWerkPo    = Integer.parseInt(request.getParameter("fspWerkPo"));
						fspMaGru     = Integer.parseInt(request.getParameter("fspMaGru"));
						fspOrdNr     = Integer.parseInt(request.getParameter("fspOrdNr"));
						fspPfIni     = Integer.parseInt(request.getParameter("fspPfIni"));
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						error = 1;
					}
					sqlString = "INSERT INTO plrv11.bde_fs_param (fsp_werk_po,fsp_magru,fsp_ordnr,fsp_fs,fsp_bez,fsp_pf_ini) VALUES ("+
						fspWerkPo + ","  +
						fspMaGru  + ","  +
						fspOrdNr  + ",'"  +
						fspFs     + "','" +
						fspBez    + "',"+
						fspPfIni  +  ")";
					backFormular="fsParam";
				}
				// ********************************************************************
				if (formParameter.compareTo("puffer")==0) {
					pfBez = request.getParameter("pfBez");
					pfFs  = request.getParameter("pfFs");
					pfKz  = request.getParameter("pfKz").charAt(0);
					try {
						pfPfNr    = Integer.parseInt(request.getParameter("pfNr"));
						pfStatus  = Integer.parseInt(request.getParameter("pfStatus"));
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						error = 1;
					}
					sqlString = "INSERT INTO plrv11.bde_pfdat (pf_pfnr,pf_bez,pf_kz,pf_status,pf_fs) VALUES ("+
						pfPfNr   + ",'"  +
						pfBez    + "','" +
						String.valueOf(pfKz).toUpperCase()     + "',"  +
						pfStatus + ",'" +
						pfFs     + "')";
					backFormular="puffer";
				}
				// ********************************************************************
				if (formParameter.compareTo("pufferPl")==0) {
					ppFs   = request.getParameter("ppFs");
					ppPfPlatz = request.getParameter("ppPfPlatz");
					try {
						ppPfPlatz = request.getParameter("ppPfPlatz");
						ppPfNr    = Integer.parseInt(request.getParameter("pfNr"));
						ppPfZone  = Integer.parseInt(request.getParameter("ppPfZone"));
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						error = 1;
					}
					sqlString = "INSERT INTO plrv11.bde_pufpl (pp_pf_platz,pp_pfnr,pp_pfzone,pp_abnr,pp_ab2,pp_ab3,pp_fs,pp_kz,pp_zuteil_kz) VALUES ('"+
						ppPfPlatz  + "',"  +
						ppPfNr     + "," +
						ppPfZone   + "," +
						"0,0,0,'"  +    // Initial wird fuer die AB-Nr 0 eingetragen
						ppFs       + "','D','N')";  // Initial wird ein Pufferplatz auf deaktiviert" gesetzt
					backFormular="pufferPl&pfNr="+request.getParameter("pfNr");
				}
				// Fehlerbehandlung fuer alle Faelle
				try {
					if (error==0) {
						stmt.executeUpdate(sqlString);
					}
				}
				catch (SQLException se) {
					error=1;
					se.printStackTrace();
					sqlError=se.getMessage();
				}
				stmt.close();
				if (error!=0) {
					out.println("</HEAD><BODY BGCOLOR=#FFFFFF><FONT COLOR=red>");
					out.println("Sie haben Eingaben gemacht, die nicht verarbeitet werden k&ouml;nnen!</FONT><BR>Fehler:"+sqlString+"<BR>"+sqlError);
					if (error==3) out.println("<BR><B>Personalnummer muss ungleich 0 sein</B>");
				} else {
					out.println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"1; URL="+appRoot+"bde?modul=anlegen&formular=" + backFormular +params+"\">" +
						javaScripts +
						"</HEAD><BODY bgcolor=\"#CCCCCC\">"+
						"<CENTER>Datensatz wurde angelegt:" + meldung +"</CENTER>");
				}
			}
			// **************************************************** L I S T E N ******************************************************
			if ((modulParameter.compareTo("liste")==0) && (userRecht>=30)) {
				String result   = new String();
				String sqlString2= new String();
				if (formParameter.compareTo("ma")==0) {
					String anmeld= new String("");
					String lap   = new String();
					String anzAuftr = new String();
					String fp = new String();
					Statement stmt2 = con.createStatement();
					ResultSet rs2;
					String lman    = new String();
					String lanmeld = new String();
					String klasse  = new String();
					int    lmaxab;
					try {
						result += "<TR BGCOLOR=\"white\"><TD>AP</TD><TD>PNr</TD><TD>Name</TD><TD>&nbsp;</TD>";
						result += "<TD>AA</TD><TD>Max</TD><TD>FP</TD><TD>&nbsp;</TD></TR>";
						if (request.getParameter("klass").compareTo("user")==0) {
							klasse = "WHERE ma_prod='J'  ";
						}
						if (request.getParameter("klass").compareTo("admin")==0) {
							klasse = "WHERE ma_prod='N'  ";
						}
						sqlString = "SELECT ma_pnr,ma_nname,ma_vname,ma_ap,ma_anmeld,ma_kst,ma_max_ab FROM plrv11.bde_madat " +
						klasse +" and ma_kst="+request.getParameter("kst")+" ORDER BY ma_kst,ma_ap";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						while (rs.next()) {
							try {
								if ( (rs.getString("MA_PNR")!= null) && (rs.getInt("MA_PNR")!=0) ) {
									lanmeld = rs.getString("ma_anmeld");
									lmaxab = rs.getInt("ma_max_ab");
									if (lanmeld==null) {
										anmeld="<TD BGCOLOR=red>&nbsp;</TD>";
									} else if (lanmeld.compareTo("J")==0) {
										anmeld="<TD BGCOLOR=yellow>&nbsp;</TD>";
									} else {
										anmeld="<TD BGCOLOR=\"#ddffdd\">&nbsp;</TD>";
									}
									// Anzahl der Auftraege dieses MAs selektieren
									stmt2.executeQuery("SELECT count(*) FROM plrv11.bde_ab_fs_ma WHERE abf_pnr="+
											rs.getString("ma_pnr")+" AND abf_fs_status in (5,10,30)");
									rs2 = stmt2.getResultSet();
									rs2.next();
									anzAuftr=rs2.getString(1);
									try {
										// Anzahl freier Pufferplätze ermitteln
										sqlString2  = "SELECT count(*) FROM plrv11.bde_pufpl ";
										sqlString2 += "WHERE substr(pp_pf_platz,3,3)='" +rs.getString("ma_ap").substring(2,5)+"'";
										sqlString2 += " AND substr(pp_pf_platz,6,2) ='30' ";
										sqlString2 += " AND substr(pp_pf_platz,8,2)  ='"+rs.getString("ma_ap").substring(9,11);
										sqlString2 += "' AND pp_kz IN ('N','U')";
										stmt2.executeQuery(sqlString2);
										rs2 = stmt2.getResultSet();
										rs2.next();
										fp=rs2.getString(1);
										try {
											lap = rs.getString("ma_ap").substring(8,11);
											maAP= rs.getString("ma_ap");
										}
										catch (StringIndexOutOfBoundsException sioobex) {
											lap = "<FONT COLOR=red>Fehler!!</FONT>";
										}
									}
									catch (Exception ex) {
										lap ="0";
										maAP="0";
									}
									lman = rs.getString("MA_PNR");
									result += "<TR BGCOLOR="+changeColor()+">";
									result += "<TD><A HREF=\""+appRoot+"bde?modul=liste&formular=maPf&pp="+maAP+"\" TARGET=\"main\">";
									result += lap +"</A></TD>";
									result += "<TD><A HREF=\""+appRoot+"bde?modul=anlegen&formular=ma&maPNr=";
									result += lman+"&klass="+request.getParameter("klass")+"&kst="+request.getParameter("kst")+"\" target=main>" + lman;
									result += "</A></TD>";
									result += "<TD>"+ rs.getString("MA_NNAME").trim() + "<B>,</B>";
									result += rs.getString("MA_VNAME").trim() + "</TD>"+anmeld+"<TD>"+anzAuftr+ "</TD><TD>" + lmaxab;
									result += "</TD><TD>"+fp+"</TD>";
									result += "<TD><A HREF=\""+appRoot+ "bde?modul=planung&formular=auswahl&modus=pNr&fs=99" + rs.getString("ma_kst");
									result += "999999&userSicht=" + userSicht;
									result += "&pNr="+lman+"\" target=\"main\">Auftr.</A></TD></TR>\n";
									rs2.close();
								} else {
									result += "<TR BGCOLOR='red'><TD>Fehler</TD></TR>";
								}
							} catch (SQLException sqlex) {
								sqlex.printStackTrace();
								result += "<H1><FONT COLOR=red>SQL-Fehler beim MA-Listen</FONT></H1>Fehler:"+sqlex.getSQLState();
							}
						}
						rs.close();
						stmt2.close();
					}
					catch( Exception ex) {
						ex.printStackTrace();
						result += "<H1><FONT COLOR=red>Unbekannter Fehler beim MA-Listen</FONT></H1>";
						result += ex.getMessage();
					}
				}
				if (formParameter.compareTo("fsParam")==0) {
					stmt.executeQuery("SELECT FSP_FS,FSP_BEZ,fsp_magru FROM PLRV11.BDE_FS_PARAM ORDER BY FSP_FS");
					rs = stmt.getResultSet();
					while (rs.next()) {
						result = result+"<TR BGCOLOR="+changeColor()+"><TD><A HREF=\""+appRoot+"bde?modul=anlegen&formular=fsParam&fs="+
						rs.getString("FSP_FS")+"&fspMaGru="+rs.getString("fsp_magru") +"\" target=\"main\">" +
						rs.getString("FSP_FS") + "</A></TD><TD>" + rs.getString("FSP_BEZ") +
						"</TD><TD><A HREF=\""+appRoot+"bde?modul=loeschen&formular=fsParam&loeschID=" +
						rs.getString("FSP_FS") +"&fspMaGru="+rs.getString("fsp_magru")+"\">L&ouml;schen</A></TD></TR>\n";
					}
					rs.close();
				}
				if (formParameter.compareTo("ap")==0) {
					sqlString = "SELECT AP_PLATZNR,AP_KST,AP_FS,AP_BEZ,AP_KZ FROM PLRV11.BDE_APDAT WHERE ap_kst=";
					sqlString += request.getParameter("kst") +" ORDER BY AP_PLATZNR";
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						result += "<TR BGCOLOR="+changeColor()+"><TD><A HREF=\"";
						result += appRoot+"bde?modul=anlegen&formular=ap&apPlatzNr="+rs.getString("AP_PLATZNR")+"&kst="+request.getParameter("kst");
						result += "\" target=\"main\">" + rs.getString("AP_PLATZNR") + "</A></TD>";
						result += "<TD><A HREF=\""+appRoot+"bde?modul=loeschen&formular=ap&loeschID=";
						result += rs.getString("AP_PLATZNR") +"&kst="+request.getParameter("kst")+ "\" target=\"main\" >L&ouml;schen</A></TD></TR>\n";
					}
					rs.close();
				}
				if (formParameter.compareTo("maPf")==0) {
					sqlString =  "SELECT pp_pf_platz,pp_abnr,pp_kz,abf_fs_status,abf_prog FROM plrv11.bde_pufpl ";
					sqlString +=  "INNER JOIN plrv11.bde_ab_fs_ma ON abf_abnr=pp_abnr AND pp_fs=abf_fs ";
					sqlString +=  "INNER JOIN plrv11.plr_auftr_status ON abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix  ";
					sqlString += " WHERE substr(pp_pf_platz,3,3)='"+request.getParameter("pp").substring(2,5)+"'";
					sqlString += " AND substr(pp_pf_platz,6,2)  ='30' ";
					sqlString += " AND substr(pp_pf_platz,8,2)  ='"+request.getParameter("pp").substring(9,11)+"'";
					sqlString += " AND as_status<>99";
					String farbe = new String();
					try {
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						result += "<TR><TH>Platz</TH><TH>AB-Nr</TH><TH>KZ</TH><TH>Prog.</TH><TH>FS-Status</TH></TR>";
						while (rs.next()) {
							result += "<TR BGCOLOR=\""+changeColor()+"\"><TD>";
							result += rs.getString("pp_pf_platz");
							if (rs.getString("pp_kz").compareTo("J")==0) {
								farbe ="yellow";
							} else if (rs.getString("pp_kz").compareTo("D")==0) {
								farbe ="red";
							} else if (rs.getString("pp_kz").compareTo("U")==0) {
								farbe ="#66ff66";
							} else if (rs.getString("pp_kz").compareTo("B")==0) {
								farbe ="#6666ff";
							} else {
								farbe = "green";
							}
							result +="</TD><TD>"+rs.getString("pp_abnr");
							result +="</TD><TD BGCOLOR=\""+farbe+"\">"+rs.getString("pp_kz");
							result +="</TD><TD>"+rs.getString("abf_prog");
							result +="</TD><TD>"+rs.getString("abf_fs_status")+"</TD></TR>";
						}
						rs.close();
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
				if (formParameter.compareTo("puffer")==0) {
					stmt.executeQuery("SELECT  PF_PFNR,PF_BEZ FROM PLRV11.BDE_PFDAT ORDER BY PF_PFNR");
					rs = stmt.getResultSet();
					while (rs.next()) {
						result = result+"<TR BGCOLOR="+changeColor()+"><TD><A HREF=\""+appRoot+"bde?modul=anlegen&formular=puffer&pfNr=" +
						rs.getString("PF_PFNR") + "\" target=\"main\">" +  rs.getString("PF_PFNR") +
						"</A></TD><TD><A HREF=\""+appRoot+"bde?modul=anlegen&formular=pufferPl&pfNr="+rs.getString("PF_PFNR")+
						"\" TARGET=\"main\">"+rs.getString("PF_BEZ")+"</A></TD>"+
						"<TD><A HREF=\""+appRoot+"bde?modul=loeschen&formular=puffer&loeschID=" + rs.getString("PF_PFNR") +"\">L&ouml;schen</A></TD></TR>\n";
					}
					rs.close();
				}
				if (formParameter.compareTo("pufferPl")==0) {
					stmt.executeQuery("SELECT  PP_PF_PLATZ,PP_PFNR,pp_pfzone,pp_kz,pp_abnr FROM PLRV11.BDE_PUFPL WHERE PP_PFNR="+
						request.getParameter("pfNr")+" ORDER BY PP_PF_PLATZ");
					result=result+"<TR><TH COLSPAN=3> Pl&auml;tze des Puffers "+request.getParameter("pfNr") +"</TH></TR>";
					rs = stmt.getResultSet();
					while (rs.next()) {
						result = result+"<TR BGCOLOR="+changeColor()+"><TD>"+
						"<A HREF=\""+appRoot+"bde?modul=anlegen&formular=pufferPl&pfPlNr="+
						rs.getString("pp_pf_platz")+"&pfNr="+request.getParameter("pfNr")+"\" target=\"main\">"+
						rs.getString("PP_PF_PLATZ") +
						"</A></TD><TD>"+rs.getString("pp_abnr")+"</TD><TD>"+rs.getString("pp_pfzone")+"</TD>";
						if (rs.getString("pp_kz").compareTo("J")==0) {
							result +="<TD BGCOLOR=yellow>&nbsp;</TD></TR>";
						} else if (rs.getString("pp_kz").compareTo("D")==0) {
							result +="<TD BGCOLOR=red>&nbsp;</TD></TR>";
						} else if (rs.getString("pp_kz").compareTo("U")==0) {
							result +="<TD BGCOLOR=\"#66ff66\">&nbsp;</TD></TR>";
						} else if (rs.getString("pp_kz").compareTo("B")==0) {
							result +="<TD BGCOLOR=\"#6666ff\">&nbsp;</TD></TR>";
						} else {
							result +="<TD BGCOLOR=green>&nbsp;</TD></TR>";
						}
					}
					rs.close();
				}
				stmt.close();
				out.println("</HEAD><BODY bgcolor=\"#DDDDDD\">"+
					"<TABLE>"+result);
				out.println("</TABLE>");
			}
			// ****************************************************** L O E S C H E N ******************************************************************
			if ((modulParameter.compareTo("loeschSicher")==0) && (userRecht>=30)) {
				if (formParameter.compareTo("ma")==0) {
					out.println("</HEAD><BODY bgcolor=\"#DDDDDD\">");
					sqlString = "SELECT ma_nname,ma_vname,ma_pnr FROM plrv11.bde_madat WHERE ma_pnr="+request.getParameter("loeschID");
					stmt.executeQuery(sqlString);
					out.println("<B><FONT COLOR=\"red\">Sind Sie sicher, dass Sie den folgenden Benutzer endg&uuml;ltig l&ouml;schen wollen?</FONT></B><BR>");
					rs = stmt.getResultSet();
					while (rs.next()) {
						out.println(rs.getString("ma_nname").trim()+", "+rs.getString("ma_vname").trim()+" - P-Nr.:"+rs.getString("ma_pnr")+"<BR>");
					}
					rs.close();
					out.println("<TABLE BORDER=\"0\">");
					out.println("<TR><FORM NAME=\"loeschSicher\" ACTION=\""+appRoot+"\"><TD>");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"loeschen\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"ma\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"loeschID\" VALUE=\""+request.getParameter("loeschID")+"\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"kst\"      VALUE=\""+request.getParameter("kst")+"\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"klass\"    VALUE=\""+request.getParameter("klass")+"\">");
					out.println("<INPUT TYPE=\"submit\" NAME=\"ok\"       VALUE=\"Ja, l&ouml;schen\">");
					out.println("</TD></FORM>");
					out.println("<FORM NAME=\"loeschAbbr\" ACTION=\""+appRoot+"\"><TD>");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"    VALUE=\"anlegen\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\" VALUE=\"ma\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"kst\"      VALUE=\""+request.getParameter("kst")  +"\">");
					out.println("<INPUT TYPE=\"hidden\" NAME=\"klass\"    VALUE=\""+request.getParameter("klass")+"\">");
					out.println("<INPUT TYPE=\"submit\" NAME=\"abbr\"     VALUE=\"Abbrechen\">");
					out.println("</TD></FORM></TR></TABLE>");
				}
			}
			if ((modulParameter.compareTo("loeschen")==0) && (userRecht>=30)) {
				String loeschID     = new String();
				long nr;
				formParameter= request.getParameter("formular");
				loeschID = (request.getParameter("loeschID"));
				backFormular=formParameter;
				backModul   = "anlegen";
				if (formParameter.compareTo("ap")==0) {
					nr        = Long.parseLong(loeschID);
					sqlString = "DELETE FROM PLRV11.BDE_APDAT WHERE AP_PLATZNR="+nr;
					params ="&kst="+request.getParameter("kst");
				}
				if (formParameter.compareTo("ma")==0) {
					nr        = Long.parseLong(loeschID);
					sqlString = "DELETE FROM PLRV11.BDE_MADAT WHERE MA_PNR="+nr;
					stmt.executeUpdate(sqlString);
					sqlString = "DELETE FROM PLRV11.BDE_MA_PROG WHERE MAP_PNR="+nr;
					params="&klass="+request.getParameter("klass")+"&kst="+request.getParameter("kst");
					// Was passiert mit Auftraegen, die solche MAs noch haben?
					// Erst umplanen, dann loeschen!
				}
				if (formParameter.compareTo("fsParam")==0) {
					nr        = Long.parseLong(loeschID);
					sqlString = "DELETE FROM PLRV11.BDE_FS_PARAM WHERE FSP_FS="+nr +" AND FSP_MAGRU=" +request.getParameter("fspMaGru");
				}
				if (formParameter.compareTo("einarb")==0) {
					backFormular = "ma";
					backModul    = "anlegen";
					StringTokenizer aufbrecher = new StringTokenizer(loeschID,new String(","));
					try {
						maPNr   = Integer.parseInt(aufbrecher.nextToken());
					}
					catch (NumberFormatException nfe) {
						nfe.printStackTrace();
					}
					params       = "&maPNr=" + maPNr+"&klass="+request.getParameter("klass") + "&kst="+request.getParameter("kst");
					sqlString = "DELETE FROM PLRV11.BDE_MA_PROG WHERE MAP_PNR=" +maPNr + " AND MAP_FS=" + aufbrecher.nextToken() + " AND MAP_PROG="+aufbrecher.nextToken();
				}
				if (formParameter.compareTo("puffer")==0) {
					nr = Long.parseLong(loeschID);
					sqlString = "DELETE FROM PLRV11.BDE_PFDAT WHERE PF_PFNR="+nr;
				}
				if (formParameter.compareTo("pufferPl")==0) {
					StringTokenizer aufbrecher = new StringTokenizer(loeschID,new String(","));
					String puffer = new String();
					String platz  = new String();
					platz  = aufbrecher.nextToken();
					puffer = aufbrecher.nextToken();
					sqlString = "DELETE FROM PLRV11.BDE_PUFPL WHERE PP_PFNR="+puffer+" AND PP_PF_PLATZ='"+platz+"'";
					backFormular="pufferPl&pfNr="+puffer;
				}
				stmt.executeUpdate(sqlString);
				stmt.close();
				out.println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"1; URL=" + appRoot + "bde?modul=" + backModul +"&formular="+ backFormular + params +"\">" +
					javaScripts +
					"</HEAD><BODY bgcolor=\"#DDDDDD\">"+
					"<CENTER>Datensatz gel&ouml;scht "+meldung +"</CENTER>");
			}
			// ****************************************************** A E N D E R N  ******************************************************************
			if ((modulParameter.compareTo("aendern")==0) && (userRecht>=30)){
				String aendID = new String();
				formParameter = request.getParameter("formular");
				backFormular  = formParameter;
				backModul     = request.getParameter("modul");
				aendID = (request.getParameter("loeschID"));
				try {
					if (formParameter.compareTo("ap1")==0) {
						backModul="anlegen";
						backFormular="ap";
						sqlString = "UPDATE plrv11.bde_apdat SET"+
						"   ap_kst=" + Integer.parseInt(request.getParameter("apKst")) +
						",  ap_fs ='"+ request.getParameter("apFs")  +
						"', ap_bez='"+ request.getParameter("apBez") +
						"', ap_kz ='"+ request.getParameter("apKz") +
						"' WHERE ap_platznr='" + request.getParameter("apPlatzNr") + "'";
						params="&kst="+request.getParameter("kst");
					}
					if (formParameter.compareTo("ap2")==0) {
						backModul="akt_inakt";
						backFormular="ap";
						sqlString = "UPDATE plrv11.bde_apdat SET ap_kz='" +
						request.getParameter("apKz") + "' WHERE ap_platznr='" +
						request.getParameter("apPlatzNr") + "'";
					}
					if (formParameter.compareTo("ma")==0) {
						String abt= new String("0");
						// Datensatzbeschreibung siehe BDE-Systembeschreibung 5.4.2 Rueckmeldungen von TDD an BDE
						// Im Falle einer Aenderung: Mitarbeiter Abmelden bzw. Abmelden und neu Anmelden
						// Nur für Mitarbeiter der Polsterei soll diese Datei generiert werden
						int anzDS=0; //Anzahl Datensaetze
						sqlString = "SELECT att_bez FROM plrv11.plr_attrAttr WHERE att_tabname='kstZuAbt' AND att_attr="+request.getParameter("maKst");
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						while (rs.next()) {
						abt = rs.getString("att_bez");
						}
						if (abt.compareTo("P")==0) {
						textDatei anmeldDatei = new textDatei();
						anmeldDatei.setNameTimestamp(bcd.getVomTddDir(),"P",".bde");
						if (request.getParameter("maAnmeld").compareTo("J")==0) {
							anmeldDatei.write("1102E001" +
									zeit.getTimestamp("yyyyMMddHHmmss")+
									"04890000000MD07"+
									anmeldDatei.format('0',4,request.getParameter("maPNr")) +
									anmeldDatei.format('0',6,request.getParameter("maTel")) +
									"N\n");
							anzDS++;
						}
						anmeldDatei.write("1102E001" +
								zeit.getTimestamp("yyyyMMddHHmmss")+
								"04890000000MD07"+
								anmeldDatei.format('0',4,request.getParameter("maPNr")) +
								anmeldDatei.format('0',6,request.getParameter("maTel")) +
								request.getParameter("maAnmeld")+"\n");
						anzDS++;
						// Endesatz
						anmeldDatei.write("1102E999"+zeit.getTimestamp("yyyyMMddHHmmss")+"042999999999907"+
								rbTextFormat.format('0',5,anzDS)+"\n");
						}
						backModul="anlegen";
						params="&maPNr="+request.getParameter("maPNr")+"&klass="+request.getParameter("klass")+"&kst="+request.getParameter("kst");
						maUser=request.getParameter("maUser");
						if (maUser.compareTo("")==0) {
						maUser=" ";
						}
						sqlString = "UPDATE plrv11.bde_madat SET"+
						"  ma_nname='" + request.getParameter("maNName")       +
						"', ma_vname='"+ request.getParameter("maVName")       +
						"', ma_kst="   + Integer.parseInt(request.getParameter("maKst"))         +
						", ma_tel="    + Integer.parseInt(request.getParameter("maTel"))         +
						", ma_ap='"    + request.getParameter("maAP")          +
						"', ma_max_ab="+ Integer.parseInt(request.getParameter("maMaxAnzAuftr")) +
						", ma_anmeld='"+ request.getParameter("maAnmeld")      +
						"', ma_recht=" + Integer.parseInt(request.getParameter("maRecht"))       +
						", ma_pin="    + Integer.parseInt(request.getParameter("maPIN"))         +
						", ma_prod='"  + request.getParameter("maProd")        +
						"', ma_user='" + maUser      +
						"', ma_Werk="  + request.getParameter("maWerk") +
						",ma_team="    + request.getParameter("team") +
						",ma_team_kz='"+ request.getParameter("teamKz") +
						"',ma_drucker='"+ request.getParameter("maDrucker") +
						"' WHERE ma_pnr=" +request.getParameter("maPNr");
					}
					if (formParameter.compareTo("fsParam")==0) {
						backModul="anlegen";
						params="&fs="+request.getParameter("fspFs")+"&fspMaGru="+request.getParameter("fspMaGru");
						sqlString = "UPDATE plrv11.bde_fs_param SET"+
						"   fsp_werk_po=" + Integer.parseInt(request.getParameter("fspWerkPo"))+
						",  fsp_magru  =" + Integer.parseInt(request.getParameter("fspMaGru")) +
						",  fsp_ordnr  =" + Integer.parseInt(request.getParameter("fspOrdNr")) +
						",  fsp_bez    ='"+ request.getParameter("fspBez")                     +
						"', fsp_pf_ini = "+ Integer.parseInt(request.getParameter("fspPfIni")) +
						"  WHERE fsp_fs='" + request.getParameter("fspFs")+"' AND fsp_magru="+request.getParameter("fspMaGru");
					}
					if (formParameter.compareTo("puffer")==0) {
						backModul="anlegen";
						params="&pfNr=" + request.getParameter("pfNr");
						sqlString = "UPDATE plrv11.bde_pfdat SET"+
						"  pf_bez='"       +request.getParameter("pfBez") +
						"',pf_kz ='"       +request.getParameter("pfKz")  +
						"',pf_status="     +Integer.parseInt(request.getParameter("pfStatus"))  +
						", pf_fs    ='"     +request.getParameter("pfFs")+
						"'  WHERE pf_pfNr=" + request.getParameter("pfNr");
					}
					if (formParameter.compareTo("pufferPl")==0) {
						backModul="anlegen";
						params="&pfNr=" + request.getParameter("pfNr");
						sqlString = "UPDATE plrv11.bde_pufpl SET "+
						" pp_kz ='"+ request.getParameter("ppKz")+"'"+
						" WHERE pp_pf_platz='"+request.getParameter("ppPfPlatz")+"' AND"+
						" pp_pfnr ="+request.getParameter("pfNr");
					}
					try {
						stmt.executeUpdate(sqlString);
						sqlError="";
						out.println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"0; URL=" +
							appRoot + "bde?modul=" +
							backModul +"&klass=" +
							request.getParameter("klass")+"&formular="+
							backFormular+params+"\">" +
							"</HEAD><BODY bgcolor=\"#DDDDD\">"+
							"<CENTER><B>Datensatz ge&auml;ndert " + meldung +"</B></CENTER>");
					}
					catch(SQLException se) {
						se.printStackTrace();
						out.println("</HEAD><BODY><FONT COLOR=red>Ihre Einaben konnten nicht verarbeitet werden</FONT><BR>"+ se.getMessage());
						out.println("<BR>"+sqlString);
					}
					finally {
						stmt.close();
					}
				}
				catch( NumberFormatException nfe){
					nfe.printStackTrace();
					out.println("<META HTTP-EQUIV=\"refresh\" CONTENT=\"0; URL=" + appRoot + "bde?modul="+backModul+"&formular="+backFormular+params+"\">" +
							"</HEAD><BODY bgcolor=\"#DDDDD\">"+
							"<CENTER><BLINK>Fehlerhafte Eingabdaten!</BLINK></BR>" + meldung +"</CENTER>");
				}
			}
			// ***************************************************** S T A T I S T I K *************************************************************
			if(modulParameter.compareTo("stat")==0) {
				String sqlString2 = new String();
				Statement stmt2 = con.createStatement();
				ResultSet rs2;
				String sqlString3 = new String();
				Statement stmt3 = con.createStatement();
				ResultSet rs3;
				int aktFabt=0;
				out.println(javaScripts+"</HEAD><BODY bgcolor=\"#CCCCCC\" onload=\"setFrame('list','/blank.html')\">");
				out.println("<CENTER><H1>Statistikdaten BDE</H1>");
				out.println("<H2>Mitarbeiter</H2>");
				out.println("<TABLE BORDER=1>");
				out.println("<TR BGCOLOR=#dddddd><TD>Fertigungsstufe</TD><TD>Angemeldete Mitarbeiter</TD></TR>");
				sqlString ="SELECT ap_fs,count(*) "+
					"FROM plrv11.bde_madat,plrv11.bde_apdat "+
					"WHERE ma_ap=ap_platznr "+
					"AND    ma_anmeld='J' "+
					"GROUP BY ap_fs";
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					out.println("<TR><TD>"+rs.getString("ap_fs") +"</TD><TD>"+rs.getString(2)+"</TD></TR>\n");
				}
				out.println("</TABLE>");
				out.println("<H2>Geplante SE Polsterei (245)</H2>");
				// Über Fabrikkalender ermitteln: heutige Fabtag und die nächsten 4
				sqlString = "SELECT fab_fab_tag FROM plrv11.plr_fabkal_dat WHERE fab_datum='"+zeit.getTimestamp("ddMMyy")+"' AND fab_ausfall='J'";
				rs = stmt.executeQuery(sqlString);
				int i=0;
				while (rs.next()) {
					aktFabt=rs.getInt("fab_fab_tag");
					i++;
				}
				if (i==0) {
					out.println("Aktueller Tag ist kein gueltiger Fabrik-Tag");
				} else {
					sqlString= "SELECT fab_fab_tag FROM plrv11.plr_fabkal_dat WHERE fab_ausfall='J' AND fab_fab_tag>="+aktFabt+" AND fab_fab_tag<(20+"+aktFabt+")";
					rs = stmt.executeQuery(sqlString);
					i=0;
					out.println("<TABLE><TR BGCOLOR=#DDDDFF><TD>Fabriktag</TD><TD>Anzahl SE</TD></TR>");
					while ((rs.next() && i<5)) {
						sqlString2 = "SELECT sum(ako_se) FROM plrv11.plr_auftr_kopf,plrv11.plr_auftr_status WHERE ako_fabt_vs="+rs.getInt("fab_fab_tag")+
						" AND as_aend_ix=ako_aend_ix AND as_abnr=ako_abnr AND as_status<>99";
						rs2 = stmt2.executeQuery(sqlString2);
						while (rs2.next()) {
						out.println("<TR BGCOLOR=#DDDDDD><TD>"+rs.getInt("fab_fab_tag")+"</TD><TD>"+rs2.getInt(1)+"</TD></TR>\n");
						}
						i++;
					}
					out.println("</TABLE>");
				}
				out.println("<H2>Gefertigte SE für aktuellen Tag</H2>");
				sqlString = "SELECT att_bez,SUM(ako_se) FROM plrv11.plr_auftr_kopf,plrv11.bde_ab_mz,plrv11.plr_attrAttr,plrv11.plr_auftr_status WHERE "+
				"abm_fs_status=40 AND abm_fs=att_bez AND att_tabname='auswAnzSE' AND as_abnr=ako_abnr AND as_aend_ix=ako_aend_ix AND " +
				"as_abnr=abm_abnr AND as_aend_ix=abm_aend_ix AND as_status<>99 AND "+
				"substr(abm_sendtime,1,6) = '"+ zeit.getTimestamp("yyMMdd")+"' AND ako_abnr=abm_abnr GROUP BY att_bez";
				rs = stmt.executeQuery(sqlString);
				out.println("<TABLE><TR BGCOLOR=#ddddff><TD>Fertigungsstufe</TD><TD>SE</TD></TR>");
				while (rs.next()) {
					out.println("<TR BGCOLOR=#dddddd><TD>"+rs.getString(1)+"</TD><TD>"+rs.getString(2)+"</TD></TR>\n");
				}
				out.println("</TABLE><H2>Gefertigte Kundendienst-SE f&uuml;r den aktuellen Tag</H2><TABLE>");
				sqlString = "SELECT att_bez,SUM(ako_se) FROM plrv11.plr_auftr_kopf,plrv11.bde_ab_mz,plrv11.plr_attrAttr,plrv11.plr_auftr_status WHERE "+
				"abm_fs_status=40 AND "+
				"abm_fs=att_bez AND "+
				"att_tabname='auswAnzSE' AND "+
				"as_abnr=ako_abnr AND "+
				"as_aend_ix=ako_aend_ix AND "+
				"as_abnr=abm_abnr AND "+
				"as_aend_ix=abm_aend_ix AND "+
				"as_status<>99 AND "+
				"(ako_aart2=15 or ako_aart2=16) AND "+
				"substr(abm_sendtime,1,6) = '"+  zeit.getTimestamp("yyMMdd") +"' AND "+
				"ako_abnr=abm_abnr GROUP BY att_bez";
				rs = stmt.executeQuery(sqlString);
				out.println("<TABLE><TR BGCOLOR=#ddddff><TD>Fertigungsstufe</TD><TD>SE</TD></TR>");
				while (rs.next()) {
					out.println("<TR BGCOLOR=#dddddd><TD>"+rs.getString(1)+"</TD><TD>"+rs.getString(2)+"</TD></TR>\n");
				}
				out.println("</TABLE><H2>R&uuml;ckstand</H2>");
				out.println("<CENTER><TABLE bgcolor=\"#dddddd\">");
				sqlString = "SELECT DISTINCT fsp_fs FROM plrv11.bde_fs_param ORDER BY fsp_fs";
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					// Fabtag ermitteln
					sqlString2 = "SELECT fab_fab_tag FROM plrv11.plr_fabkal_dat WHERE fab_datum='"+zeit.getTimestamp("ddMMyy")+"'";
					stmt2.execute(sqlString2);
					rs2 = stmt2.getResultSet();
					rs2.next();
					sqlString3  = "SELECT COUNT(*) FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status WHERE ";
					sqlString3 += "as_abnr=abf_abnr AND as_aend_ix=abf_aend_ix AND abf_fs='"+rs.getString("fsp_fs")+
						"' AND as_status<>99 AND abf_fs_status=1 AND abf_fabt_pps="+rs2.getString("fab_fab_tag");
					stmt3.execute(sqlString3);
					rs3 = stmt3.getResultSet();
					rs3.next();
					out.println("<TR BGCOLOR=\"#ddddff\"><TD>Fertigungsstufe</TD><TD>"+rs.getString("fsp_fs")+"</TD><TD>&nbsp;</TD></TR>");
					out.println("<TR><TD>Nicht zugewiesen  </TD><TD>heute ("+rs2.getString("fab_fab_tag")+")</TD><TD ALIGN=right>"+rs3.getString(1) +"</TD></TR>");
					sqlString3  = "SELECT COUNT(*) FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status WHERE ";
					sqlString3 += "as_abnr=abf_abnr AND as_aend_ix=abf_aend_ix AND abf_fs='"+rs.getString("fsp_fs") +
						"' AND as_status<>99 AND abf_fs_status=1 AND abf_fabt_pps<"+rs2.getString("fab_fab_tag");
					stmt3.execute(sqlString3);
					rs3 = stmt3.getResultSet();
					rs3.next();
					out.println("<TR><TD>&nbsp;            </TD><TD>vorher (<"+rs2.getString("fab_fab_tag")+")</TD><TD ALIGN=right>"+rs3.getString(1) +"</TD></TR>");
					sqlString3  = "SELECT COUNT(*) FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status WHERE ";
					sqlString3 += "as_abnr=abf_abnr AND as_aend_ix=abf_aend_ix AND abf_fs='"+rs.getString("fsp_fs")+
						"'  AND as_status<>99 AND abf_fs_status>=5 AND abf_fs_status<40 AND abf_fabt_pps="+rs2.getString("fab_fab_tag");
					stmt3.execute(sqlString3);
					rs3 = stmt3.getResultSet();
					rs3.next();
					out.println("<TR><TD>Bereits zugewiesen</TD><TD>heute (" +rs2.getString("fab_fab_tag")+")</TD><TD ALIGN=right>"+rs3.getString(1) +"</TD></TR>");
					sqlString3  = "SELECT COUNT(*) FROM plrv11.bde_ab_fs_ma,plrv11.plr_auftr_status WHERE ";
					sqlString3 += "as_abnr=abf_abnr AND as_aend_ix=abf_aend_ix  AND abf_fs='"+rs.getString("fsp_fs")+
						"' AND as_status<>99 AND abf_fs_status>=5 and abf_fs_status<=30 AND abf_fabt_pps<"+rs2.getString("fab_fab_tag");
					stmt3.execute(sqlString3);
					rs3 = stmt3.getResultSet();
					rs3.next();
					out.println("<TR><TD>&nbsp;</TD><TD>vorher (<"+rs2.getString("fab_fab_tag")+")</TD><TD ALIGN=right>"+rs3.getString(1)+"</TD></TR>");
				}
				out.println("</TABLE>");
				stmt2.close();
				stmt3.close();
			}
			// ********************************************* A U F T R A G S B E A R B E I T U N G *************************************************
			if (((modulParameter.compareTo("auftrBearb"))==0) && (userRecht>=30)) {
				if (userRecht>=30) {
					if (formParameter.compareTo("stextEin")==0) {
						int abNr=0;
						int aendIx=0;
						String akText = new String();
						int rsCounter=0;

						out.println(javaScripts+"</HEAD><BODY BGCOLOR=\"#CCCCCC\" onload=\"setFrame('list','/blank.html')\">\n");
						out.println("<H2>Sondertexte</H2>\n");
						try {
							abNr=Integer.parseInt(request.getParameter("abNr"));
							// Zur Kontrolle, ob Auftrag überhaupt im Auftragsbestan ist
							sqlString = "SELECT ako_abnr,as_aend_ix FROM plrv11.plr_auftr_kopf,plrv11.plr_auftr_status " +
								"WHERE ako_abnr=as_abnr AND "+
								"ako_aend_ix=as_aend_ix AND "+
								"as_status<>99 AND ako_abnr="+
								request.getParameter("abNr");
							stmt.executeQuery(sqlString);
							rs = stmt.getResultSet();
							while (rs.next()) {
								abNr   = rs.getInt("ako_abnr");
								aendIx = rs.getInt("as_aend_ix");
								rsCounter++;
							}
							if (rsCounter>0) {
								sqlString = "SELECT akt_text FROM plrv11.plr_ak_texte,plrv11.plr_auftr_status " +
								"WHERE akt_abnr=as_abnr AND "+
								"akt_aend_ix=as_aend_ix AND "+
								"akt_text_art='X' AND "+
								"akt_text_pos=0 AND "+
								"as_status<>99 AND akt_abnr="+
								request.getParameter("abNr");
								stmt.executeQuery(sqlString);
								rs = stmt.getResultSet();
								while (rs.next()) {
								akText = rs.getString("akt_text");
								rsCounter++;
								}
								out.println("<FORM ACTION=\""+appRoot+"bde\" method=\"GET\">\n");
								out.println("<INPUT TYPE=\"hidden\" NAME=\"modul\"     VALUE=\"auftrBearb\">\n");
								out.println("<INPUT TYPE=\"hidden\" NAME=\"formular\"  VALUE=\"stextSpeich\">\n");
								out.println("<INPUT TYPE=\"hidden\" NAME=\"userSicht\" VALUE=\""+request.getParameter("userSicht")+"\">\n");
								out.println("<INPUT TYPE=\"hidden\" NAME=\"abNr\"      VALUE=\""+abNr  +"\">\n");
								out.println("<INPUT TYPE=\"hidden\" NAME=\"aendIx\"    VALUE=\""+aendIx+"\">\n");
								out.println("<TABLE BORDER=1>");
								out.println("<TR><TD>Sondertext für Aufrag "+abNr+"</TD><TD><INPUT TYPE=\"TEXT\" name=\"akText\" MAXLENGTH=50 SIZE=50 VALUE=\""+akText+"\"></TD></TR>\n");
								out.println("<TR><TD>&nbsp;</TD><TD ALIGN=right><INPUT TYPE=\"SUBMIT\" VALUE=\"OK\"></TD></TR>\n");
								out.println("</TABLE></FORM>");
							} else {
								out.println("<FONT COLOR=red>Auftrag "+abNr+" ist nicht im Auftragsbestand.</FONT>");
							}
						}
						catch (NumberFormatException nfe) {
							out.println("<FONT COLOR=red>Fehlerhafte Eingabe:</FONT> Auftragsnummer muss Zahl sein!<BR>");
						}
					}
					if (formParameter.compareTo("stextSpeich")==0) {
						try {
							sqlString = "DELETE FROM plrv11.plr_ak_texte WHERE akt_abnr="+request.getParameter("abNr")+
								" AND akt_aend_ix="+request.getParameter("aendIx") +
								" AND akt_text_art='X' AND akt_text_pos=0";
							stmt.executeQuery(sqlString);
							sqlString = "INSERT INTO plrv11.plr_ak_texte (akt_abnr,akt_aend_ix,akt_text_art,akt_text_pos,akt_text) VALUES ("+
								request.getParameter("abNr")   +","+
								request.getParameter("aendIx") +",'X',0,'"+
								request.getParameter("akText")+"')";
							stmt.executeQuery(sqlString);
							out.println(javaScripts+"</HEAD><BODY BGCOLOR=\"#CCCCCC\" onload=\"setFrame('list','/blank.html');setFrame('main','"+
									appRoot+"bde?modul=planung&formular=vorauswahl&userSicht="+request.getParameter("userSicht")+"');\">");
							out.println("<H2>Sonderfertigungstext gespeichert</H2>");
						}
						catch (SQLException se) {
							out.println(javaScripts+"</HEAD><BODY BGCOLOR=\"#CCCCCC\" onload=\"setFrame('list','/blank.html')\">");
							out.println("<FONT COLOR=red>Eingabe wurde nicht gespeichert:</FONT><BR>"+se.getMessage());
						}
						finally {
							stmt.close();
						}
					}
				}
			}
			// ************************************************ P L A N U N G  **********************************************
			if ((modulParameter.compareTo("planung")==0) && (userRecht>=15) ) {
				if (userRecht>=20) {
				if (formParameter.compareTo("paketAuswahl")==0) {
					out.println(javaScripts+"</HEAD><BODY bgcolor=\"#CCCCCC\" onload=\"setFrame('list','/blank.html')\">"+
						fertStuf.getPaketAuswahl());
				}
				if (formParameter.compareTo("vorauswahl")==0) {
					out.println(javaScripts+"</HEAD><BODY bgcolor=\"#CCCCCC\" onLoad=\"setFrame('list','/blank.html')\">"+
						fertStuf.getVorselektion(request.getParameter("userSicht")));
				}
				if (formParameter.compareTo("auswahl")==0) {
					out.println(javaScripts +"</HEAD><BODY bgcolor=\"#CCCCCC\">"+
						fertStuf.getSelektion(request,userRecht));
				}
				if (formParameter.compareTo("maZuweisung")==0) {
					out.println(javaScripts+"</HEAD><BODY bgcolor=\"#DDDDD\" onLoad=\"javascript:parent['main'].location.reload();\">" +
						fertStuf.maZuweis(request));
				}
				if (formParameter.compareTo("maPaketZuweisung")==0) {
					out.println(javaScripts+"</HEAD><BODY bgcolor=\"#DDDDD\" onLoad=\"javascript:parent['main'].location.reload();\">" +
						fertStuf.maPaketZuweis(request));
				}
				if (formParameter.compareTo("vorSplit")==0) {
					out.println(fertStuf.vorSplit(request));
				}
				if (formParameter.compareTo("split")==0) {
					out.println(fertStuf.split(request));
				}
				if (formParameter.compareTo("bezug")==0) {
					out.println(fertStuf.bezugMelden(request));
				}
				if (formParameter.compareTo("hohePrio")==0) {
						int aendIx = 0;
						int zaehler= 0;
						sqlString =  "SELECT abf_abnr,abf_aend_ix ";
						sqlString += "FROM plrv11.bde_ab_fs_ma ";
						sqlString += "INNER JOIN plrv11.plr_auftr_status ON abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix ";
						sqlString += "WHERE abf_abnr="+Integer.parseInt(request.getParameter("abNr"));
						sqlString += " AND as_status<>99";
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						while (rs.next()) {
							aendIx = rs.getInt("abf_aend_ix");
							zaehler++;
						}
						rs.close();
						out.println("</HEAD><BODY bgcolor=\"#dddddd\">");
						if (zaehler>1) {
							sqlString  = "UPDATE plrv11.bde_ab_fs_ma SET abf_vorzug=1 ";
							sqlString += " WHERE abf_aend_ix=" +aendIx;
							sqlString += " AND abf_abnr="+Integer.parseInt(request.getParameter("abNr"));
							stmt.executeQuery(sqlString);
							out.println("Auftrag <B>"+request.getParameter("abNr")+"</B> wurde hoch priorisiert<BR>");
						} else {
							out.println("Auftrag ist nicht vorhanden.");
						}

				}
				if (formParameter.compareTo("eilab")==0) {
					int aendIx = 0;
					int zaehler= 0;
					sqlString = "SELECT as_aend_ix FROM plrv11.plr_auftr_status WHERE as_status<>99 AND as_abnr="+request.getParameter("abNr");
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						aendIx = rs.getInt("as_aend_ix");
						zaehler++;
					}
					out.println("</HEAD><BODY bgcolor=\"#dddddd\">");
					if (zaehler==1) {
						sqlString  = "UPDATE plrv11.plr_auftr_kopf SET ako_kz_eilab='"+request.getParameter("eilKz");
						sqlString += "' WHERE ako_abnr="+request.getParameter("abNr")+" AND ako_aend_ix="+aendIx;
						stmt.executeQuery(sqlString);
						if (request.getParameter("eilKz").compareTo("J")==0) {
							out.println("Auftrag <B>"+request.getParameter("abNr")+"</B> wurde als Eilauftrag gekennzeichnet.<BR>");
						} else {
							out.println("Eil-Kennzeichen für Auftrag <B>"+request.getParameter("abNr")+"</B> wurde storniert.<BR>");
						}
					} else {
						out.println("Auftrag ist nicht vorhanden.");
					}
				}
				if (formParameter.compareTo("ma")==0) {
					int prog=0;
					int abNr=0;
					int pNr =0;
					try {
					prog = Integer.parseInt(request.getParameter("prog"));
					abNr = Integer.parseInt(request.getParameter("abNr"));
					pNr  = Integer.parseInt(request.getParameter("pNr"));
					}
					catch (NumberFormatException nfe) {
					log("Modul: Planung\nFormular: ma\nWider erwarten koennen hier tatsaechlich Fehler auftreten! Umwandeln von String");
					}
					out.println("</HEAD><BODY bgcolor=\"#DDDDDD\">"+fertStuf.getEinarbeit(request.getParameter("fs"),prog,abNr,pNr));
				}
				if (formParameter.compareTo("maPaket")==0) {
					int abNrFeld[] = new int [5];
					int prog       = 0;
					int pNr        = 0;
					int lauf       = 0;
					try {
						StringTokenizer aufbrecher = new StringTokenizer(request.getParameter("abNr"),new String(","));
						while (aufbrecher.hasMoreTokens()) {
							abNrFeld[lauf] = Integer.parseInt(aufbrecher.nextToken());
							lauf++;
						}
						prog = Integer.parseInt(request.getParameter("prog"));
						pNr  = Integer.parseInt(request.getParameter("pNr"));
					}
					catch (NumberFormatException nfe) {
						System.out.println("Modul: Planung\nFormular: ma\nWider erwarten koennen hier tatsaechlich fehler auftreten! Umwandeln von String");
						nfe.printStackTrace();
					}
					out.println("</HEAD><BODY bgcolor=\"#DDDDDD\">"+fertStuf.getPaketEinarbeit(request.getParameter("fs"),prog,abNrFeld,pNr));
				}
			} // Ende ModulParameter Planung
		}
			// Ab hier wird fuer alle Formulare wieder das gleiche ausgegeben.
			out.println("</BODY></HTML>");
			out.close();
			stmt.close();
		}
		catch(Exception ex) {
			PrintWriter out;
			out = response.getWriter();
			response.setContentType("text/html");
			log("Allgemeiner Fehler:\n");
			log("Erzeugt durch Benutzer:"+request.getRemoteUser());
			log("\nin Datei   :"+ ex.getStackTrace()[0].getFileName());
			log("\nin Methode :"+ ex.getStackTrace()[0].getMethodName());
			log("\nin Zeile   :"+ ex.getStackTrace()[0].getLineNumber());
			log("\nAusgabe    :"+ ex.getStackTrace()[1].toString());
			out.println("<BR>Erzeugt durch Benutzer:"+request.getRemoteUser());
			out.println("<BR>in Datei   :"+ ex.getStackTrace()[0].getFileName());
			out.println("<BR>in Methode :"+ ex.getStackTrace()[0].getMethodName());
			out.println("<BR>in Zeile   :"+ ex.getStackTrace()[0].getLineNumber());
			out.println("<BR>Ausgabe    :"+ ex.getStackTrace()[1].toString());
			out.println("<BR>Meldung    :"+ ex.getMessage());
			ex.printStackTrace();
			out.close();
		}
		finally {
			try {
				con.close();
				oraBde.close();
			}
			catch(SQLException sqlex) {
				sqlex.printStackTrace();
				log("Fehler beim Abbau der Datenbankverbindung");
			}
		}
	}
}
