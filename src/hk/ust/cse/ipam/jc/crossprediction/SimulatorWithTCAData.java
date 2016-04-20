package hk.ust.cse.ipam.jc.crossprediction;

import hk.ust.cse.ipam.utils.ArrayListUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;

public class SimulatorWithTCAData implements Runnable {
	
	static ExecutorService executor;
	static long predictionProcessed = 0;
	
	int numRepeat = 50;
	int folds = 2;
	String normOption = "NoN";
	
	String sourceProjectName, targetProjectName, coOccurrenceAnalyzer, strCutoff, strClassifier;
	Instances sourceInstances, targetInstances;
	
	String dataPath = "data" + File.separator + "TCAData" +File.separator + normOption + File.separator;
	
	public static void main(String[] args) {
		int numOfThreads = args.length==0?1:Integer.parseInt(args[0]);
		System.err.println("ThreadPoolSize: " + numOfThreads);
		executor = Executors.newFixedThreadPool(numOfThreads);
		
		System.out.println("Prediction,Analyzer,classifier,cutoff,fmeasure,auc");
		
		runPredictionCombinations();
		executor.shutdown();
	}

	static void runPredictionCombinations(){
	
		// this also 
		//String[] sources = {"AEEEM", "ReLink", "MIMAll", "PROMISE", "NASA", "SOFTLAB", "albrecht", "pitsB", "ovarian_61902", "winequality"};
		String[] sources = {"AEEEM", "ReLink", "MIMAll", "PROMISE", "NASA", "SOFTLAB"};
		//String[] sources = {"MIMAll", "PROMISE", "NASA", "SOFTLAB"};
		ArrayList<String[]> targetGroups = new ArrayList<String[]>();
		
		String[] AEEEM = {"EQ","JDT", "LC","ML","PDE"};
		targetGroups.add(AEEEM);
		String[] ReLink = {"Apache","Safe","Zxing"};
		targetGroups.add(ReLink);
		String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		targetGroups.add(MIM);
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka","tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		targetGroups.add(PROMISE);
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		targetGroups.add(NASA);
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		targetGroups.add(SOFTLAB);
		
		//String classifier = "weka.classifiers.trees.RandomForest";
		String[] mlAlgorithms = {
				"weka.classifiers.trees.RandomForest",
				//"weka.classifiers.bayes.BayesNet",
				//"weka.classifiers.bayes.NaiveBayes",
				"weka.classifiers.functions.Logistic",
				//"weka.classifiers.functions.SMO"};
				//"weka.classifiers.functions.MultilayerPerceptron"
				};
		String analyzer = "AL1DAnalyzer";
		String cutoff = "0.55";
		for(String classifier:mlAlgorithms){
			for(int s = 0; s<targetGroups.size();s++){
				for(int t=0;t<targetGroups.size();t++){
					if(s==t)
						continue;
					for(String source:targetGroups.get(s)){
						for(String target:targetGroups.get(t))
							executor.execute(new SimulatorWithTCAData(source,target,analyzer,cutoff,classifier));
					}
				}
			}
		}
	}
	
	public SimulatorWithTCAData(String srcProjectName,String tarProjectName,String analyzer,String cutoff,String classifier){
		sourceProjectName = srcProjectName;
		targetProjectName = tarProjectName;
		coOccurrenceAnalyzer = analyzer;
		strCutoff = cutoff;
		strClassifier = classifier;
	}
	
	@Override
	public void run(){
		loadInstances();
		if(sourceInstances!=null)
			predict();
	}

	private void loadInstances() {
		// load arff files
		String sourcePath = dataPath + "TCAed_" + sourceProjectName + "_to_" + targetProjectName + "_" + coOccurrenceAnalyzer + "_" + strCutoff + "_" + normOption + "_dim10_S.arff";
		String targetPath = dataPath + "TCAed_" + sourceProjectName + "_to_" + targetProjectName + "_" + coOccurrenceAnalyzer + "_" + strCutoff + "_" + normOption + "_dim10_T.arff";
		
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(sourcePath));
			sourceInstances = new Instances(reader);
			sourceInstances.setClass(sourceInstances.attribute(WekaUtils.labelName));
			reader.close();
			
			reader = new BufferedReader(new FileReader(targetPath));
			targetInstances = new Instances(reader);
			targetInstances.setClass(targetInstances.attribute(WekaUtils.labelName));
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void predict(){
		try {
			Classifier classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			Evaluation crossEval = new Evaluation(sourceInstances);
			classifier.buildClassifier(sourceInstances);
			
			ArrayList<Double> avgFmeasure = new ArrayList<Double>();
			ArrayList<Double> avgAUC = new ArrayList<Double>();
			
			int posClassValueIndex = WekaUtils.getClassValueIndex(sourceInstances, WekaUtils.strPos);
			for(int i=0; i<numRepeat; i++){
				targetInstances.randomize(new Random(i));
				targetInstances.stratify(folds);
				for(int fold=0; fold<folds;fold++){
					Instances testInstances = targetInstances.testCV(folds, fold);
					crossEval.evaluateModel(classifier, testInstances);
					
					avgFmeasure.add(crossEval.fMeasure(posClassValueIndex));
					avgAUC.add(crossEval.areaUnderROC(posClassValueIndex));
					
				}
			}
			
			
			System.out.println(sourceProjectName + ">>" + targetProjectName + "," + 
								coOccurrenceAnalyzer + "," + strClassifier +"," +
								strCutoff +  "_" + normOption + "," +
								ArrayListUtil.getAverage(avgFmeasure) + "," + 
								ArrayListUtil.getAverage(avgAUC));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
