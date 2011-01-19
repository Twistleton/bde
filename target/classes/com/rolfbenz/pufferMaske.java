package com.rolfbenz;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*

private String pmn           = new String("");
private int    abNr          = 0;
private int    abNr2         = 0;
private int    abNr3         = 0;
private int    platzNr       = 0;
private int    pzone         = -1;
private int    pf_status     = 0;
private int    abf_pf_status = 0;
private int    as_aend_ix    = 0;

*/


public class pufferMaske extends HttpServlet {
private String appRoot       = "";
private bdeZeit zeit         = new bdeZeit();
private String aktColor      = new String();
private String dbname        = new String("");
private String dbip          = new String("");
private int    dbport        = 0;
private String dbuser        = new String("");
private String dbpass        = new String("");
private String errorDir      = new String("");


textDatei errorDatei   = new textDatei("/tmp","puffer.log");

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

private bdeConfigDatei bcd = new bdeConfigDatei("/etc/bdeServlet.conf");

public void reInit() {
	aktColor   = "ddddff";
	// Auswerten der Config-Datei
	log(bcd.verarbeiten());
	log("Servlet wird mit Config-Daten neu initialisiert.");
}

public void doPost (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	doGet(request,response);
}

public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	Connection con;
	bdeDb oraBde;
	PrintWriter out;
	response.setContentType("text/html");
	out = response.getWriter();
	out.println("<html><head><title>Pufferverwaltung</title></head>");
	out.println("<body onload=\"document.form.abnr.focus();\">");
	// out.println(">>>>>>"+request.getRequestURL()+"<<<<<<");
	String      user             = new String();
	String      userAp           = new String();
	int         userRecht        = 1;
	int         userWerk         = 0;
	int         userKst          = 0;
	char        userSicht        = '0';

	String pmn           = new String("");
	int    as_status     = 0;
	int    abNr          = 0;
	int    abNr2         = 0;
	int    abNr3         = 0;
	int    platzNr       = 0;
	int    pzone         = -1;

	try { //Globales Try
		errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ":" + request.getRemoteAddr()+"\n");
		pmn = request.getParameter("pmn");
		String    aktDatum       = new String();
		int         fabTag         = 0;
		try {
			abNr = Integer.parseInt(request.getParameter("abnr"));
			platzNr = Integer.parseInt(request.getParameter("platznr"));
		} catch (NumberFormatException nfe) {
		}

		oraBde = new bdeDb(bcd.getDbIp(),bcd.getDbName(),bcd.getDbUser(),bcd.getDbPass(),bcd.getDbPort());
		con = (Connection) oraBde.getConnection();
		try {
//			con.setClientIdentifier("pufferMaske");
			// Zur Generierung eines Benutzer-Abhaengigen Menues werden Benutzer-Daten aus Datenbank gelesen
			user = request.getRemoteUser();
            System.out.println("===>" + user);
//			user = "u516";
			String sqlString = "SELECT ma_user,ma_recht,ma_werk,ma_kst,ma_ap,att_Bez "+
				"FROM plrv11.bde_madat,plrv11.plr_attrAttr WHERE att_tabname='kstZuAbt' and att_attr=ma_kst and ma_user='"+user+"'";
			Statement stmt = con.createStatement();
			stmt.executeQuery(sqlString);
			ResultSet rs = stmt.getResultSet();
			rs.next();
			userRecht = rs.getInt("ma_recht");
			userWerk  = rs.getInt("ma_werk");
			userKst   = rs.getInt("ma_kst");
			userSicht = rs.getString("att_bez").charAt(0);
			userAp    = rs.getString("ma_ap");
			rs.close();
			stmt.close();
		}
		catch(SQLException sqlex) {
			log("Fehler beim Generieren des benutzerabhängigen Menüs"+sqlex.getMessage());
		}
		//    out.println("Userrecht: "+userRecht+"<br>Userwerk: "+userWerk+"<br>Userkst: "+userKst+"<br>Usersicht: "+userSicht+"<br>UserAp: "+userAp+"<br>");
		try {
			Statement   stmt           = con.createStatement();
			String      sqlString      = new String();
			ResultSet   rs;
			int         rsZaehl        = 0;
			sqlString = "SELECT fab_fab_tag FROM plrv11.plr_fabkal_dat WHERE fab_datum='"+bdeZeit.getTimestamp("ddMMyy")+"'";
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()) {
				fabTag = rs.getInt("fab_fab_tag");
			}
			rs.close(); 
			stmt.close();
		} catch (SQLException sqlex) {
			log("Fehler beim Lesen: "+sqlex);
			out.println("<br>"+sqlex+"<br>");
		}
		// ***** Puffer bestimmen
		int pfNr = Integer.parseInt(userAp.substring(0,8));
		//    out.println("Puffernummer: "+pfNr+"<br>UserAp: "+userAp);
		try {
			pfNr = Integer.parseInt(request.getParameter("pfNr"));
		} catch (NumberFormatException nfe) {
		}
		if (pfNr == 10999100) pfNr = 10144100;
		if (pfNr == 10245200){
			try {
				pzone = Integer.parseInt(request.getParameter("pzone"));
				abNr2 = Integer.parseInt(request.getParameter("abnr2"));
				abNr3 = Integer.parseInt(request.getParameter("abnr3"));
				//out.println("<br>ZONE: "+pzone+"<br>");
				if (request.getParameter("abnr2") != null) {
					Statement   stmt          = con.createStatement();
					String      sqlString      = new String();
					ResultSet   rs;
					sqlString = "SELECT as_status FROM plrv11.plr_auftr_status where as_abnr="+abNr2+" and as_status<>99";
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						as_status = rs.getInt("as_status");
					}
					if (as_status != 90)
						abNr2 = 0;
				}
			} catch (NumberFormatException nfe) {
			} catch (SQLException sqlex) {
				// fehlerhafte Eingaben in AB2 verhindern ... 15.9.09
				log("Fehler beim Lesen: "+sqlex);
				// out.println("<br>"+sqlex+"<br>");
				abNr2 = 0;
			}
		}
		if (pfNr == 10144100 || pfNr == 10144200 || pfNr == 10144400 || pfNr == 10243200 || pfNr == 10245400){
			try {
				pzone = Integer.parseInt(request.getParameter("pzone"));
			} catch (NumberFormatException nfe) {
			}
		}
		String choosePf = new String();
		if ((userAp.compareTo("10999100999") == 0) || (user.compareTo("stul")==0)){
			choosePf = "<SELECT NAME=\"pfNr\" onChange=\"document.form.submit()\">";
			try {
				Statement   stmt           = con.createStatement();
				String      sqlString      = new String();
				ResultSet   rs;
				int         rsZaehl        = 0;
				int pf_pfnr = 0;
				String pf_bez = "";	
				sqlString = "SELECT pf_pfnr,pf_bez FROM plrv11.bde_pfdat ORDER BY pf_pfnr";
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					pf_pfnr = rs.getInt("pf_pfnr");
					pf_bez = rs.getString("pf_bez");
					if (user.compareTo("pvw") == 0){
						if ((pf_bez.compareTo("PMN") != 0) && (pf_bez.compareTo("PVP") != 0)  &&
											(pf_bez.compareTo("PPO") != 0) ) {
							if (pfNr == pf_pfnr){
								choosePf += "\n<OPTION VALUE=\""+pf_pfnr+"\" SELECTED>"+pf_bez;
							} else {
								choosePf += "\n<OPTION VALUE=\""+pf_pfnr+"\">"+pf_bez;
							}
						}
					} else if (user.compareTo("stul") == 0) {
						if ((pf_bez.compareTo("PFT1") != 0)  && (pf_bez.compareTo("PKZ") != 0) &&
							(pf_bez.compareTo("PPO") != 0) && (pf_bez.compareTo("PVP") != 0) ) {
							if (pfNr == pf_pfnr) {
								choosePf += "\n<OPTION VALUE=\""+pf_pfnr+"\" SELECTED>"+pf_bez;
							} else {
								choosePf += "\n<OPTION VALUE=\""+pf_pfnr+"\">"+pf_bez;
							}
						}
					} else {
						if (pfNr == pf_pfnr){
							choosePf += "\n<OPTION VALUE=\""+pf_pfnr+"\" SELECTED>"+pf_bez;
						} else {
							choosePf += "\n<OPTION VALUE=\""+pf_pfnr+"\">"+pf_bez;
						}
					}
				}
				rs.close(); 
				stmt.close();
			} catch (SQLException sqlex) {
				log("Fehler beim Lesen: "+sqlex);
				out.println("<br>"+sqlex+"<br>");
			}
			choosePf += "</SELECT>";
		} else {
			choosePf = "&nbsp;";
		}
			
		// ***** Formular
		out.println("<p>");
		out.println("<table  width=100% align=\"center\">");
		out.println("<tr>");
		out.println("<td align=\"left\"><img src=\"/rb-logo.gif\"></td>");
		out.println("<td align=center bgcolor=\"#007FFF\"><font FACE=\"ARIAL\" SIZE=\"5\"><b>Pufferverwaltung</b></font></td>");
		out.println("<td align=\"right\">"+bdeZeit.getTimestamp("dd.MM.yyyy")+" / "+fabTag+"</td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</p><hr><p>");
		out.println("<form ACTION=\"pufferMaske\" METHOD=\"POST\" NAME=\"form\">");
		out.println("<table border=2 align=\"center\">");
		out.println("<tr VALIGN=\"middle\">");
		out.println("<td align=\"left\" valign=\"center\">"+choosePf+"</td>");
		out.println("<td align=\"left\">&nbsp;&nbsp;Auftrag&nbsp;&nbsp;</td>");
		out.print("<td><input type=\"text\" name=\"abnr\" tabindex=\"1\" size=\"6\" maxlength=\"6\" value=\"");
		try {
			if (pmn.compareTo("zuweisen")!=0){
				if (abNr!=0){
					//	Wert steht vom input im Datenfeld, keine erneute Ausgabe erforderlich = sinnvoll ??
					//	 out.println(abNr);
				}
			} 
		} catch(NullPointerException npe) {
		}
		if (pfNr == 10245100) {
		  out.println("\" onBlur=\"document.form.platznr.focus();\"></td>");
		} else {
		  out.println("\"></TD>");
		}
		out.println("<td align=\"center\">");
		//   if (pfNr != 10245200) {
		out.println("<input type=\"submit\" name=\"pmn\" value=\"Details\">");
		//          } 
		// else {
		// out.println("&nbsp;");
		// }
		out.println("</td>");
		out.println("</tr><tr>");	
		if (pfNr == 10245400 || pfNr == 10245200 || pfNr == 10243200 || pfNr == 10144400 || pfNr == 10144200 || pfNr == 10144100){
		// Fuer Puffer PFR bis zu 3 Auftraege auf einem Platz
		   if (pfNr == 10245200){
			out.println("<td>&nbsp;</td>");
			out.println("<td align=\"left\">&nbsp;&nbsp;Auftrag&nbsp;2&nbsp;</td>");
			out.print("<td><input type=\"text\" name=\"abnr2\" tabindex=\"2\" size=\"6\" maxlength=\"6\" value=\"");
			try {
				if (pmn.compareTo("zuweisen")!=0){
					if (abNr2!=0){
						// out.println(abNr2);
					}
				} 
			} catch(NullPointerException npe) {
				// Keine Log Eintraege mehr: npe.printStackTrace();
			}
			out.println("\"></td></tr><tr>");
			out.println("<td>&nbsp;</td>");
			out.println("<td align=\"left\">&nbsp;&nbsp;Auftrag&nbsp;3&nbsp;</td>");
			out.println("<td><input type=\"text\" name=\"abnr3\" tabindex=\"3\" size=\"6\" maxlength=\"6\" value=\"");
			try {
				if (pmn.compareTo("zuweisen")!=0){
					if (abNr3!=0){
						// out.println(abNr3);
					}
				} 
			} catch(NullPointerException npe) {
				// Keine Log-Eintraege mehr : npe.printStackTrace();
			}
			out.println("\"></td></tr><tr>");
		   }
		   out.println("<td>&nbsp;</td>");
		   out.println("<td align=\"left\">&nbsp;&nbsp;P-Zone&nbsp;</td>");
		   String choosePz = new String();
		   out.println("<td><SELECT NAME=\"pzone\">");
		   try {
			Statement   stmt           = con.createStatement();
			String      sqlString      = new String();
			ResultSet   rs;
			int         rsZaehl        = 0;
			int pf_pfnr = 0;
			String pf_bez = "";	
			// PFR wird bis 80für automatisch Zuweisungen genutzt, hier also ab pp_pfzone 81
			//sqlString = "SELECT DISTINCT pp_pfzone FROM plrv11.bde_pufpl WHERE pp_pfzone>=81";
			// WERKSNEUORDNUNG
			sqlString = "SELECT DISTINCT pp_pfzone FROM plrv11.bde_pufpl WHERE pp_pfnr="+pfNr+" order by pp_pfzone";
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()) {
				if (pzone == rs.getInt("pp_pfzone")){
					out.println("<OPTION VALUE=\""+rs.getInt("pp_pfzone")+"\" SELECTED>"+rs.getInt("pp_pfzone"));
				} else {
					out.println("<OPTION VALUE=\""+rs.getInt("pp_pfzone")+"\">"+rs.getInt("pp_pfzone"));
				}
			}
			rs.close();
			stmt.close();
		   } catch (SQLException sqlex) {
	             log("Fehler beim Lesen: "+sqlex);
	 	     out.println("<br>"+sqlex+"<br>");
	 	   }
		   out.println("</SELECT></td>");
		//}
		// folgende Puffer sind ohne Platz-Zuweisung ...
		//if (pfNr == 10245400 || pfNr == 10245200 || pfNr == 10144100 || pfNr == 10144200){
		} else {
			out.println("<td></td><td align=\"left\">&nbsp;&nbsp;Platz</td>");
			out.print("<td><input type=\"text\" name=\"platznr\" tabindex=\"2\" size=\"6\" maxlength=\"3\" value=\"");
			try {
				if (pmn.compareTo("zuweisen")!=0){
					if (platzNr!=0){
						// out.println(platzNr);
					}
				} 
			} catch(NullPointerException npe) {
				// Keine Log-Eintraege mehr:  npe.printStackTrace();
			}
			out.println("\"></td>");
		}
		out.println("<td align=\"center\" bgcolor=\"red\"><input type=\"submit\" name=\"pmn\" value=\"zuweisen\"></td>");
		out.println("<td align=\"center\" bgcolor=\"lightgreen\"><input type=\"submit\" name=\"pmn\" value=\"belegte Plätze\"></td>");
		out.println("<td align=\"center\" bgcolor=\"lightblue\"><input type=\"submit\" name=\"pmn\" value=\"freie Plätze\"></td>");
		out.println("<td align=\"center\"><input type=\"submit\" name=\"pmn\" value=\"freigeben\"></td>");
		out.println("</tr>");
		out.println("</table>");
		out.println("</form>");
		out.println("</p><hr>");
		// ***** Details anzeigen
		if (request.getParameter("pmn").compareTo("Details")==0){
			out.println(showDetails(con, pfNr,abNr));
		}
		// ***** Liste der belegten Plaetze
		if (request.getParameter("pmn").substring(0,7).compareTo("belegte")==0){
		//if (request.getParameter("pmn").compareTo("belegte Plätze")==0){
			out.println(listPlaces(con, "J", pfNr,request));
		}
		// ***** Liste der freien Plaetze
		if (request.getParameter("pmn").substring(0,5).compareTo("freie")==0){
			out.println(listPlaces(con, "N", pfNr,request));
		}
		// ***** Puffer zuweisen
		if (request.getParameter("pmn").compareTo("zuweisen")==0){
			errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + " Zuweisung:"+user+" Puffernummer:"+pfNr+ "/ Pufferzone: "+pzone+"\n");
			try {
				Statement   stmt1 = con.createStatement();
				String      sql1  = new String();
				ResultSet   rs1;
				sql1 = "SELECT COUNT(*) FROM plrv11.bde_pufpl WHERE ";
				sql1 += "pp_kz='N' AND pp_pfzone="+pzone+" AND pp_pfnr="+pfNr;
				stmt1.executeQuery(sql1);
				rs1 = stmt1.getResultSet();
				while (rs1.next()) {
					errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": Anzahl freier Plätze : "+rs1.getString(1));
				}
				rs1.close();
				stmt1.close();
			} 
			catch(SQLException sqlex) {
			
			}
			out.println(assignPlace(con, pfNr, out,platzNr, abNr,abNr2,abNr3,pzone));
		}
		// ***** Platz freigeben
		if (request.getParameter("pmn").compareTo("freigeben")==0){
			out.println(freePlace(con, pfNr,platzNr,abNr));
		}
		con.close();
	} catch (Exception npe) { //Globales Catch
		// Ehemals NullPointerException
		npe.printStackTrace();
	}
	out.println("</body>");
	out.println("</html>");
	out.close();
}
public String showDetails(Connection con, int pp_pfnr, int inAbNr){
	String ret = new String();
	try {
		Statement   stmt           = con.createStatement();
		String      sqlString      = new String();
		ResultSet   rs;
		int         rsZaehl        = 0;
		// plr_auftr_status nach as_abNr und as_status!=99 durchsuchen und as_aend_ix auslesen
		int         as_aend_ix     = 0;
		int         as_status      = 0;
		sqlString = "SELECT as_aend_ix, as_status FROM plrv11.plr_auftr_status WHERE as_status <> 99 AND as_abnr = " + inAbNr;
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()){
			rsZaehl++;
			as_aend_ix = rs.getInt("as_aend_ix");
			as_status = rs.getInt("as_status");
		}
		rs.close();
		if (rsZaehl > 0){
			// plr_auftr_kopf nach ako_abnr und ako_aend_ix durchsuchen 
			rsZaehl = 0;
			int ako_fabt_pps       = 0;
			int ako_fabt_vs        = 0;
			int ako_uhr_zeit       = 0;
			int ako_prog           = 0;
			int ako_se             = 0;
			String ako_sf_hw_auftr = new String();
			int ako_leitbezug      = 0;
			String ako_kz_pg       = new String();
			sqlString = "SELECT ako_fabt_pps, ako_fabt_vs, ako_uhr_zeit, ako_prog, ako_se, ako_sf_hw_auftr, ako_leitbezug, ako_kz_pg";
			sqlString += " FROM plrv11.plr_auftr_kopf WHERE ako_aend_ix = "+as_aend_ix+" AND ako_abnr = " + inAbNr;
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()){
				rsZaehl++;
				ako_fabt_pps    = rs.getInt("ako_fabt_pps");
				ako_fabt_vs     = rs.getInt("ako_fabt_vs");
				ako_uhr_zeit    = rs.getInt("ako_uhr_zeit");
				ako_prog        = rs.getInt("ako_prog");
				ako_se          = rs.getInt("ako_se");
				ako_leitbezug   = rs.getInt("ako_leitbezug");
				ako_sf_hw_auftr = rs.getString("ako_sf_hw_auftr");
				ako_kz_pg       = rs.getString("ako_kz_pg");
			}
			rs.close();
			if (rsZaehl > 0){
				ret = "<p>ABNR: "+inAbNr+"<br><br>PufferNr: "+pp_pfnr+"<br><br>";
				rsZaehl = 0;
				String ppPfPlatz = new String();
				sqlString = "SELECT pp_pf_platz FROM plrv11.bde_pufpl WHERE pp_pfnr="+pp_pfnr+" AND pp_kz='J' AND pp_abnr=" + inAbNr;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()){
					rsZaehl++;
					ppPfPlatz = rs.getString("pp_pf_platz");
				}
				rs.close();
				if (rsZaehl != 0){
					/* Plätze werden nur in 10243100 (PMN) und 10245100 (PKZ) 3-stellig verwaltet, sonst Zonen */
					if(ppPfPlatz.substring(5,6).compareTo("1") == 0 && ppPfPlatz.substring(2,3).compareTo("2") == 0) {
						ret += "Der Auftrag ist auf Platz "+ppPfPlatz.substring(8)+" eingelagert.<br><br>";
					} else {
						ret += "Der Auftrag ist in Zone "+ppPfPlatz.substring(7,9)+" eingelagert.<br><br>";
					}
				}
				ret += "<table border=\"1\" align=\"center\">";
				ret += "<tr><th>FabTag PPS</th><th>FabTag / Uhrzeit VS</th><th>Programm</th><th>SE</th>";
				ret += "<th>SF HW</th><th>Leitbezug</th><th>KZ PG</th></tr>";
				ret += "<tr><td>"+ako_fabt_pps+"</td><td>"+ako_fabt_vs+" / "+ako_uhr_zeit+"</td><td>"+ako_prog+"</td><td>"+ako_se+"</td>";
				ret += "<td>"+ako_sf_hw_auftr+"</td><td>"+ako_leitbezug+"</td><td>"+ako_kz_pg+"</td></tr>";
				ret += "</table></p>";
				// plr_art_pos nach ap_abnr und ap_aend_ix durchsuchen
				rsZaehl = 0;
				int    ap_posnr    = 0;
				String ap_artnr    = new String();
				int    ap_menge    = 0;
				String ap_me       = new String();
				String ap_bezeichn = new String();
				String ap_bezug    = new String();
				String ap_bezbez   = new String();
				sqlString = "SELECT ap_posnr, ap_artnr, ap_menge, ap_me, ap_bezeichn, ap_bezug, ap_bezbez";
				sqlString += " FROM plrv11.plr_art_pos WHERE ap_aend_ix = "+as_aend_ix+" AND ap_abnr = " + inAbNr;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				ret += "<p><table border=\"1\" align=\"center\">";
				ret += "<tr><th>PosNr</th><th>ArtNr</th><th>Menge</th><th>ME</th><th>Bezeichnung</th><th>Bezug</th><th>Bezugbez.</th></tr>";
				while (rs.next()){
					rsZaehl++;
					ap_posnr    = rs.getInt("ap_posnr");
					ap_artnr    = rs.getString("ap_artnr");
					ap_menge    = rs.getInt("ap_menge");
					ap_me       = rs.getString("ap_me");
					ap_bezeichn = rs.getString("ap_bezeichn");
					ap_bezug    = rs.getString("ap_bezug");
					ap_bezbez   = rs.getString("ap_bezbez");
					ret += "<tr><td>"+ap_posnr+"</td><td>"+ap_artnr+"</td><td>"+ap_menge+"</td><td>"+ap_me+"</td>";
					ret += "<td>"+ap_bezeichn+"</td><td>"+ap_bezug+"</td><td>"+ap_bezbez+"</td></tr>";
				}
				ret += "</table></p>";
				rs.close();
			} else {
				ret += "ABNR "+inAbNr+" ist nicht im Auftragskopf!";
			}
		} else {
			ret += "Auftrag "+inAbNr+" ist nicht im Bestand.";
		}
		stmt.close();
	} catch (SQLException sqlex) {
		log("Fehler beim Lesen: "+sqlex);
		ret += "<br>"+sqlex+"<br>";
	}
	return ret;
}
public String assignPlace(Connection con, int pp_pfnr, PrintWriter out,int inPlatzNr,int inAbNr,int inAbNr2,int inAbNr3,int inPZone){
	String pp_pf_platz = new String();
	String ret         = new String();
	String pfad        = new String();
	String ako_vpkz    = new String();
	String pp_kz 	    = new String();
	int     redo        = 1;
	int     ga_zulief   = 0;
	int     ga_abr_werk = 0;
	int     pf_status   = 0;
	int abf_pf_status   = 0;
	int as_aend_ix      = 0;
	int vgart	    = 0;
	int fabTag    = 0;
	switch (pp_pfnr){
	case 10144100:
		// PFT1 NEUE WERKSORDNUNG
		//Bekannt ist: abNr, pzone
		pfad = "/home/ss06/SS06/vom_db";
		vgart=15;
		pp_pf_platz = searchPlatzNr(con,pp_pfnr,inPZone);
		break;
	case 10144200:
		// PVOPO NEUE WERKSORDNUNG
		//Bekannt ist: abNr, pzone
		pfad = "/home/ss06/SS06/vom_db";
		vgart=13;
		pp_pf_platz = searchPlatzNr(con,pp_pfnr,inPZone);
		break;
	case 10144400:
		// PGEST NEUE WERKSORDNUNG
		//Bekannt ist: abNr, pzone
		pfad = "/home/ss06/SS06/vom_db";
		vgart=27;
		pp_pf_platz = searchPlatzNr(con,pp_pfnr,inPZone);
		break;
	case 10243100:
		// PMN
		//Bekannt ist: abNr, platzNr
		pfad = "/home/ss06/SS04/vom_nae";
		vgart=23;
		pp_pf_platz = makePlatzNr(pp_pfnr, inPlatzNr);
		break;
	case 10243200:
		// PBZ
		//Bekannt ist: abNr
		pfad = "/home/ss06/SS04/vom_nae";
		vgart=25;
		pp_pf_platz = searchPlatzNr(con,pp_pfnr, inPZone);
		break;
	case 10245100:
		// PKZ
		//Bekannt ist: abNr, platzNr
		pfad = "/home/ss06/SS11/vom_tdd";
		vgart=17;
		pp_pf_platz = makePlatzNr(pp_pfnr, inPlatzNr);
		break;
	case 10245200:
		// PFR
		//Bekannt ist: abNr, pzone (evtl. abNr2, abNr3)
		pfad = "/home/ss06/SS06/vom_db";
		vgart=19;
		pp_pf_platz = searchPlatzNr(con,pp_pfnr,inPZone);
		redo = 3;
		break;
	case 10245400:
		// PQS NEUE WERKSORDNUNG
		//Bekannt ist: abNr, pzone
		pfad = "/home/ss06/SS06/nach_db";
		vgart=21;
		pp_pf_platz = searchPlatzNr(con,pp_pfnr,inPZone);
		break;
	default:
		pp_pf_platz = "0";
		out.println("<br>Puffer nicht in Switch-Anweisung.<br>");
		break;
	}
	// out.println("<br>Puffer: "+pp_pfnr+"<br>AB: "+abNr+"<br>Platz &uuml;bergeben: "+
	// platzNr+"<br>Platz ermittelt:"+pp_pf_platz+"<br>AB2: "+abNr2+
	// "<br>AB3: "+abNr3+"<br>Zone: "+pzone+"<br>Pfad: "+pfad+"<br>");
	try {
		Statement   stmt           = con.createStatement();
		String      sqlString, sqlppString,sqlabfString,sqlakoString,sqlgaString      = new String();
		ResultSet   rs, rsAB;
		int         rsZaehl        = 0;
		String      checkResult    = new String();
		int         fehler         = 0;
		pf_status=getPfErhoeStatus(pp_pfnr,con);
		as_aend_ix=getAendIx(inAbNr, con);
		abf_pf_status=getAktPfstatus(inAbNr, pp_pfnr, as_aend_ix, con);
		sqlString = "SELECT fab_fab_tag FROM plrv11.plr_fabkal_dat WHERE fab_datum='"+bdeZeit.getTimestamp("ddMMyy")+"'";
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()) {
			fabTag = rs.getInt("fab_fab_tag");
		}
		rs.close(); 
		if (redo == 1){
			checkResult = checkAssignPlace(con, pp_pfnr, pfad, pp_pf_platz, inAbNr);
			// out.println("checkAssign:"+pp_pfnr+", "+pp_pf_platz+", "+inAbNr+", "+abf_pf_status);
			if (checkResult.compareTo("1") == 0){
				int new_status = abf_pf_status + pf_status;
				//PFT1 + PKZ erhöhen abf_fs=10144100 NEUE WERKSORDNUNG
				if(pp_pfnr == 10144100 || pp_pfnr == 10245100) {
                                  sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status="+new_status;
                                  sqlString += " WHERE abf_fs='"+pp_pfnr+"000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
				  //neuen Puffer PFF abfragen zwecks PF_STATUS 10245100000 bei PKZ einlagern
				  //hinzugefügt am 19.3.2007
				  if(pp_pfnr == 10245100) {
					sqlppString =  "SELECT pp_kz FROM plrv11.bde_pufpl WHERE pp_pfnr='10245500' AND pp_abnr="+inAbNr;
					stmt.executeQuery(sqlppString);
					rs = stmt.getResultSet();
					while (rs.next())
						pp_kz  = rs.getString("pp_kz");
					rs.close();
					if(pp_kz.compareTo("J")== 0) {
                                  		sqlabfString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status=40";
                                  		sqlabfString += " WHERE abf_fs='10245100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
						stmt.executeQuery(sqlabfString);
					}
				  }
				//Ende Änderung vom 19.3.2007
				} else {
				  // Sonderfall PBZ bezieht sich auf plr_ga_dat 13.06.2007
				  if(pp_pfnr == 10243200) {
				    sqlString = "UPDATE plrv11.plr_ga_dat SET ga_status=ga_status+7, ga_bez_vorh='X'";
				    sqlString += " WHERE ga_bez_vorh<>'X' AND ga_abnr="+inAbNr+" AND ga_aend_ix="+as_aend_ix;
				  } else {
				    sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status="+new_status;
				    sqlString += " WHERE abf_fs='"+pp_pfnr+"000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
				  }
				}
				stmt.executeQuery(sqlString);
				errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss")+":"+sqlString+"\n");
				sqlString = "COMMIT";
				stmt.executeQuery(sqlString);
				out.println("<br>Status f&uuml;r "+inAbNr+" auf "+new_status+" erh&ouml;ht.");
				sqlString = "UPDATE plrv11.bde_pufpl SET pp_abnr="+inAbNr+", pp_kz='J' WHERE pp_pf_platz='"+pp_pf_platz+"'";
				stmt.executeQuery(sqlString);
				errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " +sqlString+"\n");
				// ML_Protokoll seit 7.4.2009
				sqlString = "INSERT into plrv11.ml_protokoll (pro_abnr, pro_timestamp, pro_vgart,";
				sqlString += "pro_vgfabt,pro_vgtime,pro_werk,pro_gueltkz,pro_urheber, pro_stoergrund) ";
				sqlString += "VALUES ("+inAbNr+",'"+bdeZeit.getTimestamp("yyyyMMddHHmmss")+"',";
				sqlString += +vgart+","+fabTag+","+bdeZeit.getTimestamp("HHmm")+","+"10,1,8,0)";
				stmt.executeQuery(sqlString);
				sqlString = "COMMIT";
				stmt.executeQuery(sqlString);
				/* Plätze werden nur in 10243100 (PMN) und 10245100 (PKZ) 3-stellig verwaltet, sonst Zonen */
                                if(pp_pf_platz.substring(5,6).compareTo("1") == 0 && pp_pf_platz.substring(2,3).compareTo("2") == 0) {
                                  out.println("<br>ABNR "+inAbNr+" auf Platz "+pp_pf_platz.substring(8)+" eingelagert.");
                                } else {
                                  out.println("<br>ABNR "+inAbNr+" in Zone "+pp_pf_platz.substring(7,9)+" eingelagert.");
                                }

				// P-VOPO wieder freigeben NEUE WERKSORDNUNG
				if(pp_pfnr == 10144100) { 
				  out.println("<br>ABNR "+inAbNr+" wird in PVOPO wieder freigegeben ...");
				  sqlString = "UPDATE plrv11.bde_pufpl SET pp_kz='N' WHERE pp_abnr="+inAbNr+" AND pp_pfnr=10144200 and pp_kz='J'";
				  errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " + sqlString+"\n");
				  stmt.executeQuery(sqlString);
				  sqlString = "COMMIT";
				  stmt.executeQuery(sqlString);
				}
				
				// File fuer Uwe zusammenbauen
				if (pp_pfnr == 10243100)
					schreibeDatei(1,pfad,"AP",inAbNr,pp_pfnr,new_status);
				if (pp_pfnr == 10144100) {
					sqlgaString =  "SELECT ga_zulief, ga_abr_werk FROM plrv11.plr_ga_dat WHERE ga_abnr="+inAbNr;
					stmt.executeQuery(sqlgaString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						ga_zulief   = rs.getInt("ga_zulief");
						ga_abr_werk = rs.getInt("ga_abr_werk");
					}
					rs.close();
					if(ga_zulief > 0)
						schreibeDatei(1,pfad,"GA"+ga_zulief,inAbNr,pp_pfnr,new_status);
					if(ga_abr_werk == 38) {
						schreibeDatei(1,pfad,"AP",inAbNr,pp_pfnr,new_status);
						schreibeDatei(1,pfad,"SAP",inAbNr,pp_pfnr,new_status);
					}
					if(ga_abr_werk == 31) {
						schreibeDatei(1,pfad,"SAP",inAbNr,pp_pfnr,new_status);
					}
				}
				// File fuer Uwe, drucken einer Liste wenn ako_vpkz='K'
				if (pp_pfnr == 10245400) {
					sqlakoString =  "SELECT ako_vpkz FROM plrv11.plr_auftr_kopf WHERE ako_abnr="+inAbNr;
					stmt.executeQuery(sqlakoString);
					rs = stmt.getResultSet();
					while (rs.next())
						ako_vpkz  = rs.getString("ako_vpkz");
					rs.close();
					if(ako_vpkz.compareTo("K")== 0) {
					  schreibeDatei(1,pfad,"AP",inAbNr,pp_pfnr,new_status);
					}
				}
				//Material zurueckmelden
				out.println(matRueckMeld(con,pp_pfnr, inAbNr));
				ret += "<br>Erfolgreich "+inAbNr+"<br>";
			} else {
				out.println(checkResult);
				fehler = 1;
			}
		}
		if (redo == 3){
			checkResult = checkAssignPlace(con, pp_pfnr, pfad, pp_pf_platz, inAbNr);
			if (checkResult.compareTo("1") == 0) {
				//out.println(checkResult);
				// ML_Protokoll seit 7.4.2009
				sqlString = "INSERT into plrv11.ml_protokoll (pro_abnr, pro_timestamp, pro_vgart, pro_vgfabt,";
				sqlString += "pro_vgtime, pro_werk, pro_gueltkz, pro_urheber, pro_stoergrund) ";
				sqlString += "VALUES ("+inAbNr+",'"+bdeZeit.getTimestamp("yyyyMMddHHmmss")+"',";
				sqlString += +vgart+","+fabTag+","+bdeZeit.getTimestamp("HHmm")+","+"10,1,8,0)";
				stmt.executeQuery(sqlString);

				if (inAbNr2 == 0 && inAbNr3 == 0) {
					sqlString = "UPDATE plrv11.bde_pufpl SET pp_abnr="+inAbNr+", pp_ab2=0, pp_ab3=0, pp_kz='J' WHERE pp_pf_platz='"+pp_pf_platz+"'";
				} else {
					if (inAbNr3 == 0) {
					sqlString = "UPDATE plrv11.bde_pufpl SET pp_abnr="+inAbNr+",pp_ab2="+inAbNr2+", pp_ab3=0, pp_kz='J',pp_zuteil_kz='A' WHERE pp_pf_platz='"+pp_pf_platz+"'";
					} else {
					sqlString = "UPDATE plrv11.bde_pufpl SET pp_abnr="+inAbNr+",pp_ab2="+inAbNr2+", pp_ab3="+inAbNr3+", pp_kz='J',pp_zuteil_kz='A' WHERE pp_pf_platz='"+pp_pf_platz+"'";
					}
				}
				stmt.executeQuery(sqlString);
				errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " +sqlString+"\n");
				sqlString = "COMMIT";
				stmt.executeQuery(sqlString);
				int myAbNr = inAbNr;
				int new_status = abf_pf_status + pf_status;
				for (int a = 0; a < redo; a++){
					//if (a == 0) myAbNr = abNr;
					if (a == 1) myAbNr = inAbNr2;
					if (a == 2) myAbNr = inAbNr3;
					if (myAbNr > 0){
						if (inAbNr2 > 0) {
							sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_platz="+pp_pf_platz+",abf_pf_status="+new_status;
							sqlString += " WHERE abf_fs='10245100000' AND abf_abnr="+myAbNr;
						} else {
							sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status="+new_status;
							sqlString += " WHERE abf_fs='10245100000' AND abf_abnr="+myAbNr+" AND abf_aend_ix="+as_aend_ix;
						}
						stmt.executeQuery(sqlString);
						sqlString = "COMMIT";
						stmt.executeQuery(sqlString);
						out.println("<br>ABNR "+myAbNr+" in Zone "+pp_pf_platz.substring(7,9)+" eingelagert.");
						// alle eingegebenen ABNR auf GA_STATUS "unterwegs" prüfen
						sqlString = "UPDATE plrv11.plr_ga_dat SET ga_status=50 WHERE ga_abnr="+myAbNr+" AND ga_status=49";
						errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " +sqlString+"\n");
						stmt.executeQuery(sqlString);
						sqlString = "COMMIT";
						stmt.executeQuery(sqlString);
						// if (pp_pfnr == 10245200)
					       // 	schreibeDatei(1,pfad,"SAP",myAbNr,pp_pfnr,new_status);
						out.println("<br>Status f&uuml;r "+myAbNr+" auf "+new_status+" erh&ouml;ht.");
						//Material zurueckmelden
						out.println(matRueckMeld(con,pp_pfnr, myAbNr));
					}
				}
				ret += "<br>Erfolgreich!<br>";
				sqlgaString =  "SELECT ga_zulief, ga_abr_werk FROM plrv11.plr_ga_dat WHERE ga_abnr="+inAbNr;
				stmt.executeQuery(sqlgaString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					ga_zulief   = rs.getInt("ga_zulief");
					ga_abr_werk = rs.getInt("ga_abr_werk");
				}
				rs.close();
				if(ga_zulief > 0) {
					schreibeDatei(1,pfad,"GA"+ga_zulief,inAbNr,pp_pfnr,new_status);
					// schreibeDatei(1,pfad,"SAP",inAbNr,pp_pfnr,new_status);
				}
				if (inAbNr2 > 0) {
					sqlgaString =  "SELECT ga_zulief, ga_abr_werk FROM plrv11.plr_ga_dat WHERE ga_abnr="+inAbNr2;
					stmt.executeQuery(sqlgaString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						ga_zulief   = rs.getInt("ga_zulief");
						ga_abr_werk = rs.getInt("ga_abr_werk");
					}
					rs.close();
					if(ga_zulief > 0) {
						schreibeDatei(1,pfad,"GA"+ga_zulief,inAbNr2,pp_pfnr,new_status);
						// schreibeDatei(1,pfad,"SAP",inAbNr2,pp_pfnr,new_status);
					}
				}
				if (inAbNr3 > 0) {
					sqlgaString =  "SELECT ga_zulief, ga_abr_werk FROM plrv11.plr_ga_dat WHERE ga_abnr="+inAbNr3;
					stmt.executeQuery(sqlgaString);
					rs = stmt.getResultSet();
					while (rs.next()) {
						ga_zulief   = rs.getInt("ga_zulief");
						ga_abr_werk = rs.getInt("ga_abr_werk");
					}
					rs.close();
					if(ga_zulief > 0) {
						schreibeDatei(1,pfad,"GA"+ga_zulief,inAbNr3,pp_pfnr,new_status);
						// schreibeDatei(1,pfad,"SAP",inAbNr3,pp_pfnr,new_status);
					}
				}
			} else {
				out.println(checkResult);
				fehler = 1;
			}
		}
		if (fehler != 0){
			out.println("<br>Fehler beim Einlagern. Abbruch!<br>");
		}
		stmt.close();
	} catch (SQLException sqlex) {
	    log("Fehler beim Lesen: "+sqlex);
	    out.println("<br>"+sqlex+"<br>");
	}
	return ret;
}
public String checkAssignPlace(Connection con, int pp_pfnr,String pfad, String pp_pf_platz, int aktAbNr){
	int    pf_status     = 0;
	int    abf_pf_status = 0;
	int    as_aend_ix    = 0;
	String ret = new String();
	try {
		Statement   stmt           = con.createStatement();
		String      sqlString      = new String();
		ResultSet   rs;
		ResultSet   rsAB;
		int         rsZaehl        = 0;
		int         as_status      = 0;
		// plr_auftr_status nach as_abNr und as_status!=99 durchsuchen und as_aend_ix auslesen
		sqlString = "SELECT as_aend_ix, as_status FROM plrv11.plr_auftr_status WHERE as_status <> 99 AND as_abnr = " + aktAbNr;
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()){
			rsZaehl++;
			as_aend_ix = rs.getInt("as_aend_ix");
			as_status = rs.getInt("as_status");
		}
		rs.close();
		if (rsZaehl == 1){
			String pp_kz = new String();
			String pfPlatz = new String();
			rsZaehl = 0;
			sqlString = "SELECT pp_pf_platz FROM plrv11.bde_pufpl WHERE pp_kz='J' AND pp_pfnr="+pp_pfnr+" AND pp_abNr="+aktAbNr;
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()){
				rsZaehl++;
				pfPlatz = rs.getString("pp_pf_platz");
			}
			if (rsZaehl==0){
				rsZaehl = 0;
				sqlString = "SELECT pp_kz FROM plrv11.bde_pufpl WHERE pp_pf_platz="+pp_pf_platz;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()){
					rsZaehl++;
					pp_kz = rs.getString("pp_kz");
				}
				if (rsZaehl == 1){
					if (pp_kz.compareTo("N")==0){
						rsZaehl = 0;
						sqlString = "SELECT pf_status FROM plrv11.bde_pfdat WHERE pf_pfnr="+pp_pfnr;
						stmt.executeQuery(sqlString);
						rs = stmt.getResultSet();
						while (rs.next()){
							rsZaehl++;
							pf_status = rs.getInt("pf_status");
						}
						if (rsZaehl == 1){
							rsZaehl = 0;
							int abf_fs_status = 0;
							// Puffererhöhung auf abf_fs 10144100000 ...
							if(pp_pfnr == 10144200)
								sqlString =  "SELECT abf_pf_status, abf_fs_status "+
								"FROM plrv11.bde_ab_fs_ma WHERE abf_fs='10144100000' AND abf_abnr="+
								aktAbNr+" AND abf_aend_ix="+as_aend_ix;
							else {
							// PBZ wirkt sich nicht auf ab_fs_ma aus 13.06.2007
							  if(pp_pfnr == 10245100 || pp_pfnr == 10245200 || pp_pfnr == 10243200)
								sqlString =  "SELECT abf_pf_status, abf_fs_status "+
								"FROM plrv11.bde_ab_fs_ma WHERE abf_fs='10245100000' AND abf_abnr="+
								aktAbNr+" AND abf_aend_ix="+as_aend_ix;
							  else
								sqlString = "SELECT abf_pf_status, abf_fs_status "+
								"FROM plrv11.bde_ab_fs_ma WHERE abf_fs='"+pp_pfnr+
								"000' AND abf_abnr="+aktAbNr+" AND abf_aend_ix="+as_aend_ix;
							}
							stmt.executeQuery(sqlString);
							rs = stmt.getResultSet();
							while (rs.next()){
								rsZaehl++;
								abf_pf_status = rs.getInt("abf_pf_status");
								abf_fs_status = rs.getInt("abf_fs_status");
							}
//		ret = "0"+abf_pf_status+abf_fs_status;
							if (rsZaehl == 1){
								if (abf_fs_status == 1){
									if (abf_pf_status < 40){
									ret = "1";
									}
								} else {
									if(pp_pfnr == 10245100) { // wenn im PFF
										ret = "1";
									} else {
										ret += "<br>ABNR "+aktAbNr+" ist bereits zugewiesen!<br>Wurde nicht eingelagert!!<br>";
									}
								}
							} else {
								ret += "Auftrag "+aktAbNr+" ist in bde_ab_fs_ma nicht vorhanden.<br>NICHT EINGELAGERT!<br>";
							}
						} else {
						  ret += "Puffernummer "+pp_pfnr+" ist in bde_pfdat nicht vorhanden.<br>NICHT EINGELAGERT!<br>";
						}
					} else {
						ret += "<br>Der Platz "+pp_pf_platz+" ist belegt. Bitte andere PlatzNr eingeben!<br>";
					}
				} else {
					ret += aktAbNr+"<br>Platznummer "+pp_pf_platz+" f&uuml;r diesen Puffer nicht angelegt!<br>";
				}
			} else {
				ret += "<br>Der Auftrag "+aktAbNr+" ist bereits auf Pufferplatz "+pfPlatz+" eingelagert!<br>";
			}
		} else {
			ret += "<br>Auftrag "+aktAbNr+" ist nicht im Bestand!<br>";
		}
		stmt.close();
	} catch (SQLException sqlex) {
		log("Fehler beim Lesen: "+sqlex);
		ret += "<br>"+sqlex+"<br>";
	}
	return ret;
}
public String listPlaces(Connection con, String kz, int pp_pfnr,HttpServletRequest request){
	String ret = new String();
	try {
		Statement   stmt           = con.createStatement();
		String      sqlString      = new String();
		ResultSet   rs;
		int         pp_abnr        = 0;
		int         pp_ab2         = 0;
		int         pp_ab3         = 0;
		String      pp_fs          = new String();
		String      pp_pf_platz    = new String();
		int         pp_pfzone      = 0;
		String      pp_kz          = new String();
		String      platz          = new String();	
		if (pp_pfnr == 10245200){
			sqlString = "SELECT pp_abnr, pp_ab2, pp_ab3, pp_fs, pp_pf_platz, pp_pfzone, pp_kz ";
			sqlString += "FROM plrv11.bde_pufpl WHERE pp_pfnr="+pp_pfnr+" AND pp_kz='"+kz+"' ORDER BY pp_pf_platz";
		} else {
			sqlString = "SELECT pp_abnr, pp_fs, pp_pf_platz, pp_pfzone, pp_kz ";
			sqlString += " FROM plrv11.bde_pufpl WHERE pp_pfnr="+pp_pfnr+" AND pp_kz='"+kz+"' ORDER BY pp_pf_platz";
		}
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		ret = "<p><table border=\"1\" align=\"center\">";
		ret += "<tr><th>AbNr.</th>";
		if (pp_pfnr == 10245200){
			ret += "<th>AbNr. 2</th><th>AbNr. 3</th>";
		}
		ret += "<th>Fert.-Stufe</th><th>Platz-Nr.</th><th>P-Zone</th><th>Belegt</th></tr>";
		while (rs.next()){
			pp_abnr     = rs.getInt("pp_abnr");
			if (pp_pfnr == 10245200){
				pp_ab2     = rs.getInt("pp_ab2");
				pp_ab3     = rs.getInt("pp_ab3");
			}
			pp_fs       = rs.getString("pp_fs");
			pp_pf_platz = rs.getString("pp_pf_platz");
			pp_pfzone   = rs.getInt("pp_pfzone");
			pp_kz       = rs.getString("pp_kz");
			try {
				platz       = pp_pf_platz.substring(8);
			} catch (IndexOutOfBoundsException ioobe){}
			if (pp_pfnr != 10245200){
				// ret += "<tr><td><a href=\"/servlets/pufferMaske?pfNr="+pp_pfnr+"&abnr="+pp_abnr+
				// "&platznr="+platz+"&pmn="+pmn+"\">"+pp_abnr+"</a></td>";
				ret += "<tr><td><a href=\""+request.getRequestURL()+"?pfNr="+pp_pfnr+"&abnr=";
				ret += pp_abnr+"&platznr="+platz+"&pmn="+"Details"+"\">"+pp_abnr+"</a></td>";
			}
			if (pp_pfnr == 10245200){
				// ret += "<tr><td><a href=\"/servlets/pufferMaske?pfNr="+pp_pfnr+"&abnr="+pp_abnr+"&abnr2="+
				// pp_ab2+"&abnr3="+pp_ab3+"&platznr="+platz+"&pzone="+pp_pfzone+"&pmn="+pmn+"\">"+pp_abnr+"</a></td>";
				ret += "<tr><td><a href=\""+request.getRequestURL()+"?pfNr="+pp_pfnr+"&abnr="+pp_abnr+"&abnr2=";
				ret += pp_ab2+"&abnr3="+pp_ab3+"&platznr="+platz+"&pzone="+pp_pfzone+"&pmn="+"Details"+"\">"+pp_abnr+"</a></td>";
				ret += "<td>"+pp_ab2+"</td>";
				ret += "<td>"+pp_ab3+"</td>";
			}
			// hier ist's passiert
			if (pp_kz.compareTo("N")==0) {
				ret += "<td>"+pp_fs+"</td><td>"+pp_pf_platz+"</td><td>"+pp_pfzone+"</td><td align=center bgcolor=\"#007FFF\">"+pp_kz+"</td></tr>";
			} else {
				ret += "<td>"+pp_fs+"</td><td>"+pp_pf_platz+"</td><td>"+pp_pfzone+"</td><td align=center bgcolor=\"#32CC99\">"+pp_kz+"</td></tr>";
			}
		}
		ret += "</table></p>";
		rs.close();
		stmt.close();
	} catch (SQLException sqlex) {
	    log("Fehler beim Lesen: "+sqlex);
	    ret += "<br>"+sqlex+"<br>";
	}
	return ret;
}

public String freePlace(Connection con, int pp_pfnr,int inPlatzNr,int inAbNr){
	String pp_pf_platz = new String();
	String ret         = new String();
	String pfad        = new String();
	int pf_status     = 0;
	int abf_pf_status = 0;
	int as_aend_ix    = 0;
	int vgart	  = 0;
	int fabTag	  = 0;
	try {
		Statement   stmt           = con.createStatement();
		String      sqlString      = new String();
		ResultSet   rs;
		int         rsZaehl        = 0;
		if (inPlatzNr == 0){
		// bei PFT1, PVOPO und PFR wird keine Platznummer übergeben ...
		// ret = "<br>Die Platznummer muss gr&ouml;sser 0 sein.";
		}
		switch (pp_pfnr){
		case 10243100:
		case 10245100:
			if(pp_pfnr == 10243100)
			  vgart = 24;
			else
			  vgart = 18;
			// PMN, PKZ
			//Bekannt ist: abNr, platzNr
			pp_pf_platz = makePlatzNr(pp_pfnr, inPlatzNr);
			break;
		case 10144100:
		case 10144200:
		case 10144400:
		case 10243200:
		case 10245200:
		case 10245400:
			if(pp_pfnr == 10144100)
			  vgart = 16;
			if(pp_pfnr == 10144200)
			  vgart = 14;
			if(pp_pfnr == 10144400)
			  vgart = 28;
			if(pp_pfnr == 10243200)
			  vgart = 26;
			if(pp_pfnr == 10245200)
			  vgart = 20;
			if(pp_pfnr == 10245400)
			  vgart = 22;
			// PFT1, PVOPO, PFR, PQS, PBZ
			//Bekannt ist: abNr, pzone (evtl. abNr2, abNr3)
			sqlString = "SELECT * FROM (SELECT pp_pf_platz FROM plrv11.bde_pufpl WHERE pp_abnr='"+inAbNr+"' AND pp_kz='J' AND pp_pfnr="+pp_pfnr+") WHERE ROWNUM<2";
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()){
			pp_pf_platz = rs.getString("pp_pf_platz");
			}
			break;
		default:
			pp_pf_platz = "0";
			ret = "<br>Puffer nicht in Switch-Anweisung.<br>";
			break;
		}
		// plr_auftr_status nach as_abNr und as_status!=99 durchsuchen und as_aend_ix auslesen
		sqlString = "SELECT fab_fab_tag FROM plrv11.plr_fabkal_dat WHERE fab_datum='"+bdeZeit.getTimestamp("ddMMyy")+"'";
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()) {
			fabTag = rs.getInt("fab_fab_tag");
		}
		rs.close(); 
		int as_status      = 0;
		sqlString = "SELECT as_aend_ix, as_status FROM plrv11.plr_auftr_status WHERE as_status <> 99 AND as_abnr = " + inAbNr;
		rsZaehl = 0;
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()){
			rsZaehl++;
			as_aend_ix = rs.getInt("as_aend_ix");
			as_status = rs.getInt("as_status");
		}
		rs.close();
		if (rsZaehl == 1){
			int ab = 0;
			String kz = new String();
			sqlString = "SELECT pp_abnr, pp_kz FROM plrv11.bde_pufpl WHERE pp_pf_platz='"+pp_pf_platz+"'";
			stmt.executeQuery(sqlString);
			rs = stmt.getResultSet();
			while (rs.next()){
				ab = rs.getInt("pp_abnr");
				kz = rs.getString("pp_kz");
			}
			if ((ab == inAbNr)&&(kz.compareTo("J")==0)){
				rsZaehl = 0;
				pf_status = 0;
				sqlString = "SELECT pf_status FROM plrv11.bde_pfdat WHERE pf_pfnr="+pp_pfnr;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()){
					rsZaehl++;
					pf_status = rs.getInt("pf_status");
				}
				if (rsZaehl==1){
					rsZaehl = 0;
					abf_pf_status = 0;
					int abf_fs_status = 0;
					//PVOPO + PKZ verändern Pufferstatus auf 10144100 ...
					if(pp_pfnr == 10144200 || pp_pfnr == 10245100) {
					  sqlString  = "SELECT abf_pf_status, abf_fs_status ";
					  sqlString += " FROM plrv11.bde_ab_fs_ma WHERE abf_fs='10144100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
					}
					else { 
					  // PBZ verändert keine ab_fs_ma Daten, dient nur zum Druchlauf 13.06.07
					  if(pp_pfnr == 10245200 || pp_pfnr == 10243200) {
					    sqlString  = "SELECT abf_pf_status, abf_fs_status ";
					    sqlString += " FROM plrv11.bde_ab_fs_ma WHERE abf_fs='10245100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
					  } else {
					    sqlString  = "SELECT abf_pf_status, abf_fs_status ";
					    sqlString += " FROM plrv11.bde_ab_fs_ma WHERE abf_fs='"+pp_pfnr+"000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
					  }
					}
					stmt.executeQuery(sqlString);
					rs = stmt.getResultSet();
					while (rs.next()){
						rsZaehl++;
						abf_pf_status = rs.getInt("abf_pf_status");
						abf_fs_status = rs.getInt("abf_fs_status");
					}
					if (rsZaehl==1){
						int new_status = abf_pf_status - pf_status;
						if (abf_fs_status == 1){
						  //PVOPO + PKZ verändern Pufferstatus auf 10144100 ...
						  if(pp_pfnr == 10144200 || pp_pfnr == 10245100) {
							sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status="+new_status;
							sqlString += " WHERE abf_fs='10144100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
						  } else {
					  		if(pp_pfnr == 10245200 || pp_pfnr == 10243200) {
							// PBZ verändert ga_status nicht abf_pf_status 13.06.2007
							  if(pp_pfnr == 10243200) {
							    sqlString = "UPDATE plrv11.plr_ga_dat SET ga_status=ga_status-7,ga_bez_vorh=' '";
							    sqlString += " WHERE ga_bez_vorh='X' AND ga_abnr="+inAbNr+" AND ga_aend_ix="+as_aend_ix;
							  } else {
							    sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status="+new_status;
							    sqlString += " WHERE abf_fs='10245100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
							  }
					  		} else {
							  sqlString = "UPDATE plrv11.bde_ab_fs_ma SET abf_pf_status="+new_status;
							  sqlString += " WHERE abf_fs='"+pp_pfnr+"000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+as_aend_ix;
							}
						  }
							stmt.executeQuery(sqlString);
							errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " +sqlString+"\n");
							sqlString = "COMMIT";
							stmt.executeQuery(sqlString);
							ret += "<br>Status f&uuml;r "+inAbNr+" auf "+new_status+" verringert.";
						} else {
							ret += "Status f&uuml;r "+inAbNr+" wurde nicht ver&auml;ndert.";
						}
						sqlString = "UPDATE plrv11.bde_pufpl SET pp_kz='N', pp_ab2=0, pp_ab3=0 WHERE pp_pf_platz='"+pp_pf_platz+"'";
						errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " +sqlString+"\n");
						stmt.executeQuery(sqlString);
				// ML_Protokoll seit 7.4.2009
				sqlString = "INSERT into plrv11.ml_protokoll (pro_abnr, pro_timestamp, pro_vgart,";
				sqlString += "pro_vgfabt,pro_vgtime,pro_werk,pro_gueltkz,pro_urheber, pro_stoergrund) ";
				sqlString += "VALUES ("+inAbNr+",'"+bdeZeit.getTimestamp("yyyyMMddHHmmss")+"',";
				sqlString += +vgart+","+fabTag+","+bdeZeit.getTimestamp("HHmm")+","+"10,1,8,0)";
				stmt.executeQuery(sqlString);

						sqlString = "COMMIT";
						stmt.executeQuery(sqlString);
						ret += "<br>Platz "+pp_pf_platz.substring(8,11)+" freigegeben.";
					} else {
						ret += "Auftrag ist in bde_ab_fs_ma nicht vorhanden.<br>NICHT FREIGEGEBEN!";
					}
				} else {
					ret += "Puffernummer ist in bde_pfdat nicht vorhanden.<br>NICHT FREIGEGEBEN!";
				}
			} else{
				ret += "Platz war nicht belegt oder ABNR wurde nicht gefunden.";
			}
			rs.close();
		} else {
			sqlString = "UPDATE plrv11.bde_pufpl SET pp_kz='N', pp_ab2=0, pp_ab3=0 WHERE pp_pf_platz='"+pp_pf_platz+"'";
			errorDatei.write(bdeZeit.getTimestamp("ddMMyyyyHHmmss") + ": " +sqlString+"\n");
			stmt.executeQuery(sqlString);
			sqlString = "COMMIT";
			stmt.executeQuery(sqlString);
			ret += "ABNR nicht gefunden.";
		}
		stmt.close();
	} catch (SQLException sqlex) {
	    log("Fehler beim Lesen: "+sqlex);
	}
	return ret;
}

private String searchPlatzNr(Connection con,int pfNr,int inPZone){
	String      ret = new String();
	try {
		Statement   stmt           = con.createStatement();
		String      sqlString      = new String();
		String      pp_pf_platz    = new String();
		ResultSet   rs;
		if (inPZone == -1){
			sqlString = "SELECT * FROM (SELECT pp_pf_platz FROM plrv11.bde_pufpl WHERE pp_kz='N' AND pp_pfnr="+pfNr+") where ROWNUM<2";
		} else {
			sqlString  = "SELECT * FROM (SELECT pp_pf_platz FROM plrv11.bde_pufpl";
			sqlString += " WHERE pp_kz='N' AND pp_pfnr="+pfNr+" AND pp_pfzone="+inPZone+") where ROWNUM<2";
		}
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()){
			pp_pf_platz = rs.getString("pp_pf_platz");
		}
		ret = pp_pf_platz;//.substring(8,11);
		rs.close();
		stmt.close();
	} catch (SQLException sqlex) {
		log("Fehler beim Lesen: "+sqlex);
		ret = "<br>"+sqlex+"<br>";
	}
	return ret;
}
private String makePlatzNr(int pfNr, int platzNr){
	String ret = new String();
	String pfN = new String();
	String plN = new String();
	pfN = Integer.toString(pfNr);
	plN = Integer.toString(platzNr);
	if (plN.length()==1){
		plN = "00"+Integer.toString(platzNr);
	}
	if (plN.length()==2){
		plN = "0"+Integer.toString(platzNr);
	}
	ret = pfN + plN;
	return ret;
}
private String matRueckMeld(Connection con,int pfNr, int abNr){
	String ret = new String();
	try {
		Statement   stmt           = con.createStatement();
		Statement   stmtMatTyp     = con.createStatement();
		Statement   stmtUpd        = con.createStatement();
		String      sqlString      = new String();
		String      updString      = new String();
		String      selMatTyp      = new String();
		String      matTyp         = new String();
		int         ps_subsys      = 0;
		ResultSet   rs, rsMatTyp;
		//Welche Kommissioniersysteme koennen durch diesen Puffer zurueckgemeldet werden
		sqlString = "SELECT ps_subsys FROM plrv11.plr_pf_subsys WHERE ps_pfnr="+pfNr;
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()){
			ps_subsys = rs.getInt("ps_subsys");
			//Welche Materialtypen gehoeren zu diesem Kommissioniersystem
			selMatTyp = "SELECT pei_ch_wert FROM plrv11.plr_eintrag WHERE pei_body='KOMM-SYS' AND pei_pos='"+ps_subsys+"'";
			stmtMatTyp.executeQuery(selMatTyp);
			rsMatTyp = stmtMatTyp.getResultSet();
			while (rsMatTyp.next()){
				matTyp = rsMatTyp.getString("pei_ch_wert").substring(3);
			}
			rsMatTyp.close();
			//Materialien zurueckmelden
			updString  = "UPDATE plrv11.plr_mat_dat SET mt_rueck_meld='J' WHERE mt_abnr="+abNr;
			updString +=" AND mt_mat_typ IN ("+matTyp+") and mt_abnr in (select ga_abnr from plrv11.plr_ga_dat where ga_status>=10 and ga_abnr="+abNr+")";
			stmtUpd.executeQuery(updString);
		}
		sqlString = "COMMIT";
		stmt.executeQuery(sqlString);
		rs.close();
		stmt.close();
		stmtMatTyp.close();
		stmtUpd.close();
	} catch (SQLException sqlex) {
		log("Fehler beim Lesen: "+sqlex);
		ret += "<br>"+sqlex+"<br>";
	}
	return ret;
}
public String schreibeDatei(int typ, String pfad, String datAnf, int abNr, int fs, int fsStatus) {
	try {
		FileOutputStream ausgabeStrom;
		PrintWriter      ausgabe;
		String dateiName = new String();
		String ret = new String();
		dateiName = datAnf + bdeZeit.getTimestamp("yyMMddHHmmss");
		File datei = new File(pfad + File.separator + dateiName +".bde");
		ausgabeStrom = new FileOutputStream(datei);
		ausgabe      = new PrintWriter(ausgabeStrom);
		String filler = new String("");
		if (abNr < 100000){
			filler = "0";
		}
		if (abNr < 10000){
			filler = "00";
		}
		if (abNr < 1000){
			filler = "000";
		}
		if (abNr < 100){
			filler = "0000";
		}
		//Einlagerungen im Puffer
		if(fs == 10144100||fs == 10245200||fs == 10245400)
			ausgabe.print(abNr+"\n");
		else {
			ret = "0402E001"+bdeZeit.getTimestamp("yyyyMMddHHmmss")+"05590"+filler+abNr+"PF0400000000"+fs+"000"+fsStatus+"N\n";
			ausgabe.print(ret);
			ausgabe.print("0402E999"+bdeZeit.getTimestamp("yyyyMMddHHmmss")+"042999999999904");
		}
		ausgabe.flush();	
		ausgabe.close();
		ausgabeStrom.close();
	} catch (IOException ioe) {
		ioe.printStackTrace();
	}
	return String.valueOf("0");
}
public int getPfErhoeStatus(int inPfNr,Connection inCon) {
	int ret=-1;
	String sql= "SELECT pf_status FROM plrv11.bde_pfdat WHERE pf_pfnr="+inPfNr;
	try {
		Statement stmt = inCon.createStatement();
		ResultSet rs;
		stmt.executeQuery(sql);
		rs = stmt.getResultSet();
		while (rs.next()){
			ret = rs.getInt("pf_status");
		}
		rs.close();
		stmt.close();
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
	return ret;
}
public int getAendIx(int inAbNr,Connection inCon) {
	int ret=-1;
	String sql= "SELECT as_aend_ix FROM plrv11.plr_auftr_status WHERE as_aend_ix<99 and as_abnr="+inAbNr;
	try {
		Statement stmt = inCon.createStatement();
		ResultSet rs;
		stmt.executeQuery(sql);
		rs = stmt.getResultSet();
		while (rs.next()){
			ret = rs.getInt("as_aend_ix");
		}
		rs.close();
		stmt.close();
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
	return ret;
}
public int getAktPfstatus(int inAbNr,int inPfNr,int inAendIx,Connection inCon) {
	int ret=-1;
	String sqlString  = new String("");
	if(inPfNr == 10144200)
		sqlString =  "SELECT abf_pf_status, abf_fs_status FROM plrv11.bde_ab_fs_ma WHERE abf_fs='10144100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+inAendIx;
	else {
		// PBZ läuft hier nur durch, der Pufferstatus ist egal 14.6.07
		if(inPfNr == 10245100 || inPfNr == 10245200 || inPfNr == 10243200)
			sqlString =  "SELECT abf_pf_status, abf_fs_status FROM plrv11.bde_ab_fs_ma WHERE abf_fs='10245100000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+inAendIx;
		else
			sqlString = "SELECT abf_pf_status, abf_fs_status FROM plrv11.bde_ab_fs_ma WHERE abf_fs='"+inPfNr+"000' AND abf_abnr="+inAbNr+" AND abf_aend_ix="+inAendIx;
	}
	try {
		Statement stmt = inCon.createStatement();
		ResultSet rs;
		stmt.executeQuery(sqlString);
		rs = stmt.getResultSet();
		while (rs.next()){
			ret = rs.getInt("abf_pf_status");
			// wann wird der Wert gebraucht?
			// abf_fs_status = rs.getInt("abf_fs_status");
		}
		rs.close();
		stmt.close();
	}
	catch (Exception ex) {
		ex.printStackTrace();
	}
	return ret;
}
}

