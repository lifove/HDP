package hk.ust.cse.ipam.jc.crossprediction.data;

public class Measure {
	double precision, recall, fmeasure, avgFmeasureOnVarThresholds,PRC, AUC, MCC, pd, pf, bal;
	
	public Measure(double precision,double recall,double fmeasure,double avgFmeasureOnVarThresholds, double PRC,double AUC, double MCC, double pd,double pf,double bal){
		this.precision = precision;
		this.recall = recall;
		this.fmeasure = fmeasure;
		this.avgFmeasureOnVarThresholds = avgFmeasureOnVarThresholds;
		this.PRC = PRC;
		this.AUC = AUC;
		this.MCC = MCC;
		this.pd = pd;
		this.pf = pf;
		this.bal = bal;
	}

	public double getPrecision() {
		return precision;
	}
	public double getRecall() {
		return recall;
	}
	public double getFmeasure() {
		return fmeasure;
	}

	public double getAUC() {
		return AUC;
	}

	public double getPd() {
		return pd;
	}

	public double getPf() {
		return pf;
	}

	public double getBal() {
		return bal;
	}

	public double getFmeasureOnVarThresholds() {
		return avgFmeasureOnVarThresholds;
	}

	public double getPRC() {
		return PRC;
	}

	public double getMCC() {
		// TODO Auto-generated method stub
		return MCC;
	}
	
}
