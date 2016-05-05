package net.lifove.hdp;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;

public class KSAnalyzer implements Runnable {
	
	String matchingID;
	double matchingScore = 0.0;
	double[] sourceMetric,targetMetric;
	
	public KSAnalyzer(double[] sourceMetric, double[] targetMetric, String mathingID) {
		this.matchingID=mathingID;
		this.sourceMetric = sourceMetric;
		this.targetMetric = targetMetric;
	}

	@Override
	public void run() {
		matchingScore = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(sourceMetric, targetMetric);
	}
	
	public String getMachingID(){
		return matchingID;
	}
	
	public double getMatchingScore(){
		return matchingScore;
	}
}
