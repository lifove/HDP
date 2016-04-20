package hk.ust.cse.ipam.jc.crossprediction;


import hk.ust.cse.ipam.utils.FileUtil;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DriverWithCommonFeatures {

	static ExecutorService executor;
	static long predictionProcessed = 0;
	
	ProjectGroupInfo source;
	ProjectGroupInfo target;
	boolean headerRequired;
	int predictionID;
	
	HashMap<String,String> hashMapCoFeatures = new HashMap<String,String>();
	
	HashMap<String,String[]> projectGroups = new HashMap<String,String[]>();
	
	public DriverWithCommonFeatures(){
		
	}
	
	public DriverWithCommonFeatures(ProjectGroupInfo source,ProjectGroupInfo target, String analyzer,boolean headerRequired,int predictionID){
		this.source = source;
		this.target = target;
		this.headerRequired = headerRequired;
		this.predictionID = predictionID;
	}
	
	public void execute(){
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		projectGroups.put("AEEEM",AEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		projectGroups.put("ReLink",ReLink);
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
				"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		projectGroups.put("PROMISE",PROMISE);
			
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		projectGroups.put("NASA",NASA);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		projectGroups.put("SOFTLAB",SOFTLAB);
		
		loadCoFeatures();
		System.err.println("ThreadPoolSize: " + numOfThreads);
		executor = Executors.newFixedThreadPool(numOfThreads);
		runPredictionCombinations();
		executor.shutdown();
	}

	private void runPredictionCombinations() {
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo("data/AEEEM/", "class", "buggy", AEEEM);
				
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", ReLink);
		
		
		String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		ProjectGroupInfo projectGroupMIM = new ProjectGroupInfo("data/MIM/", "class", "buggy", MIM);
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		
		
		ProjectGroupInfo projectGroupPROMISE = new ProjectGroupInfo("data/promise/", "bug", "buggy", PROMISE);
		
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		
		ProjectGroupInfo projectGroupNASA = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", SOFTLAB);
		
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
		
		//String[] efforts = {"albrecht","china","maxwell","miyazaki94"};
		String[] efforts = {"albrecht"};
		ProjectGroupInfo projectGroupEffort = new ProjectGroupInfo("data/effort/", "label", "buggy", efforts);
		
		String[] severity = {"pitsB"};
		ProjectGroupInfo projectGroupSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity);
		
		String[] severity_fs = {"pitsB_FS"};
		ProjectGroupInfo projectGroupSeverity_fs = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity_fs);
		
		//String[] severityAll = {"pitsAll"};
		//ProjectGroupInfo projectGroupAllSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severityAll);
		
		//String[] medical = {"ovarian_61902"};
		String[] medical = {"breast_cancer"};
		ProjectGroupInfo projectMedical = new ProjectGroupInfo("data/Medical/", "Class", "M", medical); //Cancer
		
		String[] wine = {"winequality"};
		ProjectGroupInfo projectWine = new ProjectGroupInfo("data/Wine/", "quality", "buggy", wine);
		
		
			
		run(projectGroupAEEEM, projectGroupRelink,true,1);
		//run(projectGroupAEEEM, projectGroupMIM, false,2);
		run(projectGroupAEEEM, projectGroupPROMISE, false,3);
		run(projectGroupAEEEM, projectGroupNASA, false,4);
		run(projectGroupAEEEM, projectGroupSOFTLAB, false,5);
		
		run(projectGroupRelink, projectGroupAEEEM, false,6);
		//run(projectGroupRelink, projectGroupMIM, false,7);
		run(projectGroupRelink, projectGroupPROMISE, false,8);
		run(projectGroupRelink, projectGroupNASA, false,9);
		run(projectGroupRelink, projectGroupSOFTLAB, false,10);
		
		/*run(projectGroupMIM, projectGroupAEEEM, false,11);
		run(projectGroupMIM, projectGroupRelink, false,12);
		run(projectGroupMIM, projectGroupPROMISE, false,13);
		run(projectGroupMIM, projectGroupNASA, false,14);
		run(projectGroupMIM, projectGroupSOFTLAB, false,15);*/
		
		run(projectGroupPROMISE, projectGroupAEEEM, false,16);
		run(projectGroupPROMISE, projectGroupRelink, false,17);
		//run(projectGroupPROMISE, projectGroupMIM, false,18);
		run(projectGroupPROMISE, projectGroupNASA, false,19);
		run(projectGroupPROMISE, projectGroupSOFTLAB, false,20);
		
		run(projectGroupNASA, projectGroupAEEEM, false,21);
		run(projectGroupNASA, projectGroupRelink, false,22);
		//run(projectGroupNASA, projectGroupMIM, false,23);
		run(projectGroupNASA, projectGroupPROMISE, false,24);
		run(projectGroupNASA, projectGroupSOFTLAB, false,25);
		
		run(projectGroupSOFTLAB, projectGroupAEEEM, false,26);
		run(projectGroupSOFTLAB, projectGroupRelink, false,27);
		//run(projectGroupSOFTLAB, projectGroupMIM, false,28);
		run(projectGroupSOFTLAB, projectGroupPROMISE, false,29);
		run(projectGroupSOFTLAB, projectGroupNASA, false,30);
		
		/*run(projectGroupAllAEEEM, projectGroupRelink, true,1);
		run(projectGroupAllAEEEM, projectGroupMIM, false,2);
		run(projectGroupAllAEEEM, projectGroupPROMISE, false,3);
		run(projectGroupAllAEEEM, projectGroupNASA, false,4);
		run(projectGroupAllAEEEM, projectGroupSOFTLAB, false,5);
		
		run(projectGroupAllReLink, projectGroupAEEEM, false,6);
		run(projectGroupAllReLink, projectGroupMIM, false,7);
		run(projectGroupAllReLink, projectGroupPROMISE, false,8);
		run(projectGroupAllReLink, projectGroupNASA, false,9);
		run(projectGroupAllReLink, projectGroupSOFTLAB, false,10);
		
		run(projectGroupAllMIM, projectGroupAEEEM, false,11);
		run(projectGroupAllMIM, projectGroupRelink, false,12);
		run(projectGroupAllMIM, projectGroupPROMISE, false,13);
		run(projectGroupAllMIM, projectGroupNASA, false,14);
		run(projectGroupAllMIM, projectGroupSOFTLAB, false,15);
		
		run(projectGroupAllPROMISE, projectGroupAEEEM, false,16);
		run(projectGroupAllPROMISE, projectGroupRelink, false,17);
		run(projectGroupAllPROMISE, projectGroupMIM, false,18);
		run(projectGroupAllPROMISE, projectGroupNASA, false,19);
		run(projectGroupAllPROMISE, projectGroupSOFTLAB, false,20);
		
		run(projectGroupAllNASA, projectGroupAEEEM, false,21);
		run(projectGroupAllNASA, projectGroupRelink, false,22);
		run(projectGroupAllNASA, projectGroupMIM, false,23);
		run(projectGroupAllNASA, projectGroupPROMISE, false,24);
		run(projectGroupAllNASA, projectGroupSOFTLAB, false,25);
		
		run(projectGroupAllSOFTLAB, projectGroupAEEEM, false,26);
		run(projectGroupAllSOFTLAB, projectGroupRelink, false,27);
		run(projectGroupAllSOFTLAB, projectGroupMIM, false,28);
		run(projectGroupAllSOFTLAB, projectGroupPROMISE, false,29);
		run(projectGroupAllSOFTLAB, projectGroupNASA, false,30);*/
		
		// non-defect >> defect
		/*run(projectGroupEffort, projectGroupAEEEM, true,31);
		run(projectGroupEffort, projectGroupRelink, false,32);
		//run(projectGroupEffort, projectGroupMIM, false,33);
		run(projectGroupEffort, projectGroupPROMISE, false,34);
		run(projectGroupEffort, projectGroupNASA, false,35);
		run(projectGroupEffort, projectGroupSOFTLAB, false,36);
		
		/*run(projectGroupSeverity, projectGroupAEEEM, false,37);
		run(projectGroupSeverity, projectGroupRelink, false,38);
		run(projectGroupSeverity, projectGroupMIM, false,39);
		run(projectGroupSeverity, projectGroupPROMISE, false,40);
		run(projectGroupSeverity, projectGroupNASA, false,41);
		run(projectGroupSeverity, projectGroupSOFTLAB, false,42);
		
		run(projectGroupSeverity_fs, projectGroupAEEEM, false,37);
		run(projectGroupSeverity_fs, projectGroupRelink, false,38);
		//run(projectGroupSeverity_fs, projectGroupMIM, false,39);
		run(projectGroupSeverity_fs, projectGroupPROMISE, false,40);
		run(projectGroupSeverity_fs, projectGroupNASA, false,41);
		run(projectGroupSeverity_fs, projectGroupSOFTLAB, false,42);
		
		/*run(projectMedical, projectGroupAEEEM, false,43);
		run(projectMedical, projectGroupRelink, false,44);
		//run(projectMedical, projectGroupMIM, false,45);
		run(projectMedical, projectGroupPROMISE, false,46);
		run(projectMedical, projectGroupNASA, false,47);
		run(projectMedical, projectGroupSOFTLAB, false,48);
			
		run(projectWine, projectGroupAEEEM, false,49);
		run(projectWine, projectGroupRelink, false,50);
		//run(projectWine, projectGroupMIM, false,51);
		run(projectWine, projectGroupPROMISE, false,52);
		run(projectWine, projectGroupNASA, false,53);
		run(projectWine, projectGroupSOFTLAB, false,54);*/
		
	}

	private void loadCoFeatures() {
		// load CoFeatures
		ArrayList<String> lines = FileUtil.getLines("data" + File.separator + "commonfeatures.txt", false);
		
		for(String line:lines){
			String[] splitLine = line.split(":");
			if(splitLine.length!=2)
				continue;
			
			String[] projectGroups = splitLine[0].split(">>");
			
			ArrayList<String> combinations = getCombinations(projectGroups);
			
			for(String combi:combinations)
				hashMapCoFeatures.put(combi, splitLine[1]);
		}
	}
	
	private ArrayList<String> getCombinations(String[] projectGrps) {
		
		ArrayList<String> combis = new ArrayList<String>();
		
		String[] sources = projectGroups.get(projectGrps[0]);
		String[] targets = projectGroups.get(projectGrps[1]);
		
		for(String src:sources){
			for(String tgt:targets){
				combis.add(src + "_" + tgt);
			}
		}
		return combis;
	}

	/**
	 * @param args
	 */
	int numOfThreads = 1;
	boolean saveNewData = false;
	public static void main(String[] args) {
		DriverWithCommonFeatures runner = new DriverWithCommonFeatures();
		runner.numOfThreads = Integer.parseInt(args[0]);
		if(args.length>=2)
			runner.saveNewData = Boolean.parseBoolean(args[1]);
		runner.execute();
	}
	
	public void run(ProjectGroupInfo source,ProjectGroupInfo target, boolean headerRequired,int predictionID) {
		String header = headerRequired? "-header":"";
		//String mlAlgorithm = "weka.classifiers.trees.J48";
		//String mlAlgorithm = "weka.classifiers.trees.RandomForest";
		//String mlAlgorithm = "weka.classifiers.functions.Logistic";
		//String mlAlgorithm = "weka.classifiers.bayes.BayesNet";
		//String mlAlgorithm = "weka.classifiers.bayes.NaiveBayes";
		
		String[] mlAlgorithms = {//"weka.classifiers.trees.J48",
				//"weka.classifiers.trees.RandomForest",
				//"weka.classifiers.bayes.BayesNet",
				//"weka.classifiers.bayes.NaiveBayes",
				"weka.classifiers.functions.Logistic",
				//"weka.classifiers.functions.SMO",//};
				//"weka.classifiers.functions.MultilayerPerceptron"
				};
		
		DecimalFormat dec = new DecimalFormat("0.00");
		//for(int i=1;i<=10;i++){
			for(String mlAlgorithm:mlAlgorithms){
				for(String sourceProject:source.projects){
					for(String targetProject: target.projects){
						String sourcePath = source.dirPath + sourceProject +".arff";
						String targetPath = target.dirPath + targetProject +".arff";
						String options = "-sourcefile " +sourcePath +" " +
											"-srclabelname " + source.labelName + " -possrclabel " + source.posLabel + " " +
											"-targetfile " + targetPath + " " +
											"-tgtlabelname " + target.labelName + " -postgtlabel " + target.posLabel + " " +
											"-mlalgorithm " + mlAlgorithm + " " +
											//"-cutoff " + dec.format(1-i*0.1) +" " +
											"-cutoff " + dec.format(0.00) +" " +
											"-predictionID " + predictionID + " " +
											header;
						
						if(sourceProject.equals(targetProject))
							continue;
						
						String combinationKey = sourceProject + "_" + targetProject ;
						
						//TODO for SimulatorWIthStaticCoFeatures
						String coFeatures = hashMapCoFeatures.get(combinationKey);
						executor.execute(new SimulatorWIthCommonFeatures(options.split(" "),coFeatures,saveNewData));
						
						//TODO
						//executor.execute(new Simulator(options.split(" ")));
						
						header = "";
					}
				}
			}
		//}
    }
}
