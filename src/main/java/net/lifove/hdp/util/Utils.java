package net.lifove.hdp.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import weka.filters.supervised.attribute.AttributeSelection;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class Utils {
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
	 * Print prediction result of each instance
	 * @param target
	 * @param classifier
	 * @throws Exception
	 */
	static public void printPredictionResultForEachInstance(Instances target, Classifier classifier) throws Exception {
		// show prediction result for each instance
		for(int instIdx = 0; instIdx < target.numInstances(); instIdx++){
			double predictedLabelIdx = classifier.classifyInstance(target.get(instIdx));
				System.out.println("HDP: Instance " + (instIdx+1) + " predicted as, " + 
						target.classAttribute().value((int)predictedLabelIdx));
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
				String[] labels = matchedAttributes.get(i).split("-");
				int[] intLabels = {Integer.parseInt(labels[0]),Integer.parseInt(labels[1])};
				vals[i] = isSource?instance.value(intLabels[0]):instance.value(intLabels[1]);
			}
			// assign label value
			String currentInstaceLabel = instance.stringValue(instances.attribute(labelName));
			if(currentInstaceLabel.equals(labelPos))
				vals[attributes.size()-1] = Utils.dblPosValue;
			else if(currentInstaceLabel.equals(getNegLabel(instances,labelPos)))
				vals[attributes.size()-1] = Utils.dblNegValue;
			
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
