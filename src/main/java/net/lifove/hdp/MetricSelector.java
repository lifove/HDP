package net.lifove.hdp;

import net.lifove.hdp.util.Utils;
import weka.core.Instances;

public class MetricSelector {
	Instances instances;
	
	public MetricSelector(Instances source){
		instances = source;
	}
	
	public Instances getNewInstances(){
		// select 15% of metrics
		int numSelectedMetrics = (int) ((int)(instances.numAttributes()-1)*0.15);
		if(numSelectedMetrics==0){
			numSelectedMetrics=1;
		}

		return Utils.featrueSelectionBySignificanceAttributeEval(instances,numSelectedMetrics);//featrueSelectionByChiSquare(instances,numSelectedMetrics);
	}
}
