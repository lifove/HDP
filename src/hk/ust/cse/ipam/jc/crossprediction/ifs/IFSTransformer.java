package hk.ust.cse.ipam.jc.crossprediction.ifs;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import hk.ust.cse.ipam.utils.FileUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

public class IFSTransformer {

	public static void main(String[] args) {
		new IFSTransformer().run(args);
	}

	private void run(String[] args) {
		String dataPath = args[0];
		String arffName = args[1];
		
		String className = args[2];
		String strBuggyLabel = args[3];
		
		Instances instances = WekaUtils.loadArff(dataPath + File.separator + arffName, className);
		
		// apply z-score normalization (Standardize) for each metric
		instances = WekaUtils.applyStandardize(instances);
		
		// === generating new metric set
		// mode, median, mean, Harmonic mean, minimum, maximum, range (deviation of min and max),
		// Variation ratio (the propostion of cases that are not the mode)
		// First Quartile, third Quartile, Interquartile range, variance, std, coefficient of variation, skewness, Kurtosis
		
		ArrayList<Attribute> atts = new ArrayList<Attribute>();
		ArrayList<Instance> newInstances = new ArrayList<Instance>();
		
		String[] attrName = {"mode","median","mean","harmonicMean","minimum","maximum",
							"range","variationRatio","FirstQuartile","ThirdQuartile","InterquatileRange",
							"Variance","std","coeffVariation","skewness","kurtosis",WekaUtils.labelName};
		
		int numInstances = instances.numInstances();
		
		double[][] data = new double[numInstances][attrName.length];
		
		// fill data from instances
		for(int instIdx=0; instIdx < numInstances;instIdx++){
			int buggyValueIdx = WekaUtils.getClassValueIndex(instances, strBuggyLabel);
			
			int instanceValueIdx = 0;
			// check instance label value
			if(buggyValueIdx != instances.get(instIdx).classValue())
				instanceValueIdx = 1;
			
			data[instIdx] = getNewMetrics(instances.get(instIdx),attrName.length,instanceValueIdx);
		}
		
		for(int dim = 0; dim < attrName.length; dim++)
		{
			Attribute current = new Attribute(attrName[dim], dim);
			
			// for label attribute
			if(attrName[dim].equals(WekaUtils.labelName)){
				// add class label
				ArrayList<String> labels = new ArrayList<String>();
				labels.add(WekaUtils.strPos); // buggy value index is 0
				labels.add(WekaUtils.strNeg); // clean value index is 1
				
				current = new Attribute(WekaUtils.labelName, labels);
			}
			
			// create instances in the beginning
		    if(dim == 0)
		    {
		        for(int obj = 0; obj < numInstances; obj++)
		        {
		        	newInstances.add(new DenseInstance(attrName.length));
		        }
		    }

		    // assign instance values
		    for(int obj = 0; obj < numInstances; obj++)
		    {
		    	newInstances.get(obj).setValue(dim, data[obj][dim]);
		    }

		    atts.add(current);
		}

		Instances newDataset = new Instances("Dataset", atts, instances.size());

		for(Instance inst : newInstances)
		    newDataset.add(inst);
		
		FileUtil.writeAFile(newDataset.toString(),dataPath + File.separator + "ifs_" + arffName);
		
	}

	private double[] getNewMetrics(Instance instance,int numNewAttributes,double labelIdxValue) {
		
		double[] allValuesIncludingLabel = instance.toDoubleArray();
		double[] values = new double[allValuesIncludingLabel.length-1];
		
		// set values without a label value
		System.arraycopy( allValuesIncludingLabel, 0, values, 0, allValuesIncludingLabel.length-1 );
		
		double[] newValues = new double[numNewAttributes];
		
		DescriptiveStatistics stat = new DescriptiveStatistics(values);
		
		// (1) mode: The value that occurs most frequently in a population
		newValues[0] = StatUtils.mode(values)[0];
		
		// (2) median
		newValues[1] = stat.getPercentile(50);
		
		// (3) mean
		newValues[2] = stat.getMean();
		
		// (4) harmonicMean
		newValues[3] = WekaUtils.harmonicMean(values);
		
		// (5) minimum
		newValues[4] = stat.getMin();
		
		// (6) maximum
		newValues[5] = stat.getMax();
		
		// (7) range
		newValues[6] = stat.getMax()-stat.getMin();
		
		// (8) variationRatio
		newValues[7] = 1.0 - StatUtils.mode(values)[0]/(stat.getN());
		
		// (9) FirstQuartile
		newValues[8] = stat.getPercentile(25);
		
		// (10) ThirdQuartile
		newValues[9] = stat.getPercentile(75);
		
		// (11) InterquatileRange
		newValues[10] = stat.getPercentile(75) - stat.getPercentile(25);
		
		// (12) Variance
		newValues[11] = stat.getPopulationVariance();
		
		// (13) std
		newValues[12] = stat.getStandardDeviation();
		
		// (14) coeffVariation
		newValues[13] = stat.getStandardDeviation()/stat.getMean();
		
		// (15) skewness
		newValues[14] = stat.getSkewness();
		
		// (16) kurtosis
		newValues[15] = stat.getKurtosis();
		
		// (17) WekaUtils.labelName
		newValues[16] = labelIdxValue;
		
		return newValues;
	}

}
