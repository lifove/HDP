package hk.ust.cse.ipam.jc.crossprediction.data;

import hk.ust.cse.ipam.jc.crossprediction.CoOccurrenceAnalyzer;

import java.util.ArrayList;
import java.util.HashMap;

import weka.core.Instances;

public class SharedDataForAnalyzer {
	
	ArrayList<CoOccurrenceAnalyzer> coOccAnalyzers;
	
	public Instances getTargetInstnaces(int i) {
		return coOccAnalyzers.get(i).targetInstances;
	}

	public ArrayList<Integer> getIndexOfLabeledTargetInstances(int i) {
		return coOccAnalyzers.get(i).indexOfLabeledTargetInstances;
	}

	public SharedDataForAnalyzer(ArrayList<CoOccurrenceAnalyzer> analyzers){
		coOccAnalyzers = analyzers;
	}
	
	public ArrayList<MatchedAttribute> getAllMatchedAttributes(int i) {
		return coOccAnalyzers.get(i).allMatchedAttributes;
	}
}
