package com.rolfbenz;

public class pen {
    private int aktivRot;
    private int aktivGruen;
    private int aktivBlau;

    private int inaktivRot;
    private int inaktivGruen;
    private int inaktivBlau;
    public pen() {	

    }    
    public pen(int iARot, int iAGruen, int iABlau,int iIRot,int iIGruen,int iIBlau) {
	aktivRot   = iARot;
	aktivGruen = iAGruen;
	aktivBlau  = iABlau;

        inaktivRot   = iIRot;
	inaktivGruen = iIGruen;
	inaktivBlau  = iIBlau;
    }
    public String getHtmlColor() {
	return(textDatei.format('0',2,Integer.toHexString(aktivRot))+
	       textDatei.format('0',2,Integer.toHexString(aktivGruen))+
	       textDatei.format('0',2,Integer.toHexString(aktivBlau)));	
    }
    public void change() {
	int zr,zg,zb;
	zr=aktivRot;
	zg=aktivGruen;
	zb=aktivBlau;
	
	aktivRot     = inaktivRot;
	aktivGruen   = inaktivGruen;
	aktivBlau    = inaktivBlau;

	inaktivRot   = zr;
	inaktivGruen = zg;
	inaktivBlau  = zb;	
    }
}
