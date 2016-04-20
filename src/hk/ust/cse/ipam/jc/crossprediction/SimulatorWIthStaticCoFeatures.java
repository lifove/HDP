package hk.ust.cse.ipam.jc.crossprediction;

import hk.ust.cse.ipam.jc.crossprediction.CoOccurrenceAnalyzer.DataKind;
import hk.ust.cse.ipam.jc.crossprediction.data.SharedDataForAnalyzer;
import hk.ust.cse.ipam.jc.crossprediction.data.MatchedAttribute;
import hk.ust.cse.ipam.jc.crossprediction.data.Measure;
import hk.ust.cse.ipam.jc.crossprediction.data.Measures;
import hk.ust.cse.ipam.utils.ArrayListUtil;
import hk.ust.cse.ipam.utils.FileUtil;
import hk.ust.cse.ipam.utils.SimpleCrossPredictor;
import hk.ust.cse.ipam.utils.WekaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.primitives.Doubles;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.Prediction;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class SimulatorWIthStaticCoFeatures implements Runnable{

	Options options;
	
	boolean SAVENEWDATAFILES = false;
	String pathToSaveNewData = "data" + File.separator + "cross" + File.separator;
	
	boolean DISPLAYDETAILED = false;
	boolean RUNINCREMENTALTEST = false;
	boolean RANDOMALAIYZER = false;
	boolean SRCWITHIN = true;
	
	int repeat = 500;
	int folds = 2;
	boolean useTargetLabeledInstances = false;
	static boolean useFeatureSelection = false;
	static boolean applyFilterManually = false;
	static boolean saveFoldPredictionResults = false;

	public static HashMap<String,Instances> instancesKept = new HashMap<String,Instances>();
	public static HashMap<String,SharedDataForAnalyzer> analyzersKept = new HashMap<String,SharedDataForAnalyzer>(); // key: sourcepath + targetpath + analyzers

	Instances sourceInstances,targetInstances,newSourceInstances,newTargetInstances;
	String sourcePath;
	String sourceProjectName;
	String sourceLabelName;
	String sourceLabelPos;

	String targetPath;
	String targetProjectName;
	String targetLabelName;
	String targetLabelPos;
	String coFeatures;
	String coOccurrenceAnalyzerOption;
	String strClassifier;
	
	ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
	
	public static HashMap<String,Measure> withinResults = new HashMap<String,Measure>();

	boolean header=false;
	// change this option to apply cutoff
	boolean applycutoff = false;
	double cutoff = 0.0;
	
	int predictionID = 1;
	
	//MannWhitneyUTest statTest = new MannWhitneyUTest();

	RConnection rServe;
	
	/**
	 * @param args
	 */
	public SimulatorWIthStaticCoFeatures(String[] args,String coFeatures,boolean saveNewData, boolean verbose,int folds,int repeat){
		// load command line options
		options = createOptions();
		
		if(args.length < options.getOptions().size()){
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "CrossPredictionSimulator", options );
		}

		parseOptions(options, args);
		
		this.coFeatures = coFeatures;
		matchedAttributes = getMatchedAttributesFromStringCoFeatures(coFeatures);
		
		this.folds = folds;
		this.repeat = repeat;
	
		SAVENEWDATAFILES = saveNewData;
		
		sourceProjectName = sourcePath.substring(sourcePath.lastIndexOf("/")+1).replace(".arff", "");
		targetProjectName = targetPath.substring(targetPath.lastIndexOf("/")+1).replace(".arff", "");
		predictionInfo = sourceProjectName + ">>" +
				targetProjectName;
		
		DISPLAYDETAILED = verbose;
	}
	
	public void run(){
		
		targetInstances = loadArff2(targetPath,false,sourceLabelName,targetLabelName,sourcePath,targetPath);
		sourceInstances = loadArff2(sourcePath,true,sourceLabelName,targetLabelName,sourcePath,targetPath);
		
		if(applyFilterManually)
			matchedAttributes = CoOccurrenceAnalyzer.applyFilters(targetInstances, sourceInstances, matchedAttributes, sourceLabelPos,true,true);
				
		
		// normalize data set
		//System.err.println("Normalization applied: Min-max normalization!!!");
		//sourceInstances = WekaUtils.applyNormalize(sourceInstances);
		//targetInstances = WekaUtils.applyNormalize(targetInstances);
		//System.err.println("Normalization applied: Log Filter!!!");
		//sourceInstances = WekaUtils.applyNormalizeByLog(sourceInstances);
		//targetInstances = WekaUtils.applyNormalizeByLog(targetInstances);
		
		if(useFeatureSelection==true){
			System.err.println("FEATURE SELECTION CfsSubsetEval " + predictionInfo);
			sourceInstances = WekaUtils.featrueSelectionByCfsSubsetEval(sourceInstances);
		}

		// conduct experiments
		if(header){
			
			System.out.println("Type,No,Group,Prediction,Source,Target,Analyzer,Classifier," +
					"ItrtCount,#OriSourceInstnaces,#OriTargetInstances," +
					"#TargetInstacesForWithin,#NewSourceInst.,#NewBuggySourceInst.,#NewTargetInst.,#NewBuggyTargetInst.," +
					"#OriSrcAttributes,#OriTarAttributes,#SrcSelectedFeatures," +
					"#CoOcfeatures,CoOcFeatrues,MaxMatchingScore," +
					"SourceWithinPrecision,TargetWithinPrecision,CrossPrecision,CrossPrecisionFTHD," +
					"SourceWithinRecall,TargetWithinRecall,CrossRecall,CrossRecallFTHD," +
					"SourceWithinF,TargetWithinF,CrossF,CrossFmeasureFTHD,CrossFRA,FWin,FP-value,FWinFTHD,FP-valueFTHD,FWinRA,FP-valueRA,FWinRACross,FP-valueRACross,FImproved," +
					"TargetWithinFOnVarThresholds, CrossFOnVarThresholds,TargetWithinAUPRC,CrossAUPRC," +
					"SourceWithinAUC, TargetWithinAUC,CrossAUC,CrossAUCRA,AUCWin,AUCP-value,AUCWinRA,AUCP-valueRA,AUCWinRACross,AUCP-valueRACross,AUCImproved," +
					"SourceWithinMCC, TargetWithinMCC,CrossMCC,CrossMCCRA,MCCWin,MCCP-value,MCCWinRA,MCCP-valueRA,MCCWinRACross,MCCP-valueRACross,MCCImproved," +
					"SourceWithinPD,TargetWihinPD,CrossPD,PDWin,PDImproved," +
					"SourceWithinPF,TargetWihinPF,CrossPF,PFWIn,PFImproved," +
					"SourceWithinBal,TargetWihinBal,CrossBal,CrossBalRA,BalWin,BalP-value,BalWinRA,BalP-valueRA,BalWinRACross,BalP-valueRACross,BalImproved");
		}
		//runPrediction();
		try {
			rServe = new RConnection();
			runPredictionOnSameTestSet();
			rServe.close();
		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.println("library(Rserve)");
			System.err.println("Rserve(args=\"--no-save\")");
			System.exit(0);
		}

		// display/save results
	}

	public synchronized static void processCounter(String predInfo,String coOccurrenceAnalyzerOpt, double cutoffValue,String classifier) {
			Driver.predictionProcessed++;
			System.err.println("Finished!" + predInfo +  " " + coOccurrenceAnalyzerOpt + "_" + cutoffValue + "," + " on " + classifier);
			System.err.println("predictionProcessed=" + Driver.predictionProcessed +"\r");
	}

	String predictionInfo = "";

	private double medianWFOnVarThresholds;
	/*void runPrediction(){
		int numSourceInstances = sourceInstances.numInstances();
		int numTargetInstances = targetInstances.numInstances();
		int numAttributesByFeatureSelection = coOccurAnalyzers.numOfAttibutesByFeatureSelection;
		int numCoOccurFeautres = newSourceInstances.numAttributes()-1; // except for a label attribute
		double sourceWithinF = 0.0;
		double targetWithinF = 0.0;
		double crossF = 0.0;

		double sourceWithinAUC = 0.0;
		double targetWithinAUC = 0.0;
		double crossAUC = 0.0;

		double sourceWithinPD = 0.0;
		double targetWithinPD = 0.0;
		double crossPD = 0.0;

		double sourceWithinPF = 0.0;
		double targetWithinPF = 0.0;
		double crossPF = 0.0;

		double sourceWithinBal = 0.0;
		double targetWithinBal = 0.0;
		double crossBal = 0.0;

		Classifier classifier = null;
		// prepare classifier
		try {
			
			int posClassIndex = WekaUtils.getClassValueIndex(sourceInstances, sourceLabelPos);

			// repeat 50
			Evaluation eval = null;
			
			int repeat = 50;
			
			if(withinResults.containsKey(sourcePath)){
				Measure withinResult = withinResults.get(sourcePath);
				sourceWithinF = withinResult.getFmeasure();
				sourceWithinAUC = withinResult.getAUC();
				sourceWithinPD = withinResult.getPd();
				sourceWithinPF = withinResult.getPf();
				sourceWithinBal = withinResult.getBal();
			}
			else{
				double sumSourceWithinF = 0.0;
				double sumSourceWithinAUC = 0.0;
				double sumSourceWithinPD = 0.0;
				double sumSourceWithinPF = 0.0;
				double sumSourceWithinBal = 0.0;
				
				// Perform 2-fold cross-validation on source.
				for(int i=1; i<=repeat;i++){
					
					eval = WekaUtils.nfoldCrossValidation(strClassifier, 2, sourceInstances, sourceLabelName, i);
						
					sumSourceWithinF += eval.fMeasure(posClassIndex);
					sumSourceWithinAUC += eval.areaUnderROC(posClassIndex);
					sumSourceWithinPD += eval.truePositiveRate(posClassIndex);
					sumSourceWithinPF += eval.falsePositiveRate(posClassIndex);
					sumSourceWithinBal += WekaUtils.getBalance(eval.truePositiveRate(posClassIndex), eval.falsePositiveRate(posClassIndex));
				}
				
				sourceWithinF = sumSourceWithinF/repeat;
				sourceWithinAUC = sumSourceWithinAUC/repeat;
				sourceWithinPD = sumSourceWithinPD/repeat;
				sourceWithinPF = sumSourceWithinPF/repeat;
				sourceWithinBal = sumSourceWithinBal/repeat;
				
				// put result in withinResults HashMap
				withinResults.put(sourcePath,new Measure(sourceWithinF,sourceWithinAUC,sourceWithinPD,sourceWithinPF,sourceWithinBal));
				
			}
			
			
			if(withinResults.containsKey(targetPath)){
				Measure withinResult = withinResults.get(targetPath);
				targetWithinF = withinResult.getFmeasure();
				targetWithinAUC = withinResult.getAUC();
				targetWithinPD = withinResult.getPd();
				targetWithinPF = withinResult.getPf();
				targetWithinBal = withinResult.getBal();
			}
			else{
				double sumTargetWithinF = 0.0;
				double sumTargetWithinAUC = 0.0;
				double sumTargetWithinPD = 0.0;
				double sumTargetWithinPF = 0.0;
				double sumTargetWithinBal = 0.0;
				
				// Perform 2-fold cross-validation on target..
				for(int i=1; i<=repeat;i++){
					eval = WekaUtils.nfoldCrossValidation(strClassifier, 2, targetInstances, targetLabelName, i);

					sumTargetWithinF += eval.fMeasure(posClassIndex);
					sumTargetWithinAUC += eval.areaUnderROC(posClassIndex);
					sumTargetWithinPD += eval.truePositiveRate(posClassIndex);
					sumTargetWithinPF += eval.falsePositiveRate(posClassIndex);
					sumTargetWithinBal += WekaUtils.getBalance(eval.truePositiveRate(posClassIndex), eval.falsePositiveRate(posClassIndex));
				}
				
				targetWithinF = sumTargetWithinF/repeat;
				targetWithinAUC = sumTargetWithinAUC/repeat;
				targetWithinPD = sumTargetWithinPD/repeat;
				targetWithinPF = sumTargetWithinPF/repeat;
				targetWithinBal = sumTargetWithinBal/repeat;
				
				// put result in withinResults HashMap
				withinResults.put(targetPath,new Measure(targetWithinF,targetWithinAUC,targetWithinPD,targetWithinPF,targetWithinBal));
			}

			if(numCoOccurFeautres!=0){
				// Perform cross-domain prediction
				classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
				newSourceInstances.setClass(newSourceInstances.attribute(WekaUtils.labelName));
				classifier.buildClassifier(newSourceInstances);
				newTargetInstances.setClass(newTargetInstances.attribute(WekaUtils.labelName));
				eval = new Evaluation(newSourceInstances);
				eval.evaluateModel(classifier, newTargetInstances);
				posClassIndex = WekaUtils.getClassValueIndex(newSourceInstances, WekaUtils.strPos);
				crossF =  eval.fMeasure(posClassIndex);
				crossAUC = eval.areaUnderROC(posClassIndex);
				crossPD = eval.truePositiveRate(posClassIndex);
				crossPF = eval.falsePositiveRate(posClassIndex);
				crossBal = WekaUtils.getBalance(crossPD, crossPF);
			}
			else{
				crossF = Double.NaN;
				crossAUC = Double.NaN;
				crossPD = Double.NaN;
				crossPF = Double.NaN;
				crossBal = Double.NaN;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		String cutoff = applycutoff==true?coOccurAnalyzers.cutoff+"":"NOCUTOFF";
		
		DecimalFormat dec = new DecimalFormat("0.00");
		
		System.out.println(predictionInfo + "," +
				coOccurrenceAnalyzerOption + "_" + dec.format(cutoff) + "," + 
				strClassifier + "," + 
				numSourceInstances + "," +
				numTargetInstances + "," + 
				numAttributesByFeatureSelection + "," +
				numCoOccurFeautres + "," +
				sourceWithinF + "," + 
				targetWithinF + "," +
				crossF + "," + 
				"(" + (crossF-targetWithinF) + ")," +
				sourceWithinAUC + "," + 
				targetWithinAUC + "," +
				crossAUC + "," +
				"(" + (crossAUC-targetWithinAUC) + ")," +
				sourceWithinPD + "," + 
				targetWithinPD + "," +
				crossPD + "," +
				"(" + (crossPD-targetWithinPD)+")," +
				sourceWithinPF + "," + 
				targetWithinPF + "," +
				crossPF + "," +
				"(" + (crossPF-targetWithinPF) +")," +
				sourceWithinBal + "," + 
				targetWithinBal + "," +
				crossBal + "," +
				"(" + (crossBal-targetWithinBal) + ")"
				);

	}*/

	Instances getNewInstancesByCoFeatrues(Instances instances, ArrayList<MatchedAttribute> matchedAttrs,DataKind dataKind,String labelName,String labelPos){
		ArrayList<MatchedAttribute> matchedAttributes = matchedAttrs;
		
		// create attribute information
		ArrayList<Attribute> attributes = WekaUtils.createAttributeInfoForClassfication(matchedAttributes.size()+1); //for label +1
		Instances newInstnaces = new Instances("newData", attributes, 0);
		
		for(Instance instance:instances){

			double[] vals = new double[attributes.size()];
			
			// process attribute values except for label
			for(int i=0; i<attributes.size()-1;i++){
				vals[i] = dataKind==DataKind.SOURCE?instance.value(matchedAttributes.get(i).getSourceAttrIndex()):instance.value(matchedAttributes.get(i).getTargetAttrIndex());
			}
			// assign label value
			String currentInstaceLabel = instance.stringValue(instances.attribute(labelName));
			if(currentInstaceLabel.equals(labelPos))
				vals[attributes.size()-1] = WekaUtils.dblPosValue;
			else
				vals[attributes.size()-1] = WekaUtils.dblNegValue;
			
			newInstnaces.add(new DenseInstance(1.0, vals));
		}
		
		newInstnaces.setClass(newInstnaces.attribute(WekaUtils.labelName));
		
		return newInstnaces;
	}

	private ArrayList<MatchedAttribute> getMatchedAttributesFromStringCoFeatures(
			String coFeatures) {
		ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
		
		if(coFeatures == null)
			return matchedAttributes;
		
		String[] strMatchedInfo = coFeatures.split(",");
		for(String info:strMatchedInfo){
			if(info.equals(","))
				break;
			String[] splitString = info.split("\\|");
			int sourceAttIndex = Integer.parseInt(splitString[0].split("-")[0]);
			int targetAttIndex = Integer.parseInt(splitString[0].split("-")[1]);
			double score = Double.parseDouble(splitString[1]);
			
			if(score<=cutoff)
				continue;
			
			matchedAttributes.add(new MatchedAttribute(sourceAttIndex,targetAttIndex,score));
		}
		return matchedAttributes;
	}
	
	void runPredictionOnSameTestSet(){
		String sourceName = sourcePath.substring(sourcePath.lastIndexOf("/")+1).replace(".arff", "");
		String targetName = targetPath.substring(targetPath.lastIndexOf("/")+1).replace(".arff", "");
		String predictionInfo = sourceName + ">>" + targetName;
		String sourceGroupName = sourcePath.split(File.separator)[1];

		int numOfSourceInstances = -1;
		int numOfBuggySourceInstances = -1;
		int numOfTargetInstances = -1;
		int numOfBuggyTargetInstances = -1;
		int numOfSourceAttributes = sourceInstances.numAttributes();
		int numOfTargetAttributes = targetInstances.numAttributes();
		int numCoOccurFeautres = -1; // except for a label attribute		
		
		int actualIterationNum = 0;
		
		// prepare classifier
		try {
			//System.err.println("Doit3!" + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);
			Classifier classifierForSourceWithin = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			Classifier classifierForTargetWithin = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			Classifier classifierForSource = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			Classifier classfierForRandomAnalyzer = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			int posClassIndex;
			
			Measures srcWithinResults = new Measures();
			Measures tarWithinResults = new Measures();
			Measures crossResults = new Measures();
			Measures crossResultsByRandomAnalyzer = new Measures();
			
			DecimalFormat dec = new DecimalFormat("0.00");
			
			// if the number of buggy instance is less than 10, 10-fold CV is not working some folds where there are no buggy instances and return NaN fo AUC
			// in this case, we repeat our experiments by the same number of folds*repeat
			/*int numOfBuggyInstancesInTarget = newTargetInstances.attributeStats(newTargetInstances.classAttribute().index()).nominalCounts[WekaUtils.getClassValueIndex(newTargetInstances, WekaUtils.strPos)];
			int finalRepeat = repeat;
			if(numOfBuggyInstancesInTarget<folds)
				finalRepeat = (int)Math.ceil((repeat*folds)/(double)numOfBuggyInstancesInTarget);*/
			
			//System.out.println(numOfBuggyInstancesInTarget);
			//System.out.println((repeat*folds));
			
			//String[] options = weka.core.Utils.splitOptions("-D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5");
			
			//BayesNet bn = new BayesNet();
			//bn.setOptions(options);
			
			//classifierForSource = (Classifier) bn;
			//System.err.println("Doit!4" + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);
			
			//System.err.println("Doit!5" + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);
			
			
			Instances srcData = null;
			Instances tarData = null;
			int numAttributesByFeatureSelection=0;
			
			srcData = new Instances(sourceInstances);
			tarData = new Instances(targetInstances);  
			
			newSourceInstances = getNewInstancesByCoFeatrues(sourceInstances, matchedAttributes, DataKind.SOURCE, sourceLabelName, sourceLabelPos);
			newTargetInstances = getNewInstancesByCoFeatrues(targetInstances, matchedAttributes, DataKind.TARGET, targetLabelName, targetLabelPos);

			numOfSourceInstances = newSourceInstances.numInstances();
			numOfBuggySourceInstances = WekaUtils.getNumInstancesByClass(newSourceInstances, WekaUtils.strPos);
			numOfTargetInstances = newTargetInstances.numInstances();
			numOfBuggyTargetInstances = WekaUtils.getNumInstancesByClass(newTargetInstances, WekaUtils.strPos);
			numCoOccurFeautres = newSourceInstances.numAttributes()-1; // except for a label attribute		
			
			//----------------------------------------
			// if it is 0, no need to proceed, just displayinfo
			String cutoff = applycutoff==true?dec.format(this.cutoff)+"":"NOCUTOFF";
			if (numCoOccurFeautres==0){
				System.out.println("A" + "," + sourceGroupName + "," + predictionID + "," + predictionInfo + "," + sourceName + "," + targetName + "," + 
						coOccurrenceAnalyzerOption + "_" + cutoff + "," + 
						strClassifier + "," + 
						actualIterationNum + "," +
						sourceInstances.numInstances() + "," +
						targetInstances.numInstances() + "," +
						tarData.numInstances() + "," + // actual num of instances for target within
						numOfSourceInstances + "," +
						numOfTargetInstances + "," +
						(numOfSourceAttributes-1) + "," +	// except for label
						(numOfTargetAttributes-1) + "," +	// except for label
						numAttributesByFeatureSelection + "," +
						numCoOccurFeautres + "," +
						getCoOccurFeatures(matchedAttributes));
				
				processCounter(predictionInfo,coOccurrenceAnalyzerOption,this.cutoff,strClassifier);
				
				return;
			}
			
			if(SAVENEWDATAFILES){
				// projectname_to_projectname_analyzername_cutoff_S.arff
				WekaUtils.writeADataFile(newSourceInstances, pathToSaveNewData + sourceProjectName + "_to_" + targetProjectName + "_" + coOccurrenceAnalyzerOption + "_" + cutoff + "_S.arff");
				// projectname_to_projectname_analyzername_cutoff_S.arff
				WekaUtils.writeADataFile(newTargetInstances, pathToSaveNewData + sourceProjectName + "_to_" + targetProjectName + "_" + coOccurrenceAnalyzerOption + "_" + cutoff + "_T.arff");
			}
			
			// build source classifier once
			Instances newSrcData = new Instances(newSourceInstances);
			classifierForSource.buildClassifier(newSrcData);
			
			if(folds==1){
				runCrossPrediction(predictionInfo,newSourceInstances,newTargetInstances,strClassifier,WekaUtils.strPos);
				return;				
			}
			
			for(int i=0; i<repeat; i++){

				// randomize with different seed for each iteration
				srcData.randomize(new Random(i)); 
				srcData.stratify(folds);

				// randomize with different seed for each iteration
				tarData.randomize(new Random(i)); 
				tarData.stratify(folds);
				
				for(int n=0;n < folds;n++){
					
					actualIterationNum++;

					String keyForSavedWithinResults = sourcePath + "_" + strClassifier + "_" + i + "_" + n;
					
					double srcPrecision;
					double srcRecall;
					double srcF;
					double medianSrcFOnVarThreshold;
					double srcPRC;
					double srcAUC;
					double srcMCC;
					double srcPD;
					double srcPF;
					double srcBal;
					if(withinResults.containsKey(keyForSavedWithinResults)){
						Measure withinResult = withinResults.get(keyForSavedWithinResults);
						srcPrecision = withinResult.getPrecision();
						srcRecall = withinResult.getRecall();
						srcF = withinResult.getFmeasure();
						medianSrcFOnVarThreshold = withinResult.getFmeasureOnVarThresholds();
						srcPRC = withinResult.getPRC();
						srcAUC = withinResult.getAUC();
						srcMCC = withinResult.getMCC();
						srcPD = withinResult.getPd();
						srcPF = withinResult.getPf();
						srcBal = withinResult.getBal();
					}
					else{
						
						if(SRCWITHIN){
							// source-within
							Instances srcTrain = srcData.trainCV(folds, n);
							Instances srcTest = srcData.testCV(folds, n);
							
							// build and evaluate classifier
							classifierForSourceWithin.buildClassifier(srcTrain);
							Evaluation srcEval = new Evaluation(sourceInstances);
							srcEval.evaluateModel(classifierForSourceWithin, srcTest);
							
							posClassIndex = WekaUtils.getClassValueIndex(srcData, sourceLabelPos);
							srcPrecision = srcEval.precision(posClassIndex);
							srcRecall = srcEval.recall(posClassIndex);
							srcF = srcEval.fMeasure(posClassIndex);
							medianSrcFOnVarThreshold = WekaUtils.getAvgFmeasureByVarThresholds(srcEval.predictions(),posClassIndex);
							srcPRC = srcEval.areaUnderPRC(posClassIndex);
							srcAUC = srcEval.areaUnderROC(posClassIndex);
							srcMCC = srcEval.matthewsCorrelationCoefficient(posClassIndex);
							srcPD = srcEval.truePositiveRate(posClassIndex);
							srcPF = srcEval.falsePositiveRate(posClassIndex);
							srcBal = WekaUtils.getBalance(srcPD, srcPF);
						}
						else{
							srcPrecision = Double.NaN;
							srcRecall = Double.NaN;
							srcF = Double.NaN;
							medianSrcFOnVarThreshold = Double.NaN;
							srcPRC = Double.NaN;
							srcAUC = Double.NaN;
							srcMCC = Double.NaN;
							srcPD = Double.NaN;
							srcPF = Double.NaN;
							srcBal = Double.NaN;
						}
						// put result in withinResults HashMap
						if(SRCWITHIN)
							withinResults.put(keyForSavedWithinResults,new Measure(srcPrecision,srcRecall,srcF,medianSrcFOnVarThreshold,srcPRC,srcAUC,srcMCC, srcPD,srcPF,srcBal));
					}

					// target-within, tarTest is used for cross-prediction. That is why it is defined here.
					Instances tarTest = tarData.testCV(folds, n);
					
					keyForSavedWithinResults = targetPath + "_" + strClassifier + "_" + i + "_" + n;
					double tarPrecision,tarRecall,tarF,medianTarFOnVarThreshold,tarPRC,tarAUC,tarMCC,tarPD,tarPF,tarBal;
					if(withinResults.containsKey(keyForSavedWithinResults)){
						Measure withinResult = withinResults.get(keyForSavedWithinResults);
						tarPrecision = withinResult.getPrecision();
						tarRecall = withinResult.getRecall();
						tarF = withinResult.getFmeasure();
						medianTarFOnVarThreshold = withinResult.getFmeasureOnVarThresholds();
						tarPRC = withinResult.getPRC();
						tarAUC = withinResult.getAUC();
						tarMCC = withinResult.getMCC();
						tarPD = withinResult.getPd();
						tarPF = withinResult.getPf();
						tarBal = withinResult.getBal();
					}
					else{
						//TODO
						//System.out.println("!!!" + coOccurAnalyzer.targetInstances);
						
						// TODO 10% target to rest of target
						/*Instances tarTrain = new Instances(targetInstances,0);
						if(coOccurAnalyzers.get(i).indexOfLabeledTargetInstances != null){
							for(int labeledInstanceIndex:coOccurAnalyzers.get(i).indexOfLabeledTargetInstances){
								tarTrain.add(targetInstances.get(labeledInstanceIndex));
							}
						}*/
						
						// TODO rest of target
						//tarData.removeAll(tarTrain);
						//tarTest = tarData;

						Instances tarTrain = tarData.trainCV(folds, n);

						classifierForTargetWithin.buildClassifier(tarTrain);
						// tarEval accumulates each result whenever call evaluateModel. So, always create new object to get one single result in each iteration
						// argument can be tarData or tarTrain or tarTest. That does not affect the result for a single result.
						// But use tarData when Evaluation object takes care of multiple results of n-fold CV.
						Evaluation tarEval = new Evaluation(tarData);
						
						tarEval.evaluateModel(classifierForTargetWithin, tarTest);
						
						posClassIndex = WekaUtils.getClassValueIndex(tarData, targetLabelPos);
						tarPrecision = tarEval.precision(posClassIndex);
						tarRecall = tarEval.recall(posClassIndex);
						tarF = tarEval.fMeasure(posClassIndex);
						medianTarFOnVarThreshold = WekaUtils.getAvgFmeasureByVarThresholds(tarEval.predictions(),posClassIndex);
						tarPRC = tarEval.areaUnderPRC(posClassIndex);
						tarAUC = tarEval.areaUnderROC(posClassIndex);
						tarMCC = tarEval.matthewsCorrelationCoefficient(posClassIndex);
						tarPD = tarEval.truePositiveRate(posClassIndex);
						tarPF = tarEval.falsePositiveRate(posClassIndex);
						tarBal = WekaUtils.getBalance(tarPD, tarPF);
						
						//System.out.println(tarEval.toSummaryString());
						
						// put result in withinResults HashMap
						withinResults.put(keyForSavedWithinResults,new Measure(tarPrecision,tarRecall,tarF,medianTarFOnVarThreshold,tarPRC,tarAUC,tarMCC,tarPD,tarPF,tarBal));
					}

					double crossPrecision = Double.NaN;
					double crossRecall = Double.NaN;
					double crossF = Double.NaN;
					double crossFOnVarThresholds = Double.NaN;
					double crossPRC = Double.NaN;
					double crossMCC = Double.NaN;
					double crossAUC = Double.NaN;
					double crossPD = Double.NaN;
					double crossPF = Double.NaN;
					double crossBal = Double.NaN;
					
					// prediction results using the best f-measure threshold of the training set.
					double crossPrecisionFTHD = Double.NaN;
					double crossRecallFTHD = Double.NaN;
					double crossFmeasureFTHD = Double.NaN;
					double crossMCCFTHD = Double.NaN;
					
					
					double crossPrecisionByRandomAnalyzer = Double.NaN;
					double crossRecallByRandomAnalyzer = Double.NaN;
					double crossFByRandomAnalyzer = Double.NaN;
					double crossAUCByRandomAnalyzer = Double.NaN;
					double crossMCCByRandomAnalyzer = Double.NaN;
					double crossPDByRandomAnalyzer = Double.NaN;
					double crossPFByRandomAnalyzer = Double.NaN;
					double crossBalByRandomAnalyzer = Double.NaN;
					
					posClassIndex = WekaUtils.getClassValueIndex(newSrcData,WekaUtils.strPos);
					
					// to get best threshold from the source
					Evaluation sourceEval = new Evaluation(newSrcData);
					sourceEval.evaluateModel(classifierForSource, newSrcData);
					
					// get Results buy different thresholds
					Instances srcResultsCurve = WekaUtils.getCurve(sourceEval.predictions(), posClassIndex);
					
					// >= threshold (contains the probability threshold that gives rise to the previous performance values.)
					double bestThresholdForFMeasure = WekaUtils.getBestThresholdForFMeasure(srcResultsCurve);
					double bestThresholdPositionForFmeasure = WekaUtils.getBestThresholdPositionForFMeasure(srcResultsCurve);
										
					//FileUtil.print(WekaUtils.getPrecisionRecallFmeasureFromCurve(srcResultsCurve), 0);
					
					//System.out.println("--------");
					
					//target-cross
					//Instances newSrcTrain = newSrcRandData.trainCV(folds, n);
					Instances newTarTest = WekaUtils.getInstancesWithSelectedAttributes(tarTest,
							getMatchedIndice(DataKind.TARGET,matchedAttributes),targetLabelPos);
					Evaluation crossEval = new Evaluation(newSrcData);
					//classifier.buildClassifier(newSrcRandData);
					crossEval.evaluateModel(classifierForSource, newTarTest);
					
					if(saveFoldPredictionResults){
					//if(true){
						System.out.println(predictionInfo + ",=====");
						
						for(int predIdx=0; predIdx<crossEval.predictions().size();predIdx++){
							Prediction pred = (Prediction) crossEval.predictions().get(predIdx);
							System.out.println(predictionInfo + "," + predIdx + "," + classifierForSource.distributionForInstance(newTarTest.get(predIdx))[0] + "," +
									classifierForSource.distributionForInstance(newTarTest.get(predIdx))[1] +
									"," +(pred.actual()==posClassIndex?WekaUtils.strPos:WekaUtils.strNeg) + "," + (pred.predicted()==posClassIndex?WekaUtils.strPos:WekaUtils.strNeg));
						}
					}
					
					if(RUNINCREMENTALTEST)
						incrementalTest(newSrcData,newTarTest);
					
					crossPrecision = crossEval.precision(posClassIndex);
					crossRecall = crossEval.recall(posClassIndex);
					crossF = crossEval.fMeasure(posClassIndex);
					crossFOnVarThresholds = WekaUtils.getAvgFmeasureByVarThresholds(crossEval.predictions(),posClassIndex);
					crossPRC = crossEval.areaUnderPRC(posClassIndex);
					crossAUC = crossEval.areaUnderROC(posClassIndex);
					crossMCC = crossEval.matthewsCorrelationCoefficient(posClassIndex);
					crossPD = crossEval.truePositiveRate(posClassIndex);
					crossPF = crossEval.falsePositiveRate(posClassIndex);
					crossBal = WekaUtils.getBalance(crossPD, crossPF);
					
					// get results by different thresholds
					Instances curve = WekaUtils.getCurve(crossEval.predictions(), posClassIndex);
					
					//System.out.println(curve);
					
					//TP, FN, FP, TN, P, R, F, Threshold
					//String resultsByThreshold = WekaUtils.getPredictionResultsByThreshold(WekaUtils.getPrecisionRecallFmeasureFromCurve(curve),bestThresholdForFMeasure);
					String resultsByThreshold = WekaUtils.getPredictionResultsByThresholdPosition(WekaUtils.getPrecisionRecallFmeasureFromCurve(curve),bestThresholdPositionForFmeasure);
					String[] resultValuesByThreshold = resultsByThreshold.split(",");
					
					crossPrecisionFTHD = Double.parseDouble(resultValuesByThreshold[4]);
					crossRecallFTHD = Double.parseDouble(resultValuesByThreshold[5]);
					crossFmeasureFTHD = Double.parseDouble(resultValuesByThreshold[6]);
					
					//System.out.println(crossPrecisionFTHD + "," + crossRecallFTHD + "," + crossFmeasureFTHD + "," + bestThresholdForFMeasure);
					
					//FileUtil.print(WekaUtils.getPrecisionRecallFmeasureFromCurve(curve), 0);
					
					double[] srcThreholds = srcResultsCurve.attributeToDoubleArray(srcResultsCurve.attribute("Threshold").index());
					double[] tarThreholds = curve.attributeToDoubleArray(curve.attribute("Threshold").index());
					
					double pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(srcThreholds, tarThreholds);
					/*double pValue = Double.NaN; //new KolmogorovSmirnovTest().kolmogorovSmirnovTest(srcThreholds, tarThreholds);
					try {
						rServe.assign("treated", srcThreholds);
						rServe.assign("control", tarThreholds);
						RList l = rServe.eval("ks.test(control,treated,exact=TRUE)").asList();
						pValue = l.at("p.value").asDouble();
					} catch (REngineException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(0);
					} catch (REXPMismatchException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						System.exit(0);
					}*/
					//System.out.println(pValue);
					if(pValue <0.05){
						//System.out.println("an interaction ignored since threshold values have different distribution between source and target!!!");
						actualIterationNum--; // adjust actualIterationNum as we ignore this iteration
						continue;
					}
					
					
					if(RANDOMALAIYZER){
						//---------
						// for random analyzer, build model and test every time with new source and target randomly matched.
						int numMatchedAttributesForRandnomAnalyzer = srcData.numAttributes()>tarTest.numAttributes()?tarTest.numAttributes()-1:srcData.numAttributes()-1;
						ArrayList<MatchedAttribute> matchedAttributesByRandom = CoOccurrenceAnalyzer.matchedAttributeByRandom(srcData, tarTest, 
								numMatchedAttributesForRandnomAnalyzer, srcData.classIndex(), tarTest.classIndex());
						Instances newSrcDataRandomAnlalyzer = getNewInstancesByCoFeatrues(srcData, matchedAttributesByRandom, DataKind.SOURCE, sourceLabelName, sourceLabelPos);
						classfierForRandomAnalyzer.buildClassifier(newSrcDataRandomAnlalyzer);
						Instances newTarTestByRandomAnalyzer = getNewInstancesByCoFeatrues(tarTest, matchedAttributesByRandom, DataKind.TARGET, targetLabelName, targetLabelPos);
						
						Evaluation crossEvalByRandomAnalyzer = new Evaluation(newSrcDataRandomAnlalyzer);
						//classifier.buildClassifier(newSrcRandData);
						crossEvalByRandomAnalyzer.evaluateModel(classfierForRandomAnalyzer, newTarTestByRandomAnalyzer);
						
						crossPrecisionByRandomAnalyzer = crossEvalByRandomAnalyzer.precision(posClassIndex);
						crossRecallByRandomAnalyzer = crossEvalByRandomAnalyzer.recall(posClassIndex);
						crossFByRandomAnalyzer = crossEvalByRandomAnalyzer.fMeasure(posClassIndex);
						crossAUCByRandomAnalyzer = crossEvalByRandomAnalyzer.areaUnderROC(posClassIndex);
						crossMCCByRandomAnalyzer = crossEvalByRandomAnalyzer.matthewsCorrelationCoefficient(posClassIndex);
						crossPDByRandomAnalyzer = crossEvalByRandomAnalyzer.truePositiveRate(posClassIndex);
						crossPFByRandomAnalyzer = crossEvalByRandomAnalyzer.falsePositiveRate(posClassIndex);
						crossBalByRandomAnalyzer = WekaUtils.getBalance(crossPDByRandomAnalyzer, crossPFByRandomAnalyzer);

					}
					
					if(Double.isNaN(crossAUC)){
						continue;
					}
					
					// consider only cases which have at least one featre on new datasets
					// if there is no co-occurrent features, we don't have to include that case in results.
					srcWithinResults.setAllMeasures(srcPrecision,srcRecall,srcF,-1,-1,srcAUC,srcMCC,srcPD,srcPF,srcBal);
					tarWithinResults.setAllMeasures(tarPrecision,tarRecall,tarF,medianTarFOnVarThreshold,tarPRC,tarAUC,tarMCC,tarPD,tarPF,tarBal);
					crossResults.setAllMeasures(crossPrecision,crossRecall,crossF, crossFOnVarThresholds,crossPRC,crossAUC,crossMCC,crossPD, crossPF, crossBal,crossPrecisionFTHD,crossRecallFTHD,crossFmeasureFTHD);
					crossResultsByRandomAnalyzer.setAllMeasures(crossPrecisionByRandomAnalyzer,crossRecallByRandomAnalyzer,crossFByRandomAnalyzer,-1,-1,crossAUCByRandomAnalyzer,crossMCCByRandomAnalyzer, crossPDByRandomAnalyzer, crossPFByRandomAnalyzer, crossBalByRandomAnalyzer);
					
					if(DISPLAYDETAILED){
						System.out.println("D," + i + ";" + n + "," + sourceGroupName +"," + predictionInfo + "," + sourceName + "," + targetName + "," +
								coOccurrenceAnalyzerOption + "_" + cutoff + "," + 
								strClassifier + "," + 
								actualIterationNum + "," +
								sourceInstances.numInstances() + "," +
								targetInstances.numInstances() + "," +
								tarData.numInstances() + "," + // actual num of instances for target within
								numOfSourceInstances + "," +
								numOfBuggySourceInstances + "," +
								numOfTargetInstances + "," +
								numOfBuggyTargetInstances + "," +
								(numOfSourceAttributes-1) + "," +	// except for label
								(numOfTargetAttributes-1) + "," +	// except for label
								numAttributesByFeatureSelection + "," +
								numCoOccurFeautres + "," +
								getCoOccurFeatures(matchedAttributes) + "," + "NA," + //getCoOccurFeatures(coOccurAnalyzers.get(i).matchedAttributeByRandom) + "," +
								srcPrecision + "," +
								tarPrecision + "," + 
								crossPrecision + "," +
								crossPrecisionFTHD + "," +
								srcRecall + "," +
								tarRecall + "," +
								crossRecall + "," +
								crossRecallFTHD + "," +
								srcF + "," +
								tarF + "," +
								crossF + "," +
								crossFmeasureFTHD + "," +
								crossFByRandomAnalyzer + "," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								(crossF-tarF) +"," +
								"-," +
								"-," +
								"-," +
								"-," +
								srcAUC + "," +
								tarAUC + "," +
								crossAUC + "," +
								crossAUCByRandomAnalyzer + "," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								(crossAUC-tarAUC) + "," +
								srcMCC + "," +
								tarMCC + "," +
								crossMCC + "," +
								crossMCCByRandomAnalyzer + "," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								(crossMCC-tarMCC) + "," +
								srcPD + "," +
								tarPD + "," +
								crossPD + "," +
								"-," +
								(crossPD-tarPD) + "," +
								srcPF + "," +
								tarPF + "," +
								crossPF + "," +
								"-," +
								(tarPF - crossPF) + "," +
								srcBal + "," +
								tarBal + "," +
								crossBal + "," +
								crossBalByRandomAnalyzer + "," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								(crossBal - tarBal)
							);
					}
					
					if(actualIterationNum==repeat*folds)
						break;
				}
				if(actualIterationNum==repeat*folds)
					break;
			}
			
			double medianSrcPrecision=Double.NaN, medianTarPrecision=Double.NaN, medianCrossPrecision=Double.NaN,
			medianSrcRecall=Double.NaN, medianTarRecall=Double.NaN, medianCrossRecall=Double.NaN,
			medianSrcF=Double.NaN, medianTarF=Double.NaN, medianCrossF=Double.NaN,
			medianWFOnVarThresholds = Double.NaN, medianCrossFOnVarThresholds=Double.NaN,
			medianWAUPRC=Double.NaN,medianCrossAUPRC=Double.NaN,
			medianSrcAUC=Double.NaN, medianTarAUC=Double.NaN, medianCrossAUC=Double.NaN,
			medianSrcMCC=Double.NaN, medianTarMCC=Double.NaN, medianCrossMCC=Double.NaN,
			medianSrcPD=Double.NaN, medianTarPD=Double.NaN, medianCrossPD=Double.NaN,
			medianSrcPF=Double.NaN, medianTarPF=Double.NaN, medianCrossPF=Double.NaN,
			medianSrcBal=Double.NaN, medianTarBal=Double.NaN, medianCrossBal=Double.NaN;
			
			double medianCrossPrecisionFTHD=Double.NaN, medianCrossRecallFTHD=Double.NaN, medianCrossFmeasureFTHD=Double.NaN;

			double medianCrossFByRandomAnalyzer=Double.NaN,
			medianCrossAUCByRandomAnalyzer=Double.NaN,
			medianCrossMCCByRandomAnalyzer=Double.NaN,
			medianCrossPDByRandomAnalyzer=Double.NaN, medianCrossPFByRandomAnalyzer=Double.NaN,
			medianCrossBalByRandomAnalyzer=Double.NaN;
			
			double pFmeasure=Double.NaN,pFmeasureByRandomAnalyzer=Double.NaN,pFmeasureRAAndCross=Double.NaN;
			String FWinTieLoss="", FWinTieLossByRandomAnalyzer="", FWinTieLossRAAndCross="";
			double pAUC=Double.NaN, pAUCByRandomAnalyzer=Double.NaN, pAUCRAAndCross=Double.NaN;
			String AUCWinTieLoss="", AUCWinTieLossByRandomAnalyzer="", AUCWinTieLossRAAndCross="";
			double pMCC=Double.NaN, pMCCByRandomAnalyzer=Double.NaN, pMCCRAAndCross=Double.NaN;
			String MCCWinTieLoss="", MCCWinTieLossByRandomAnalyzer="", MCCWinTieLossRAAndCross="";
			double pBal=Double.NaN, pBalByRandomAnalyzer=Double.NaN, pBalRAAndCross=Double.NaN;
			String balWinTieLoss="", balWinTieLossByRandomAnalyzer="", balWinTieLossRAAndCross="";
			
			double pFmeasureFTHD=Double.NaN;
			String FWinTieLossFTHD="";
			
			String pdWinTieLoss="", pfWinTieLoss="";

			// statistical test
	
			medianSrcPrecision = ArrayListUtil.getMedian(srcWithinResults.getPrecisions());
			medianTarPrecision = ArrayListUtil.getMedian(tarWithinResults.getPrecisions());
			medianCrossPrecision = ArrayListUtil.getMedian(crossResults.getPrecisions());
			medianSrcRecall = ArrayListUtil.getMedian(srcWithinResults.getRecalls());
			medianTarRecall = ArrayListUtil.getMedian(tarWithinResults.getRecalls());
			medianCrossRecall = ArrayListUtil.getMedian(crossResults.getRecalls());
			medianSrcF = ArrayListUtil.getMedian(srcWithinResults.getFmeasures());
			medianTarF = ArrayListUtil.getMedian(tarWithinResults.getFmeasures());
			medianCrossF = ArrayListUtil.getMedian(crossResults.getFmeasures());
			medianCrossFByRandomAnalyzer = ArrayListUtil.getMedian(crossResultsByRandomAnalyzer.getFmeasures());
			
			medianCrossPrecisionFTHD = ArrayListUtil.getMedian(crossResults.getPrecisionsFTHD());
			medianCrossRecallFTHD = ArrayListUtil.getMedian(crossResults.getRecallsFTHD());
			medianCrossFmeasureFTHD = ArrayListUtil.getMedian(crossResults.getFmeasuresFTHD());
			
			pFmeasure = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getFmeasures()),
									Doubles.toArray(crossResults.getFmeasures()));
			pFmeasureFTHD = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getFmeasures()),
					Doubles.toArray(crossResults.getFmeasuresFTHD()));
			pFmeasureByRandomAnalyzer = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getFmeasures()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getFmeasures()));
			pFmeasureRAAndCross = WilcoxonSignedRankTest(Doubles.toArray(crossResults.getFmeasures()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getFmeasures()));
			
			FWinTieLoss = getWinTieLoss(pFmeasure,medianTarF,medianCrossF);
			FWinTieLossFTHD = getWinTieLoss(pFmeasureFTHD,medianTarF,medianCrossFmeasureFTHD);
			FWinTieLossByRandomAnalyzer = getWinTieLoss(pFmeasureByRandomAnalyzer,medianTarF,medianCrossFByRandomAnalyzer);
			FWinTieLossRAAndCross = getWinTieLoss(pFmeasureRAAndCross,medianCrossFByRandomAnalyzer,medianCrossF);
			
			medianWFOnVarThresholds = ArrayListUtil.getMedian(tarWithinResults.getFmeasureOnVarThresholds());
			medianCrossFOnVarThresholds = ArrayListUtil.getMedian(crossResults.getFmeasureOnVarThresholds());
			
			medianWAUPRC = ArrayListUtil.getMedian(tarWithinResults.getAUPRCs());
			medianCrossAUPRC = ArrayListUtil.getMedian(crossResults.getAUPRCs());
			
			medianSrcAUC = ArrayListUtil.getMedian(srcWithinResults.getAUCs());
			medianTarAUC = ArrayListUtil.getMedian(tarWithinResults.getAUCs());
			medianCrossAUC = ArrayListUtil.getMedian(crossResults.getAUCs());
			medianCrossAUCByRandomAnalyzer = ArrayListUtil.getMedian(crossResultsByRandomAnalyzer.getAUCs());
			
			medianSrcMCC = ArrayListUtil.getMedian(srcWithinResults.getMCCs());
			medianTarMCC = ArrayListUtil.getMedian(tarWithinResults.getMCCs());
			medianCrossMCC = ArrayListUtil.getMedian(crossResults.getMCCs());
			medianCrossMCCByRandomAnalyzer = ArrayListUtil.getMedian(crossResultsByRandomAnalyzer.getMCCs());
			
			pAUC = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getAUCs()),
					Doubles.toArray(crossResults.getAUCs()));
			pAUCByRandomAnalyzer = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getAUCs()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getAUCs()));
			pAUCRAAndCross = WilcoxonSignedRankTest(Doubles.toArray(crossResults.getAUCs()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getAUCs()));
			
			AUCWinTieLoss = getWinTieLoss(pAUC,medianTarAUC,medianCrossAUC);
			AUCWinTieLossByRandomAnalyzer = getWinTieLoss(pAUCByRandomAnalyzer,medianTarAUC,medianCrossAUCByRandomAnalyzer);
			AUCWinTieLossRAAndCross = getWinTieLoss(pAUCRAAndCross,medianCrossAUCByRandomAnalyzer,medianCrossAUC);
			
			pMCC = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getMCCs()),
					Doubles.toArray(crossResults.getMCCs()));
			pMCCByRandomAnalyzer = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getMCCs()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getMCCs()));
			pMCCRAAndCross = WilcoxonSignedRankTest(Doubles.toArray(crossResults.getMCCs()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getMCCs()));
			
			MCCWinTieLoss = getWinTieLoss(pMCC,medianTarMCC,medianCrossMCC);
			MCCWinTieLossByRandomAnalyzer = getWinTieLoss(pMCCByRandomAnalyzer,medianTarMCC,medianCrossMCCByRandomAnalyzer);
			MCCWinTieLossRAAndCross = getWinTieLoss(pMCCRAAndCross,medianCrossMCCByRandomAnalyzer,medianCrossMCC);
			
			medianSrcPD = ArrayListUtil.getMedian(srcWithinResults.getPds());
			medianTarPD = ArrayListUtil.getMedian(tarWithinResults.getPds());
			medianCrossPD = ArrayListUtil.getMedian(crossResults.getPds());
			medianCrossPDByRandomAnalyzer = ArrayListUtil.getMedian(crossResultsByRandomAnalyzer.getPds());
			
			double pPD = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getPds()),
					Doubles.toArray(crossResults.getPds()));
			
			pdWinTieLoss = getWinTieLoss(pPD,medianTarPD,medianCrossPD);
			
			medianSrcPF = ArrayListUtil.getMedian(srcWithinResults.getPfs());
			medianTarPF = ArrayListUtil.getMedian(tarWithinResults.getPfs());
			medianCrossPF = ArrayListUtil.getMedian(crossResults.getPfs());
			medianCrossPFByRandomAnalyzer = ArrayListUtil.getMedian(crossResultsByRandomAnalyzer.getPfs());
			
			double pPF = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getPfs()),
					Doubles.toArray(crossResults.getPfs()));
			
			pfWinTieLoss = getWinTieLoss(pPF,medianTarPF,medianCrossPF);
			
			medianSrcBal = ArrayListUtil.getMedian(srcWithinResults.getBals());
			medianTarBal = ArrayListUtil.getMedian(tarWithinResults.getBals());
			medianCrossBal = ArrayListUtil.getMedian(crossResults.getBals());
			medianCrossBalByRandomAnalyzer = ArrayListUtil.getMedian(crossResultsByRandomAnalyzer.getBals());

			pBal = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getBals()),
					Doubles.toArray(crossResults.getBals()));
			pBalByRandomAnalyzer = WilcoxonSignedRankTest(Doubles.toArray(tarWithinResults.getBals()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getBals()));
			pBalRAAndCross = WilcoxonSignedRankTest(Doubles.toArray(crossResults.getBals()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getBals()));
			
			balWinTieLoss = getWinTieLoss(pBal,medianTarBal,medianCrossBal);
			balWinTieLossByRandomAnalyzer = getWinTieLoss(pBalByRandomAnalyzer,medianTarBal,medianCrossBalByRandomAnalyzer);
			balWinTieLossRAAndCross = getWinTieLoss(pBalRAAndCross,medianCrossBalByRandomAnalyzer,medianCrossBal);
		
			dec = new DecimalFormat("0.00");
			
			System.out.println("A" + "," + sourceGroupName + "," + predictionID + "," + predictionInfo + "," + sourceName + "," + targetName + "," +
				coOccurrenceAnalyzerOption + "_" + cutoff + "," + 
				strClassifier + "," + 
				actualIterationNum + "," +
				sourceInstances.numInstances() + "," +
				targetInstances.numInstances() + "," +
				tarData.numInstances() + "," + // actual num of instances for target within
				numOfSourceInstances + "," +
				numOfBuggySourceInstances + "," +
				numOfTargetInstances + "," +
				numOfBuggyTargetInstances + "," +
				(numOfSourceAttributes-1) + "," +	// except for label
				(numOfTargetAttributes-1) + "," +	// except for label
				numAttributesByFeatureSelection + "," +
				numCoOccurFeautres + "," +
				getCoOccurFeatures(matchedAttributes) + "," +
				getMaxCoFeatureScore(matchedAttributes) + "," +
				medianSrcPrecision + "," +
				medianTarPrecision + "," +
				medianCrossPrecision + "," +
				medianCrossPrecisionFTHD + "," +
				medianSrcRecall + "," +
				medianTarRecall + "," +
				medianCrossRecall + "," +
				medianCrossRecallFTHD + "," +
				medianSrcF + "," +
				medianTarF + "," +
				medianCrossF + "," +
				medianCrossFmeasureFTHD + "," +
				medianCrossFByRandomAnalyzer + "," +
				FWinTieLoss + "," +
				pFmeasure + "," +
				FWinTieLossFTHD + "," +
				pFmeasureFTHD + "," +
				FWinTieLossByRandomAnalyzer + "," +
				pFmeasureByRandomAnalyzer + "," +
				FWinTieLossRAAndCross + "," +
				pFmeasureRAAndCross + "," +
				(medianCrossF-medianTarF) +"," +
				medianWFOnVarThresholds + "," +
				medianCrossFOnVarThresholds + "," +
				medianWAUPRC + "," +
				medianCrossAUPRC + "," +
				medianSrcAUC + "," +
				medianTarAUC + "," +
				medianCrossAUC + "," +
				medianCrossAUCByRandomAnalyzer + "," +
				AUCWinTieLoss +"," +
				pAUC + "," +
				AUCWinTieLossByRandomAnalyzer +"," +
				pAUCByRandomAnalyzer + "," +
				AUCWinTieLossRAAndCross +"," +
				pAUCRAAndCross + "," +
				(medianCrossAUC-medianTarAUC) + "," +
				medianSrcMCC + "," +
				medianTarMCC + "," +
				medianCrossMCC + "," +
				medianCrossMCCByRandomAnalyzer + "," +
				MCCWinTieLoss +"," +
				pMCC + "," +
				MCCWinTieLossByRandomAnalyzer +"," +
				pMCCByRandomAnalyzer + "," +
				MCCWinTieLossRAAndCross +"," +
				pMCCRAAndCross + "," +
				(medianCrossMCC-medianTarMCC) + "," +
				medianSrcPD + "," +
				medianTarPD + "," +
				medianCrossPD + "," +
				pdWinTieLoss +"," +
				(medianCrossPD-medianTarPD) + "," +
				medianSrcPF + "," +
				medianTarPF + "," +
				medianCrossPF + "," +
				pfWinTieLoss + "," +
				(medianTarPF - medianCrossPF) + "," +
				medianSrcBal + "," +
				medianTarBal + "," +
				medianCrossBal + "," +
				medianCrossBalByRandomAnalyzer + "," +
				balWinTieLoss + "," +
				pBal + "," +
				balWinTieLossByRandomAnalyzer + "," +
				pBalByRandomAnalyzer + "," +
				balWinTieLossRAAndCross + "," +
				pBalRAAndCross + "," +
				(medianCrossBal - medianTarBal)
			);
			
			processCounter(predictionInfo,coOccurrenceAnalyzerOption,this.cutoff,strClassifier);
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	private void runCrossPrediction(String predictionInfo,Instances srcInstances,
			Instances tgtInstances,String mlAlgorithm,String posStrLabel) {
		System.out.println("P," + predictionInfo + "," + SimpleCrossPredictor.crossPrediction(srcInstances, tgtInstances, mlAlgorithm, posStrLabel).getAUC());
		
	}

	double WilcoxonSignedRankTest(double[] sample1,double[] sample2){
		double pValue=0.0;
		
		if(sample1.length == 0 || sample2.length==0)
			return Double.NaN;
		
		if(Double.isNaN(sample1[0]) || Double.isNaN(sample2[0])){
			return Double.NaN;
		}
		try {
			rServe.assign("treated", sample1);
			rServe.assign("control", sample2);
			RList l = rServe.eval("wilcox.test(control,treated,paired=TRUE)").asList();
			pValue =  l.at("p.value").asDouble();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		
		return pValue;
	}
	private String getMaxCoFeatureScore(
			ArrayList<MatchedAttribute> matched) {
		if(matched==null)
			return "N/A";
		
		double max = 0.0;
		
		for(MatchedAttribute ma:matched){
			if(max<ma.getMatchingScore())
				max = ma.getMatchingScore();
		}
		return "" + max;
	}

	private ArrayList<Integer> getMatchedIndice(DataKind dataKind,
			ArrayList<MatchedAttribute> matchedAttributes) {
		ArrayList<Integer> selectedAttributesIndice = new ArrayList<Integer>();
		
		for(MatchedAttribute matched:matchedAttributes){
			int selectedIndex = dataKind==DataKind.SOURCE?matched.getSourceAttrIndex():matched.getTargetAttrIndex();
			selectedAttributesIndice.add(selectedIndex);
		}
		
		return selectedAttributesIndice;
	}

	private void incrementalTest(Instances srcData, Instances tarTest) {
		try {
			Classifier classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			
			for(int i=1;i<srcData.numAttributes();i++){
				
				Instances newSrc;
				Instances newTar;
				
				Remove remove = new Remove();
				String selectedIndex = i==1? "1,last":"1-" + i +",last";
				remove.setAttributeIndices(selectedIndex);
				remove.setInvertSelection(true);
				
				remove.setInputFormat(srcData);
				newSrc = Filter.useFilter(srcData, remove);
				
				remove.setInputFormat(tarTest);
				newTar = Filter.useFilter(tarTest, remove);
				
				classifier.buildClassifier(newSrc);
				newSrc.setClass(newSrc.attribute(WekaUtils.labelName));
				Evaluation eval = new Evaluation(newSrc);
				eval.evaluateModel(classifier, newTar);
				int posClassIndex = WekaUtils.getClassValueIndex(newSrc, WekaUtils.strPos);
				Double crossAUC = eval.areaUnderROC(posClassIndex);
				Double crossMCC = eval.matthewsCorrelationCoefficient(posClassIndex);
				Double crossF = eval.fMeasure(posClassIndex);
				
				Double crossPD = eval.truePositiveRate(posClassIndex);
				Double crossPF = eval.falsePositiveRate(posClassIndex);
				Double crossBal = WekaUtils.getBalance(crossPD, crossPF);
				
				
				System.out.println(i + ",crossF,"+ crossF+ ",crossAUC,"+ crossAUC+",crossMCC,"+ crossMCC+ ",crossBal,"+ crossBal);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getCoOccurFeatures(ArrayList<MatchedAttribute> matched){
		String strMatched = "";
		DecimalFormat dec = new DecimalFormat("0.00");
		
		if(matched==null)
			return "N/A";
		
		for(MatchedAttribute ma:matched){
			String srouceAttrName = sourceInstances.attribute(ma.getSourceAttrIndex()).name();
			String targetAttrName = targetInstances.attribute(ma.getTargetAttrIndex()).name();
			
			strMatched = strMatched + "|" + ma.getSourceAttrIndex() + "(" + srouceAttrName +")" + ">>" + ma.getTargetAttrIndex() + "(" + targetAttrName +")" + "(" + dec.format(ma.getMatchingScore()) + ")";
		}
		return strMatched;
	}

	Options createOptions(){
		// create Options object
		Options options = new Options();

		// add options
		options.addOption("sourcefile", true, "Source arff file path");
		options.addOption("srclabelname", true, "Source label name");
		options.addOption("possrclabel", true, "Positive label of source");

		options.addOption("targetfile", true, "Target arff file path");
		options.addOption("tgtlabelname", true, "Target label name");
		options.addOption("postgtlabel", true, "Positive label of target");

		options.addOption("analyzer", true, "Select Co-occurrence analyzer");

		options.addOption("mlalgorithm", true, "Select machine learning algorithm e.g., weka.classifiers.trees.J48");

		options.addOption("header", false, "display a header of results");
		
		options.addOption("saveFoldPredictionResults", false, "display instance prediction result for each fold");
		
		options.addOption("cutoff", true, "Set cutoff value from 0<cutoff<1. This option override nocutoff");
		
		options.addOption("predictionID", true, "Set Prediction ID for batch runs");

		return options;
	}

	void parseOptions(Options options,String[] args){
		CommandLineParser parser = new BasicParser();
		try {
			CommandLine cmd = parser.parse(options, args);

			sourcePath = cmd.getOptionValue("sourcefile");
			sourceLabelName = cmd.getOptionValue("srclabelname");
			sourceLabelPos = cmd.getOptionValue("possrclabel");

			targetPath = cmd.getOptionValue("targetfile");
			targetLabelName = cmd.getOptionValue("tgtlabelname");
			targetLabelPos = cmd.getOptionValue("postgtlabel");

			coOccurrenceAnalyzerOption = cmd.getOptionValue("analyzer");
			strClassifier = cmd.getOptionValue("mlalgorithm");

			header = cmd.hasOption("header")? true:false;
			
			applycutoff = cmd.hasOption("cutoff")? true:false;
			if(applycutoff)
				cutoff = Double.parseDouble(cmd.getOptionValue("cutoff"));
			
			predictionID = Integer.parseInt(cmd.getOptionValue("predictionID"));
			
			saveFoldPredictionResults = cmd.hasOption("saveFoldPredictionResults")? true:false;

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	static HashMap<String,Object> lockForDataLoading = new HashMap<String,Object>();
	Instances loadArff(String path,boolean isSource){
		boolean holdingKey = false;
		
		// key control
		synchronized(lockForDataLoading){
			if(!lockForDataLoading.containsKey(path)){
				lockForDataLoading.put(path, new Object());
				holdingKey=true;
			}
		}
		
		// return instances
		synchronized(lockForDataLoading.get(path)){
			// load data file when I hold the key.
			if(holdingKey){
				try {
					// load sourceData
					BufferedReader reader = new BufferedReader(new FileReader(path));
					Instances instances = new Instances(reader);
					reader.close();
					
					String labelName = isSource?sourceLabelName:targetLabelName;
					instances.setClass(instances.attribute(labelName));
					
					//if(isSource && useFeatureSelection){
						//System.err.println("FS: " + path);
						//instances = WekaUtils.featrueSelectionByCfsSubsetEval(instances);
						//System.err.println("FS Finished: " + path);
					//}
					//System.err.println("Loaded: " + path +  " " + targetPath + " " + coOccurrenceAnalyzerOption + " " + cutoff + " " + strClassifier );
					instancesKept.put(path, instances);
					
					lockForDataLoading.get(path).notifyAll();
					
					return new Instances(instancesKept.get(path));
				
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					System.out.println("A file does not exist! (both or each) Please, check path and upper/lower cases~");
					System.out.println("Source path: " + sourcePath);
					System.out.println("Target path: " + targetPath);
					System.exit(0);
				} catch (Exception e){
					e.printStackTrace();
					System.exit(0);
				}
				
			// otherwise wait until data is loaded.
			}else{
				while(!instancesKept.containsKey(path)){
					//System.err.println("Waiting: " + path + " " +targetPath + " " + coOccurrenceAnalyzerOption + " " + cutoff + " " + strClassifier );
					try {
						lockForDataLoading.get(path).wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
						System.exit(0);
					}
				}
				//System.err.println("Ended Waiting: " + path + " " +targetPath + " " + coOccurrenceAnalyzerOption+ " " + cutoff + " " + strClassifier );
				return new Instances(instancesKept.get(path));	// create as new instances		
			}
		}
		return null;
	}
	
	static synchronized Instances loadArff2(String path,boolean isSource,String srcLabelName,String tgtLabelName,String srcPath,String tgtPath){
		
		if(instancesKept.containsKey(path)){
			return new Instances(instancesKept.get(path));	// create as new instances	
		}
		
		try {
			// load sourceData
			BufferedReader reader = new BufferedReader(new FileReader(path));
			Instances instances = new Instances(reader);
			reader.close();
			
			String labelName = isSource?srcLabelName:tgtLabelName;
			instances.setClass(instances.attribute(labelName));
			
			//if(isSource && useFeatureSelection){
				//System.err.println("FS: " + path);
				//instances = WekaUtils.featrueSelectionByCfsSubsetEval(instances);
				//System.err.println("FS Finished: " + path);
			//}
			//System.err.println("Loaded: " + path +  " " + targetPath + " " + coOccurrenceAnalyzerOption + " " + cutoff + " " + strClassifier );
			instancesKept.put(path, instances);
			
			return new Instances(instancesKept.get(path));
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("A file does not exist! (both or each) Please, check path and upper/lower cases~");
			System.out.println("Source path: " + srcPath);
			System.out.println("Target path: " + srcPath);
			System.exit(0);
		} catch (Exception e){
			e.printStackTrace();
			System.exit(0);
		}
			
		return null;
	}
	
	String getWinTieLoss(double pValue,double source,double target){
		String win="tie";
		if(pValue<0.05){
			if (source<target)
				win = "win";
			else
				win = "loss";
		}
		else
			win = "tie";
		
		return win;
	}
}
