package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import hk.ust.cse.ipam.utils.FileUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

public class RandomDatasetGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new RandomDatasetGenerator().run(args);
	}

	int numRandomRounds = 1000;
	
	private void run(String[] args) {
		String dataPath = args[0];
		String projectName = args[1];
		String filePath = dataPath + File.separator + projectName +".arff";
		String posLab = args[2];
		String classAttributeName = args[3];
		
		Instances originalDataset = WekaUtils.loadArff(filePath,classAttributeName);
		int numPosInstances = originalDataset.attributeStats(originalDataset.classIndex()).nominalCounts[WekaUtils.getClassValueIndex(originalDataset, posLab)];
		
		int numAttributesForRandomDataset = originalDataset.numAttributes()-1;
		
		for(int round = 0; round < numRandomRounds;round++){
			ArrayList<Attribute> attributes = WekaUtils.createAttributeInfoForClassfication(numAttributesForRandomDataset+1); //for label +1
			Instances randomInstances = new Instances("RandomDataset", attributes, 0);
			
			double[] maxValues = getMaxValuesOfAttributes(originalDataset);
			double[] minValues = getMinValuesOfAttributes(originalDataset);
			
			for(int count=0;count<originalDataset.numInstances();count++){
				// values for an instance
				double[] values = new double[attributes.size()];
				for(int i=0; i<attributes.size()-1;i++){
					values[i] = randomValue(maxValues[i],minValues[i]);
				}
				// assign label value
				if(count<numPosInstances)
					values[attributes.size()-1] = WekaUtils.dblPosValue;
				else
					values[attributes.size()-1] = WekaUtils.dblNegValue;
	
				randomInstances.add(new DenseInstance(1.0, values));
			}
			
			// apply Feature Selection
			//randomInstances = WekaUtils.featrueSelectionByCfsSubsetEval(randomInstances);
			
			FileUtil.writeAFile(randomInstances.toString(), dataPath + File.separator + projectName + "_random_" + String.format("%04d", (round+1)) + ".arff");
		}
	}

	private double[] getMinValuesOfAttributes(Instances originalDataset) {
		double[] values = new double[originalDataset.numAttributes()-1];
		
		for(int i=0;i<originalDataset.numAttributes()-1;i++){
			if(i==originalDataset.classIndex()){
				i--;
				continue;
			}
			values[i] = originalDataset.attributeStats(i).numericStats.min;
		}
		
		return values;
	}

	private double[] getMaxValuesOfAttributes(Instances originalDataset) {
		double[] values = new double[originalDataset.numAttributes()-1];
		
		for(int i=0;i<originalDataset.numAttributes()-1;i++){
			if(i==originalDataset.classIndex()){
				i--;
				continue;
			}
			values[i] = originalDataset.attributeStats(i).numericStats.max;
		}
		
		return values;
	}

	private double randomValue(double rangeMax, double rangeMin) {
		Random r = new Random();
		return rangeMin + (rangeMax - rangeMin) * r.nextDouble();
	}
}
