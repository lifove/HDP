package hk.ust.cse.ipam.jc.crossprediction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import weka.core.Instances;
import hk.ust.cse.ipam.jc.crossprediction.data.MatchedAttribute;
import hk.ust.cse.ipam.jc.crossprediction.util.Utils;
import hk.ust.cse.ipam.utils.FileUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

public class CoFeatureGenerator implements Runnable{
	
	Instances sourceInstances,targetOriginalInstances;
	String sourcePath;
	String sourceLabelName;
	String sourceLabelPos;

	String targetPath;
	String targetLabelName;
	String targetLabelPos;
	
	String coOccurrenceAnalyzerOption;
	static double cutoff = 0.0;
	
	static ExecutorService executor;
	static long predictionProcessed = 0;
	
	static boolean useLBMFilter = true;
	static boolean useDMFilter = true;
	
	static String fsOption = "none"; // none, auc, f, acc
	
	static HashMap<String,HashMap<String,String>> allExsitingMatchedData;
	
	static int folds=2;
	static int repeat=500;

	/**
	 * @param args
	 */
	public static void main(String[] args){
		int numOfThreads = Integer.parseInt(args[0]);
		System.err.println("ThreadPoolSize: " + numOfThreads);
		String[] analyzers = args[1].split(","); // analyzer names separated by ",";
		useLBMFilter = Boolean.parseBoolean(args[2]);
		useDMFilter = Boolean.parseBoolean(args[3]);
		
		cutoff = Double.parseDouble(args[4]);
		
		fsOption = args[5].equals("none")?"":"_FS_" + args[5];
		
		String dataPathForAllMatched = args[6];
		
		folds = Integer.parseInt(args[7]);
		repeat = Integer.parseInt(args[8]);
		
		loadExistingMatchingScores(dataPathForAllMatched);
		
		/*String[] analyzers = {
				CoOccurrenceAnalyzer.Analyzer.KSAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.ASAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.TAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.PCoAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.SCoAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.PIAnalyzer.toString(),
				//CoOccurrenceAnalyzer.Analyzer.SCoAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.UAnalyzer.toString(),
				CoOccurrenceAnalyzer.Analyzer.PAnalyzer.toString(),
		};*/
		
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
		
		String[] NASA2 = {"jm1"};
		ProjectGroupInfo projectGroupNASA2 = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA2);
		
		String[] NASA3 = {"pc2"};
		ProjectGroupInfo projectGroupNASA3 = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA3);
		
		String[] NASA4 = {"pc5","mc1"};
		ProjectGroupInfo projectGroupNASA4 = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA4);
		
		String[] NASA5 = {"mc2","kc3"};
		ProjectGroupInfo projectGroupNASA5 = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA5);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", SOFTLAB);
		
		String[] NetGene = {"httpclient","jackrabbit","lucene","rhino"};
		ProjectGroupInfo projectGroupNetGene = new ProjectGroupInfo("data/NetGene/", "class", "buggy", NetGene);
		
		
		String[] PATCH = {"eclipse","mozilla","eclipseNmozilla"};
		ProjectGroupInfo projectGroup5 = new ProjectGroupInfo("data/patch/", "flag", "false", PATCH);
		
		//String[] group6 = {"eclipse_jh","mozilla_jh","eclipseNmozilla_jh"};
		String[] PATCH_JH = {"mozilla_jh"};
		ProjectGroupInfo projectGroup6 = new ProjectGroupInfo("data/patch/", "flag", "FALSE", PATCH_JH);
		
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
		
		String[] efforts = {"albrecht","china","maxwell","miyazaki94"};
		//String[] efforts = {"albrecht"};
		ProjectGroupInfo projectGroupEffort = new ProjectGroupInfo("data/effort/", "label", "buggy", efforts);
		
		String[] datasetPrefix =  {"albrecht_random","china_random","maxwell_random","miyazaki94_random"};
		String[] effortsRandom = getRandomDatasets(datasetPrefix);
		//String[] efforts = {"albrecht"};
		ProjectGroupInfo projectGroupEffortRandom = new ProjectGroupInfo("data/effort/", "label", "buggy", effortsRandom);
		
		String[] severity = {"pitsB"};
		ProjectGroupInfo projectGroupSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity);
		
		String[] severity_fs = {"pitsB_FS"};
		ProjectGroupInfo projectGroupSeverity_fs = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity_fs);
		
		String[] severity_fsRandom = {"pitsB_FS_random"};
		ProjectGroupInfo projectGroupSeverity_fsRandom = new ProjectGroupInfo("data/Severity/", "label", "buggy", severity_fsRandom);
		
		String[] severityAll = {"pitsAll"};
		ProjectGroupInfo projectGroupAllSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severityAll);
		
		//String[] medical = {"ovarian_61902"};
		String[] medical = {"breast_cancer"};
		ProjectGroupInfo projectMedical = new ProjectGroupInfo("data/Medical/", "Class", "M", medical);
		
		String[] datasetPrefix2 = {"breast_cancer_random"};
		String[] medicalRandom = getRandomDatasets(datasetPrefix2);
		ProjectGroupInfo projectMedicalRandom = new ProjectGroupInfo("data/Medical/", "label", "buggy", medicalRandom);
		
		String[] wine = {"winequality"};
		ProjectGroupInfo projectWine = new ProjectGroupInfo("data/Wine/", "quality", "buggy", wine);
		
		String[] datasetPrefix3 = {"winequality_random"};
		String[] wineRandom = getRandomDatasets(datasetPrefix3);
		ProjectGroupInfo projectWineRandom = new ProjectGroupInfo("data/Wine/", "label", "buggy", wineRandom);
		
		//ExecutorService executorPre = Executors.newFixedThreadPool(18);
		executor = Executors.newFixedThreadPool(numOfThreads);
		
		for(int i=0; i < analyzers.length;i++){
			
			threadRunner(projectGroupAEEEM, projectGroupRelink, analyzers[i], true,1);
			//threadRunner(projectGroupAEEEM, projectGroupMIM, analyzers[i], false,2);
			threadRunner(projectGroupAEEEM, projectGroupPROMISE, analyzers[i], false,3);
			threadRunner(projectGroupAEEEM, projectGroupNASA, analyzers[i], false,4);
			threadRunner(projectGroupAEEEM, projectGroupNASA2, analyzers[i], false,41);
			threadRunner(projectGroupAEEEM, projectGroupNASA3, analyzers[i], false,42);
			threadRunner(projectGroupAEEEM, projectGroupNASA4, analyzers[i], false,43);
			threadRunner(projectGroupAEEEM, projectGroupNASA5, analyzers[i], false,44);
			threadRunner(projectGroupAEEEM, projectGroupSOFTLAB, analyzers[i], false,5);
			//threadRunner(projectGroupAEEEM, projectGroupNetGene, analyzers[i], false,31);
			
			threadRunner(projectGroupRelink, projectGroupAEEEM, analyzers[i], false,6);
			//threadRunner(projectGroupRelink, projectGroupMIM, analyzers[i], false,7);
			threadRunner(projectGroupRelink, projectGroupPROMISE, analyzers[i], false,8);
			threadRunner(projectGroupRelink, projectGroupNASA, analyzers[i], false,9);
			threadRunner(projectGroupRelink, projectGroupNASA2, analyzers[i], false,91);
			threadRunner(projectGroupRelink, projectGroupNASA3, analyzers[i], false,92);
			threadRunner(projectGroupRelink, projectGroupNASA4, analyzers[i], false,93);
			threadRunner(projectGroupRelink, projectGroupNASA5, analyzers[i], false,94);
			threadRunner(projectGroupRelink, projectGroupSOFTLAB, analyzers[i], false,10);
			//threadRunner(projectGroupRelink, projectGroupNetGene, analyzers[i], false,32);
			
			/*threadRunner(projectGroupMIM, projectGroupAEEEM, analyzers[i], false,11);
			threadRunner(projectGroupMIM, projectGroupRelink, analyzers[i], false,12);
			threadRunner(projectGroupMIM, projectGroupPROMISE, analyzers[i], false,13);
			threadRunner(projectGroupMIM, projectGroupNASA, analyzers[i], false,14);
			threadRunner(projectGroupMIM, projectGroupSOFTLAB, analyzers[i], false,15);*/
			
			threadRunner(projectGroupPROMISE, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupPROMISE, projectGroupRelink, analyzers[i], false,17);
			//threadRunner(projectGroupPROMISE, projectGroupMIM, analyzers[i], false,18);
			threadRunner(projectGroupPROMISE, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupPROMISE, projectGroupNASA2, analyzers[i], false,191);
			threadRunner(projectGroupPROMISE, projectGroupNASA3, analyzers[i], false,192);
			threadRunner(projectGroupPROMISE, projectGroupNASA4, analyzers[i], false,193);
			threadRunner(projectGroupPROMISE, projectGroupNASA5, analyzers[i], false,194);
			threadRunner(projectGroupPROMISE, projectGroupSOFTLAB, analyzers[i], false,20);
			//threadRunner(projectGroupPROMISE, projectGroupNetGene, analyzers[i], false,33);
			
			threadRunner(projectGroupNASA, projectGroupAEEEM, analyzers[i], false,21);
			threadRunner(projectGroupNASA, projectGroupRelink, analyzers[i], false,22);
			//threadRunner(projectGroupNASA, projectGroupMIM, analyzers[i], false,23);
			threadRunner(projectGroupNASA, projectGroupPROMISE, analyzers[i], false,24);
			threadRunner(projectGroupNASA, projectGroupSOFTLAB, analyzers[i], false,25);
			threadRunner(projectGroupNASA, projectGroupNASA2, analyzers[i], false,25);
			threadRunner(projectGroupNASA, projectGroupNASA3, analyzers[i], false,25);
			threadRunner(projectGroupNASA, projectGroupNASA4, analyzers[i], false,25);
			threadRunner(projectGroupNASA, projectGroupNASA5, analyzers[i], false,25);
			//threadRunner(projectGroupNASA, projectGroupNetGene, analyzers[i], false,34);
			
			threadRunner(projectGroupSOFTLAB, projectGroupAEEEM, analyzers[i], false,26);
			threadRunner(projectGroupSOFTLAB, projectGroupRelink, analyzers[i], false,27);
			//threadRunner(projectGroupSOFTLAB, projectGroupMIM, analyzers[i], false,28);
			threadRunner(projectGroupSOFTLAB, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectGroupSOFTLAB, projectGroupNASA, analyzers[i], false,30);
			threadRunner(projectGroupSOFTLAB, projectGroupNASA2, analyzers[i], false,30);
			threadRunner(projectGroupSOFTLAB, projectGroupNASA3, analyzers[i], false,30);
			threadRunner(projectGroupSOFTLAB, projectGroupNASA4, analyzers[i], false,30);
			threadRunner(projectGroupSOFTLAB, projectGroupNASA5, analyzers[i], false,30);
			//threadRunner(projectGroupSOFTLAB, projectGroupNetGene, analyzers[i], false,35);
			
			threadRunner(projectGroupNASA2, projectGroupAEEEM, analyzers[i], false,21);
			threadRunner(projectGroupNASA2, projectGroupRelink, analyzers[i], false,22);
			threadRunner(projectGroupNASA2, projectGroupNASA, analyzers[i], false,24);
			threadRunner(projectGroupNASA2, projectGroupNASA3, analyzers[i], false,24);
			threadRunner(projectGroupNASA2, projectGroupNASA4, analyzers[i], false,24);
			threadRunner(projectGroupNASA2, projectGroupNASA5, analyzers[i], false,24);
			threadRunner(projectGroupNASA2, projectGroupPROMISE, analyzers[i], false,24);
			threadRunner(projectGroupNASA2, projectGroupSOFTLAB, analyzers[i], false,25);
			
			threadRunner(projectGroupNASA3, projectGroupAEEEM, analyzers[i], false,21);
			threadRunner(projectGroupNASA3, projectGroupRelink, analyzers[i], false,22);
			threadRunner(projectGroupNASA3, projectGroupNASA, analyzers[i], false,24);
			threadRunner(projectGroupNASA3, projectGroupNASA2, analyzers[i], false,24);
			threadRunner(projectGroupNASA3, projectGroupNASA4, analyzers[i], false,24);
			threadRunner(projectGroupNASA3, projectGroupNASA5, analyzers[i], false,24);
			threadRunner(projectGroupNASA3, projectGroupPROMISE, analyzers[i], false,24);
			threadRunner(projectGroupNASA3, projectGroupSOFTLAB, analyzers[i], false,25);
			
			threadRunner(projectGroupNASA4, projectGroupAEEEM, analyzers[i], false,21);
			threadRunner(projectGroupNASA4, projectGroupRelink, analyzers[i], false,22);
			threadRunner(projectGroupNASA4, projectGroupNASA, analyzers[i], false,24);
			threadRunner(projectGroupNASA4, projectGroupNASA2, analyzers[i], false,24);
			threadRunner(projectGroupNASA4, projectGroupNASA3, analyzers[i], false,24);
			threadRunner(projectGroupNASA4, projectGroupNASA5, analyzers[i], false,24);
			threadRunner(projectGroupNASA4, projectGroupPROMISE, analyzers[i], false,24);
			threadRunner(projectGroupNASA4, projectGroupSOFTLAB, analyzers[i], false,25);
			
			threadRunner(projectGroupNASA5, projectGroupAEEEM, analyzers[i], false,21);
			threadRunner(projectGroupNASA5, projectGroupRelink, analyzers[i], false,22);
			threadRunner(projectGroupNASA5, projectGroupNASA, analyzers[i], false,24);
			threadRunner(projectGroupNASA5, projectGroupNASA2, analyzers[i], false,24);
			threadRunner(projectGroupNASA5, projectGroupNASA3, analyzers[i], false,24);
			threadRunner(projectGroupNASA5, projectGroupNASA4, analyzers[i], false,24);
			threadRunner(projectGroupNASA5, projectGroupPROMISE, analyzers[i], false,24);
			threadRunner(projectGroupNASA5, projectGroupSOFTLAB, analyzers[i], false,25);
			
			/*threadRunner(projectGroupNetGene, projectGroupAEEEM, analyzers[i], false,36);
			threadRunner(projectGroupNetGene, projectGroupRelink, analyzers[i], false,37);
			//threadRunner(projectGroupSOFTLAB, projectGroupMIM, analyzers[i], false,28);
			threadRunner(projectGroupNetGene, projectGroupPROMISE, analyzers[i], false,38);
			threadRunner(projectGroupNetGene, projectGroupNASA, analyzers[i], false,39);
			threadRunner(projectGroupNetGene, projectGroupSOFTLAB, analyzers[i], false,40);*/
			
			
			// defect >> defect
			/*threadRunner(projectGroupAllAEEEM, projectGroupRelink, analyzers[i], true,1);
			threadRunner(projectGroupAllAEEEM, projectGroupMIM, analyzers[i], false,2);
			threadRunner(projectGroupAllAEEEM, projectGroupPROMISE, analyzers[i], false,3);
			threadRunner(projectGroupAllAEEEM, projectGroupNASA, analyzers[i], false,4);
			threadRunner(projectGroupAllAEEEM, projectGroupSOFTLAB, analyzers[i], false,5);
			
			threadRunner(projectGroupAllReLink, projectGroupAEEEM, analyzers[i], false,6);
			threadRunner(projectGroupAllReLink, projectGroupMIM, analyzers[i], false,7);
			threadRunner(projectGroupAllReLink, projectGroupPROMISE, analyzers[i], false,8);
			threadRunner(projectGroupAllReLink, projectGroupNASA, analyzers[i], false,9);
			threadRunner(projectGroupAllReLink, projectGroupSOFTLAB, analyzers[i], false,10);
			
			threadRunner(projectGroupAllMIM, projectGroupAEEEM, analyzers[i], false,11);
			threadRunner(projectGroupAllMIM, projectGroupRelink, analyzers[i], false,12);
			threadRunner(projectGroupAllMIM, projectGroupPROMISE, analyzers[i], false,13);
			threadRunner(projectGroupAllMIM, projectGroupNASA, analyzers[i], false,14);
			threadRunner(projectGroupAllMIM, projectGroupSOFTLAB, analyzers[i], false,15);
			
			threadRunner(projectGroupAllPROMISE, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupAllPROMISE, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectGroupAllPROMISE, projectGroupMIM, analyzers[i], false,18);
			threadRunner(projectGroupAllPROMISE, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupAllPROMISE, projectGroupSOFTLAB, analyzers[i], false,20);
			
			threadRunner(projectGroupAllNASA, projectGroupAEEEM, analyzers[i], false,21);
			threadRunner(projectGroupAllNASA, projectGroupRelink, analyzers[i], false,22);
			threadRunner(projectGroupAllNASA, projectGroupMIM, analyzers[i], false,23);
			threadRunner(projectGroupAllNASA, projectGroupPROMISE, analyzers[i], false,24);
			threadRunner(projectGroupAllNASA, projectGroupSOFTLAB, analyzers[i], false,25);
			
			threadRunner(projectGroupAllSOFTLAB, projectGroupAEEEM, analyzers[i], false,26);
			threadRunner(projectGroupAllSOFTLAB, projectGroupRelink, analyzers[i], false,27);
			threadRunner(projectGroupAllSOFTLAB, projectGroupMIM, analyzers[i], false,28);
			threadRunner(projectGroupAllSOFTLAB, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectGroupAllSOFTLAB, projectGroupNASA, analyzers[i], false,30);*/
			
			// non-defect >> defect
			/*threadRunner(projectGroupEffort, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupEffort, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectGroupEffort, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupEffort, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectGroupEffort, projectGroupSOFTLAB, analyzers[i], false,20);
			
			/*threadRunner(projectGroupSeverity, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupSeverity, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectGroupSeverity, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupSeverity, projectGroupSOFTLAB, analyzers[i], false,20);*/
			
			/*threadRunner(projectGroupSeverity_fs, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupSeverity_fs, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectGroupSeverity_fs, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupSeverity_fs, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectGroupSeverity_fs, projectGroupSOFTLAB, analyzers[i], false,20);
			
			threadRunner(projectMedical, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectMedical, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectMedical, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectMedical, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectMedical, projectGroupSOFTLAB, analyzers[i], false,20);
			
			threadRunner(projectWine, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectWine, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectWine, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectWine, projectGroupPROMISE, analyzers[i], false,19);
			threadRunner(projectWine, projectGroupSOFTLAB, analyzers[i], false,20);*/
			
			// non-defect >> defect
			/*threadRunner(projectGroupEffortRandom, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupEffortRandom, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectGroupEffortRandom, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupEffortRandom, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectGroupEffortRandom, projectGroupSOFTLAB, analyzers[i], false,20);

			/*threadRunner(projectGroupSeverity, projectGroupAEEEM, analyzers[i], false,16);
						threadRunner(projectGroupSeverity, projectGroupRelink, analyzers[i], false,17);
						threadRunner(projectGroupSeverity, projectGroupNASA, analyzers[i], false,19);
						threadRunner(projectGroupSeverity, projectGroupSOFTLAB, analyzers[i], false,20);*/

			/*threadRunner(projectGroupSeverity_fsRandom, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectGroupSeverity_fsRandom, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectGroupSeverity_fsRandom, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectGroupSeverity_fsRandom, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectGroupSeverity_fsRandom, projectGroupSOFTLAB, analyzers[i], false,20);*/

			/*threadRunner(projectMedicalRandom, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectMedicalRandom, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectMedicalRandom, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectMedicalRandom, projectGroupPROMISE, analyzers[i], false,29);
			threadRunner(projectMedicalRandom, projectGroupSOFTLAB, analyzers[i], false,20);

			threadRunner(projectWineRandom, projectGroupAEEEM, analyzers[i], false,16);
			threadRunner(projectWineRandom, projectGroupRelink, analyzers[i], false,17);
			threadRunner(projectWineRandom, projectGroupNASA, analyzers[i], false,19);
			threadRunner(projectWineRandom, projectGroupPROMISE, analyzers[i], false,19);
			threadRunner(projectWineRandom, projectGroupSOFTLAB, analyzers[i], false,20);
			
			/*ArrayList<ProjectGroupInfo> sourceGroup = new ArrayList<ProjectGroupInfo>();
			sourceGroup.add(projectGroupEffort);
			sourceGroup.add(projectGroupSeverity);
			sourceGroup.add(projectMedical);
			sourceGroup.add(projectWine);
			
			ArrayList<ProjectGroupInfo> targetGroupDivided = new ArrayList<ProjectGroupInfo>();
			targetGroupDivided.addAll(divideProjectGroup(projectGroupAEEEM));
			targetGroupDivided.addAll(divideProjectGroup(projectGroupRelink));
			targetGroupDivided.addAll(divideProjectGroup(projectGroupMIM));
			targetGroupDivided.addAll(divideProjectGroup(projectGroupPROMISE));
			targetGroupDivided.addAll(divideProjectGroup(projectGroupNASA));
			targetGroupDivided.addAll(divideProjectGroup(projectGroupSOFTLAB));
			
			for(ProjectGroupInfo source:sourceGroup){
				for(ProjectGroupInfo target:targetGroupDivided)
					threadRunner(source, target, analyzers[i], false,-1);
			}*/
			
			/*threadRunner(projectGroupEffort, projectGroupAEEEM, analyzers[i], false,31);
			threadRunner(projectGroupEffort, projectGroupRelink, analyzers[i], false,32);
			threadRunner(projectGroupEffort, projectGroupMIM, analyzers[i], false,33);
			threadRunner(projectGroupEffort, projectGroupPROMISE, analyzers[i], false,34);
			threadRunner(projectGroupEffort, projectGroupNASA, analyzers[i], false,35);
			threadRunner(projectGroupEffort, projectGroupSOFTLAB, analyzers[i], false,36);
			
			threadRunner(projectGroupSeverity, projectGroupAEEEM, analyzers[i], false,37);
			threadRunner(projectGroupSeverity, projectGroupRelink, analyzers[i], false,38);
			threadRunner(projectGroupSeverity, projectGroupMIM, analyzers[i], false,39);
			threadRunner(projectGroupSeverity, projectGroupPROMISE, analyzers[i], false,40);
			threadRunner(projectGroupSeverity, projectGroupNASA, analyzers[i], false,41);
			threadRunner(projectGroupSeverity, projectGroupSOFTLAB, analyzers[i], false,42);
			
			threadRunner(projectMedical, projectGroupAEEEM, analyzers[i], false,43);
			threadRunner(projectMedical, projectGroupRelink, analyzers[i], false,44);
			threadRunner(projectMedical, projectGroupMIM, analyzers[i], false,45);
			threadRunner(projectMedical, projectGroupPROMISE, analyzers[i], false,46);
			threadRunner(projectMedical, projectGroupNASA, analyzers[i], false,47);
			threadRunner(projectMedical, projectGroupSOFTLAB, analyzers[i], false,48);
				
			threadRunner(projectWine, projectGroupAEEEM, analyzers[i], false,49);
			threadRunner(projectWine, projectGroupRelink, analyzers[i], false,50);
			threadRunner(projectWine, projectGroupMIM, analyzers[i], false,51);
			threadRunner(projectWine, projectGroupPROMISE, analyzers[i], false,52);
			threadRunner(projectWine, projectGroupNASA, analyzers[i], false,53);
			threadRunner(projectWine, projectGroupSOFTLAB, analyzers[i], false,54);*/

		}
		executor.shutdown();
	}

	private static void loadExistingMatchingScores(String dataPathForAllMatched) {
		allExsitingMatchedData = new HashMap<String,HashMap<String,String>>();
		
		// check if the matching score file exits.
		File dataForMatchingScore = new File(dataPathForAllMatched);
		if(!dataForMatchingScore.exists())
			return;
		
		ArrayList<String> lines = FileUtil.getLines(dataPathForAllMatched, false);
		for(String line:lines){
			// matched data looks like this
			// JDT_Zxing_KSAnalyzer:(62):17-15|0.03846055315052033,17-14|0.031143648482113817,...
			String[] splitLine = line.split(":");
			String combinationWithAnalyzer = splitLine[0];
			String[] matchedMetricsWithScore = splitLine[2].split(",");
			
			HashMap<String,String> matchingScores = new HashMap<String,String>();
			
			// first metric index is 0
			for(String matched:matchedMetricsWithScore){
				String[] splitMatchingData = matched.split("\\|");
				matchingScores.put(splitMatchingData[0], splitMatchingData[1]);
			}
			
			allExsitingMatchedData.put(combinationWithAnalyzer, matchingScores);
		}
	}
	
	private static String[] getRandomDatasets(String[] datasetPrefix) {
		
		int numDatasets = 1000;
		
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

	private static ArrayList<ProjectGroupInfo> divideProjectGroup(
			ProjectGroupInfo projectGroupInfo) {
		ArrayList<ProjectGroupInfo> dividedGroupInfo = new ArrayList<ProjectGroupInfo>();
		String[] listProjects = projectGroupInfo.projects;
		for(int i=0; i<listProjects.length;i++){
			String[] projectName = {listProjects[i]};
			ProjectGroupInfo dividedProject = new ProjectGroupInfo(projectGroupInfo.dirPath,projectGroupInfo.labelName, projectGroupInfo.posLabel, projectName);
			dividedGroupInfo.add(dividedProject);
		}
		return dividedGroupInfo;
	}

	static public void threadRunner(ProjectGroupInfo source,ProjectGroupInfo target, String analyzer,boolean headerRequired,int predictionID){
		
		for(String sourceProject:source.projects){
			
			String newSourceProject = sourceProject + fsOption;
			
			for(String targetProject: target.projects){
				
				executor.execute(new CoFeatureGenerator(newSourceProject,
						targetProject,
						source.dirPath + newSourceProject + ".arff",
						target.dirPath + targetProject +".arff",
						source.labelName,
						target.labelName,
						source.posLabel,
						target.posLabel,
						analyzer));
			}
		}
	}
	
	String sourceProjectName, targetProjectName;
	public CoFeatureGenerator(String sourceProject,
			String targetProject,
			String srcPath,
			String tgtPath,
			String srcLabelName,
			String tgtLabelName,
			String srcLabelPos,
			String tgtLabelPos,
			String analyzer){
	
		sourceProjectName = sourceProject;
		targetProjectName = targetProject;
		sourcePath = srcPath;
		targetPath = tgtPath;
		sourceLabelName = srcLabelName;
		targetLabelName = tgtLabelName;
		sourceLabelPos = srcLabelPos;
		targetLabelPos = tgtLabelPos;
		coOccurrenceAnalyzerOption = analyzer;
	}

	
	@Override
	public void run() {
		
		String combinationKey = sourceProjectName + "_" + targetProjectName + "_" + coOccurrenceAnalyzerOption;
		// load instances
		sourceInstances = loadArff(sourcePath,sourceLabelName);
		targetOriginalInstances = loadArff(targetPath,targetLabelName);
		
		if(folds==1)
			matchingMetrics(combinationKey, -1, -1, false);
		else if(folds>1){
			for(int i=0;i<repeat;i++){
				
				// randomize with different seed for each iteration
				targetOriginalInstances.randomize(new Random(i)); 
				targetOriginalInstances.stratify(folds);
				
				for(int n=0;n < folds;n++){
					matchingMetrics(combinationKey, i, n, true);
				}
			}
		}
	}

	private void matchingMetrics(String combinationKey, int repeatIdx, int foldIdx,boolean nFold) {
		
		
		Instances targetInstances = null;
		String strMatchedAttributes = combinationKey + "_" + String.format("%03d", foldIdx) + "_" + repeatIdx + ":(" + sourceInstances.numAttributes() +")" + ":";
		ArrayList<MatchedAttribute> allMatchedAttributes = null;
		String originalCombinationKey = sourceProjectName.replace(fsOption, "") + "_" + targetProjectName + "_" + coOccurrenceAnalyzerOption + "_" + String.format("%03d", foldIdx) + "_" + repeatIdx;
		
		if(nFold){
			targetInstances = targetOriginalInstances.testCV(folds, foldIdx);
		}
		else{
			targetInstances = targetOriginalInstances;
			strMatchedAttributes = combinationKey + ":(" + sourceInstances.numAttributes() +")" + ":";
			originalCombinationKey = sourceProjectName.replace(fsOption, "") + "_" + targetProjectName + "_" + coOccurrenceAnalyzerOption;
		}

		// normalize data set
		//System.err.println("Normalization Applied: Minmax Normalization Filter!!!");
		//sourceInstances = WekaUtils.applyNormalize(sourceInstances);
		//targetInstances = WekaUtils.applyNormalize(targetInstances);
		//System.err.println("Normalization Applied: LOG Filter!!!");
		//sourceInstances = WekaUtils.applyNormalizeByLog(sourceInstances);
		//targetInstances = WekaUtils.applyNormalizeByLog(targetInstances);
		
		//System.err.println("SOURCE FEATURE SELECTION APPLIED: featrueSelectionByCfsSubsetEval: " + sourceProjectName);
		//sourceInstances = WekaUtils.featrueSelectionByCfsSubsetEval(sourceInstances);
		if(allExsitingMatchedData.get(originalCombinationKey)==null){
			
			System.err.println("No matched data for " + combinationKey);
			System.err.println("Mathing in progress for " + combinationKey);
			
			// run co-occurrence analyzer
			// run analyzer
			CoOccurrenceAnalyzer coOccurAnalyzer=null;
			RConnection rServe;
			//try {
				rServe = null; //new RConnection();
			
				coOccurAnalyzer = new CoOccurrenceAnalyzer(coOccurrenceAnalyzerOption,sourceInstances,targetInstances,
						sourceLabelName,sourceLabelPos,targetLabelName,targetLabelPos,true,cutoff,false,false,useLBMFilter,useDMFilter,rServe);
				
				coOccurAnalyzer.runAnalyzer();
				
				//rServe.close();
			/*} catch (RserveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.exit(0);
			}*/
			//for(MatchedAttribute matched:coOccurAnalyzer.matchedAttribute){
			//for(MatchedAttribute matched:coOccurAnalyzer.allMatchedAttributes){
			
			if(useLBMFilter || useDMFilter){
				allMatchedAttributes = coOccurAnalyzer.allMatchedAttributeAfterFiltering;
			}
			else
				allMatchedAttributes = coOccurAnalyzer.allMatchedAttributes;
		}
		else{
			Instances originalSourceInstances = loadArff(sourcePath.replace(fsOption, ""),sourceLabelName);
			//String originalCombinationKey = sourceProjectName.replace(fsOption, "") + "_" + targetProjectName + "_" + coOccurrenceAnalyzerOption;
			allMatchedAttributes = new ArrayList<MatchedAttribute>();
			
			for(int srcAttrIdx = 0; srcAttrIdx < sourceInstances.numAttributes();srcAttrIdx++){
				if(srcAttrIdx==sourceInstances.classIndex())
					continue;
				
				String attributeName = sourceInstances.attribute(srcAttrIdx).name();
				
				// get originalAttribute index from the name
				int originalSourceIndex = originalSourceInstances.attribute(attributeName).index();
				for(int tarAttrIdx = 0; tarAttrIdx < targetInstances.numAttributes();tarAttrIdx++){
					if(tarAttrIdx==targetInstances.classIndex())
						continue;
					
					
					/*if(allExsitingMatchedData.get(originalCombinationKey)==null){
						System.out.println(originalCombinationKey);
						System.exit(0);
					}*/
					
					allMatchedAttributes.add(
							new MatchedAttribute(srcAttrIdx,tarAttrIdx,
									Double.parseDouble(allExsitingMatchedData.get(originalCombinationKey).get(originalSourceIndex+"-"+tarAttrIdx)))
					);
				}
			}
			
			Collections.sort(allMatchedAttributes);
			
		}
		
		for(MatchedAttribute matched:allMatchedAttributes){	
			strMatchedAttributes = strMatchedAttributes + matched.getSourceAttrIndex() + "-" + matched.getTargetAttrIndex() + "|" + matched.getMatchingScore() + ",";
		}
		
		System.out.println(strMatchedAttributes);
		System.err.println("Processed=" + (++predictionProcessed) + " " + combinationKey);
	}
	
	// reuse instances
	static HashMap<String,Instances> instancesKept = new HashMap<String,Instances>();
	static synchronized Instances loadArff(String path,String labelName){
		
		if(instancesKept.containsKey(path))
			return instancesKept.get(path);
		
		Instances instances = null;

		System.err.println("load a arff file: " + path);
		instances = Utils.loadArff(path,labelName);
				
		instancesKept.put(path, instances);
		System.err.println("Ended loading a arff file: " + path);
	
		return instances;
	}
}
