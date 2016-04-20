package hk.ust.cse.ipam.jc.crossprediction.data;

import java.util.ArrayList;

public class Measures {
	ArrayList<Double> precisions, recalls, fmeasures, fmeasuresOnVarThresholds, AUPRCs, AUCs, MCCs, pds, pfs, bals;
	ArrayList<Double> precisionsFTHD, recallsFTHD, fmeasuresFTHD;
	
	public Measures(){
		precisions = new ArrayList<Double>();
		recalls = new ArrayList<Double>();
		fmeasures = new ArrayList<Double>();
		fmeasuresOnVarThresholds = new ArrayList<Double>();
		AUPRCs = new ArrayList<Double>();
		AUCs = new ArrayList<Double>();
		MCCs = new ArrayList<Double>();
		pds = new ArrayList<Double>();
		pfs = new ArrayList<Double>();
		bals = new ArrayList<Double>();
		precisionsFTHD = new ArrayList<Double>();
		recallsFTHD = new ArrayList<Double>();
		fmeasuresFTHD = new ArrayList<Double>();
	}

	public void setAllMeasures(double precision,double recall, double fmeasure,double fmeasureOnVarThresholds,double AUPRC,double AUC,double MCC, double pd,double pf,double bal){
		precisions.add(precision);
		recalls.add(recall);
		fmeasures.add(fmeasure);
		fmeasuresOnVarThresholds.add(fmeasureOnVarThresholds);
		AUPRCs.add(AUPRC);
		AUCs.add(AUC);
		MCCs.add(MCC);
		pds.add(pd);
		pfs.add(pf);
		bals.add(bal);
	}
	
	public void setAllMeasures(double precision,double recall, double fmeasure,double fmeasureOnVarThresholds,double AUPRC,double AUC,double MCC, double pd,double pf,double bal,
			double precisionFTHD, double recallFTHD, double fmeasureFTHD){
		precisions.add(precision);
		recalls.add(recall);
		fmeasures.add(fmeasure);
		fmeasuresOnVarThresholds.add(fmeasureOnVarThresholds);
		AUPRCs.add(AUPRC);
		AUCs.add(AUC);
		MCCs.add(MCC);
		pds.add(pd);
		pfs.add(pf);
		bals.add(bal);
		
		precisionsFTHD.add(precisionFTHD);
		recallsFTHD.add(recallFTHD);
		fmeasuresFTHD.add(fmeasureFTHD);
		
	}
	
	public void setPrecision(double precision) {
		precisions.add(precision);
	}
	
	public void setRecall(double recall) {
		recalls.add(recall);
	}
	
	public void setFmeasure(double fmeasure) {
		fmeasures.add(fmeasure);
	}
	
	public void setPrecisionFTHD(double precision) {
		precisionsFTHD.add(precision);
	}
	
	public void setRecallFTHD(double recall) {
		recallsFTHD.add(recall);
	}
	
	public void setFmeasureFTHD(double fmeasure) {
		fmeasuresFTHD.add(fmeasure);
	}
	
	public void setFmeasureByVarThresholds(double fmeasure) {
		fmeasuresOnVarThresholds.add(fmeasure);
	}
	
	public void setAUPRC(double AUPRC) {
		AUPRCs.add(AUPRC);
	}

	public void setAUC(double AUC) {
		AUCs.add(AUC);
	}
	
	public void setMCC(double MCC) {
		AUCs.add(MCC);
	}

	public void setPd(double pd) {
		pds.add(pd);
	}

	public void setPf(double pf) {
		pfs.add(pf);
	}

	public void setBal(double bal) {
		bals.add(bal);
	}
	
	public ArrayList<Double> getPrecisions() {
		return precisions;
	}
	
	public ArrayList<Double> getRecalls() {
		return recalls;
	}
	public ArrayList<Double> getFmeasures() {
		return fmeasures;
	}
	
	public ArrayList<Double> getPrecisionsFTHD() {
		return precisionsFTHD;
	}
	
	public ArrayList<Double> getRecallsFTHD() {
		return recallsFTHD;
	}
	public ArrayList<Double> getFmeasuresFTHD() {
		return fmeasuresFTHD;
	}
	
	public ArrayList<Double> getFmeasureOnVarThresholds() {
		return fmeasuresOnVarThresholds;
	}
	
	public ArrayList<Double> getAUPRCs() {
		return AUPRCs;
	}
	
	public ArrayList<Double> getAUCs() {
		return AUCs;
	}
	
	public ArrayList<Double> getMCCs() {
		return MCCs;
	}

	public ArrayList<Double> getPds() {
		return pds;
	}

	public ArrayList<Double> getPfs() {
		return pfs;
	}

	public ArrayList<Double> getBals() {
		return bals;
	}
}
