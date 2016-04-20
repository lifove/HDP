package hk.ust.cse.ipam.jc.crossprediction.data;

import java.util.ArrayList;

public class MatchedAttribute implements Comparable<MatchedAttribute> {
	int sourceAttrIndex;
	int targetAttrIndex;
	double matchingScore;
	ArrayList<Double> listOfMultipleMatchingScore = new ArrayList<Double>();
	
	@Override
	public int compareTo(MatchedAttribute that) {

		if(!Double.isNaN(this.matchingScore) && Double.isNaN(that.matchingScore))
			return -1;
		
		if(Double.isNaN(this.matchingScore) && !Double.isNaN(that.matchingScore))
			return 1;
		
		if(Double.isNaN(this.matchingScore) && Double.isNaN(that.matchingScore))
			return 0;
		
		if (this.matchingScore > that.matchingScore)
			return -1;
		else if (this.matchingScore < that.matchingScore)
			return 1;
		return 0;
	}
	
	public void finalizeMatchingScoreByAverage(){
		double sum=0.0;
		for(Double value:listOfMultipleMatchingScore)
			sum = sum + value;
		
		matchingScore = sum/listOfMultipleMatchingScore.size();
	}
	
	public ArrayList<Double> getListOfMultipleMatchingScore(){
		return listOfMultipleMatchingScore;
	}
	
	public MatchedAttribute(int sourceAttrIndex,int targetAttrIndex){
		this.sourceAttrIndex = sourceAttrIndex;
		this.targetAttrIndex = targetAttrIndex;
		this.matchingScore = -1; // default value, this constructor uses only for finally selected matched attributes
	}
	
	public MatchedAttribute(int sourceAttrIndex,int targetAttrIndex,double matchingScore){
		this.sourceAttrIndex = sourceAttrIndex;
		this.targetAttrIndex = targetAttrIndex;
		this.matchingScore = matchingScore;
	}

	public int getSourceAttrIndex() {
		return sourceAttrIndex;
	}

	public int getTargetAttrIndex() {
		return targetAttrIndex;
	}

	public double getMatchingScore() {
		return matchingScore;
	}
	
	public void setMatchingScore(double value) {
		matchingScore = value;
	}
}
