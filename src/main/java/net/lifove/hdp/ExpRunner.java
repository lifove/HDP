package net.lifove.hdp;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.lifove.hdp.util.Utils;
import net.lifove.hdp.util.Utils.FeatureSelectors;
import weka.core.Instances;

public class ExpRunner {
	
	String [] projects = {
			//"MIM/MIMEtc.arff",
			//"MIM/MIMMylyn.arff",
			//"MIM/MIMTeam.arff",
			"ReLink/Safe.arff",
			"ReLink/Apache.arff",
			"ReLink/Zxing.arff",
			"NASA/mc2.arff", // new
			"NASA/pc5.arff", // new
			"NASA/pc1.arff",
			"NASA/pc2.arff", // new
			"NASA/jm1.arff", // new
			"NASA/pc4.arff",
			"NASA/kc3.arff", // new
			"NASA/pc3.arff",
			"NASA/mw1.arff",
			"NASA/cm1.arff",
			"NASA/mc1.arff", // new
			"SOFTLAB/ar5.arff",
			"SOFTLAB/ar3.arff",
			"SOFTLAB/ar4.arff",
			"SOFTLAB/ar1.arff",
			"SOFTLAB/ar6.arff",
			"CK/ant-1.3.arff",
			"CK/arc.arff",
			"CK/camel-1.0.arff",
			"CK/poi-1.5.arff",
			"CK/redaktor.arff",
			"CK/skarbonka.arff",
			"CK/tomcat.arff",
			"CK/velocity-1.4.arff",
			"CK/xalan-2.4.arff",
			"CK/xerces-1.2.arff",
			"AEEEM/PDE.arff",
			"AEEEM/EQ.arff",
			"AEEEM/LC.arff",
			"AEEEM/JDT.arff",
			"AEEEM/ML.arff",
	};

	public static void main(String[] args) {
		new ExpRunner().run(args);
	}
	
	public void run(String[] args){
		String pathToDataset = args[0];
		String pathToSavedMatchingScores = args[1];
		String pathToSaveResults = args[2];
		String strFSelector = args[3];
		Double cutoff = Double.parseDouble(args[4]);
		String analyzer = args[5];
		String ml = args[6]; //"weka.classifiers.functions.SimpleLogistic"
		Boolean isWPDPWithFS = Boolean.parseBoolean(args[7]); // true of false;
		int numThreadsForMatching = Integer.parseInt(args[8]);
		
		Runner runner = new Runner();
		FeatureSelectors fSelector = null;
		if(strFSelector.equals("GainRatio"))
			fSelector = FeatureSelectors.GainRatio;
		else if(strFSelector.equals("ChiSquare"))
			fSelector = FeatureSelectors.ChiSquare;
		else if(strFSelector.equals("RelieF"))
			fSelector = FeatureSelectors.RelieF;
		else if(strFSelector.equals("Significance"))
			fSelector = FeatureSelectors.Significance;
		else if(strFSelector.equals("None"))
			fSelector = FeatureSelectors.None;
		
		DecimalFormat dec = new DecimalFormat("0.00");
		conductExp(runner, projects, pathToDataset, pathToSavedMatchingScores, pathToSaveResults, fSelector, dec, cutoff,analyzer, ml, isWPDPWithFS,numThreadsForMatching);
		
	}
	
	private void conductExp(Runner runner, String[] projects, String pathToDataset, String pathToSavedMatchingScores,String pathToSaveResults,
			FeatureSelectors fSelector, DecimalFormat dec, double cutoff,String analyzer, String mlAlg, boolean isWPDPWithFS,int numThreadsForMatching) {
		
		String strIsWPDPWithFS = isWPDPWithFS? "_WPDP_FS":"";
		Path path = Paths.get(pathToSaveResults + "/HDP_C" + dec.format(cutoff) + "_" + fSelector.name()+ "_" + analyzer +  "_" + mlAlg + strIsWPDPWithFS + "_main.txt");
		
		HashMap<String,ArrayList<String>> mapMatchedMetrics = new HashMap<String,ArrayList<String>>();
		
		HashMap<String,String> withinResults = new HashMap<String,String>();
		
		// key srcName-tarName value = HashMap<String,Double> (key=srcAttrIdx + "-" + tarAttrIdx, score)
		HashMap<String,HashMap<String,Double>> matchingScoresByAttributeIndices = loadExsitingMatchingScores(pathToSavedMatchingScores,analyzer);
		
		OpenOption openOpt = StandardOpenOption.CREATE;
		HashSet<String> existingPrediciton = new HashSet<String>(); //"repeat,fold,Group/data.arff,group/data2.arff"
		if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
			openOpt = StandardOpenOption.APPEND;
			ArrayList<String> lines = Utils.getLines(path.toString(),false);
			for(int i=0;i<lines.size()-1;i++){ // ignore the last line which might be corrupted.
				String[] splitLine=lines.get(i).split(",");
				existingPrediciton.add(splitLine[0]+","+splitLine[1]+","+splitLine[2]+","+splitLine[3]);
				String key = splitLine[3] + splitLine[1] + "," + splitLine[2]; // target + repeat + "," + fold;
				String withinResult = splitLine[5] + "," + splitLine[6] + "," + splitLine[7] +"," + splitLine[8];
				withinResults.put(key, withinResult);
			}
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,openOpt)) {
			
			HashMap<String,MetricSelector> selectedSourceFeatures = new HashMap<String,MetricSelector>();
			
			for(String target:projects){
				for(String source:projects){
					if(source.equals(target))
						continue;
					
					String sourceName = source.split("/")[1].replace(".arff", "");
					String targetName = target.split("/")[1].replace(".arff", "");
					
					String[] srclabelInfo = getLabelInfo(source);
					String[] tarlabelInfo = getLabelInfo(target);
					
					Instances sourceInstances = Utils.loadArff(pathToDataset +  source, srclabelInfo[0]);
					Instances targetInstances = Utils.loadArff(pathToDataset +  target, tarlabelInfo[0]);
					
					// Skip datasets with the same number of attributes
					if(sameMetricSets(sourceInstances,targetInstances)){
						System.err.println("SKIP: the number of attributes is same.: " + source + "==> " + target);
						continue;
					}
					
					HashMap<String,Double>  matchingScores = matchingScoresByAttributeIndices.size()!=0?
							getMatchingScoresByAttributeNames(sourceName,targetName,sourceInstances,targetInstances,matchingScoresByAttributeIndices.get(sourceName + "-" +targetName)):new HashMap<String, Double>();
					
					if(!selectedSourceFeatures.containsKey(sourceName)){
						MetricSelector mSelector = new MetricSelector(sourceInstances,fSelector);
						sourceInstances = mSelector.getNewInstances();
						selectedSourceFeatures.put(sourceName,mSelector);
					} else
						sourceInstances = selectedSourceFeatures.get(sourceName).getNewInstances();
					
					ArrayList<String> selectedSrcAttrNames = getAttrNames(sourceInstances);
					
					ArrayList<String> strMatchedMetrics;
					
					String keyForMatchedMetrics =source + target;
					
					if(mapMatchedMetrics.containsKey(keyForMatchedMetrics))
						strMatchedMetrics = mapMatchedMetrics.get(keyForMatchedMetrics);
					else{
						//HashMap<String,Double> matchingScores = loadExsitingMatchingScores();
						if(matchingScores.size()!=0){
							matchingScores = getMatchingScoresBasedOnSelectedSrcAttributes(selectedSrcAttrNames,matchingScores);
							strMatchedMetrics = new MetricMatcher(sourceInstances,targetInstances,cutoff,matchingScores).match();
						}else{
							strMatchedMetrics = new MetricMatcher(sourceInstances,targetInstances,cutoff,numThreadsForMatching).match();
						}
						mapMatchedMetrics.put(keyForMatchedMetrics, strMatchedMetrics);
					}
					
					int numRuns = 500;
					int folds =2;
					
					for(int repeat=0;repeat < numRuns;repeat++){
						
						// randomize with different seed for each iteration
						targetInstances.randomize(new Random(repeat)); 
						targetInstances.stratify(folds);
						
						for(int fold = 0; fold < folds; fold++){
							
							if(existingPrediciton.contains(repeat + "," +fold + "," + source + "," + target)) continue;
							
							String withinResult = "";
							
							String key = target + repeat + "," + fold; 
							if(withinResults.containsKey(key))
								withinResult = withinResults.get(key);
							else{
								withinResult = Utils.doCrossPrediction(targetInstances.trainCV(folds, fold), 
													targetInstances.testCV(folds, fold),
													tarlabelInfo[1],mlAlg,isWPDPWithFS,fSelector);
								withinResults.put(key,withinResult);		
							}
							
							String result = runner.doHDP(false, sourceInstances, targetInstances.testCV(folds, fold), srclabelInfo[0], srclabelInfo[1],
									tarlabelInfo[0], tarlabelInfo[1], strMatchedMetrics, cutoff, true,FeatureSelectors.None,sourceName,targetName,mlAlg);
							
							if(result.equals(""))
								continue;
							
							writer.write(repeat + "," +fold + "," + source + "," + target + "," + 
											source + target + "," +
											withinResult + ","+ result + "\n" );
						}
					}
				}
			}
			
			writer.write("##########END#########");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private HashMap<String, Double> getMatchingScoresBasedOnSelectedSrcAttributes(
			ArrayList<String> selectedSrcAttrNames, HashMap<String, Double> matchingScores) {
		
		ArrayList<String> keysToBeDeleted = new ArrayList<String>();
		
		for(String key:matchingScores.keySet()){
			String srcAttrName = key.split("-")[0];
			if(!selectedSrcAttrNames.contains(srcAttrName))
				keysToBeDeleted.add(key);
				
		}
		
		for(String key:keysToBeDeleted){
			matchingScores.remove(key);
		}
		
		return matchingScores;
	}

	private ArrayList<String> getAttrNames(Instances sourceInstances) {
		ArrayList<String> attrNames = new ArrayList<String>();
		
		for(int idx=0;idx<sourceInstances.numAttributes();idx++){
			if(idx==sourceInstances.classIndex())
				continue;
			attrNames.add(sourceInstances.attribute(idx).name());
		}
		
		return attrNames;
	}

	private HashMap<String, Double> getMatchingScoresByAttributeNames(String sourceName, String targetName,
			Instances sourceInstances, Instances targetInstances, HashMap<String, Double> matchingScoresByAttrIndices) {
		
		HashMap<String, Double> matchingScores = new HashMap<String, Double>();
		
		for(String key:matchingScoresByAttrIndices.keySet()){
			String[] splitKey = key.split("-");
			String nameKey = sourceInstances.attribute(Integer.parseInt(splitKey[0])).name() + "-" + 
						targetInstances.attribute(Integer.parseInt(splitKey[1])).name();
			
			matchingScores.put(nameKey, matchingScoresByAttrIndices.get(key));
		}
		
		return matchingScores;
	}

	private HashMap<String, HashMap<String,Double>> loadExsitingMatchingScores(String pathToSavedMatchingScores,String targetAnalyzer) {
		
		HashMap<String, HashMap<String,Double>> matchigScores = new HashMap<String, HashMap<String,Double>>();
		
		ArrayList<String> lines = Utils.getLines(pathToSavedMatchingScores, false);
		//EQ_Apache_KSAnalyzer:(62):34-25|0.5341583259987994,25-16|0.06697771143246822,25-22|0.06697771143246822,25-23|0.06697771143246822,25-24|0.06697771143246822,17-18|0.01901330625300679,25-9|0.01853080500445481,17-20|0.01538122187577895,17-19|0.007897313650542803,56-1|0.00751903269119536,17-25|0.006496444366023146,34-23|0.002761266925471406,34-20|0.0026577341144828903,25-11|0.0020435225174835203,34-22|0.0010791322997167896,56-0|6.84379370588295E-4,24-5|3.803812184364208E-4,27-2|3.803812184364208E-4,56-5|1.6208595014410854E-4,34-16|1.357503293103468E-4,32-20|8.760849538347326E-5,25-25|7.821786108341833E-5,40-2|6.40427387863518E-5,34-24|6.085291961488437E-5,51-3|4.32417052848022E-5,3-3|3.497275689923196E-5,24-0|3.086217235814814E-5,24-1|3.086217235814814E-5,32-25|3.086217235814814E-5,56-2|
		
		for(String line:lines){
			String[] splitByColon = line.split(":");
			String soruceName = splitByColon[0].split("_")[0];
			String targetName = splitByColon[0].split("_")[1];
			String analyzer = splitByColon[0].split("_")[2];
			
			if(!analyzer.equals(targetAnalyzer)) continue;
			
			String[] matchingInfo = splitByColon[2].split(",");
			
			HashMap<String,Double> mapMatchingInfo= new HashMap<String,Double>();
			
			for(String matching:matchingInfo){
				String[] splitInfo = matching.split("\\|");
				int srcAttrIdx = Integer.parseInt(splitInfo[0].split("-")[0]);
				int tarAttrIdx = Integer.parseInt(splitInfo[0].split("-")[1]);
				double score = Double.parseDouble(splitInfo[1]);
				mapMatchingInfo.put(srcAttrIdx + "-" + tarAttrIdx,score);				
			}

			matchigScores.put(soruceName + "-" + targetName, mapMatchingInfo);
		}
		
		return matchigScores;
	}

	private boolean sameMetricSets(Instances sourceInstances, Instances targetInstances) {
		
		if(sourceInstances.numAttributes()!=targetInstances.numAttributes())
			return false;
		
		for(int attrIdx = 0; attrIdx < sourceInstances.numAttributes();attrIdx++){
			if(!sourceInstances.attribute(attrIdx).name().equals(targetInstances.attribute(attrIdx).name()))
				return false;
		}
		
		return true;
	}

	private String[] getLabelInfo(String path) {
		
		String[] labelInfo = new String[2];
		
		String group = path.substring(0, path.indexOf("/"));
		
		if(group.equals("ReLink")){
			labelInfo[0] = "isDefective";
			labelInfo[1] = "TRUE";
		}
		
		if(group.equals("NASA")){
			labelInfo[0] = "Defective";
			labelInfo[1] = "Y";
		}
		
		if(group.equals("AEEEM")){
			labelInfo[0] = "class";
			labelInfo[1] = "buggy";
		}
		
		if(group.equals("SOFTLAB")){
			labelInfo[0] = "defects";
			labelInfo[1] = "true";
		}
		
		if(group.equals("CK")){
			labelInfo[0] = "bug"; // bug or class
			labelInfo[1] = "buggy";
		}
		
		if(group.equals("MIM")){
			labelInfo[0] = "class"; // bug or class
			labelInfo[1] = "buggy";
		}
		
		return labelInfo;
	}
}
