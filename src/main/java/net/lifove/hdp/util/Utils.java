package net.lifove.hdp.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import net.lifove.hdp.MetricSelector;
import net.lifove.hdp.util.Utils.FeatureSelectors;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.GainRatioAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.attributeSelection.SignificanceAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Utils {
	
	public static String doCrossPrediction(Instances source,Instances target,String strPos,String mlAlg,boolean applyFeatureSelection,FeatureSelectors fSelector){
		String result = "";
		
		int posClassValueIndex = source.attribute(source.classIndex()).indexOfValue(strPos);
		try {
			Classifier classifier = (Classifier) weka.core.Utils.forName(Classifier.class, mlAlg, null);
			
			if(applyFeatureSelection)
				source = new MetricSelector(source,fSelector).getNewInstances();

			classifier.buildClassifier(source);
			
			Evaluation eval = new Evaluation(source);
			eval.evaluateModel(classifier, target);
			
			result = eval.precision(posClassValueIndex) + "," + eval.recall(posClassValueIndex) + "," +
					eval.fMeasure(posClassValueIndex) + "," + eval.areaUnderROC(posClassValueIndex);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * Load Instances from arff file. Last attribute will be set as class attribute
	 * @param path arff file path
	 * @return Instances
	 */
	public static Instances loadArff(String path,String classAttributeName){
		Instances instances=null;
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(path));
			instances = new Instances(reader);
			reader.close();
			instances.setClassIndex(instances.attribute(classAttributeName).index());
		} catch (NullPointerException e) {
			System.err.println("Class label name, " + classAttributeName + ", does not exist! Please, check if the label name is correct.");
			instances = null;
		} catch (FileNotFoundException e) {
			System.err.println("Data file, " +path + ", does not exist. Please, check the path again!");
		} catch (IOException e) {
			System.err.println("I/O error! Please, try again!");
		}

		return instances;
	}
	
	static public ArrayList<String> getLines(String file,boolean removeHeader){
		ArrayList<String> lines = new ArrayList<String>();
		String thisLine="";
		//Open the file for reading
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				lines.add(thisLine);
			} // end while 
			br.close();
		} // end try
		catch (IOException e) {
			System.err.println("Error: " + e);
			//System.exit(0);
		}
		
		if(removeHeader)
			lines.remove(0);
		
		return lines;
	}
	
	
	public static enum FeatureSelectors {
		ChiSquare,
		Significance,
		GainRatio,
		RelieF,
		None
	}
	
	static public Instances featureSelection(Instances data,int numSelected,FeatureSelectors fSelector){
		
		if(fSelector.equals(FeatureSelectors.ChiSquare))
			return featrueSelectionByChiSquare(data,numSelected);
		else if(fSelector.equals(FeatureSelectors.Significance))
			return featrueSelectionBySignificanceAttributeEval(data,numSelected);
		else if(fSelector.equals(FeatureSelectors.RelieF))
			return featrueSelectionByRelieF(data,numSelected);
		else if(fSelector.equals(FeatureSelectors.GainRatio))
			return featrueSelectionByGainRatio(data,numSelected);
		
		return data;
	}
	
	/**
	 * Select features based on chi-squared attribute selection
	 * @param data instances
	 * @param numSelected the number features to be selected.
	 * @return
	 */
	static public Instances featrueSelectionBySignificanceAttributeEval(Instances data,int numSelected){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		SignificanceAttributeEval eval = new SignificanceAttributeEval();
		Ranker search = new Ranker();
		//search.setThreshold(-1.7976931348623157E308);
		search.setNumToSelect(numSelected);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}
	
	/**
	 * Select features based on chi-squared attribute selection
	 * @param data instances
	 * @param numSelected the number features to be selected.
	 * @return
	 */
	static public Instances featrueSelectionByChiSquare(Instances data,int numSelected){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		ChiSquaredAttributeEval eval = new ChiSquaredAttributeEval();
		Ranker search = new Ranker();
		//search.setThreshold(-1.7976931348623157E308);
		search.setNumToSelect(numSelected);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}
	
	/**
	 * Select features based on RelieF attribute selection
	 * @param data instances
	 * @param numSelected the number features to be selected.
	 * @return
	 */
	static public Instances featrueSelectionByRelieF(Instances data,int numSelected){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		ReliefFAttributeEval eval = new ReliefFAttributeEval();
		Ranker search = new Ranker();
		//search.setThreshold(-1.7976931348623157E308);
		search.setNumToSelect(numSelected);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}
	
	/**
	 * Select features based on GainRatio attribute selection
	 * @param data instances
	 * @param numSelected the number features to be selected.
	 * @return
	 */
	static public Instances featrueSelectionByGainRatio(Instances data,int numSelected){
		Instances newData = null;

		AttributeSelection filter = new AttributeSelection();  // package weka.filters.supervised.attribute!
		GainRatioAttributeEval eval = new GainRatioAttributeEval();
		Ranker search = new Ranker();
		//search.setThreshold(-1.7976931348623157E308);
		search.setNumToSelect(numSelected);
		filter.setEvaluator(eval);
		filter.setSearch(search);
		try {
			filter.setInputFormat(data);

			// generate new data
			newData = Filter.useFilter(data, filter);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return newData;
	}
	
	/**
	 * Print prediction result of each instance
	 * @param target
	 * @param classifier
	 * @throws Exception
	 */
	static public void printPredictionResultForEachInstance(Instances target, Classifier classifier) throws Exception {
		// show prediction result for each instance
		for(int instIdx = 0; instIdx < target.numInstances(); instIdx++){
			double predictedLabelIdx = classifier.classifyInstance(target.get(instIdx));
				System.out.println("HDP: Instance " + (instIdx+1) + " predicted as " + 
						target.classAttribute().value((int)predictedLabelIdx) +
						" (Actual: " + target.instance(instIdx).stringValue(target.classIndex()) + ")");
		}
	}
	
	/**
	 * Get new instances based on matched metrics
	 * @param instances
	 * @param matchedAttrs
	 * @param isSource
	 * @param labelName
	 * @param labelPos
	 * @return
	 */
	static public Instances getNewInstancesByMatchedMetrics(Instances instances, ArrayList<String> matchedAttrs,boolean isSource,
			String labelName,String labelPos){
		ArrayList<String> matchedAttributes = matchedAttrs;
		
		// create attribute information
		ArrayList<Attribute> attributes = createAttributeInfoForClassfication(matchedAttributes.size()+1); //for label +1
		Instances newInstnaces = new Instances("newData", attributes, 0);
		
		for(Instance instance:instances){

			double[] vals = new double[attributes.size()];
			
			// process attribute values except for label
			for(int i=0; i<attributes.size()-1;i++){
				String[] matchedMetricsInfo = matchedAttributes.get(i).split("\\(");
				String[] labels = matchedMetricsInfo[0].split("-");
				int[] intLabels = {Integer.parseInt(labels[0]),Integer.parseInt(labels[1])};
				vals[i] = isSource?instance.value(intLabels[0]):instance.value(intLabels[1]);
			}
			// assign label value
			String currentInstaceLabel = instance.stringValue(instances.attribute(labelName));
			if(currentInstaceLabel.equals(labelPos))
				vals[attributes.size()-1] = Utils.dblPosValue;
			else if(currentInstaceLabel.equals(getNegLabel(instances,labelPos)))
				vals[attributes.size()-1] = Utils.dblNegValue;
			else
				vals[attributes.size()-1] = Double.NaN;
			
			newInstnaces.add(new DenseInstance(1.0, vals));
		}
		
		newInstnaces.setClass(newInstnaces.attribute(Utils.labelName));
		
		return newInstnaces;
	}
	
	/**
	 * Get the negative label string value from the positive label value
	 * @param instances
	 * @param positiveLabel
	 * @return
	 */
	static public String getNegLabel(Instances instances, String positiveLabel){
		if(instances.classAttribute().numValues()==2){
			int posIndex = instances.classAttribute().indexOfValue(positiveLabel);
			if(posIndex==0)
				return instances.classAttribute().value(1);
			else
				return instances.classAttribute().value(0);
		}
		else{
			System.err.println("Class labels must be binary");
			System.exit(0);
		}
		return null;
	}
	
	/**
	 * Generate new instances by random labeling
	 * @param instances
	 * @return new instances with randomly labeled
	 */
	public static Instances randomizeLabeling(Instances instances){

		Instances randomlyLabeledData = new Instances(instances);

		double[] lableValues = new double[instances.numInstances()];

		
		// get label values
		for(int instIdx=0;instIdx<instances.numInstances();instIdx++){// ignore label attribute
			lableValues[instIdx] = instances.instance(instIdx).classValue();
		}

		shuffleArray(lableValues);

		for(int instIdx=0;instIdx<instances.numInstances();instIdx++){// ignore label attribute
			randomlyLabeledData.instance(instIdx).setValue(instances.classAttribute(), lableValues[instIdx]);
		}

		return randomlyLabeledData;
	}
	
	/**
	 * Implementing Fisher-Yates shuffle
	 * @param ar double array to be shuffled
	 */
	static void shuffleArray(double[] ar)
	{
		Random rnd = new Random();
		for (int i = ar.length - 1; i >= 0; i--)
		{
			int index = rnd.nextInt(i + 1);
			// Simple swap
			double a = ar[index];
			ar[index] = ar[i];
			ar[i] = a;
		}
	}
	
	/**
	 * Create a list of attributes for the given number of attributes
	 * @param numOfAttributes The number of attributes to create
	 * @return ArrayList of Attribute
	 */
	static public ArrayList<Attribute> createAttributeInfoForClassfication(long numOfAttributes){
		// create attribute information
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();

		// add attributes from matchedAttribute
		for(int i=0; i<numOfAttributes-1;i++){
			Attribute attribute = new Attribute("attr" + (i+1));
			attributes.add(attribute);
		}

		//add label as the last attribute
		ArrayList<String> labels = new ArrayList<String>();
		labels.add(strPos);
		labels.add(strNeg);
		Attribute label = new Attribute(labelName, labels);
		attributes.add(label);

		dblPosValue = attributes.get(attributes.size()-1).indexOfValue(strPos);
		dblNegValue = attributes.get(attributes.size()-1).indexOfValue(strNeg);

		return attributes;
	}
	
	/** String value of the positive label */
	static final public String strPos = "buggy";
	/** String value of the negative label */
	static final public String strNeg = "clean";
	/** String value of label attribute */
	static final public String labelName = "label";
	/** double value of the positive label */
	static public double dblPosValue = 0;
	/** double value of the positive label */
	static public double dblNegValue = 1;
}
