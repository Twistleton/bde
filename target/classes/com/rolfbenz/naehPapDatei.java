package com.rolfbenz;

import java.io.*;
import java.sql.*;

public class naehPapDatei extends basisDatei {
	// Eigenschaften
	Connection con;

	// Konstruktoren
 	public naehPapDatei() {
	}
    	public naehPapDatei(String iPfad) {
		super(iPfad);		
	}
	public naehPapDatei(String iPfad, String iDateiname, String iEndung) {
		super(iPfad,iDateiname,iEndung);
	}
	public naehPapDatei(File iPfad) {
		super(iPfad);
	}

	// Methoden
	public void setDb(bdeDb iBdeDb) {
		con = iBdeDb.getConnection();	
	}
	public int schonGedruckt(int iAbNr) {
     	int anzGedruckt=0;
		String sqlString;
		ResultSet rs;
		try  {
			Statement stmt = con.createStatement();
			sqlString = new String("");
			sqlString  = "SELECT COUNT(*)";
			sqlString += " FROM plrv11.bde_ap_druck WHERE apd_abnr="+iAbNr;
			sqlString += " AND apd_werk=10 AND apd_kst=243 AND apd_belegnr=7 AND apd_wdh_kz='N'";
			stmt.execute(sqlString);
			rs = stmt.getResultSet();
			rs.next();
			anzGedruckt = rs.getInt(1);
		}
		catch(SQLException sqlex) {
			sqlex.printStackTrace();
		}
		return anzGedruckt;
	}
	public int  druckeDatei(bdeConfigDatei iConfig,int iAbNr,int iPNr, String iPufPlatzNr) {
		int ret;
          ret =this.schonGedruckt(iAbNr);
		this.schreibeDatei(iAbNr,iPNr,iPufPlatzNr);
		if (ret==0) {
			try {
				Process p = Runtime.getRuntime().exec("lpr -P"+iConfig.getNaeDrucker()+" "+iConfig.getNapDir()+"/"+this.getName());
			}
			catch(IOException ioe) {
				ioe.printStackTrace();
			}		

		}
		return ret;
	}

	public void schreibeDatei(int iAbNr,int iPNr,String iPufPlatzNr) {
		super.oeffnen();		
         	String sqlString = new String("");
		String sqlApa    = new String("");
		try {
			Statement stmt = con.createStatement();
			Statement stmtApa = con.createStatement();
			ResultSet rs;
			ResultSet rsApa;
			
			try {
				ausgabe.write("ABNA2");
				ausgabe.write("\n");

				// Auftrags-Kopf-Daten
				sqlString = new String();
				sqlString =  "SELECT ako_abnr,ako_aend_ix,ako_prog,ako_fabt_pps,ako_prior,ako_se,ako_fabt_zuschn,";
				sqlString += "ako_kz_pg,ako_sf_hw_auftr,sko_fabt,sko_uhrzeit ";
				sqlString += " FROM plrv11.plr_auftr_kopf,plrv11.plr_auftr_status,plrv11.plr_subsys_kopf ";
				sqlString += " WHERE ako_abnr="+iAbNr+" AND ako_abnr=as_abnr AND ako_aend_ix=as_aend_ix AND as_status<>99 AND ";
				sqlString += " sko_abnr=ako_abnr AND sko_aend_ix=as_aend_ix and sko_logsys=4 and sko_subsys=4"; // SSN Termin 
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();	
				while (rs.next()) {
					ausgabe.write("AKO");
					ausgabe.write(rbTextFormat.format(' ',6,rs.getString("ako_abnr")));
					ausgabe.write(rbTextFormat.format(' ',4,rs.getString("ako_fabt_pps")));
					ausgabe.write(rbTextFormat.format('0',3,rs.getString("ako_prog")));
					ausgabe.write(rbTextFormat.format(' ',2,rs.getString("ako_prior")));
					ausgabe.write(rbTextFormat.format(' ',2,rs.getString("ako_se")));
					ausgabe.write(rbTextFormat.format(' ',4,rs.getString("ako_fabt_zuschn")));
					ausgabe.write(rbTextFormat.format(' ',1,rs.getString("ako_kz_pg")));
					ausgabe.write(rbTextFormat.format(' ',1,rs.getString("ako_sf_hw_auftr")));
					ausgabe.write(rbTextFormat.format(' ',4,rs.getString("sko_fabt")));
					ausgabe.write(rbTextFormat.format(' ',4,rs.getString("sko_uhrzeit")));
					ausgabe.write("\n");
				}

				// Auftrags-Sondertexte
				sqlString = "SELECT akt_abnr,akt_text_pos,akt_text FROM plrv11.plr_ak_texte,plrv11.plr_auftr_status ";
				sqlString +="WHERE akt_abnr="+iAbNr+" AND akt_abnr=as_abnr AND akt_aend_ix=as_aend_ix AND as_status<>99 AND ";
				sqlString +="akt_text_art=5 ORDER BY akt_text_pos"; // Textart 5 einschränken und Sortieren nach Positionsnr
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					ausgabe.write("AKT");
					ausgabe.write(rbTextFormat.format(' ',6,rs.getString("akt_abnr")));
					ausgabe.write(rbTextFormat.format(' ',2,rs.getString("akt_text_pos")));
					ausgabe.write(rbTextFormat.linksb(' ',50,rs.getString("akt_text")));
					ausgabe.write("\n");
				}				

				// Transportinformation
				sqlString = "SELECT ma_nname FROM plrv11.bde_madat WHERE ma_pnr="+iPNr;
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					ausgabe.write("APF");
					ausgabe.write(rbTextFormat.format(' ',6,iAbNr));
					ausgabe.write(rbTextFormat.format(' ',11,iPufPlatzNr));
					ausgabe.write(rbTextFormat.format(' ',4,iPNr));
					ausgabe.write(rbTextFormat.linksb(' ',24,rs.getString("ma_nname")));
					ausgabe.write("\n");
				}
				
				// Lohninformation
				sqlString  = "SELECT abf_abnr,abf_dezi,abf_fs,a.att_Bez,b.att_Bez ";
				sqlString += "FROM plrv11.bde_ab_fs_ma,plrv11.plr_attrAttr a,plrv11.plr_auftr_status,plrv11.plr_attrAttr b ";
				sqlString += "WHERE abf_abnr="+iAbNr+" AND abf_abnr=as_abnr AND abf_aend_ix=as_aend_ix AND as_status<>99 AND ";
				sqlString += "a.att_tabname='abtlg' AND a.att_attr=substr(abf_fs,3,3) ";
				sqlString += "AND abf_fs between '10242999000' and  '10243999000' ";
				sqlString += "AND b.att_tabname='fs' AND b.att_attr=abf_fs ORDER BY abf_fs desc";
				stmt.executeQuery(sqlString);
				rs = stmt.getResultSet();
				while (rs.next()) {
					ausgabe.write("ABF");
					ausgabe.write(rbTextFormat.format(' ',6,iAbNr));
					ausgabe.write(rbTextFormat.linksb(' ',24,rs.getString(4)));
					ausgabe.write(rbTextFormat.format(' ',7,rs.getString("abf_dezi")));
					ausgabe.write(rbTextFormat.format(' ',11,rs.getString("abf_fs")));		
					ausgabe.write(rbTextFormat.linksb(' ',24,rs.getString(5)));
					ausgabe.write("\n");

					// Positionsarbeitsgaenge
					sqlApa  = "SELECT apz_abnr,apz_posnr,apz_subsys,apz_agnr,apz_lfdnr,apz_asn,";
					sqlApa += "apz_ort,apz_tr,apz_te,apz_ag_text,apz_tmix_pos,apz_tmix_ss,";
					sqlApa += "apz_tmix_fs,apz_tmix_fstext ";
					sqlApa += "FROM plrv11.plr_bez_pos ";
					sqlApa += "INNER JOIN plrv11.plr_auftr_status ON apz_abnr=as_abnr AND apz_aend_ix=as_aend_ix ";
					sqlApa += "WHERE apz_abnr="+iAbNr;
					sqlApa += "AND as_status<>99 ";
					sqlApa += "AND apz_tmix_ss = 5 ";
					sqlApa += "AND apz_tmix_fs="+rs.getString("abf_fs");
					sqlApa += "ORDER BY apz_tmix_pos,apz_posnr ";
					rsApa = stmtApa.executeQuery(sqlApa);
					while (rsApa.next()) {
						ausgabe.write("APA");
						ausgabe.write(rbTextFormat.format(' ', 6,iAbNr));
						ausgabe.write(rbTextFormat.format(' ', 2,rsApa.getString("apz_posnr")));
						ausgabe.write(rbTextFormat.format(' ', 2,rsApa.getString("apz_subsys")));
						ausgabe.write(rbTextFormat.format(' ', 3,rsApa.getString("apz_agnr")));
						ausgabe.write(rbTextFormat.format(' ', 3,rsApa.getString("apz_lfdnr")));
						ausgabe.write(rbTextFormat.format(' ', 3,rsApa.getString("apz_asn")));
						ausgabe.write(rbTextFormat.format(' ',11,rsApa.getString("apz_ort")));
						ausgabe.write(rbTextFormat.format(' ', 5,rsApa.getString("apz_tr")));
						ausgabe.write(rbTextFormat.format(' ', 5,rsApa.getString("apz_te")));
						ausgabe.write(rbTextFormat.format(' ',24,rsApa.getString("apz_ag_text")));
						ausgabe.write(rbTextFormat.format(' ', 3,rsApa.getString("apz_tmix_pos")));
						ausgabe.write(rbTextFormat.format(' ', 2,rsApa.getString("apz_tmix_ss")));
						ausgabe.write(rbTextFormat.format(' ',11,rsApa.getString("apz_tmix_fs")));
						ausgabe.write(rbTextFormat.format(' ',24,rsApa.getString("apz_tmix_fstext")));
						ausgabe.write("\n");
					}
				}
				// Eintragung in Druck-Tabelle vornehmen
				sqlString = "INSERT INTO plrv11.bde_ap_druck (apd_abnr,apd_werk,apd_kst,apd_belegnr,apd_wdh_kz) ";
				sqlString = "VALUES ("+iAbNr+",10,243,7,'N')";
				stmt.execute(sqlString);
			}
			catch (Exception e) {
				e.printStackTrace();	
			}		
			super.schliessen();
			super.setNameTimestamp();
			super.aktiviereDatei();					
		}
		catch (SQLException sqlex) {
			sqlex.printStackTrace();
		}		
	}		
}
