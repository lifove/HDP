package net.lifove.hdp;

import net.lifove.hdp.util.Utils;
import net.lifove.hdp.util.Utils.FeatureSelectors;
import weka.core.Instances;

public class MetricSelector {
	Instances selectedinstances;
	FeatureSelectors fSelector;
	public MetricSelector(Instances source, FeatureSelectors fSelector){
		this.fSelector = fSelector;
		Instances instances = new Instances(source);
		// select 15% of metrics
		int numSelectedMetrics = (int) ((int)(instances.numAttributes()-1)*0.15);
		if(numSelectedMetrics==0){
			numSelectedMetrics=1;
		}
		selectedinstances = Utils.featureSelection(instances,numSelectedMetrics,fSelector);//featrueSelectionByChiSquare(instances,numSelectedMetrics)
	}

	public Instances getNewInstances(){
		return selectedinstances;
	}
}
