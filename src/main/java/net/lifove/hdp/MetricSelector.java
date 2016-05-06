package net.lifove.hdp;

import weka.core.Instances;

public class MetricSelector {
	Instances instances;
	
	public MetricSelector(Instances source){
		instances = source;
	}
	
	public Instances getNewInstances(){
		return instances;
	}
}
