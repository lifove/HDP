package net.lifove.hdp;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

import com.google.common.primitives.Doubles;

import net.lifove.hdp.util.Utils.FeatureSelectors;


public class ResultTableGenerator {

	DecimalFormat dec = new DecimalFormat("0.000");
	DecimalFormat decPercent = new DecimalFormat("0.0");

	ArrayList<String> orderedProjectName = new ArrayList<String>();
	HashMap<String,String> sourceGroups = new HashMap<String,String>();

	@Test
	public void testMain() {

		orderedProjectName.add("EQ".toLowerCase());
		orderedProjectName.add("JDT".toLowerCase());
		orderedProjectName.add("LC".toLowerCase());
		orderedProjectName.add("ML".toLowerCase());
		orderedProjectName.add("PDE".toLowerCase());
		orderedProjectName.add("Apache".toLowerCase());
		orderedProjectName.add("Safe".toLowerCase());
		orderedProjectName.add("Zxing".toLowerCase());
		orderedProjectName.add("ant-1.3");
		orderedProjectName.add("arc");
		orderedProjectName.add("camel-1.0");
		orderedProjectName.add("poi-1.5");
		orderedProjectName.add("redaktor");
		orderedProjectName.add("skarbonka");
		orderedProjectName.add("tomcat");
		orderedProjectName.add("velocity-1.4");
		orderedProjectName.add("xalan-2.4");
		orderedProjectName.add("xerces-1.2");
		orderedProjectName.add("CM1".toLowerCase());
		orderedProjectName.add("MW1".toLowerCase());
		orderedProjectName.add("PC1".toLowerCase());
		orderedProjectName.add("PC3".toLowerCase());
		orderedProjectName.add("PC4".toLowerCase());
		orderedProjectName.add("JM1".toLowerCase());
		orderedProjectName.add("PC2".toLowerCase());
		orderedProjectName.add("PC5".toLowerCase());
		orderedProjectName.add("MC1".toLowerCase());
		orderedProjectName.add("MC2".toLowerCase());
		orderedProjectName.add("KC3".toLowerCase());
		orderedProjectName.add("ar1");
		orderedProjectName.add("ar3");
		orderedProjectName.add("ar4");
		orderedProjectName.add("ar5");
		orderedProjectName.add("ar6");

		sourceGroups.put("EQ".toLowerCase(),"AEEEM");
		sourceGroups.put("JDT".toLowerCase(),"AEEEM");
		sourceGroups.put("LC".toLowerCase(),"AEEEM");
		sourceGroups.put("ML".toLowerCase(),"AEEEM");
		sourceGroups.put("PDE".toLowerCase(),"AEEEM");
		sourceGroups.put("Apache".toLowerCase(),"Relink");
		sourceGroups.put("Safe".toLowerCase(),"Relink");
		sourceGroups.put("Zxing".toLowerCase(),"Relink");
		sourceGroups.put("ant-1.3","MORPH");
		sourceGroups.put("arc","MORPH");
		sourceGroups.put("camel-1.0","MORPH");
		sourceGroups.put("poi-1.5","MORPH");
		sourceGroups.put("redaktor","MORPH");
		sourceGroups.put("skarbonka","MORPH");
		sourceGroups.put("tomcat","MORPH");
		sourceGroups.put("velocity-1.4","MORPH");
		sourceGroups.put("xalan-2.4","MORPH");
		sourceGroups.put("xerces-1.2","MORPH");
		sourceGroups.put("CM1".toLowerCase(),"NASA");
		sourceGroups.put("MW1".toLowerCase(),"NASA");
		sourceGroups.put("PC1".toLowerCase(),"NASA");
		sourceGroups.put("PC3".toLowerCase(),"NASA");
		sourceGroups.put("PC4".toLowerCase(),"NASA");
		sourceGroups.put("JM1".toLowerCase(),"NASA");
		sourceGroups.put("PC2".toLowerCase(),"NASA");
		sourceGroups.put("PC5".toLowerCase(),"NASA");
		sourceGroups.put("MC1".toLowerCase(),"NASA");
		sourceGroups.put("MC2".toLowerCase(),"NASA");
		sourceGroups.put("KC3".toLowerCase(),"NASA");
		sourceGroups.put("ar1","SOFTLAB");
		sourceGroups.put("ar3","SOFTLAB");
		sourceGroups.put("ar4","SOFTLAB");
		sourceGroups.put("ar5","SOFTLAB");
		sourceGroups.put("ar6","SOFTLAB");

		String pathToResults = System.getProperty("user.home") + "/HDP/Results/";

		ArrayList<String> linesIFS = getLines(pathToResults + "IFS_results.txt",false);
		ArrayList<String> linesCM = getLines(pathToResults + "HDP_common_metrics.txt",false);
		ArrayList<String> linesCLAMI = getLines(pathToResults + "CLAMI_for_HDP.txt",false);

		DecimalFormat decForCutoff = new DecimalFormat("0.00");
		DecimalFormat dec = new DecimalFormat("0.000");

		boolean isWPDPWithFS = true;

		//for(double cutoff=0.05;cutoff<0.06;cutoff=cutoff+0.05){


		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.Logistic",FeatureSelectors.GainRatio,isWPDPWithFS);

		// various FSs
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.Logistic",FeatureSelectors.ChiSquare,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.Logistic",FeatureSelectors.Significance,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.Logistic",FeatureSelectors.RelieF,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.Logistic",FeatureSelectors.None,isWPDPWithFS);

		// variius MLs
		/*generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.SimpleLogistic",FeatureSelectors.GainRatio,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.bayes.BayesNet",FeatureSelectors.GainRatio,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.SMO",FeatureSelectors.GainRatio,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.trees.J48",FeatureSelectors.GainRatio,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.trees.LMT",FeatureSelectors.GainRatio,isWPDPWithFS);
		generate(orderedProjectName, pathToResults, linesIFS, linesCM, linesCLAMI, decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.trees.RandomForest",FeatureSelectors.GainRatio,isWPDPWithFS);*/


		//}
	}

	private void generate(ArrayList<String> orderedProjectName, String pathToResults, ArrayList<String> linesIFS,
			ArrayList<String> linesCM,
			ArrayList<String> linesCLAMI,
			DecimalFormat decForCutoff,
			DecimalFormat dec, double cutoff,String analyzer, String mlAlg,FeatureSelectors fSelector, boolean isWPDPWithFS) {
		System.out.println("\n\n====cutoff: " + decForCutoff.format(cutoff) + "_" + fSelector.name() + "_" + analyzer + "_" + mlAlg);

		String pathStrWPDPWithFS = isWPDPWithFS? "_WPDP_FS":"";
		ArrayList<String> linesHDP = getLines(pathToResults + "HDP_C" + 
				decForCutoff.format(cutoff) + "_" + 
				fSelector.name() + "_" + 
				analyzer + "_" + 
				mlAlg +
				pathStrWPDPWithFS +
				"_main.txt",false);

		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsHDP = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		HashSet<String> validHDPPrediction = new HashSet<String>(); // value: source target repeat folder
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsWPDP = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsCM = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsIFS = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsCLAMI = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: project

		HashMap<String,ArrayList<String>> medianResultsForEachCombination = new HashMap<String,ArrayList<String>>(); // key sourceGroupName, value: medians and WTL results for combinations in the same source group
		/*int millis = 1000;
		long startTime = System.currentTimeMillis();
		System.out.println("Start process HDP results: " + (int)startTime/millis + "s");*/

		// get HashMap from HDP results
		//int lineCount = 0;
		getHashMapHDP(linesHDP, resultsHDP, validHDPPrediction, resultsWPDP);
		//System.out.println("End process HDP results: " + (int)(System.currentTimeMillis()-startTime)/millis + "s");

		// get HashMap from CM results
		//System.out.println("Start process CM results: " + (int)(System.currentTimeMillis()-startTime)/millis + "s");
		//lineCount = 0;
		getHashMapCM(linesCM, validHDPPrediction, resultsCM);
		//System.out.println("End process CM results: " + (int)(System.currentTimeMillis()-startTime)/millis + "s");

		// get HashMap from IFS results
		//System.err.println(linesIFS.size());
		getHashMapIFS(linesIFS, validHDPPrediction, resultsIFS);

		// get HashMap from CLAMI results
		getHashMapCLAMI(linesCLAMI, validHDPPrediction, resultsCLAMI);

		/*
		 *  treatment <-c(1,2,3)
		 *  control <-c(1,2,3)
		 *  cliff.delta(treatment,control)
		 */
		try {
			/*
			 * Run Rserve in R terminal to make RConnection
			 * > library(Rserve)
			 * > Rserve()
			 */
			RConnection c= new RConnection();
			c.eval("library('effsize')");
			HashMap<Integer,String> resultLines = new HashMap<Integer,String>();
			HashMap<Integer,String> resultLinesWinTieLoss = new HashMap<Integer,String>();
			int[] totalWPDPWTL = {0,0,0};
			int[] totalCMWTL = {0,0,0};
			int[] totalIFSWTL = {0,0,0};
			int[] totalCLAMIWTL = {0,0,0};

			for(String key: orderedProjectName){

				// skip if there are no prediction combinations for a target
				if(resultsHDP.get(key)==null){
					resultLines.put(orderedProjectName.indexOf(key), key + "\t&" +
							"-\t&" + 
							"-\t&" +  
							"-\t&" +  
							"-\t& - \\\\ \\hline");

					String strWTL = key + "\t&-\t&-\t&-\t&-\t&-\t&-\t&-\t&-\t&-\t&-\\\\ \\hline";

					resultLinesWinTieLoss.put(orderedProjectName.indexOf(key), strWTL);

					continue;
				}

				String target = key;
				HashMap<String,ArrayList<Prediction>> predicitonsHDPBySource = resultsHDP.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsWPDPBySource = resultsWPDP.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsCMBySource = resultsCM.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsIFSBySource = resultsIFS.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsCLAMIBySource = resultsCLAMI.get(key);


				// compute effsize: compare all values between treatment and control by a corresponding source

				// treatment
				ArrayList<Double> treatment = new ArrayList<Double>();
				ArrayList<Double> mediansHDP = new ArrayList<Double>();
				ArrayList<String> validSources = new ArrayList<String>();
				for(String srcKey:predicitonsHDPBySource.keySet()){
					validSources.add(srcKey);
					ArrayList<Prediction> predicitonsHDP = predicitonsHDPBySource.get(srcKey);

					ArrayList<Double> aucValues = new ArrayList<Double>();
					for(int i=0; i<predicitonsHDP.size();i++){
						treatment.add(predicitonsHDP.get(i).AUC);
						aucValues.add(predicitonsHDP.get(i).AUC);
					}
					mediansHDP.add(getMedian(aucValues));
				}

				// WPDP
				ArrayList<Double> wpdp = new ArrayList<Double>();
				ArrayList<Double> mediansWPDP = new ArrayList<Double>();
				for(String srcKey:validSources){
					ArrayList<Prediction> predicitonsWPDP = predicitonsWPDPBySource.get(srcKey);
					ArrayList<Double> aucValues = new ArrayList<Double>();
					for(int i=0; i<predicitonsWPDP.size();i++){
						wpdp.add(predicitonsWPDP.get(i).AUC);
						aucValues.add(predicitonsWPDP.get(i).AUC);
					}
					mediansWPDP.add(getMedian(aucValues));
				}

				c.assign("treatment", Doubles.toArray(treatment));
				c.assign("control", Doubles.toArray(wpdp));
				RList lWPDP = c.eval("cliff.delta(treatment,control)").asList();

				// CM
				ArrayList<Double> cm = new ArrayList<Double>();
				ArrayList<Double> mediansCM = new ArrayList<Double>();
				for(String srcKey:validSources){
					ArrayList<Prediction> predicitonsCM = predicitonsCMBySource.get(srcKey);
					ArrayList<Double> aucValues = new ArrayList<Double>();
					for(int i=0; i<predicitonsCM.size();i++){
						cm.add(predicitonsCM.get(i).AUC);
						aucValues.add(predicitonsCM.get(i).AUC);
					}
					mediansCM.add(getMedian(aucValues));
				}

				c.assign("control", Doubles.toArray(cm));
				RList lCM = c.eval("cliff.delta(treatment,control)").asList();

				// IFS
				ArrayList<Double> ifs = new ArrayList<Double>();
				ArrayList<Double> mediansIFS = new ArrayList<Double>();
				for(String srcKey:validSources){
					ArrayList<Prediction> predicitonsIFS = predicitonsIFSBySource.get(srcKey);
					ArrayList<Double> aucValues = new ArrayList<Double>();
					for(int i=0; i<predicitonsIFS.size();i++){
						ifs.add(predicitonsIFS.get(i).AUC);
						aucValues.add(predicitonsIFS.get(i).AUC);
					}
					mediansIFS.add(getMedian(aucValues));
				}
				c.assign("control", Doubles.toArray(ifs));
				RList lIFS = c.eval("cliff.delta(treatment,control)").asList();

				// CLAMI
				ArrayList<Double> clami = new ArrayList<Double>();
				ArrayList<Double> mediansCLAMI = new ArrayList<Double>();
				for(String srcKey:validSources){
					ArrayList<Prediction> predicitonsCLAMI = predicitonsCLAMIBySource.get(srcKey); // clami don't have source, but we compute CLAMI has source for each comparison  
					ArrayList<Double> aucValues = new ArrayList<Double>();
					for(int i=0; i<predicitonsCLAMI.size();i++){
						clami.add(predicitonsCLAMI.get(i).AUC);
						aucValues.add(predicitonsCLAMI.get(i).AUC);
					}
					mediansCLAMI.add(getMedian(aucValues));
				}
				c.assign("control", Doubles.toArray(clami));
				RList lCLAMI = c.eval("cliff.delta(treatment,control)").asList();


				// get result lines
				Double wAUC = getMedian(mediansWPDP);
				Double wAUCCliffDelta = lWPDP.at("estimate").asDouble();
				Double cmAUC = getMedian(mediansCM);
				Double cmAUCCliffDelta = lCM.at("estimate").asDouble();
				Double ifsAUC = getMedian(mediansIFS);
				Double ifsAUCCliffDelta = lIFS.at("estimate").asDouble();
				Double clamiAUC = getMedian(mediansCLAMI);
				Double clamiAUCCliffDelta = lCLAMI.at("estimate").asDouble();
				Double hdpAUC = getMedian(mediansHDP);

				String wAUCMagnitute = getCliffsDeltaMagnitute(wAUCCliffDelta);
				String cmAUCMagnitute = getCliffsDeltaMagnitute(cmAUCCliffDelta);
				String ifsAUCMagnitute = getCliffsDeltaMagnitute(ifsAUCCliffDelta);
				String clamiAUCMagnitute = getCliffsDeltaMagnitute(clamiAUCCliffDelta);

				String strHDPAUC = dec.format(hdpAUC);
				String strWPDPAUC = dec.format(wAUC);
				String strCMAUC = dec.format(cmAUC);
				String strIFSAUC = dec.format(ifsAUC);
				String strCLAMIAUC = dec.format(clamiAUC);

				int wTestWPDPHDP = isSignificantByWilcoxonTest(mediansWPDP,mediansHDP);
				if(wTestWPDPHDP==1)
					strHDPAUC = "{\\bf " + strHDPAUC + "}";
				else if(wTestWPDPHDP==-1)
					strWPDPAUC = "{\\bf " + strWPDPAUC + "}";

				int wTestCMHDP = isSignificantByWilcoxonTest(mediansCM,mediansHDP);
				if(wTestCMHDP==1)
					strHDPAUC = "\\underline{" + strHDPAUC + "}";
				else if(wTestCMHDP==-1)
					strCMAUC = "\\underline{" + strCMAUC + "}";

				int wTestIFSHDP = isSignificantByWilcoxonTest(mediansIFS,mediansHDP);
				if(wTestIFSHDP==1)
					strHDPAUC = strHDPAUC + "*";
				else if(wTestIFSHDP==-1)
					strIFSAUC = strIFSAUC + "*";
				
				int wTestCLAMIHDP = isSignificantByWilcoxonTest(mediansCLAMI,mediansHDP);
				if(wTestCLAMIHDP==1)
					strHDPAUC = strHDPAUC + "$^{\\&}$";
				else if(wTestCLAMIHDP==-1)
					strCLAMIAUC = strCLAMIAUC + "$^{\\&}$";

				resultLines.put(orderedProjectName.indexOf(key), target + "\t&" +
						strWPDPAUC + " (" + dec.format(wAUCCliffDelta) + "," + wAUCMagnitute + ")\t&" + 
						strCMAUC + " (" + dec.format(cmAUCCliffDelta) + "," + cmAUCMagnitute + ")\t&" + 
						strIFSAUC + " (" + dec.format(ifsAUCCliffDelta) + "," + ifsAUCMagnitute + ")\t&" + 
						strCLAMIAUC + " (" + dec.format(clamiAUCCliffDelta) + "," + clamiAUCMagnitute + ")\t&" + 
						strHDPAUC + " \\\\ \\hline");

				//get results liens for Win/Tie/Loss evaluation
				int[] wpdpWTL = {0,0,0}; // W/T/L against WPDP;
				int[] cmWTL = {0,0,0}; // W/T/L against CM;
				int[] ifsWTL = {0,0,0}; // W/T/L against IFS;
				int[] clamiWTL = {0,0,0}; // W/T/L against CLAMI;
				for(String source:predicitonsHDPBySource.keySet()){
					ArrayList<Prediction> hdpPredicitons = predicitonsHDPBySource.get(source);
					ArrayList<Prediction> wdpPredictions = predicitonsWPDPBySource.get(source);
					ArrayList<Prediction> cmPredictions = predicitonsCMBySource.get(source);
					ArrayList<Prediction> ifsPredictins = predicitonsIFSBySource.get(source);
					ArrayList<Prediction> clamiPredictins = predicitonsCLAMIBySource.get(source); // clami don't have source, but we compute CLAMI has source for each comparison 

					ArrayList<Double> hdpAUCs = new ArrayList<Double>();
					ArrayList<Double> wpdpAUCs = new ArrayList<Double>();
					ArrayList<Double> cmAUCs = new ArrayList<Double>();
					ArrayList<Double> ifsAUCs = new ArrayList<Double>();
					ArrayList<Double> clamiAUCs = new ArrayList<Double>();
					for(int i=0; i<1000;i++){
						hdpAUCs.add(hdpPredicitons.get(i).AUC);
						wpdpAUCs.add(wdpPredictions.get(i).AUC);
						cmAUCs.add(cmPredictions.get(i).AUC);
						ifsAUCs.add(ifsPredictins.get(i).AUC);
						clamiAUCs.add(clamiPredictins.get(i).AUC);
					}

					Double medianWPDP = getMedian(wpdpAUCs);
					Double medianCM = getMedian(cmAUCs);
					Double medianIFS = getMedian(ifsAUCs);
					Double medianCLAMI = getMedian(clamiAUCs);
					Double medianHDP = getMedian(hdpAUCs);

					String wtlAgainstWPDP = evaluateWTL(wpdpAUCs,hdpAUCs);
					String wtlAgainstCM = evaluateWTL(cmAUCs,hdpAUCs);
					String wtlAgainstIFS = evaluateWTL(ifsAUCs,hdpAUCs);
					String wtlAgainstCLAMI = evaluateWTL(clamiAUCs,hdpAUCs);

					String sourceGroup = sourceGroups.get(source);
					String strReulstForThisCombination = source + "," + target + "," +
							dec.format(medianWPDP) + "," + wtlAgainstWPDP + "," +
							dec.format(medianCLAMI) + "," + wtlAgainstCLAMI + "," +
							dec.format(medianCM) + "," + wtlAgainstCM + "," +
							dec.format(medianIFS) + "," + wtlAgainstIFS + "," +
							dec.format(medianHDP);
					if(!medianResultsForEachCombination.containsKey(sourceGroup)){
						ArrayList<String> resultForCombination = new ArrayList<String>();
						resultForCombination.add(strReulstForThisCombination);
						medianResultsForEachCombination.put(sourceGroup,resultForCombination);
					}else{
						medianResultsForEachCombination.get(sourceGroup).add(strReulstForThisCombination);
					}
					wpdpWTL = updateWTL(wpdpWTL,wpdpAUCs,hdpAUCs);
					cmWTL = updateWTL(cmWTL,cmAUCs,hdpAUCs);
					ifsWTL = updateWTL(ifsWTL,ifsAUCs,hdpAUCs);
					clamiWTL = updateWTL(clamiWTL,clamiAUCs,hdpAUCs);
				}

				totalWPDPWTL[0] += wpdpWTL[0];
				totalWPDPWTL[1] += wpdpWTL[1];
				totalWPDPWTL[2] += wpdpWTL[2];

				totalCMWTL[0] += cmWTL[0];
				totalCMWTL[1] += cmWTL[1];
				totalCMWTL[2] += cmWTL[2];

				totalIFSWTL[0] += ifsWTL[0];
				totalIFSWTL[1] += ifsWTL[1];
				totalIFSWTL[2] += ifsWTL[2];

				totalCLAMIWTL[0] += clamiWTL[0];
				totalCLAMIWTL[1] += clamiWTL[1];
				totalCLAMIWTL[2] += clamiWTL[2];

				String strWTL = target +
						"\t&" + wpdpWTL[0] +"\t&" + wpdpWTL[1] +" \t&" + wpdpWTL[2] +
						"\t&" + cmWTL[0] +"\t&" + cmWTL[1] +" \t&" + cmWTL[2] +
						"\t&" + ifsWTL[0] +"\t&" + ifsWTL[1] +" \t&" + ifsWTL[2] +
						"\t&" + clamiWTL[0] +"\t&" + clamiWTL[1] +" \t&" + clamiWTL[2] +
						"\\\\ \\hline";

				resultLinesWinTieLoss.put(orderedProjectName.indexOf(key), strWTL);
			}

			// total WTL

			int numPredictionCombinations = totalWPDPWTL[0] +  totalWPDPWTL[1] + totalWPDPWTL[2];
			double winAgainstWPDP = ((double)totalWPDPWTL[0]/numPredictionCombinations)*100;
			double tieAgainstWPDP = ((double)totalWPDPWTL[1]/numPredictionCombinations)*100;
			double lossAgainstWPDP = ((double)totalWPDPWTL[2]/numPredictionCombinations)*100;

			double winAgainstCM = ((double)totalCMWTL[0]/numPredictionCombinations)*100;
			double tieAgainstCM = ((double)totalCMWTL[1]/numPredictionCombinations)*100;
			double lossAgaisnCM = ((double)totalCMWTL[2]/numPredictionCombinations)*100;

			double winAgainstIFS = ((double)totalIFSWTL[0]/numPredictionCombinations)*100;
			double tieAgainstIFS = ((double)totalIFSWTL[1]/numPredictionCombinations)*100;
			double lossAgainstIFS = ((double)totalIFSWTL[2]/numPredictionCombinations)*100;

			double winAgainstCLAMI = ((double)totalCLAMIWTL[0]/numPredictionCombinations)*100;
			double tieAgainstCLAMI = ((double)totalCLAMIWTL[1]/numPredictionCombinations)*100;
			double lossAgainstCLAMI = ((double)totalCLAMIWTL[2]/numPredictionCombinations)*100;


			resultLinesWinTieLoss.put(orderedProjectName.size(),
					"Total\t&\\specialcell{{" + totalWPDPWTL[0] +"}\\\\{" + decPercent.format(winAgainstWPDP) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalWPDPWTL[1] +"}\\\\{" + decPercent.format(tieAgainstWPDP) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalWPDPWTL[2] +"}\\\\{" + decPercent.format(lossAgainstWPDP) + "\\%}}\t&" +
							"\\specialcell{{" +	totalCMWTL[0] +"}\\\\{" + decPercent.format(winAgainstCM) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalCMWTL[1] +"}\\\\{" + decPercent.format(tieAgainstCM) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalCMWTL[2] +"}\\\\{" + decPercent.format(lossAgaisnCM) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalIFSWTL[0] +"}\\\\{" + decPercent.format(winAgainstIFS) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalIFSWTL[1] +"}\\\\{" + decPercent.format(tieAgainstIFS) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalIFSWTL[2] +"}\\\\{" + decPercent.format(lossAgainstIFS) + "\\%}}\t&" +
							"\\specialcell{{" +	totalCLAMIWTL[0] +"}\\\\{" + decPercent.format(winAgainstCLAMI) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalCLAMIWTL[1] +"}\\\\{" + decPercent.format(tieAgainstCLAMI) + "\\%}}\t&" + 
							"\\specialcell{{" +	totalCLAMIWTL[2] +"}\\\\{" + decPercent.format(lossAgainstCLAMI) + "\\%}}" + 
							"\\\\ \\hline");

			for(int i=0;i<resultLines.size();i++){
				System.out.println(resultLines.get(i));
			}

			String wpdpMedian = dec.format(getMedian(resultsWPDP));
			String cmMedian = dec.format(getMedian(resultsCM));
			String ifsMedian = dec.format(getMedian(resultsIFS));
			String clamiMedian = dec.format(getMedian(resultsCLAMI));
			String hdpMedian = dec.format(getMedian(resultsHDP));

			int wTestWPDPHDP = isSignificantByWilcoxonTest(resultsWPDP,resultsHDP);
			if(wTestWPDPHDP==1)
				hdpMedian = "{\\bf " + hdpMedian + "}";
			else if(wTestWPDPHDP==-1){
				wpdpMedian = "{\\bf " + wpdpMedian + "}";
			}

			int wTestCMHDP = isSignificantByWilcoxonTest(resultsCM,resultsHDP);
			if(wTestCMHDP==1)
				hdpMedian = "\\underline{" + hdpMedian + "}";
			else if(wTestCMHDP==-1)
				cmMedian = "\\underline{" + cmMedian + "}";

			int wTestIFSHDP = isSignificantByWilcoxonTest(resultsIFS,resultsHDP);
			if(wTestIFSHDP==1)
				hdpMedian = hdpMedian + "*";
			else if(wTestIFSHDP==-1)
				ifsMedian = ifsMedian + "*";
			
			int wTestCLAMIHDP = isSignificantByWilcoxonTest(resultsCLAMI,resultsHDP);
			if(wTestCLAMIHDP==1)
				hdpMedian = hdpMedian + "$^{\\&}$";
				else if(wTestCLAMIHDP==-1)
					clamiMedian = clamiMedian + "$^{\\&}$";
			
			System.out.println("\\hline\n{\\bf {\\em All}}\t&" + 
					wpdpMedian + "\t" + 
					"\t&" +
					cmMedian + "\t" +
					"\t&" +
					ifsMedian + "\t" +
					"\t&" +
					clamiMedian + "\t" +
					"\t&" +
					hdpMedian);


			for(int i=0;i<resultLinesWinTieLoss.size();i++){
				System.out.println(resultLinesWinTieLoss.get(i));
			}

		} catch (REngineException | REXPMismatchException e) {
			e.printStackTrace();
		}

		// results by source group
		for(String sourceGroup:medianResultsForEachCombination.keySet()){
			ArrayList<Double> wpdpAUCs = new ArrayList<Double>();
			ArrayList<Double> cmAUCs = new ArrayList<Double>();
			ArrayList<Double> ifsAUCs = new ArrayList<Double>();
			ArrayList<Double> clamiAUCs = new ArrayList<Double>();
			ArrayList<Double> hdpAUCs = new ArrayList<Double>();
			ArrayList<String> targets = new ArrayList<String>();
			for(String result:medianResultsForEachCombination.get(sourceGroup)){
				String[] splitString = result.split(",");
				wpdpAUCs.add(Double.parseDouble(splitString[2]));
				clamiAUCs.add(Double.parseDouble(splitString[4]));
				cmAUCs.add(Double.parseDouble(splitString[6]));
				ifsAUCs.add(Double.parseDouble(splitString[8]));
				hdpAUCs.add(Double.parseDouble(splitString[10]));
				if(!targets.contains(splitString[1]))
					targets.add(splitString[1]);
			}

			int targetCoverage = targets.size();

			Double wpdpMedian = getMedian(wpdpAUCs);
			Double cmMedian = getMedian(cmAUCs);
			Double ifsMedian = getMedian(ifsAUCs);
			Double clamiMedian = getMedian(clamiAUCs);
			Double hdpMedian = getMedian(hdpAUCs);

			String strHDPAUC = dec.format(hdpMedian);
			String strWPDPAUC = dec.format(wpdpMedian);
			String strCMAUC = dec.format(cmMedian);
			String strIFSAUC = dec.format(ifsMedian);
			String strCLAMIAUC = dec.format(clamiMedian);

			int wTestWPDPHDP = isSignificantByWilcoxonTest(wpdpAUCs,hdpAUCs);
			if(wTestWPDPHDP==1)
				strHDPAUC = "{\\bf " + strHDPAUC + "}";
			else if(wTestWPDPHDP==-1)
				strWPDPAUC = "{\\bf " + strWPDPAUC + "}";

			int wTestCMHDP = isSignificantByWilcoxonTest(cmAUCs,hdpAUCs);
			if(wTestCMHDP==1)
				strHDPAUC = "\\underline{" + strHDPAUC + "}";
			else if(wTestCMHDP==-1)
				strCMAUC = "\\underline{" + strCMAUC + "}";

			int wTestIFSHDP = isSignificantByWilcoxonTest(ifsAUCs,hdpAUCs);
			if(wTestIFSHDP==1)
				strHDPAUC = strHDPAUC + "*";
			else if(wTestIFSHDP==-1)
				strIFSAUC = strIFSAUC + "*";
			
			int wTestCLAMIHDP = isSignificantByWilcoxonTest(clamiAUCs,hdpAUCs);
			if(wTestCLAMIHDP==1)
				strHDPAUC = strHDPAUC + "$^{\\&}$";
			else if(wTestCLAMIHDP==-1)
				strCLAMIAUC = strCLAMIAUC + "$^{\\&}$";

			System.out.println(sourceGroup +"\t&" + strWPDPAUC + "\t&" + 
					strCMAUC + "\t&" + 
					strIFSAUC + "\t&" +
					strCLAMIAUC + "\t&" + 
					strHDPAUC + "\t&" +
					targetCoverage + " \\\\ \\hline\t\t" + targets.toString()
					);
		}
	}

	private void getHashMapCLAMI(ArrayList<String> linesCLAMI, HashSet<String> validHDPPrediction,
			HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsCLAMI) {
		if(resultsCLAMI.size()==0){
			for(String line:linesCLAMI){
				String[] splitLine = line.split(",");
				String target = splitLine[2].toLowerCase();
				int repeat = Integer.parseInt(splitLine[0]);
				int fold =  Integer.parseInt(splitLine[1]);
				Double AUC = Double.parseDouble(splitLine[6]);

				for(String source:orderedProjectName){  // CLAMI does not have source but we generate hashmap as a source exists to make HashMap consistent as IFS and CM results for easy comparision
					// only consider valid prediction from HDP results
					if(!validHDPPrediction.contains((source+target+repeat+ "," + fold))) continue;

					if(!resultsCLAMI.containsKey(target)){
						HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
						ArrayList<Prediction> predictions = new ArrayList<Prediction>();
						predictions.add(new Prediction(source,target,fold,repeat,AUC));
						resultsHDPBySource.put(source,predictions);
						resultsCLAMI.put(target, resultsHDPBySource);
					}else{
						if(!resultsCLAMI.get(target).containsKey(source)){
							ArrayList<Prediction> prediction = new ArrayList<Prediction>();
							prediction.add(new Prediction(source,target,fold,repeat,AUC));
							resultsCLAMI.get(target).put(source,prediction);
						}
						resultsCLAMI.get(target).get(source).add(new Prediction(source,target,fold,repeat,AUC));
					}
				}
			}	
		}
	}

	private void getHashMapIFS(ArrayList<String> linesIFS, HashSet<String> validHDPPrediction,
			HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsIFS) {
		if(resultsIFS.size()==0){
			int repeat=0;
			int fold = 0;
			for(String line:linesIFS){
				String[] splitLine = line.split(",");
				if(splitLine[0].trim().equals("A")) continue;

				String source = splitLine[1].split(">>")[0].toLowerCase();
				String target = splitLine[1].split(">>")[1].toLowerCase();
				Double AUC = Double.parseDouble(splitLine[9]);

				// only consider valid prediction from HDP results
				if(!validHDPPrediction.contains((source+target+repeat+ "," + fold))) continue;

				if(!resultsIFS.containsKey(target)){
					HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
					ArrayList<Prediction> predictions = new ArrayList<Prediction>();
					predictions.add(new Prediction(source,target,fold,repeat,AUC));
					resultsHDPBySource.put(source,predictions);
					resultsIFS.put(target, resultsHDPBySource);
				}else{
					if(!resultsIFS.get(target).containsKey(source)){
						ArrayList<Prediction> prediction = new ArrayList<Prediction>();
						prediction.add(new Prediction(source,target,fold,repeat,AUC));
						resultsIFS.get(target).put(source,prediction);
					}
					resultsIFS.get(target).get(source).add(new Prediction(source,target,fold,repeat,AUC));
				}

				if(fold<1)
					fold++;
				else{
					fold = 0;
					repeat++;
					if(repeat==500)
						repeat=0;
				}
			}
		}
	}

	private void getHashMapCM(ArrayList<String> linesCM, HashSet<String> validHDPPrediction,
			HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsCM) {
		if(resultsCM.size()==0){
			for(String line:linesCM){
				String[] splitLine = line.split(",");
				String source = splitLine[2].split("/")[1].replace(".arff", "").toLowerCase();
				String target = splitLine[3].split("/")[1].replace(".arff", "").toLowerCase();
				int repeat = Integer.parseInt(splitLine[0]);
				int fold =  Integer.parseInt(splitLine[1]);
				Double AUC = Double.parseDouble(splitLine[12]);

				//++lineCount;

				// only consider valid prediction from HDP results
				if(!validHDPPrediction.contains((source+target+repeat+ "," + fold))) continue;

				if(!resultsCM.containsKey(target)){
					HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
					ArrayList<Prediction> predictions = new ArrayList<Prediction>();
					predictions.add(new Prediction(source,target,fold,repeat,AUC));
					resultsHDPBySource.put(source,predictions);
					resultsCM.put(target, resultsHDPBySource);
				}else{
					if(!resultsCM.get(target).containsKey(source)){
						ArrayList<Prediction> prediction = new ArrayList<Prediction>();
						prediction.add(new Prediction(source,target,fold,repeat,AUC));
						resultsCM.get(target).put(source,prediction);
					}
					resultsCM.get(target).get(source).add(new Prediction(source,target,fold,repeat,AUC));
				}

				//if(lineCount%1000==0)
				//	System.out.println("Processing CM results(" + lineCount + "): " + (int)(System.currentTimeMillis()-startTime)/millis + "s");
			}
		}
	}

	private void getHashMapHDP(ArrayList<String> linesHDP,
			HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsHDP, HashSet<String> validHDPPrediction,
			HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsWPDP) {
		for(String line:linesHDP){

			String[] splitLine = line.split(",");
			String source = splitLine[2].split("/")[1].replace(".arff", "").toLowerCase();
			String target = splitLine[3].split("/")[1].replace(".arff", "").toLowerCase();
			int repeat = Integer.parseInt(splitLine[0]);
			int fold =  Integer.parseInt(splitLine[1]);
			Double wAUC = Double.parseDouble(splitLine[8]);
			Double AUC = Double.parseDouble(splitLine[12]);

			if(!resultsHDP.containsKey(target)){
				HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
				ArrayList<Prediction> predictions = new ArrayList<Prediction>();
				predictions.add(new Prediction(source,target,fold,repeat,AUC));
				resultsHDPBySource.put(source,predictions);
				resultsHDP.put(target, resultsHDPBySource);
				validHDPPrediction.add(source+target+repeat+ "," + fold);
			}else{
				if(!resultsHDP.get(target).containsKey(source)){
					ArrayList<Prediction> prediction = new ArrayList<Prediction>();
					prediction.add(new Prediction(source,target,fold,repeat,AUC));
					resultsHDP.get(target).put(source,prediction);
				}
				resultsHDP.get(target).get(source).add(new Prediction(source,target,fold,repeat,AUC));
				validHDPPrediction.add(source+target+repeat+ "," + fold);
			}

			if(!resultsWPDP.containsKey(target)){
				HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
				ArrayList<Prediction> predictions = new ArrayList<Prediction>();
				predictions.add(new Prediction(source,target,fold,repeat,wAUC));
				resultsHDPBySource.put(source,predictions);
				resultsWPDP.put(target, resultsHDPBySource);
			}else{
				if(!resultsWPDP.get(target).containsKey(source)){
					ArrayList<Prediction> prediction = new ArrayList<Prediction>();
					prediction.add(new Prediction(source,target,fold,repeat,wAUC));
					resultsWPDP.get(target).put(source,prediction);
				}
				resultsWPDP.get(target).get(source).add(new Prediction(source,target,fold,repeat,wAUC));
			}

			//if(++lineCount%1000==0)
			//	System.out.println("Processing HDP results(" + lineCount + "): " + (int)(System.currentTimeMillis()-startTime)/millis + "s");
		}
	}

	private int[] updateWTL(int[] wtlValues, ArrayList<Double> baselineAUCs, ArrayList<Double> hdpAUCs) {

		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(baselineAUCs), Doubles.toArray(hdpAUCs),false);

		Double medeanA = getMedian(baselineAUCs);
		Double medeanB = getMedian(hdpAUCs);


		// win
		if(p<0.05 && medeanA < medeanB){
			wtlValues[0]++;
			return wtlValues;
		}

		// loss
		if(p<0.05 && medeanA > medeanB){
			wtlValues[2]++;
			return wtlValues;
		}

		// otherwise, tie
		wtlValues[1]++;

		return wtlValues;
	}

	private String evaluateWTL(ArrayList<Double> baselineAUCs, ArrayList<Double> hdpAUCs){
		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(baselineAUCs), Doubles.toArray(hdpAUCs),false);

		Double medeanA = getMedian(baselineAUCs);
		Double medeanB = getMedian(hdpAUCs);

		// win
		if(p<0.05 && medeanA < medeanB){
			return "Win"; //Win
		}

		// loss
		if(p<0.05 && medeanA > medeanB){
			return "Loss";
		}

		// otherwise, tie
		return "Tie";
	}

	private int isSignificantByWilcoxonTest(ArrayList<Double> mediansA,
			ArrayList<Double> mediansB) {

		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();

		double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(mediansA), Doubles.toArray(mediansB),false);

		Double medeanA = getMedian(mediansA);
		Double medeanB = getMedian(mediansB);

		if(medeanA < medeanB && p < 0.05)
			return 1;

		if(medeanA > medeanB && p < 0.05)
			return -1;


		return 0;
	}

	private int isSignificantByWilcoxonTest(HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsA,
			HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsB) {

		ArrayList<Double> mediansA = getMedians(resultsA);
		ArrayList<Double> mediansB = getMedians(resultsB);


		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();

		double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(mediansA), Doubles.toArray(mediansB),false);

		Double medeanA = getMedian(mediansA);
		Double medeanB = getMedian(mediansB);

		if(medeanA < medeanB && p < 0.05)
			return 1;

		if(medeanA > medeanB && p < 0.05)
			return -1;

		return 0;
	}

	private String getCliffsDeltaMagnitute(Double wAUCCliffDelta) {

		Double absValue = Math.abs(wAUCCliffDelta);

		if(absValue>=0.474)
			return wAUCCliffDelta>0?"{\\bf L}":"L";
			if(absValue>=0.33)
				return wAUCCliffDelta>0?"{\\bf M}":"M";
				if(absValue>=0.147)
					return wAUCCliffDelta>0?"{\\bf S}":"S";

					return "{\\bf N}";
	}

	private Double getMedian(HashMap<String, HashMap<String, ArrayList<Prediction>>> results) {
		ArrayList<Double> medians = getMedians(results);
		return getMedian(medians);
	}

	private ArrayList<Double> getMedians(HashMap<String, HashMap<String, ArrayList<Prediction>>> results) {
		ArrayList<Double> medians = new ArrayList<Double>();

		for(String key:results.keySet()){
			HashMap<String, ArrayList<Prediction>> predictionsByTarget = results.get(key);

			for(String source:predictionsByTarget.keySet()){
				ArrayList<Double> values = new ArrayList<Double>();
				ArrayList<Prediction> predictions = predictionsByTarget.get(source);
				for(Prediction prediciton:predictions)
					values.add(prediciton.AUC);
				medians.add(getMedian(values));
			}	
		}
		return medians;
	}

	private HashMap<String, ArrayList<Double>> getMapMedians(HashMap<String, HashMap<String, ArrayList<Prediction>>> resultsHDP) {
		HashMap<String,ArrayList<Double>> mediansHDP = new HashMap<String,ArrayList<Double>>();
		for(String key:resultsHDP.keySet()){
			ArrayList<Double> medians = new ArrayList<Double>();
			for(String source:resultsHDP.get(key).keySet()){
				ArrayList<Double> values = new ArrayList<Double>();
				for(Prediction prediction:resultsHDP.get(key).get(source)){
					values.add(prediction.AUC);
				}
				Double median = getMedian(values);
				medians.add(median);
			}

			mediansHDP.put(key, medians);
		}
		return mediansHDP;
	}

	private ArrayList<String> getLines(String file,boolean removeHeader){
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

	double getMedian(ArrayList<Double> values){
		DescriptiveStatistics stat = new DescriptiveStatistics( Doubles.toArray(values));	
		return stat.getPercentile(50);
	}
}

class Prediction{
	String sourceName;
	String targetName;
	int fold;
	int repeat;
	double AUC;
	double wAUC;

	public Prediction(String srcName,String tarName,int fold,int repeat,double AUC){
		this.sourceName = srcName;
		this.targetName = tarName;
		this.fold = fold;
		this.repeat = repeat;
		this.AUC = AUC;
	}

	public Prediction(String srcName,String tarName,int fold,int repeat,double AUC, double wAUC){
		this.sourceName = srcName;
		this.targetName = tarName;
		this.fold = fold;
		this.repeat = repeat;
		this.AUC = AUC;
		this.wAUC = wAUC;
	}
}
