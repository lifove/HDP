package hk.ust.cse.ipam.jc.crossprediction;

import hk.ust.cse.ipam.utils.ArrayListUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.Utils;

public class SimulatorForTargetWithin implements Runnable {
	
	static ExecutorService executor;
	static long predictionProcessed = 0;
	
	String projectGroupDir, projectName, labelName, labelPos, strClassifier;
	Instances targetInstances;
	
	int numRepeat = 50;
	int folds = 2;
	
	public static void main(String[] args) {
		
		int numOfThreads = args.length==2?1:Integer.parseInt(args[2]);
		System.err.println("ThreadPoolSize: " + numOfThreads);
		executor = Executors.newFixedThreadPool(numOfThreads);
		
		System.out.println("Project,classifier,AvgFmeasure,avgAUC");
		
		runPredictionCombinations(args);
		executor.shutdown();
	}

	static void runPredictionCombinations(String args[]){
		
		int folds = Integer.parseInt(args[0]);
		int repeat = Integer.parseInt(args[1]);
		
		ArrayList<ProjectGroupInfo> projectGroups = new ArrayList<ProjectGroupInfo>();
				
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		projectGroups.add(new ProjectGroupInfo("data/AEEEM/", "class", "buggy", AEEEM));
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		projectGroups.add(new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", ReLink));
		
		String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		projectGroups.add(new ProjectGroupInfo("data/MIM/", "class", "buggy", MIM));
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		projectGroups.add(new ProjectGroupInfo("data/promise/", "bug", "buggy", PROMISE));
		
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		projectGroups.add(new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA));
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		projectGroups.add(new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", SOFTLAB));
		
		String[] mlAlgorithms = {//"weka.classifiers.trees.J48",
				"weka.classifiers.trees.RandomForest",
				"weka.classifiers.bayes.BayesNet",
				"weka.classifiers.bayes.NaiveBayes",
				"weka.classifiers.functions.Logistic",
				"weka.classifiers.functions.SMO",
				"weka.classifiers.functions.MultilayerPerceptron"};
		
		for(String classifier:mlAlgorithms){
			for(ProjectGroupInfo pInfo:projectGroups){
				String prjGroupDir = pInfo.dirPath;
				String[] projects = pInfo.projects;
				String labelName = pInfo.labelName;
				String labelPos = pInfo.posLabel;
				
				for(String project:projects){
					executor.execute(new SimulatorForTargetWithin(prjGroupDir,project,labelName,labelPos,classifier,folds,repeat));
				}
			}
		}	
	}
	
	public SimulatorForTargetWithin(String prjGroupDir,String prjName,String labelName,String labelPos,String classifier,int folds,int repeat){
		projectGroupDir = prjGroupDir;
		projectName = prjName;
		strClassifier = classifier;
		this.labelName = labelName;
		this.labelPos = labelPos;
		this.folds = folds;
		this.numRepeat = repeat;
	}
	
	@Override
	public void run(){
		loadInstances();
		predict();
	}

	private void loadInstances() {
		String targetPath = projectGroupDir + projectName + ".arff";
		
		BufferedReader reader;
		try {		
			reader = new BufferedReader(new FileReader(targetPath));
			targetInstances = new Instances(reader);
			targetInstances.setClass(targetInstances.attribute(labelName));
			reader.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	void predict(){
		try {
			Classifier classifier = (Classifier) Utils.forName(Classifier.class, strClassifier, null);
			
			ArrayList<Double> lstFmeasure = new ArrayList<Double>();
			ArrayList<Double> lstAUC = new ArrayList<Double>();
			
			int posClassValueIndex = WekaUtils.getClassValueIndex(targetInstances, labelPos);
			
			for(int repeat=0;repeat<numRepeat;repeat++){
				
				targetInstances.randomize(new Random(repeat)); 
				targetInstances.stratify(folds);
				
				for(int fold=0;fold<folds;fold++){
					// source-within
					Instances train = targetInstances.trainCV(folds, fold);
					Instances test = targetInstances.testCV(folds, fold);
					
					// build and evaluate classifier
					classifier.buildClassifier(train);
					Evaluation withinEval = new Evaluation(targetInstances);
					withinEval.evaluateModel(classifier, test);
					
					lstFmeasure.add(withinEval.fMeasure(posClassValueIndex));
					lstAUC.add(withinEval.areaUnderROC(posClassValueIndex));
				}
			}
			
			System.out.println(projectName + "," + strClassifier + "," + ArrayListUtil.getAverage(lstFmeasure) + "," + ArrayListUtil.getAverage(lstAUC));
			
			System.err.println("Processed=" + (++predictionProcessed));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
