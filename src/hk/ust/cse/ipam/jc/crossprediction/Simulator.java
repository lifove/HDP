package hk.ust.cse.ipam.jc.crossprediction;

import hk.ust.cse.ipam.jc.crossprediction.data.SharedDataForAnalyzer;
import hk.ust.cse.ipam.jc.crossprediction.data.MatchedAttribute;
import hk.ust.cse.ipam.jc.crossprediction.data.Measure;
import hk.ust.cse.ipam.jc.crossprediction.data.Measures;
import hk.ust.cse.ipam.utils.ArrayListUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

import java.io.BufferedReader;
import java.io.FileReader;
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
import org.apache.commons.math3.stat.inference.TestUtils;

import com.google.common.primitives.Doubles;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class Simulator implements Runnable{

	Options options;
	
	boolean DISPLAYDETAILED = false;
	boolean RUNINCREMENTALTEST = false;
	boolean RANDOMALAIYZER = false;
	boolean SRCWITHIN = false;
	
	int repeat = 50;
	int folds = 2;
	boolean useTargetLabeledInstances = false;
	static boolean useFeatureSelection = false;

	public static HashMap<String,Instances> instancesKept = new HashMap<String,Instances>();
	public static HashMap<String,SharedDataForAnalyzer> analyzersKept = new HashMap<String,SharedDataForAnalyzer>(); // key: sourcepath + targetpath + analyzers

	Instances sourceInstances,targetInstances,newSourceInstances,newTargetInstances;
	String sourcePath;
	String sourceLabelName;
	String sourceLabelPos;

	String targetPath;
	String targetLabelName;
	String targetLabelPos;

	String coOccurrenceAnalyzerOption;
	String strClassifier;
	
	public static HashMap<String,Measure> withinResults = new HashMap<String,Measure>();

	boolean header=false;
	// change this option to apply cutoff
	boolean applycutoff = false;
	double cutoff = 0.0;
	
	int predictionID = 1;
	
	ArrayList<CoOccurrenceAnalyzer> coOccurAnalyzers = null;


	/**
	 * @param args
	 */
	public Simulator(String[] args){
		// load command line options
		options = createOptions();

		if(args.length < options.getOptions().size()){
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "CrossPredictionSimulator", options );
		}

		parseOptions(options, args);
		
		predictionInfo = sourcePath.substring(sourcePath.lastIndexOf("/")+1).replace(".arff", "") + ">>" +
				targetPath.substring(targetPath.lastIndexOf("/")+1).replace(".arff", "");
	}
	
	public void run(){

		targetInstances = loadArff(targetPath,false);
		sourceInstances = loadArff(sourcePath,true);
		
		// normalize data set
		sourceInstances = WekaUtils.applyNormalize(sourceInstances);
		targetInstances = WekaUtils.applyNormalize(targetInstances);
		
		String keyForAnalyzer = sourcePath + targetPath + coOccurrenceAnalyzerOption;
		coOccurAnalyzers = getAnalyzers(keyForAnalyzer);
		
		System.err.println("Doit!" + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);
		
		//System.err.println("Doit!2" + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);

		// save new instances as files

		// conduct experiments
		if(header){
			
			System.out.println("Type,No,Prediction,Analyzer,Classifier,ItrtCount,#OriSourceInstnaces,#OriTargetInstances,#TargetInstacesForWithin,#NewSourceInst.,#NewTargetInst.,#OriSrcAttributes,#OriTarAttributes,#SrcSelectedFeatures," +
					"#CoOcfeatures,CoOcFeatrues,SourceWithinF,TargetWithinF,CrossF,CrossFRA,FWin,FP-value,FWinRA,FP-valueRA,FWinRACross,FP-valueRACross,FImproved," +
					"SourceWithinAUC, TargetWithinAUC,CrossAUC,CrossAUCRA,AUCWin,AUCP-value,AUCWinRA,AUCP-valueRA,AUCWinRACross,AUCP-valueRACross,AUCImproved,SourceWithinPD,TargetWihinPD,CrossPD,PDWin,PDImproved," +
					"SourceWithinPF,TargetWihinPF,CrossPF,PFWIn,PFImproved," +
					"SourceWithinBal,TargetWihinBal,CrossBal,CrossBalRA,BalWin,BalP-value,BalWinRA,BalP-valueRA,BalWinRACross,BalP-valueRACross,BalImproved");
		}
		//runPrediction();
		runPredictionOnSameTestSet();

		// display/save results
	}

	static Object counterLock = new Object();
	public void processCounter() {
		synchronized(counterLock){
			Driver.predictionProcessed++;
			System.err.println("Finished!" + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);
			System.err.println("predictionProcessed=" + Driver.predictionProcessed +"\r");
			
		}
	}

	String predictionInfo = "";
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
	
	void runPredictionOnSameTestSet(){

		String predictionInfo = sourcePath.substring(sourcePath.lastIndexOf("/")+1).replace(".arff", "") + ">>" +
				targetPath.substring(targetPath.lastIndexOf("/")+1).replace(".arff", "");

		int numOfSourceInstances = -1;
		int numOfTargetInstances = -1;
		int numOfSourceAttributes = sourceInstances.numAttributes();
		int numOfTargetAttributes = targetInstances.numAttributes();
		int numCoOccurFeautres = -1; // except for a label attribute		
		
		int actualIterationNum = 1;
		
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
			int numAttributesByFeatureSelection=-1;
			
			for(int i=0; i<repeat; i++){
				
				numAttributesByFeatureSelection = coOccurAnalyzers.get(i).numOfAttibutesByFeatureSelection;
				
				newSourceInstances = coOccurAnalyzers.get(i).getNewSourceInstances();
				newTargetInstances = coOccurAnalyzers.get(i).getNewTargetInstances();
				numOfSourceInstances = newSourceInstances.numInstances();
				numOfTargetInstances = newTargetInstances.numInstances();
				numCoOccurFeautres = newSourceInstances.numAttributes()-1; // except for a label attribute		
				
				srcData = new Instances(sourceInstances);
				tarData = new Instances(targetInstances);  
				
				//----------------------------------------
				// if it is 0, no need to proceed, just displayinfo
				String cutoff = applycutoff==true?dec.format(coOccurAnalyzers.get(i).cutoff)+"":"NOCUTOFF";
				if (numCoOccurFeautres==0){
					System.out.println("A" + "," + predictionID + "," + predictionInfo + "," +
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
							getCoOccurFeatures(coOccurAnalyzers.get(i).matchedAttribute));
					
					processCounter();
					
					return;
				}
				
				// build source classifier once
				Instances newSrcData = new Instances(newSourceInstances);
				classifierForSource.buildClassifier(newSrcData);
				
				// randomize with different seed for each iteration
				srcData.randomize(new Random(i)); 
				srcData.stratify(folds);

				// randomize with different seed for each iteration
				tarData.randomize(new Random(i)); 
				tarData.stratify(folds);
				
				for(int n=0;n < folds;n++){
					
					//System.err.println("Doit!Loop" + i + "-" + n + " " + predictionInfo +  " " + coOccurrenceAnalyzerOption + "_" + cutoff + "," + " on " + strClassifier);
					
					String keyForSavedWithinResults = sourcePath + "_" + strClassifier + "_" + i + "_" + n;
					
					double srcF;
					double srcAUC;
					double srcMCC;
					double srcPD;
					double srcPF;
					double srcBal;
					if(withinResults.containsKey(keyForSavedWithinResults)){
						Measure withinResult = withinResults.get(keyForSavedWithinResults);
						srcF = withinResult.getFmeasure();
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
							srcF = srcEval.fMeasure(posClassIndex);
							srcAUC = srcEval.areaUnderROC(posClassIndex);
							srcMCC = srcEval.matthewsCorrelationCoefficient(posClassIndex);
							srcPD = srcEval.truePositiveRate(posClassIndex);
							srcPF = srcEval.falsePositiveRate(posClassIndex);
							srcBal = WekaUtils.getBalance(srcPD, srcPF);
						}
						else{
							srcF = Double.NaN;
							srcAUC = Double.NaN;
							srcMCC = Double.NaN;
							srcPD = Double.NaN;
							srcPF = Double.NaN;
							srcBal = Double.NaN;
						}
						// put result in withinResults HashMap
						withinResults.put(keyForSavedWithinResults,new Measure(-1,-1,srcF,-1,-1,srcAUC,srcMCC,srcPD,srcPF,srcBal));
					}

					// target-within, tarTest is used for cross-prediction. That is why it is defined here.
					Instances tarTest = tarData.testCV(folds, n);
					
					keyForSavedWithinResults = targetPath + "_" + strClassifier + "_" + i + "_" + n;
					double tarF,tarAUC,tarMCC,tarPD,tarPF,tarBal;
					if(withinResults.containsKey(keyForSavedWithinResults)){
						Measure withinResult = withinResults.get(keyForSavedWithinResults);
						tarF = withinResult.getFmeasure();
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
						Evaluation tarEval = new Evaluation(tarTrain);
						tarEval.evaluateModel(classifierForTargetWithin, tarTest);
						
						posClassIndex = WekaUtils.getClassValueIndex(tarData, targetLabelPos);
						tarF = tarEval.fMeasure(posClassIndex);
						tarAUC = tarEval.areaUnderROC(posClassIndex);
						tarMCC = tarEval.matthewsCorrelationCoefficient(posClassIndex);
						tarPD = tarEval.truePositiveRate(posClassIndex);
						tarPF = tarEval.falsePositiveRate(posClassIndex);
						tarBal = WekaUtils.getBalance(tarPD, tarPF);
						// put result in withinResults HashMap
						withinResults.put(keyForSavedWithinResults,new Measure(-1,-1,tarF,-1,-1,tarAUC,tarMCC,tarPD,tarPF,tarBal));
					}

					double crossF = Double.NaN;
					double crossAUC = Double.NaN;
					double crossMCC = Double.NaN;
					double crossPD = Double.NaN;
					double crossPF = Double.NaN;
					double crossBal = Double.NaN;
					
					double crossFByRandomAnalyzer = Double.NaN;
					double crossAUCByRandomAnalyzer = Double.NaN;
					double crossMCCByRandomAnalyzer = Double.NaN;
					double crossPDByRandomAnalyzer = Double.NaN;
					double crossPFByRandomAnalyzer = Double.NaN;
					double crossBalByRandomAnalyzer = Double.NaN;
					
					//target-cross
					//Instances newSrcTrain = newSrcRandData.trainCV(folds, n);
					Instances newTarTest = WekaUtils.getInstancesWithSelectedAttributes(tarTest,
												coOccurAnalyzers.get(i).getMatchedIndice(CoOccurrenceAnalyzer.DataKind.TARGET,false),targetLabelPos);
					Evaluation crossEval = new Evaluation(newSrcData);
					//classifier.buildClassifier(newSrcRandData);
					crossEval.evaluateModel(classifierForSource, newTarTest);
					
					if(RUNINCREMENTALTEST)
						incrementalTest(newSrcData,newTarTest);
					
					posClassIndex = WekaUtils.getClassValueIndex(newTarTest,WekaUtils.strPos);
					crossF = crossEval.fMeasure(posClassIndex);
					crossAUC = crossEval.areaUnderROC(posClassIndex);
					crossMCC = crossEval.matthewsCorrelationCoefficient(posClassIndex);
					crossPD = crossEval.truePositiveRate(posClassIndex);
					crossPF = crossEval.falsePositiveRate(posClassIndex);
					crossBal = WekaUtils.getBalance(crossPD, crossPF);
					
					if(RANDOMALAIYZER){
						//---------
						// for random analyzer, build model and test every time with new source and target randomly matched.
						coOccurAnalyzers.get(i).generateNewSourceAndTargetdatasetsByRandomAnalyzer();
						Instances newSrcDataRandomAnlalyzer = new Instances(coOccurAnalyzers.get(i).newRandomSourceInstances);
						classfierForRandomAnalyzer.buildClassifier(newSrcDataRandomAnlalyzer);
						Instances newTarTestByRandomAnalyzer = WekaUtils.getInstancesWithSelectedAttributes(tarTest,
													coOccurAnalyzers.get(i).getMatchedIndice(CoOccurrenceAnalyzer.DataKind.TARGET,true),targetLabelPos);
						
						Evaluation crossEvalByRandomAnalyzer = new Evaluation(newSrcDataRandomAnlalyzer);
						//classifier.buildClassifier(newSrcRandData);
						crossEvalByRandomAnalyzer.evaluateModel(classfierForRandomAnalyzer, newTarTestByRandomAnalyzer);
						
						crossFByRandomAnalyzer = crossEvalByRandomAnalyzer.fMeasure(posClassIndex);
						crossAUCByRandomAnalyzer = crossEvalByRandomAnalyzer.areaUnderROC(posClassIndex);
						crossMCCByRandomAnalyzer = crossEvalByRandomAnalyzer.matthewsCorrelationCoefficient(posClassIndex);
						crossPDByRandomAnalyzer = crossEvalByRandomAnalyzer.truePositiveRate(posClassIndex);
						crossPFByRandomAnalyzer = crossEvalByRandomAnalyzer.falsePositiveRate(posClassIndex);
						crossBalByRandomAnalyzer = WekaUtils.getBalance(crossPDByRandomAnalyzer, crossPFByRandomAnalyzer);
						/*System.out.println("-===--" + coOccurAnalyzer.matchedAttribute.size());
						for(MatchedAttribute ma:coOccurAnalyzer.matchedAttribute){
							System.out.println(ma.getSourceAttrIndex() +"-" + ma.getTargetAttrIndex());
						}
						System.out.println("----" + coOccurAnalyzer.matchedAttributeByRandom.size());
						for(MatchedAttribute ma:coOccurAnalyzer.matchedAttributeByRandom){
							System.out.println(ma.getSourceAttrIndex() +"-" + ma.getTargetAttrIndex());
						}*/
					}
					
					
					if(Double.isNaN(crossAUC)){
						continue;
					}
					
					// consider only cases which have at least one featre on new datasets
					// if there is no co-occurrent features, we don't have to include that case in results.
					srcWithinResults.setAllMeasures(-1,-1,srcF,-1,-1,srcAUC,srcMCC,srcPD,srcPF,srcBal);
					tarWithinResults.setAllMeasures(-1,-1,tarF,-1,-1,tarAUC,tarMCC,tarPD,tarPF,tarBal);
					crossResults.setAllMeasures(-1,-1,crossF,-1,-1, crossAUC,crossMCC,crossPD, crossPF, crossBal);
					crossResultsByRandomAnalyzer.setAllMeasures(-1,-1,crossFByRandomAnalyzer, -1,-1,crossAUCByRandomAnalyzer,crossMCCByRandomAnalyzer, crossPDByRandomAnalyzer, crossPFByRandomAnalyzer, crossBalByRandomAnalyzer);
					
					if(DISPLAYDETAILED){
						System.out.println("D," + i + ";" + n + "," + predictionInfo + "," +
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
								getCoOccurFeatures(coOccurAnalyzers.get(i).matchedAttribute) + "*" + getCoOccurFeatures(coOccurAnalyzers.get(i).matchedAttributeByRandom) + "," +
								srcF + "," +
								tarF + "," +
								crossF + "," +
								crossFByRandomAnalyzer + "," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								"-," +
								(crossF-tarF) +"," +
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
					actualIterationNum++;
					if(actualIterationNum==repeat*folds)
						break;
				}
				if(actualIterationNum==repeat*folds)
					break;
			}
			
			double avgSrcF=Double.NaN, avgTarF=Double.NaN, avgCrossF=Double.NaN,
			avgSrcAUC=Double.NaN, avgTarAUC=Double.NaN, avgCrossAUC=Double.NaN,
			avgSrcPD=Double.NaN, avgTarPD=Double.NaN, avgCrossPD=Double.NaN,
			avgSrcPF=Double.NaN, avgTarPF=Double.NaN, avgCrossPF=Double.NaN,
			avgSrcBal=Double.NaN, avgTarBal=Double.NaN, avgCrossBal=Double.NaN;

			double avgCrossFByRandomAnalyzer=Double.NaN,
			avgCrossAUCByRandomAnalyzer=Double.NaN, avgCrossPDByRandomAnalyzer=Double.NaN, avgCrossPFByRandomAnalyzer=Double.NaN,
			avgCrossBalByRandomAnalyzer=Double.NaN;
			
			double pFmeasure=Double.NaN,pFmeasureByRandomAnalyzer=Double.NaN,pFmeasureRAAndCross=Double.NaN;
			String FWinTieLoss="", FWinTieLossByRandomAnalyzer="", FWinTieLossRAAndCross="";
			double pAUC=Double.NaN, pAUCByRandomAnalyzer=Double.NaN, pAUCRAAndCross=Double.NaN;
			String AUCWinTieLoss="", AUCWinTieLossByRandomAnalyzer="", AUCWinTieLossRAAndCross="";
			double pBal=Double.NaN, pBalByRandomAnalyzer=Double.NaN, pBalRAAndCross=Double.NaN;
			String balWinTieLoss="", balWinTieLossByRandomAnalyzer="", balWinTieLossRAAndCross="";
			
			String pdWinTieLoss="", pfWinTieLoss="";

			// statistical test
	
			avgSrcF = ArrayListUtil.getAverage(srcWithinResults.getFmeasures());
			avgTarF = ArrayListUtil.getAverage(tarWithinResults.getFmeasures());
			avgCrossF = ArrayListUtil.getAverage(crossResults.getFmeasures());
			avgCrossFByRandomAnalyzer = ArrayListUtil.getAverage(crossResultsByRandomAnalyzer.getFmeasures());
			
			pFmeasure = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getFmeasures()),
									Doubles.toArray(crossResults.getFmeasures()));
			pFmeasureByRandomAnalyzer = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getFmeasures()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getFmeasures()));
			pFmeasureRAAndCross = TestUtils.pairedTTest(Doubles.toArray(crossResults.getFmeasures()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getFmeasures()));
			
			FWinTieLoss = getWinTieLoss(pFmeasure,avgTarF,avgCrossF);
			FWinTieLossByRandomAnalyzer = getWinTieLoss(pFmeasureByRandomAnalyzer,avgTarF,avgCrossFByRandomAnalyzer);
			FWinTieLossRAAndCross = getWinTieLoss(pFmeasureRAAndCross,avgCrossFByRandomAnalyzer,avgCrossF);
			
			
			avgSrcAUC = ArrayListUtil.getAverage(srcWithinResults.getAUCs());
			avgTarAUC = ArrayListUtil.getAverage(tarWithinResults.getAUCs());
			avgCrossAUC = ArrayListUtil.getAverage(crossResults.getAUCs());
			avgCrossAUCByRandomAnalyzer = ArrayListUtil.getAverage(crossResultsByRandomAnalyzer.getAUCs());
			
			pAUC = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getAUCs()),
					Doubles.toArray(crossResults.getAUCs()));
			pAUCByRandomAnalyzer = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getAUCs()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getAUCs()));
			pAUCRAAndCross = TestUtils.pairedTTest(Doubles.toArray(crossResults.getAUCs()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getAUCs()));
			
			AUCWinTieLoss = getWinTieLoss(pAUC,avgTarAUC,avgCrossAUC);
			AUCWinTieLossByRandomAnalyzer = getWinTieLoss(pAUCByRandomAnalyzer,avgTarAUC,avgCrossAUCByRandomAnalyzer);
			AUCWinTieLossRAAndCross = getWinTieLoss(pAUCRAAndCross,avgCrossAUCByRandomAnalyzer,avgCrossAUC);
			
			avgSrcPD = ArrayListUtil.getAverage(srcWithinResults.getPds());
			avgTarPD = ArrayListUtil.getAverage(tarWithinResults.getPds());
			avgCrossPD = ArrayListUtil.getAverage(crossResults.getPds());
			avgCrossPDByRandomAnalyzer = ArrayListUtil.getAverage(crossResultsByRandomAnalyzer.getPds());
			
			double pPD = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getPds()),
					Doubles.toArray(crossResults.getPds()));
			
			pdWinTieLoss = getWinTieLoss(pPD,avgTarPD,avgCrossPD);
			
			avgSrcPF = ArrayListUtil.getAverage(srcWithinResults.getPfs());
			avgTarPF = ArrayListUtil.getAverage(tarWithinResults.getPfs());
			avgCrossPF = ArrayListUtil.getAverage(crossResults.getPfs());
			avgCrossPFByRandomAnalyzer = ArrayListUtil.getAverage(crossResultsByRandomAnalyzer.getPfs());
			
			double pPF = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getPfs()),
					Doubles.toArray(crossResults.getPfs()));
			
			pfWinTieLoss = getWinTieLoss(pPF,avgTarPF,avgCrossPF);
			
			avgSrcBal = ArrayListUtil.getAverage(srcWithinResults.getBals());
			avgTarBal = ArrayListUtil.getAverage(tarWithinResults.getBals());
			avgCrossBal = ArrayListUtil.getAverage(crossResults.getBals());
			avgCrossBalByRandomAnalyzer = ArrayListUtil.getAverage(crossResultsByRandomAnalyzer.getBals());

			pBal = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getBals()),
					Doubles.toArray(crossResults.getBals()));
			pBalByRandomAnalyzer = TestUtils.pairedTTest(Doubles.toArray(tarWithinResults.getBals()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getBals()));
			pBalRAAndCross = TestUtils.pairedTTest(Doubles.toArray(crossResults.getBals()),
					Doubles.toArray(crossResultsByRandomAnalyzer.getBals()));
			
			balWinTieLoss = getWinTieLoss(pBal,avgTarBal,avgCrossBal);
			balWinTieLossByRandomAnalyzer = getWinTieLoss(pBalByRandomAnalyzer,avgTarBal,avgCrossBalByRandomAnalyzer);
			balWinTieLossRAAndCross = getWinTieLoss(pBalRAAndCross,avgCrossBalByRandomAnalyzer,avgCrossBal);
		
			dec = new DecimalFormat("0.00");
			String cutoff = applycutoff==true?dec.format(this.cutoff)+"":"NOCUTOFF";
			System.out.println("A" + "," + predictionID + "," + predictionInfo + "," +
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
				getCoOccurFeatures(coOccurAnalyzers.get(0).matchedAttribute) + "," +
				avgSrcF + "," +
				avgTarF + "," +
				avgCrossF + "," +
				avgCrossFByRandomAnalyzer + "," +
				FWinTieLoss + "," +
				pFmeasure + "," +
				FWinTieLossByRandomAnalyzer + "," +
				pFmeasureByRandomAnalyzer + "," +
				FWinTieLossRAAndCross + "," +
				pFmeasureRAAndCross + "," +
				(avgCrossF-avgTarF) +"," +
				avgSrcAUC + "," +
				avgTarAUC + "," +
				avgCrossAUC + "," +
				avgCrossAUCByRandomAnalyzer + "," +
				AUCWinTieLoss +"," +
				pAUC + "," +
				AUCWinTieLossByRandomAnalyzer +"," +
				pAUCByRandomAnalyzer + "," +
				AUCWinTieLossRAAndCross +"," +
				pAUCRAAndCross + "," +
				(avgCrossAUC-avgTarAUC) + "," +
				avgSrcPD + "," +
				avgTarPD + "," +
				avgCrossPD + "," +
				pdWinTieLoss +"," +
				(avgCrossPD-avgTarPD) + "," +
				avgSrcPF + "," +
				avgTarPF + "," +
				avgCrossPF + "," +
				pfWinTieLoss + "," +
				(avgTarPF - avgCrossPF) + "," +
				avgSrcBal + "," +
				avgTarBal + "," +
				avgCrossBal + "," +
				avgCrossBalByRandomAnalyzer + "," +
				balWinTieLoss + "," +
				pBal + "," +
				balWinTieLossByRandomAnalyzer + "," +
				pBalByRandomAnalyzer + "," +
				balWinTieLossRAAndCross + "," +
				pBalRAAndCross + "," +
				(avgCrossBal - avgTarBal)
			);
			
			processCounter();
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
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
				Double crossF = eval.fMeasure(posClassIndex);
				
				Double crossPD = eval.truePositiveRate(posClassIndex);
				Double crossPF = eval.falsePositiveRate(posClassIndex);
				Double crossBal = WekaUtils.getBalance(crossPD, crossPF);
				
				
				System.out.println(i + ",crossF,"+ crossF+ ",crossAUC,"+ crossAUC+ ",crossBal,"+ crossBal);
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

		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	//static HashMap<String,Object> lockForAnalyzer = new HashMap<String,Object>(); // key: keyForAnalyzer+cutoff
	ArrayList<CoOccurrenceAnalyzer> getAnalyzers(String keyForAnalyzer){
		/*boolean holdingKeyForAnalyzer = false;
		
		// to get a key for each pool one by one
		synchronized(lockForAnalyzer){
			if(!lockForAnalyzer.containsKey(keyForAnalyzer)){
				lockForAnalyzer.put(keyForAnalyzer, new Object());
				holdingKeyForAnalyzer=true;
			}
		}*/
		
		CoOccurrenceAnalyzer coOccurAnalyzer=null;
		//generate a pool with the first analyzer
		
			//if(holdingKeyForAnalyzer){
	
				System.err.println("preparing for a new analyzer: " + keyForAnalyzer + " on " + strClassifier);
				
				ArrayList<CoOccurrenceAnalyzer> analyzers = new ArrayList<CoOccurrenceAnalyzer> ();
				for(int i=0;i<repeat;i++){
					
					// create copy
					Instances srcInstances = sourceInstances;
					Instances tarInstances = targetInstances;
					// shuffle all instances.
					srcInstances.randomize(new Random(i));
					tarInstances.randomize(new Random(i));
					
					// run analyzer
					coOccurAnalyzer = new CoOccurrenceAnalyzer(coOccurrenceAnalyzerOption,srcInstances,tarInstances,
							sourceLabelName,sourceLabelPos,targetLabelName,targetLabelPos,applycutoff,cutoff,useTargetLabeledInstances,useFeatureSelection,true,true);
					
					coOccurAnalyzer.runAnalyzer();
					
					//System.err.println("_New analyer: " + keyForAnalyzer +" " +  i);
					
					analyzers.add(coOccurAnalyzer);
				}
				System.err.println("Ended preparing for the first new analyzer in a pool: " + keyForAnalyzer + " on " + strClassifier);	
				
				// create pool and put the first analyzer
				//analyzersKept.put(keyForAnalyzer,new SharedDataForAnalyzer(analyzers));	
				
				//lockForAnalyzer.get(keyForAnalyzer).notifyAll();
				System.err.println("!Done Try to get lockForAnalyzer lockForAnalyzer.get(keyForAnalyzer): " + keyForAnalyzer + this.cutoff);
				return analyzers;
			//}
			
		//synchronized(lockForAnalyzer.get(keyForAnalyzer)){
			// wait for a analyzer with a specific cutoff in the pool
			//System.err.println("To be waiting...: " + keyForAnalyzer + this.cutoff);
	/*		while(!analyzersKept.containsKey(keyForAnalyzer)){
				try {
					System.err.println("Waiting until shared data is ready : " + keyForAnalyzer);
					this.wait();
					//lockForAnalyzer.get(keyForAnalyzer).wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			ArrayList<CoOccurrenceAnalyzer> analyzers = new ArrayList<CoOccurrenceAnalyzer> ();
			for(int i=0;i<repeat;i++){
				
				// create copy
				Instances srcInstances = sourceInstances;
				Instances tarInstances = targetInstances;
				// shuffle all instances.
				srcInstances.randomize(new Random(i));
				tarInstances.randomize(new Random(i));
				
				coOccurAnalyzer = new CoOccurrenceAnalyzer(coOccurrenceAnalyzerOption,srcInstances,tarInstances,
						sourceLabelName,sourceLabelPos,targetLabelName,targetLabelPos,applycutoff,cutoff,useTargetLabeledInstances,useFeatureSelection);
				
				coOccurAnalyzer.allMatchedAttributes = analyzersKept.get(keyForAnalyzer).getAllMatchedAttributes(i);
				coOccurAnalyzer.indexOfLabeledTargetInstances = analyzersKept.get(keyForAnalyzer).getIndexOfLabeledTargetInstances(i);
				coOccurAnalyzer.targetInstances = analyzersKept.get(keyForAnalyzer).getTargetInstnaces(i);
				
				coOccurAnalyzer.runAnalyzer();
				
				analyzers.add(coOccurAnalyzer);
			}
			
			return analyzers;
		//}*/
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
					
					if(isSource && useFeatureSelection){
						//System.err.println("FS: " + path);
						instances = WekaUtils.featrueSelectionByCfsSubsetEval(instances);
						//System.err.println("FS Finished: " + path);
					}
					//System.err.println("Loaded: " + path +  " " + targetPath + " " + coOccurrenceAnalyzerOption + " " + cutoff + " " + strClassifier );
					instancesKept.put(path, instances);
					
					lockForDataLoading.get(path).notifyAll();
					
					return instances;
				
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("A file does not exist! (both or each) Please, check path and upper/lower cases~");
					System.out.println("Source path: " + sourcePath);
					System.out.println("Target path: " + targetPath);
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
