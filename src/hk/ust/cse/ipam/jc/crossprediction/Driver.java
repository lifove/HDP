package hk.ust.cse.ipam.jc.crossprediction;


import hk.ust.cse.ipam.utils.FileUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Driver {

	ExecutorService executor;
	static long predictionProcessed = 0;
	
	ProjectGroupInfo source;
	ProjectGroupInfo target;
	boolean headerRequired;
	int predictionID;
	
	HashMap<String,String> hashMapCoFeatures = new HashMap<String,String>();
	
	public Driver(){
		
	}
	
	public Driver(ProjectGroupInfo source,ProjectGroupInfo target, String analyzer,boolean headerRequired,int predictionID){
		this.source = source;
		this.target = target;
		this.headerRequired = headerRequired;
		this.predictionID = predictionID;
	}
	
	public void execute(){
		loadCoFeatures();
		System.err.println("ThreadPoolSize: " + numOfThreads);
		executor = Executors.newFixedThreadPool(numOfThreads);
		runPredictionCombinations();
		executor.shutdown();
	}

	private void runPredictionCombinations() {
		/*String[] analyzers = {CoOccurrenceAnalyzer.Analyzer.PEARSONCORRELATION.toString(),
		CoOccurrenceAnalyzer.Analyzer.AVERAGEANDSTD.toString(),
		CoOccurrenceAnalyzer.Analyzer.TTESTPVALUE.toString(),
		CoOccurrenceAnalyzer.Analyzer.LABELEDAVERAGEANDSTD.toString(),
		CoOccurrenceAnalyzer.Analyzer.LABELEDPEARSONCORRELATION.toString(),
		CoOccurrenceAnalyzer.Analyzer.POSLABELEDAVERAGEANDSTD.toString(),
		CoOccurrenceAnalyzer.Analyzer.NEGLABELEDAVERAGEANDSTD.toString()};*/

		/*String[] analyzers = {
		//		"HSIC"
				//CoOccurrenceAnalyzer.Analyzer.MultiAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.ASAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.KSAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.UAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.PAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.TAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.PCoAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.AEDAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.AL1DAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.PKNAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.SKNAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.SCoAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.PIAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.UAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.TAnalyzer.toString(),
		//};*/
		
		//String[] analyzers = {
		//CoOccurrenceAnalyzer.Analyzer.PIAnalyzer.toString(),
		//CoOccurrenceAnalyzer.Analyzer.SemiPCoAnalyzer.toString()};
		//CoOccurrenceAnalyzer.Analyzer.PCoAnalyzer.toString()};
		//,CoOccurrenceAnalyzer.Analyzer.SemiASAnalyzer.toString()};
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo("data/AEEEM/", "class", "buggy", AEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", ReLink);
		
		String[] Apache = {"Apache"};
		ProjectGroupInfo apache = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", Apache);
		
		String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		ProjectGroupInfo projectGroupMIM = new ProjectGroupInfo("data/MIM/", "class", "buggy", MIM);
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		ProjectGroupInfo projectGroupPROMISE = new ProjectGroupInfo("data/promise/", "bug", "buggy", PROMISE);
		
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		ProjectGroupInfo projectGroupNASA = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", SOFTLAB);
		
		String[] NetGene = {"httpclient","jackrabbit","lucene","rhino"};
		ProjectGroupInfo projectGroupNetGene = new ProjectGroupInfo("data/NetGene/", "class", "buggy", NetGene);
		
		// merged data sets
		String[] allAEEEM = {"AEEEM"};
		ProjectGroupInfo projectGroupAllAEEEM = new ProjectGroupInfo("data/AEEEM/", "class", "buggy", allAEEEM);
		
		String[] allReLink = {"ReLink"};
		ProjectGroupInfo projectGroupAllReLink = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", allReLink);
		
		String[] allMIM = {"MIMAll"};
		ProjectGroupInfo projectGroupAllMIM = new ProjectGroupInfo("data/MIM/", "class", "buggy", allMIM);
		
		String[] allPROMISE = {"PROMISE"};
		ProjectGroupInfo projectGroupAllPROMISE = new ProjectGroupInfo("data/promise/", "bug", "buggy", allPROMISE);
		
		String[] allNASA = {"NASA"};
		ProjectGroupInfo projectGroupAllNASA = new ProjectGroupInfo("data/NASA/", "Defective", "Y", allNASA);
		
		String[] allSOFTLAB = {"SOFTLAB"};
		ProjectGroupInfo projectGroupAllSOFTLAB = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", allSOFTLAB);
		
		String[] Ar3 = {"ar3"};
		ProjectGroupInfo ar3 = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", Ar3);
		
		String[] efforts = {"albrecht","china","maxwell","miyazaki94"};
		//String[] efforts = {"albrecht"};
		ProjectGroupInfo projectGroupEffort = new ProjectGroupInfo("data/effort/", "label", "buggy", efforts);
		
		String[] datasetPrefix =  {"albrecht_random","china_random","maxwell_random","miyazaki94_random"};
		String[] effortsRandom = getRandomDatasets(datasetPrefix);
		ProjectGroupInfo projectGroupEffortRandom = new ProjectGroupInfo("data/effort/", "label", "buggy", effortsRandom);
		
		String[] severity = {"pitsB"};
		ProjectGroupInfo projectGroupSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity);
		
		String[] severity_fs = {"pitsB_FS"};
		ProjectGroupInfo projectGroupSeverity_fs = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity_fs);
		
		//String[] severityAll = {"pitsAll"};
		//ProjectGroupInfo projectGroupAllSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severityAll);
		
		//String[] medical = {"ovarian_61902"};
		String[] medical = {"breast_cancer"};
		ProjectGroupInfo projectMedical = new ProjectGroupInfo("data/Medical/", "Class", "M", medical); //Cancer
		
		String[] datasetPrefix2 = {"breast_cancer_random"};
		String[] medicalRandom = getRandomDatasets(datasetPrefix2);
		ProjectGroupInfo projectMedicalRandom = new ProjectGroupInfo("data/Medical/", "label", "buggy", medicalRandom); //Cancer
		
		String[] wine = {"winequality"};
		ProjectGroupInfo projectWine = new ProjectGroupInfo("data/Wine/", "quality", "buggy", wine);
		
		String[] datasetPrefix3 = {"winequality_random"};
		String[] wineRandom = getRandomDatasets(datasetPrefix3);
		ProjectGroupInfo projectWineRandom = new ProjectGroupInfo("data/Wine/", "label", "buggy", wineRandom);
		
		String[] src = {"Apache"};
		ProjectGroupInfo source = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", src);
		
		//String[] tgt = {"ar1"};
		//ProjectGroupInfo target = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", tgt);
		String[] tgt = {"ML"};
		ProjectGroupInfo target = new ProjectGroupInfo("data/AEEEM/", "class", "buggy", tgt);
		
		for(int i=0; i < analyzers.length;i++){
			System.err.println("Analyzer: " + analyzers[i]);
			// defect >> defect
			
			//run(source, target, analyzers[i], true,1);
			
			/*run(projectGroupAEEEM, apache, analyzers[i], true,1);
			run(projectGroupPROMISE, apache, analyzers[i], false,17);
			run(projectGroupNASA, apache, analyzers[i], false,22);*/
			//run(ar3, apache, analyzers[i], true,27);
			
			/*run(projectGroupAEEEM, projectGroupRelink, analyzers[i], true,1);
			run(projectGroupPROMISE, projectGroupRelink, analyzers[i], false,17);
			run(projectGroupNASA, projectGroupRelink, analyzers[i], false,22);
			run(projectGroupSOFTLAB, projectGroupRelink, analyzers[i], false,22);*/
			
			run(projectGroupAEEEM, projectGroupRelink, analyzers[i], true,1);
			//run(projectGroupAEEEM, projectGroupMIM, analyzers[i], false,2);
			run(projectGroupAEEEM, projectGroupPROMISE, analyzers[i], false,3);
			run(projectGroupAEEEM, projectGroupNASA, analyzers[i], false,4);
			run(projectGroupAEEEM, projectGroupSOFTLAB, analyzers[i], false,5);
			//run(projectGroupAEEEM, projectGroupNetGene, analyzers[i], false,30);
			
			
			run(projectGroupRelink, projectGroupAEEEM, analyzers[i], false,6);
			//run(projectGroupRelink, projectGroupMIM, analyzers[i], false,7);
			run(projectGroupRelink, projectGroupPROMISE, analyzers[i], false,8);
			run(projectGroupRelink, projectGroupNASA, analyzers[i], false,9);
			run(projectGroupRelink, projectGroupSOFTLAB, analyzers[i], false,10);
			//run(projectGroupRelink, projectGroupNetGene, analyzers[i], false,31);
			
			/*run(projectGroupMIM, projectGroupAEEEM, analyzers[i], false,11);
			run(projectGroupMIM, projectGroupRelink, analyzers[i], false,12);
			run(projectGroupMIM, projectGroupPROMISE, analyzers[i], false,13);
			run(projectGroupMIM, projectGroupNASA, analyzers[i], false,14);
			run(projectGroupMIM, projectGroupSOFTLAB, analyzers[i], false,15);*/
			
			run(projectGroupPROMISE, projectGroupAEEEM, analyzers[i], false,16);
			run(projectGroupPROMISE, projectGroupRelink, analyzers[i], false,17);
			//run(projectGroupPROMISE, projectGroupMIM, analyzers[i], false,18);
			run(projectGroupPROMISE, projectGroupNASA, analyzers[i], false,19);
			run(projectGroupPROMISE, projectGroupSOFTLAB, analyzers[i], false,20);
			//run(projectGroupPROMISE, projectGroupNetGene, analyzers[i], false,32);
			
			run(projectGroupNASA, projectGroupAEEEM, analyzers[i], false,21);
			run(projectGroupNASA, projectGroupRelink, analyzers[i], false,22);
			//run(projectGroupNASA, projectGroupMIM, analyzers[i], false,23);
			run(projectGroupNASA, projectGroupPROMISE, analyzers[i], false,24);
			run(projectGroupNASA, projectGroupSOFTLAB, analyzers[i], false,25);
			//run(projectGroupNASA, projectGroupNetGene, analyzers[i], false,33);
			
			run(projectGroupSOFTLAB, projectGroupAEEEM, analyzers[i], false,26);
			run(projectGroupSOFTLAB, projectGroupRelink, analyzers[i], false,27);
			//run(projectGroupSOFTLAB, projectGroupMIM, analyzers[i], false,28);
			run(projectGroupSOFTLAB, projectGroupPROMISE, analyzers[i], false,29);
			run(projectGroupSOFTLAB, projectGroupNASA, analyzers[i], false,30);
			//run(projectGroupSOFTLAB, projectGroupNetGene, analyzers[i], false,34);
			
			/*run(projectGroupNetGene, projectGroupAEEEM, analyzers[i], false,35);
			run(projectGroupNetGene, projectGroupRelink, analyzers[i], false,36);
			//run(projectGroupSOFTLAB, projectGroupMIM, analyzers[i], false,28);
			run(projectGroupNetGene, projectGroupPROMISE, analyzers[i], false,37);
			run(projectGroupNetGene, projectGroupNASA, analyzers[i], false,38);
			run(projectGroupNetGene, projectGroupSOFTLAB, analyzers[i], false,39);
			*/
			/*run(projectGroupAllAEEEM, projectGroupRelink, analyzers[i], true,1);
			run(projectGroupAllAEEEM, projectGroupMIM, analyzers[i], false,2);
			run(projectGroupAllAEEEM, projectGroupPROMISE, analyzers[i], false,3);
			run(projectGroupAllAEEEM, projectGroupNASA, analyzers[i], false,4);
			run(projectGroupAllAEEEM, projectGroupSOFTLAB, analyzers[i], false,5);
			
			run(projectGroupAllReLink, projectGroupAEEEM, analyzers[i], false,6);
			run(projectGroupAllReLink, projectGroupMIM, analyzers[i], false,7);
			run(projectGroupAllReLink, projectGroupPROMISE, analyzers[i], false,8);
			run(projectGroupAllReLink, projectGroupNASA, analyzers[i], false,9);
			run(projectGroupAllReLink, projectGroupSOFTLAB, analyzers[i], false,10);
			
			run(projectGroupAllMIM, projectGroupAEEEM, analyzers[i], false,11);
			run(projectGroupAllMIM, projectGroupRelink, analyzers[i], false,12);
			run(projectGroupAllMIM, projectGroupPROMISE, analyzers[i], false,13);
			run(projectGroupAllMIM, projectGroupNASA, analyzers[i], false,14);
			run(projectGroupAllMIM, projectGroupSOFTLAB, analyzers[i], false,15);
			
			run(projectGroupAllPROMISE, projectGroupAEEEM, analyzers[i], false,16);
			run(projectGroupAllPROMISE, projectGroupRelink, analyzers[i], false,17);
			run(projectGroupAllPROMISE, projectGroupMIM, analyzers[i], false,18);
			run(projectGroupAllPROMISE, projectGroupNASA, analyzers[i], false,19);
			run(projectGroupAllPROMISE, projectGroupSOFTLAB, analyzers[i], false,20);
			
			run(projectGroupAllNASA, projectGroupAEEEM, analyzers[i], false,21);
			run(projectGroupAllNASA, projectGroupRelink, analyzers[i], false,22);
			run(projectGroupAllNASA, projectGroupMIM, analyzers[i], false,23);
			run(projectGroupAllNASA, projectGroupPROMISE, analyzers[i], false,24);
			run(projectGroupAllNASA, projectGroupSOFTLAB, analyzers[i], false,25);
			
			run(projectGroupAllSOFTLAB, projectGroupAEEEM, analyzers[i], false,26);
			run(projectGroupAllSOFTLAB, projectGroupRelink, analyzers[i], false,27);
			run(projectGroupAllSOFTLAB, projectGroupMIM, analyzers[i], false,28);
			run(projectGroupAllSOFTLAB, projectGroupPROMISE, analyzers[i], false,29);
			run(projectGroupAllSOFTLAB, projectGroupNASA, analyzers[i], false,30);*/
			
			// non-defect >> defect
			/*run(projectGroupEffort, projectGroupAEEEM, analyzers[i], true,31);
			run(projectGroupEffort, projectGroupRelink, analyzers[i], false,32);
			//run(projectGroupEffort, projectGroupMIM, analyzers[i], false,33);
			run(projectGroupEffort, projectGroupPROMISE, analyzers[i], false,34);
			run(projectGroupEffort, projectGroupNASA, analyzers[i], false,35);
			run(projectGroupEffort, projectGroupSOFTLAB, analyzers[i], false,36);
			
			/*run(projectGroupSeverity, projectGroupAEEEM, analyzers[i], false,37);
			run(projectGroupSeverity, projectGroupRelink, analyzers[i], false,38);
			run(projectGroupSeverity, projectGroupMIM, analyzers[i], false,39);
			run(projectGroupSeverity, projectGroupPROMISE, analyzers[i], false,40);
			run(projectGroupSeverity, projectGroupNASA, analyzers[i], false,41);
			run(projectGroupSeverity, projectGroupSOFTLAB, analyzers[i], false,42);*/
			
			/*run(projectGroupSeverity_fs, projectGroupAEEEM, analyzers[i], false,37);
			run(projectGroupSeverity_fs, projectGroupRelink, analyzers[i], false,38);
			//run(projectGroupSeverity_fs, projectGroupMIM, analyzers[i], false,39);
			run(projectGroupSeverity_fs, projectGroupPROMISE, analyzers[i], false,40);
			run(projectGroupSeverity_fs, projectGroupNASA, analyzers[i], false,41);
			run(projectGroupSeverity_fs, projectGroupSOFTLAB, analyzers[i], false,42);*/
			
			/*run(projectMedical, projectGroupAEEEM, analyzers[i], false,43);
			run(projectMedical, projectGroupRelink, analyzers[i], false,44);
			//run(projectMedical, projectGroupMIM, analyzers[i], false,45);
			run(projectMedical, projectGroupPROMISE, analyzers[i], false,46);
			run(projectMedical, projectGroupNASA, analyzers[i], false,47);
			run(projectMedical, projectGroupSOFTLAB, analyzers[i], false,48);
				
			run(projectWine, projectGroupAEEEM, analyzers[i], false,49);
			run(projectWine, projectGroupRelink, analyzers[i], false,50);
			//run(projectWine, projectGroupMIM, analyzers[i], false,51);
			run(projectWine, projectGroupPROMISE, analyzers[i], false,52);
			run(projectWine, projectGroupNASA, analyzers[i], false,53);
			run(projectWine, projectGroupSOFTLAB, analyzers[i], false,54);*/
			
			// non-defect random >> defect
			/*run(projectGroupEffortRandom, projectGroupAEEEM, analyzers[i], true,31);
			run(projectGroupEffortRandom, projectGroupRelink, analyzers[i], false,32);
			//run(projectGroupEffort, projectGroupMIM, analyzers[i], false,33);
			run(projectGroupEffortRandom, projectGroupPROMISE, analyzers[i], false,34);
			run(projectGroupEffortRandom, projectGroupNASA, analyzers[i], false,35);
			run(projectGroupEffortRandom, projectGroupSOFTLAB, analyzers[i], false,36);

			/*run(projectGroupSeverity, projectGroupAEEEM, analyzers[i], false,37);
						run(projectGroupSeverity, projectGroupRelink, analyzers[i], false,38);
						run(projectGroupSeverity, projectGroupMIM, analyzers[i], false,39);
						run(projectGroupSeverity, projectGroupPROMISE, analyzers[i], false,40);
						run(projectGroupSeverity, projectGroupNASA, analyzers[i], false,41);
						run(projectGroupSeverity, projectGroupSOFTLAB, analyzers[i], false,42);*/

			/*run(projectGroupSeverity_fs, projectGroupAEEEM, analyzers[i], false,37);
						run(projectGroupSeverity_fs, projectGroupRelink, analyzers[i], false,38);
						//run(projectGroupSeverity_fs, projectGroupMIM, analyzers[i], false,39);
						run(projectGroupSeverity_fs, projectGroupPROMISE, analyzers[i], false,40);
						run(projectGroupSeverity_fs, projectGroupNASA, analyzers[i], false,41);
						run(projectGroupSeverity_fs, projectGroupSOFTLAB, analyzers[i], false,42);*/

			/*run(projectMedicalRandom, projectGroupAEEEM, analyzers[i], false,43);
			run(projectMedicalRandom, projectGroupRelink, analyzers[i], false,44);
			//run(projectMedical, projectGroupMIM, analyzers[i], false,45);
			run(projectMedicalRandom, projectGroupPROMISE, analyzers[i], false,46);
			run(projectMedicalRandom, projectGroupNASA, analyzers[i], false,47);
			run(projectMedicalRandom, projectGroupSOFTLAB, analyzers[i], false,48);

			run(projectWineRandom, projectGroupAEEEM, analyzers[i], false,49);
			run(projectWineRandom, projectGroupRelink, analyzers[i], false,50);
			//run(projectWine, projectGroupMIM, analyzers[i], false,51);
			run(projectWineRandom, projectGroupPROMISE, analyzers[i], false,52);
			run(projectWineRandom, projectGroupNASA, analyzers[i], false,53);
			run(projectWineRandom, projectGroupSOFTLAB, analyzers[i], false,54);*/
		}
	}

	private String[] getRandomDatasets(String[] datasetPrefix) {
		
		int numDatasets = 10;
		
		String[] datasetNames = new String[datasetPrefix.length*numDatasets];
		
		int itr=0;
		for(String prefix:datasetPrefix){
			for(int i=1;i<=numDatasets;i++){
				datasetNames[itr] = prefix + "_" + String.format("%04d", i);
				itr++;
			}
		}
		return datasetNames;
	}

	private void loadCoFeatures() {
		// load CoFeatures
		ArrayList<String> lines = FileUtil.getLines("data" + File.separator + staticCoFeaturesFilePath, false);
		
		if(staticCoFeaturesFilePath.equals("") || lines.size()<=0){
			System.err.println("No cofeature data! Please check the cofeature file path");
			System.exit(0);
		}
		
		for(String line:lines){
			String[] splitLine = line.split(":");
			if(splitLine.length!=3)
				continue;
			hashMapCoFeatures.put(splitLine[0], splitLine[2]);
		}
	}
	
	
	/**
	 * @param args
	 */
	int numOfThreads = 1;
	boolean saveNewData = false;
	boolean verbose = false;
	String staticCoFeaturesFilePath = "";
	String[] analyzers;
	int startCutoffIndex = 0; // inclusive
	int endCutoffIndex = 10; // inclusive
	float interval = 0.05f;
	String mlAlgs = "weka.classifiers.functions.Logistic";
	String fsOption = "none"; // none, acc, auc, f-meas
	int folds = 2;
	int repeat = 500;
	public static void main(String[] args) {
		Driver runner = new Driver();
		runner.numOfThreads = Integer.parseInt(args[0]);
		if(args.length>=2)
			runner.saveNewData = Boolean.parseBoolean(args[1]);
		
		if(args.length>=3)
			runner.staticCoFeaturesFilePath = args[2];
		
		runner.analyzers = args[3].split(","); // get analyzer names separated by ",";
		
		runner.interval = Float.parseFloat(args[6]);
		
		runner.startCutoffIndex = (int) ((Float.parseFloat(args[4]))/runner.interval);
		runner.endCutoffIndex = (int) ((Float.parseFloat(args[5]))/runner.interval);
		
		runner.mlAlgs = args[7]; // get ml algorithm names seperated by ",";
		
		runner.fsOption = args[8].equals("none")?"":"_FS_" + args[8];
		
		runner.verbose = Boolean.parseBoolean(args[9]);
		
		runner.folds = Integer.parseInt(args[10]);
		runner.repeat = Integer.parseInt(args[11]);
		
		runner.execute();
	}
	
	public void run(ProjectGroupInfo source,ProjectGroupInfo target, String analyzer,boolean headerRequired,int predictionID) {
		String header = headerRequired? "-header":"";
		//String mlAlgorithm = "weka.classifiers.trees.J48";
		//String mlAlgorithm = "weka.classifiers.trees.RandomForest";
		//String mlAlgorithm = "weka.classifiers.functions.Logistic";
		//String mlAlgorithm = "weka.classifiers.bayes.BayesNet";
		//String mlAlgorithm = "weka.classifiers.bayes.NaiveBayes";
		
		/*String[] mlAlgorithms = {//"weka.classifiers.trees.J48",
				//"weka.classifiers.trees.RandomForest",
				//"weka.classifiers.bayes.BayesNet",
				//"weka.classifiers.bayes.NaiveBayes",
				"weka.classifiers.functions.Logistic",
				//"weka.classifiers.functions.SMO",//};
				//"weka.classifiers.functions.MultilayerPerceptron"
				};*/
		
		String[] mlAlgorithms = mlAlgs.split(",");
		
		DecimalFormat dec = new DecimalFormat("0.00");
		for(int i=startCutoffIndex;i<=endCutoffIndex;i++){
			for(String mlAlgorithm:mlAlgorithms){
				for(String sourceProject:source.projects){
					for(String targetProject: target.projects){
						String sourcePath = source.dirPath + sourceProject + fsOption +".arff";
						String targetPath = target.dirPath + targetProject +".arff";
						String options = "-sourcefile " +sourcePath +" " +
											"-srclabelname " + source.labelName + " -possrclabel " + source.posLabel + " " +
											"-targetfile " + targetPath + " " +
											"-tgtlabelname " + target.labelName + " -postgtlabel " + target.posLabel + " -analyzer " + analyzer + " " +
											"-mlalgorithm " + mlAlgorithm + " " +
											"-cutoff " + dec.format(i*interval) +" " +
											//"-cutoff " + dec.format(0.90) +" " +
											"-predictionID " + predictionID + " " +
											header;
						
						if(sourceProject.equals(targetProject))
							continue;
						
						String combinationKey = sourceProject + fsOption + "_" + targetProject + "_" + analyzer;
						
						//TODO for SimulatorWIthStaticCoFeatures
						String coFeatures = hashMapCoFeatures.get(combinationKey);
						executor.execute(new SimulatorWIthStaticCoFeatures(options.split(" "),coFeatures,saveNewData,verbose,folds,repeat));
						
						//TODO
						//executor.execute(new Simulator(options.split(" ")));
						
						header = "";
					}
				}
			}
		}
    }
}
