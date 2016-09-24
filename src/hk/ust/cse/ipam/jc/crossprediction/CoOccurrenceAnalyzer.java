package hk.ust.cse.ipam.jc.crossprediction;

import hk.ust.cse.ipam.jc.crossprediction.data.MatchedAttribute;
import hk.ust.cse.ipam.utils.ArrayListUtil;
import hk.ust.cse.ipam.utils.WekaUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.commons.math3.util.MathArrays;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.google.common.primitives.Doubles;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class CoOccurrenceAnalyzer {
	Instances sourceInstances, newSourceInstances, newTargetInstances, newRandomSourceInstances, newRandomTargetInstances;
	public Instances targetInstances;
	public ArrayList<Integer> indexOfLabeledTargetInstances;
	Analyzer selectedAnalyzer;
	String sourceLabelName,targetLabelName,sourceLabelPos,targetLabelPos;
	public ArrayList<MatchedAttribute> matchedAttribute, allMatchedAttributes, allMatchedAttributeAfterFiltering, matchedAttributeByRandom;
	static HashMap<String,MatchedAttribute> mapMatchedAttribute = new HashMap<String,MatchedAttribute>(); // key "sourceIdx-targetIdx"
	
	boolean applyCutoff = false;
	public double cutoff = 0.9;
	public int numOfAttibutesByFeatureSelection = 0;
	public boolean useTargetLabeledInstances = false;
	public boolean useLBMFilter = true;
	public boolean useDMFilter = true;
	
	static public enum DataKind {SOURCE, TARGET}
	
	static public enum Analyzer {UAnalyzer, KSAnalyzer,  PAnalyzer, QAnalyzer,
	    ASAnalyzer, PCoAnalyzer, PCoAnalyzerUsingTargetLabel, TAnalyzer, PIAnalyzer,
	    PKNAnalyzer,SKNAnalyzer,
	    MultiAnalyzer,
	    AEDAnalyzer, AL1DAnalyzer, SCoAnalyzer, SemiASAnalyzerPOS, SemiASAnalyzerNEG, SemiPCoAnalyzer, SemiASAnalyzer
	}
	
	CoOccurrenceAnalyzer(String analyzer,Instances sourceInstances, Instances targetInstances,
			String sourceLabelName,String sourceLabelPos,String targetLabelName,String targetLabelPos,
			boolean applycutoff,double cutoff, boolean useTargetLabeledInstances,boolean useFeatureSelection,boolean useLBMFilter,boolean useDMFilter,RConnection rServe){
		
		selectedAnalyzer = Analyzer.valueOf(analyzer);
		this.sourceInstances = sourceInstances;
		this.targetInstances = targetInstances;
		this.sourceLabelName = sourceLabelName;
		this.targetLabelName = targetLabelName;
		this.sourceLabelPos = sourceLabelPos;
		this.targetLabelPos = targetLabelPos;
		this.applyCutoff = applycutoff;
		this.cutoff = cutoff;
		this.useTargetLabeledInstances = useTargetLabeledInstances;
		this.useLBMFilter = useLBMFilter;
		this.useDMFilter = useDMFilter;
		c=rServe;
		
	}
	
	CoOccurrenceAnalyzer(String analyzer,Instances sourceInstances, Instances targetInstances,
			String sourceLabelName,String sourceLabelPos,String targetLabelName,String targetLabelPos,
			boolean applycutoff,double cutoff, boolean useTargetLabeledInstances,boolean useFeatureSelection,boolean useLBMFilter,boolean useDMFilter){
		selectedAnalyzer = Analyzer.valueOf(analyzer);
		this.sourceInstances = sourceInstances;
		this.targetInstances = targetInstances;
		this.sourceLabelName = sourceLabelName;
		this.targetLabelName = targetLabelName;
		this.sourceLabelPos = sourceLabelPos;
		this.targetLabelPos = targetLabelPos;
		this.applyCutoff = applycutoff;
		this.cutoff = cutoff;
		this.useTargetLabeledInstances = useTargetLabeledInstances;
		this.useLBMFilter = useLBMFilter;
		this.useDMFilter = useDMFilter;
	}
	
	public void runAnalyzer(){
		
		numOfAttibutesByFeatureSelection = sourceInstances.numAttributes()-1; // except for a label attribute
		
		//TODO
		//System.out.println();
		//System.out.println("--------" + targetInstances);
		
		if(useTargetLabeledInstances){
			if(indexOfLabeledTargetInstances==null)
				indexOfLabeledTargetInstances = getLabeledTargetInstancesByPoPIndex(targetInstances,true);
		}
		
		if(allMatchedAttributes==null){
			// get labeled target instances
			
			
			// get matched Attributes
			switch(selectedAnalyzer){
			//case UAnalyzer:
			//	allMatchedAttributes = analyzeByHeuristic();
			//	break;
			case ASAnalyzer:
				allMatchedAttributes = analyzeByAvgAndStd();
				break;
			case PAnalyzer:
				allMatchedAttributes = analyzeByPercentile();
				break;
			case QAnalyzer:
				allMatchedAttributes = analyzeByQuartiles();
				break;
			case KSAnalyzer:
				allMatchedAttributes = analyzeByKolmororovSmirnovTest();
				break;
			case PCoAnalyzer:
				allMatchedAttributes = analyzeByPearsonCorrealation(false);
				break;
			case PCoAnalyzerUsingTargetLabel:
				allMatchedAttributes = analyzeByPearsonCorrealation(true);
				break;
			case TAnalyzer:
				allMatchedAttributes = analyzeByTtest();
				break;
			case UAnalyzer:
				allMatchedAttributes = analyzeByUtest();
				break;
			case PIAnalyzer:
				allMatchedAttributes = analyzeByDistanceMatrix();
				break;
			case SCoAnalyzer:
				allMatchedAttributes = analyzeBySpearmansCorrealation();
				break;
			case AEDAnalyzer:
				allMatchedAttributes = analyzeByAverageDistance(Analyzer.AEDAnalyzer);
				break;
			case AL1DAnalyzer:
				allMatchedAttributes = analyzeByAverageDistance(Analyzer.AL1DAnalyzer);
				break;
			case PKNAnalyzer:
				allMatchedAttributes = analyzeByPeakednessOrSkewness(Analyzer.PKNAnalyzer);
				break;
			case SKNAnalyzer:
				allMatchedAttributes = analyzeByPeakednessOrSkewness(Analyzer.SKNAnalyzer);
				break;
			case MultiAnalyzer:
				allMatchedAttributes = analyzeByMultiAnalyzer();
				break;
			//=============================================================
			// options below analyze co-occurrence from all source and only labeled target instances.
			case SemiPCoAnalyzer:
				if(indexOfLabeledTargetInstances==null){
					System.err.println("SemiPCoAnalyzer Please, turn on useTargetLabeledInstances");
					System.exit(0);
				}
				allMatchedAttributes = analyzeByLabeledPearsonCorrealation();
				break;
			case SemiASAnalyzerPOS:
				if(indexOfLabeledTargetInstances==null){
					System.err.println("SemiASAnalyzerPOS Please, turn on useTargetLabeledInstances");
					System.exit(0);
				}
				allMatchedAttributes = analyzeByPosLabeledAvgAndStd(true);
				break;
			case SemiASAnalyzerNEG:
				if(indexOfLabeledTargetInstances==null){
					System.err.println("SemiASAnalyzerNEG Please, turn on useTargetLabeledInstances");
					System.exit(0);
				}
				allMatchedAttributes = analyzeByPosLabeledAvgAndStd(false);
				break;
			case SemiASAnalyzer:
				if(indexOfLabeledTargetInstances==null){
					System.err.println("SemiASAnalyzer Please, turn on useTargetLabeledInstances");
					System.exit(0);
				}
				allMatchedAttributes = analyzeByLabeledAvgAndStd();
				break;
			default:
				break;
			}
		}
		
		if(useLBMFilter || useDMFilter){
			allMatchedAttributeAfterFiltering = applyFilters(sourceInstances,targetInstances,allMatchedAttributes,sourceLabelPos,useLBMFilter,useDMFilter);
			// cutoff!!, reusing allMatchedAttributes
			matchedAttribute = getFinalListOfMatchedAttributes(allMatchedAttributeAfterFiltering, cutoff);
		}
		else
			matchedAttribute = getFinalListOfMatchedAttributes(allMatchedAttributes, cutoff);
		
		// based on matched attributes, create new source and target data sets
		createNewSourceAndTargetdatasets(matchedAttribute,false);
		
		//System.out.println(newSourceInstances.toString());
		//System.out.println(newTargetInstances.toString());
		//System.exit(0);
		
	}

	private ArrayList<MatchedAttribute> analyzeByKolmororovSmirnovTest() {
		ArrayList<MatchedAttribute> matchedAttributes = getPValueOfKSTest(sourceInstances,targetInstances,sourceLabelName);
		return matchedAttributes;
	}

	static ArrayList<MatchedAttribute> applyFilters(Instances sourceInstances,
				Instances targetInstances,
				ArrayList<MatchedAttribute> matchedAttributes,
				String srcLabelPos,
				boolean applyLBMFilter,
				boolean applyDMFilter){
		
		ArrayList<MatchedAttribute> matchedAttributeFiltered = new ArrayList<MatchedAttribute>();
		
		int numSrcInstances = sourceInstances.numInstances();
		int numTarInstances = targetInstances.numInstances();
		Instances source = new Instances(sourceInstances,0,numSrcInstances);
		Instances target = new Instances(targetInstances,0,numTarInstances);
		
		for(MatchedAttribute matchedAttribute:matchedAttributes){
			int srcAttributeIndex = matchedAttribute.getSourceAttrIndex();
			int tarAttributeIndex = matchedAttribute.getTargetAttrIndex();
			
			// ignore this source feature, if its clean mean is greater than its buggy mean
			if(applyLBMFilter && isLowBuggyMedian(sourceInstances,srcAttributeIndex,srcLabelPos))
				continue;
			
			// modality is different, ignore
			if(applyDMFilter && !isTheSameModality(source,srcAttributeIndex,target,tarAttributeIndex))
				continue;
			
			// add only when all filter requirmnets are passed.
			matchedAttributeFiltered.add(matchedAttribute);
		}
		
		return matchedAttributeFiltered;
	}

	static boolean isLowBuggyMedian(Instances source,int srcAttributeIndex, String sourceLabelPos){
		
		//double[] srcFeatureAllValues = source.attributeToDoubleArray(srcAttributeIndex);
		double[] srcFeatureCleanValues = WekaUtils.getValuesByClassValue(source, srcAttributeIndex,sourceLabelPos,false);
		double[] srcFeatureBuggyValues = WekaUtils.getValuesByClassValue(source, srcAttributeIndex,sourceLabelPos,true);
		
		//DescriptiveStatistics srcAllStat = new DescriptiveStatistics(srcFeatureAllValues);
		DescriptiveStatistics srcCleanStat = new DescriptiveStatistics(srcFeatureCleanValues);
		DescriptiveStatistics srcBuggyStat = new DescriptiveStatistics(srcFeatureBuggyValues);
		//double srcAllMean = getMean(srcAllStat);
		double srcCleanMean = getMedian(srcCleanStat);
		double srcBuggyMean = getMedian(srcBuggyStat);
		
		// ignore this source feature, if its clean mean is greater than its buggy mean
		if(srcBuggyMean/srcCleanMean <= 1.1)
			return true;
		else
			return false;
	}
	
	private static boolean isTheSameModality(Instances source,int srcAttributeIndex,Instances target,int tarAttributeIndex) {
		
		double[] srcFeatureAllValues = source.attributeToDoubleArray(srcAttributeIndex);
		double[] tarFeatureAllValues = target.attributeToDoubleArray(tarAttributeIndex);
		
		DescriptiveStatistics srcStat = new DescriptiveStatistics(srcFeatureAllValues);
		DescriptiveStatistics tarStat = new DescriptiveStatistics(tarFeatureAllValues);
		
		double srcMedianOfP25 = srcStat.getPercentile(25);
		double srcMedianOfP75 = srcStat.getPercentile(75);
		double tarMedianOfP25 = tarStat.getPercentile(25);
		double tarMedianOfP75 = tarStat.getPercentile(75);
		
		double upperScore = srcMedianOfP25>tarMedianOfP25?tarMedianOfP25/srcMedianOfP25:srcMedianOfP25/tarMedianOfP25;
		double belowScore = srcMedianOfP75>tarMedianOfP75?tarMedianOfP75/srcMedianOfP75:srcMedianOfP75/tarMedianOfP75;
		
		double twoModeMedianComparisonScore =  (upperScore + belowScore)/2;
		if(Double.isNaN(belowScore) || Double.isInfinite(belowScore))
			twoModeMedianComparisonScore = upperScore;
		
		return twoModeMedianComparisonScore>0.5;		
	}

	private static double getMedian(DescriptiveStatistics stat) {
		//double value = stat.getGeometricMean() == 0 || Double.isNaN(stat.getGeometricMean())?stat.getMean():stat.getGeometricMean();
		//return stat.getMean();
		return stat.getPercentile(50);
	}

	private ArrayList<MatchedAttribute> analyzeByMultiAnalyzer() {
		// Use AS, TA, AED, AL1D together
		ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
		
		ArrayList<MatchedAttribute> allMatchedAttributesByAS = analyzeByAvgAndStd();
		ArrayList<MatchedAttribute> allMatchedAttributesByTA = analyzeByTtest();
		ArrayList<MatchedAttribute> allMatchedAttributesByAED = analyzeByAverageDistance(Analyzer.AEDAnalyzer);
		ArrayList<MatchedAttribute> allMatchedAttributesByAL1D = analyzeByAverageDistance(Analyzer.AL1DAnalyzer);
		
		HashMap<String,MatchedAttribute> hashMapForMatchedAttributes = new HashMap<String,MatchedAttribute>();
		for(int i=0;i<allMatchedAttributesByAS.size();i++){
			hashMapForMatchedAttributes = reviseHashMapForMatchedAttributes(hashMapForMatchedAttributes,allMatchedAttributesByAS,i);
			hashMapForMatchedAttributes = reviseHashMapForMatchedAttributes(hashMapForMatchedAttributes,allMatchedAttributesByTA,i);
			hashMapForMatchedAttributes = reviseHashMapForMatchedAttributes(hashMapForMatchedAttributes,allMatchedAttributesByAED,i);
			hashMapForMatchedAttributes = reviseHashMapForMatchedAttributes(hashMapForMatchedAttributes,allMatchedAttributesByAL1D,i);
		}
		
		for(String key:hashMapForMatchedAttributes.keySet()){
			hashMapForMatchedAttributes.get(key).finalizeMatchingScoreByAverage();
			matchedAttributes.add(hashMapForMatchedAttributes.get(key));
		}
		
		return matchedAttributes;
	}
	
	private HashMap<String,MatchedAttribute> reviseHashMapForMatchedAttributes(HashMap<String,MatchedAttribute> hashMapForMatchedAttributes,ArrayList<MatchedAttribute> allMatchedAttributes,int index){
		
		
		MatchedAttribute ma = allMatchedAttributes.get(index);
		String key = ma.getSourceAttrIndex() + "_" + ma.getTargetAttrIndex();
		if(hashMapForMatchedAttributes.containsKey(key))
			hashMapForMatchedAttributes.get(key).getListOfMultipleMatchingScore().add(ma.getMatchingScore());
		else{
			MatchedAttribute maForMultipleScores = new MatchedAttribute(ma.getSourceAttrIndex(), ma.getTargetAttrIndex());
			maForMultipleScores.getListOfMultipleMatchingScore().add(ma.getMatchingScore());
			hashMapForMatchedAttributes.put(key,maForMultipleScores);
		}

		return hashMapForMatchedAttributes;
	}

	private ArrayList<MatchedAttribute> analyzeByPeakednessOrSkewness(Analyzer analyzer) {
		
		ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
		
		int srcClassIndex = sourceInstances.classIndex();
		int tarClassIndex = targetInstances.classIndex();
		
		int numSrcInstances = sourceInstances.numInstances();
		int numTarInstances = targetInstances.numInstances();
		
		Instances source = new Instances(sourceInstances,0,numSrcInstances);
		Instances target = new Instances(targetInstances,0,numTarInstances);
		
		for(int srcAttributeIndex=0;srcAttributeIndex<source.numAttributes();srcAttributeIndex++){
			if(srcClassIndex==srcAttributeIndex)
				continue;
			double srcPkn = getPeakednessOrSkewness(analyzer,source.attributeToDoubleArray(srcAttributeIndex));
			for(int tarAttributeIndex=0;tarAttributeIndex<target.numAttributes();tarAttributeIndex++){
				if(tarClassIndex==tarAttributeIndex)
					continue;
				double tarPkn = getPeakednessOrSkewness(analyzer,target.attributeToDoubleArray(tarAttributeIndex));

				MatchedAttribute ma = new MatchedAttribute(srcAttributeIndex,tarAttributeIndex,srcPkn>tarPkn?tarPkn/srcPkn:srcPkn/tarPkn);
				matchedAttributes.add(ma);
			}
		}
		
		// find min and max value
		double max = -1;
		double min = 1;
		for(MatchedAttribute ma:matchedAttributes){
			if(ma.getMatchingScore() > max)
				max = ma.getMatchingScore();
			if(ma.getMatchingScore() < min)
				min = ma.getMatchingScore();
		}
		
		// set all scores in 0 to 1
		for(MatchedAttribute ma:matchedAttributes){
			double newScore = WekaUtils.minMaxNormalize(min, max, ma.getMatchingScore());
			ma.setMatchingScore(newScore);
		}
		
		return matchedAttributes;
	}
	
	double getPeakednessOrSkewness(Analyzer analyzer,double[] values){
		DescriptiveStatistics stats = new DescriptiveStatistics(values);
		if(analyzer==Analyzer.PKNAnalyzer)
			return stats.getKurtosis();
		else
			return stats.getSkewness();
	}

	public void generateNewSourceAndTargetdatasetsByRandomAnalyzer(){
		// data sets randomly matched
		matchedAttributeByRandom = matchedAttributeByRandom(sourceInstances,targetInstances,matchedAttribute.size(),sourceInstances.classIndex(),targetInstances.classIndex());
		createNewSourceAndTargetdatasets(matchedAttributeByRandom,true);
	}
	
	ArrayList<MatchedAttribute> analyzeByDistanceMatrix(){
		
		
		Instances selectedAndSortedSrcInstances = new Instances(sourceInstances,0);
		Instances selectedAndSortedTgrInstances = new Instances(targetInstances,0);
		
		// select source and target instances in the same number
		// if source is bigger, we should randomly select values of an attribute from source, otherwise, from target.
		int numOfSourceInstances = sourceInstances.numInstances();
		int numOfTargetInstances = targetInstances.numInstances();
		boolean sourceBigger = numOfSourceInstances > numOfTargetInstances? true:false;
		
		if(sourceBigger){
			selectedAndSortedSrcInstances = WekaUtils.selectAndSortInsancesUsingPopIndex(sourceInstances, numOfTargetInstances);
			selectedAndSortedTgrInstances = WekaUtils.selectAndSortInsancesUsingPopIndex(targetInstances, numOfTargetInstances);
		}
		else{
			selectedAndSortedSrcInstances = WekaUtils.selectAndSortInsancesUsingPopIndex(sourceInstances, numOfSourceInstances);
			selectedAndSortedTgrInstances = WekaUtils.selectAndSortInsancesUsingPopIndex(targetInstances, numOfSourceInstances);
		}
		
		ArrayList<MatchedAttribute> matchedAttributes = getMatchedAttributesByTheNearestNeighbor(selectedAndSortedSrcInstances,selectedAndSortedTgrInstances);
		
		/*Collections.sort(matchedAttributes);
		
		ArrayList<Integer> selectedSourceAttributeIndex = new ArrayList<Integer>();
		ArrayList<Integer> selectedTargetAttributeIndex = new ArrayList<Integer>();
		
		ArrayList<MatchedAttribute> newMatchedAttributes = new ArrayList<MatchedAttribute>();
		
		for(MatchedAttribute matchedAttribute:matchedAttributes){
			
			int sourceIndex = matchedAttribute.getSourceAttrIndex();
			int targetIndex = matchedAttribute.getTargetAttrIndex();
			double score = matchedAttribute.getMatchingScore();
			
			if(!selectedSourceAttributeIndex.contains(sourceIndex)
					&& !selectedTargetAttributeIndex.contains(targetIndex)
			){
				newMatchedAttributes.add(new MatchedAttribute(sourceIndex,targetIndex,score));
				selectedSourceAttributeIndex.add(sourceIndex);
				selectedTargetAttributeIndex.add(targetIndex);
			}
		}
		
		/*double first = newMatchedAttributes.get(0).getMatchingScore();
		double end = newMatchedAttributes.get(newMatchedAttributes.size()-1).getMatchingScore();
		
		double meanOfFirstAndEnd = (double)(first+end)/2;
		
		matchedAttributes.clear();
		
		for(MatchedAttribute matchedAttribute:newMatchedAttributes){
			double score = matchedAttribute.getMatchingScore();

			if(score>meanOfFirstAndEnd){
				matchedAttributes.add(matchedAttribute);
			}
			else
				break;
		}*/
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByAverageDistance(Analyzer analyzer) {
		ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
		
		int srcClassIndex = sourceInstances.classIndex();
		int tarClassIndex = targetInstances.classIndex();
		
		int numSrcInstances = sourceInstances.numInstances();
		int numTarInstances = targetInstances.numInstances();
		
		Instances source = new Instances(sourceInstances,0,numSrcInstances);
		Instances target = new Instances(targetInstances,0,numTarInstances);
		
		int finalValueArraySize = source.numAttributes()-target.numAttributes()>0?source.numAttributes()-1:target.numAttributes()-1;
		
		TreeMap<String,ArrayList<Double>> distValues = new TreeMap<String,ArrayList<Double>>();
		
		int repeat = 1000;
		
		for(int run=0;run<repeat;run++){
			
			source.randomize(new Random(run));
			target.randomize(new Random(run));
			
			for(int srcAttrIndex=0;srcAttrIndex<source.numAttributes()-1;srcAttrIndex++){
				if(srcAttrIndex==srcClassIndex)
					continue;

				for(int tarAttrIndex=0;tarAttrIndex<target.numAttributes()-1;tarAttrIndex++){
					if(tarAttrIndex==tarClassIndex)
						continue;
					
					double[] srcValues = amendDimension(source.attributeToDoubleArray(srcAttrIndex),finalValueArraySize);
					double[] tarValues = amendDimension(target.attributeToDoubleArray(tarAttrIndex),finalValueArraySize);
					double distance=0.0;
					if(analyzer==Analyzer.AEDAnalyzer)
						distance = MathArrays.distance(srcValues, tarValues);
					else if(analyzer==Analyzer.AL1DAnalyzer){
						distance = MathArrays.distance1(srcValues, tarValues);
					}
					
					if(distValues.get(srcAttrIndex + "_" + tarAttrIndex)!=null)
						distValues.get(srcAttrIndex + "_" + tarAttrIndex).add(distance);
					else{
						distValues.put(srcAttrIndex + "_" + tarAttrIndex, new ArrayList<Double>());
						distValues.get(srcAttrIndex + "_" + tarAttrIndex).add(distance);
					}
				}
			}	
		}
		
		double minDistance = ArrayListUtil.getAverage(distValues.get("0_0"));
		double maxDistance = 0.0;
		
		for(String key:distValues.keySet()){
			double curValue = ArrayListUtil.getAverage(distValues.get(key));
			if(curValue > maxDistance)
				maxDistance = ArrayListUtil.getAverage(distValues.get(key));
			if(curValue < minDistance){
				minDistance = curValue;
			}
		}
		
		// min-max normalization to get 0-1 values and generatematchedAttributes
		for(String key:distValues.keySet()){
			int srcAttrIndex = Integer.parseInt(key.split("_")[0]);
			int tarAttrIndex = Integer.parseInt(key.split("_")[1]);
			double curValue = ArrayListUtil.getAverage(distValues.get(key));

			matchedAttributes.add(new MatchedAttribute(srcAttrIndex, tarAttrIndex, 1.0-(curValue-minDistance)/(maxDistance-minDistance)));
		}
		
		return matchedAttributes;
	}
	
	double[] amendDimension(double[] originalData, int size){
		
		if (originalData.length==size)
			return originalData;
		
		double[] values = new double[size];
		
		for(int i=0;i<size;i++){
			if(i<originalData.length)
				values[i]=originalData[i];
			else
				values[i]=0.0;
		}
		
		return values;
	}

	ArrayList<MatchedAttribute> getMatchedAttributesByTheNearestNeighbor(Instances source,Instances target){
		ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
		
		int srcClassIndex = source.classIndex();
		int tarClassIndex = target.classIndex();
		
		for(int srcAttrIndex=0;srcAttrIndex<source.numAttributes()-1;srcAttrIndex++){
			if(srcAttrIndex==srcClassIndex)
				continue;

			for(int tarAttrIndex=0;tarAttrIndex<target.numAttributes()-1;tarAttrIndex++){
				if(tarAttrIndex==tarClassIndex)
					continue;
				
				double[] srcValues = source.attributeToDoubleArray(srcAttrIndex);
				double[] tarValues = target.attributeToDoubleArray(tarAttrIndex);
				double distance = MathArrays.distance(srcValues, tarValues);
				
				matchedAttributes.add(new MatchedAttribute(srcAttrIndex,tarAttrIndex,distance));
			}
		}
		
		// find min and max value
		double max = -1;
		double min = 1000000000;
		for(MatchedAttribute ma:matchedAttributes){
			if(ma.getMatchingScore() > max)
				max = ma.getMatchingScore();
			if(ma.getMatchingScore() < min)
				min = ma.getMatchingScore();
		}
		
		// set all scores in 0 to 1
		for(MatchedAttribute ma:matchedAttributes){
			double newScore = WekaUtils.minMaxNormalize(min, max, ma.getMatchingScore());
			ma.setMatchingScore(1-newScore);
		}
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByTtest(){
		ArrayList<MatchedAttribute> matchedAttributes = getPValueOfTtest(sourceInstances,targetInstances);
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByUtest(){
		ArrayList<MatchedAttribute> matchedAttributes = getPValueOfUtest(sourceInstances,targetInstances);
		return matchedAttributes;
	}
	
	ArrayList<Integer> getLabeledTargetInstancesByPoPIndex(Instances instances,boolean random){
		
		// Instances targetInstances = instances; for random
		// assign this to targetInstances again to keep the new order
		
		if(!random)
			this.targetInstances = WekaUtils.selectAndSortInsancesUsingPopIndex(instances, targetInstances.numInstances());
		else{
			this.targetInstances = instances;
		}
		
		int totalNumOfPosInstances = targetInstances.attributeStats(targetInstances.classAttribute().index()).nominalCounts[WekaUtils.getClassValueIndex(targetInstances, targetLabelPos)];
		int totalNumOfNegInstances = targetInstances.numInstances() - totalNumOfPosInstances;
		
		int randomSelectionSizeForPos = Math.round((float)totalNumOfPosInstances/10); //
		int randomSelectionSizeForNeg = Math.round((float)totalNumOfNegInstances/10); // 10%
		
		// feat at least 2 pos instances to analyzers
		/*if(randomSelectionSizeForPos<2){
			randomSelectionSizeForPos=2;
		}*/
		
		int totalRequiredNumInstances =  randomSelectionSizeForPos + randomSelectionSizeForNeg;
		
		ArrayList<Integer> indexOfSelectedInstances = new ArrayList<Integer>();
		
		// get pos instances selected
		for(int i=0; i < targetInstances.size();i++){
			if(indexOfSelectedInstances.size() < totalRequiredNumInstances){
				indexOfSelectedInstances.add(i);
			}
		}
		
		return indexOfSelectedInstances;
	}
	
	ArrayList<Integer> getLabeledTargetInstancesByPoPIndexAndConsideringPosAndNeg(Instances instances,boolean random){
		
		// Instances targetInstances = instances; for random
		
		// assign this to targetInstances again to keep the new order
		if(!random)
			this.targetInstances = WekaUtils.selectAndSortInsancesUsingPopIndex(instances, targetInstances.numInstances());
		else
			this.targetInstances = instances;
		
		int totalNumOfPosInstances = targetInstances.attributeStats(targetInstances.classAttribute().index()).nominalCounts[WekaUtils.getClassValueIndex(targetInstances, targetLabelPos)];
		int totalNumOfNegInstances = targetInstances.numInstances() - totalNumOfPosInstances;
		
		int randomSelectionSizeForPos = Math.round((float)totalNumOfPosInstances/10); //
		int randomSelectionSizeForNeg = Math.round((float)totalNumOfNegInstances/10); // 10%
		
		// feat at least 2 pos instances to analyzers
		if(randomSelectionSizeForPos<2){
			randomSelectionSizeForPos=2;
		}
		
		ArrayList<Integer> indexOfSelectedPosInstances = new ArrayList<Integer>();
		ArrayList<Integer> indexOfSelectedNegInstances = new ArrayList<Integer>();
		
		// get pos instances selected
		for(int i=0; i < targetInstances.size();i++){
			double labelValue = targetInstances.get(i).value(targetInstances.attribute(targetLabelName));
			if(labelValue==WekaUtils.getClassValueIndex(targetInstances, targetLabelPos)
					&& indexOfSelectedPosInstances.size() < randomSelectionSizeForPos){
				indexOfSelectedPosInstances.add(i);
			}
		}
		
		//System.out.println(randomSelectionSize);
		
		// get neg instances selected
		String negClassStringValue = WekaUtils.getNegClassStringValue(targetInstances, targetLabelName, targetLabelPos);
		for(int i=0; i < targetInstances.size();i++){
			double labelValue = targetInstances.get(i).value(targetInstances.attribute(targetLabelName));
			if(labelValue==WekaUtils.getClassValueIndex(targetInstances, negClassStringValue)
					&& indexOfSelectedNegInstances.size() < randomSelectionSizeForNeg){
				indexOfSelectedNegInstances.add(i);
			}
		}
		indexOfSelectedPosInstances.addAll(indexOfSelectedNegInstances);
		
		return indexOfSelectedPosInstances;
	}
	
	ArrayList<MatchedAttribute> analyzeByPosLabeledAvgAndStd(boolean isPos){
		ArrayList<MatchedAttribute> matchedAttributes= getMatchedAttributesByPosLabeledAvgAndStd(isPos);
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> getMatchedAttributesByPosLabeledAvgAndStd(boolean isPos){
		ArrayList<MatchedAttribute> matchedAttributes= new ArrayList<MatchedAttribute>();
		
		Instances labeledTargetInstances = WekaUtils.getInstancesFromIndice(targetInstances,indexOfLabeledTargetInstances);
		Instances sourceInstancesToAnalyze = WekaUtils.getInstancesBySpecificLabel(sourceInstances,sourceLabelPos,isPos);
		Instances targetInstancesToAnalyze = WekaUtils.getInstancesBySpecificLabel(labeledTargetInstances,targetLabelPos,isPos);
		
		ArrayList<Double> sourceAttrsAverage = WekaUtils.getAverageFromAllAttributes(sourceInstancesToAnalyze);
		ArrayList<Double> sourceAttrsSTD = WekaUtils.getSTDFromAllAttributes(sourceInstancesToAnalyze);
		ArrayList<Double> targetAttrsAverage = WekaUtils.getAverageFromAllAttributes(targetInstancesToAnalyze);
		ArrayList<Double> targetAttrsSTD = WekaUtils.getSTDFromAllAttributes(targetInstancesToAnalyze);
		
		for(int s=0;s<sourceAttrsAverage.size();s++){
			
			for(int t=0;t<targetAttrsAverage.size();t++){
				
				double averageDiff = 1/(Math.abs(sourceAttrsAverage.get(s)-targetAttrsAverage.get(t))+1);
				double stdDiff = 1/(Math.abs(sourceAttrsSTD.get(s)-targetAttrsSTD.get(t))+1);
				
				matchedAttributes.add(new MatchedAttribute(s,t,(averageDiff+stdDiff)/2));
			}
		}
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByLabeledAvgAndStd(){
		
		ArrayList<MatchedAttribute> posLabeledMatchedAttribute = getMatchedAttributesByPosLabeledAvgAndStd(true);
		ArrayList<MatchedAttribute> negLabeledMatchedAttribute = getMatchedAttributesByPosLabeledAvgAndStd(false);
		
		ArrayList<MatchedAttribute> matchedAttributes = new ArrayList<MatchedAttribute>();
		
		for(int i=0; i < posLabeledMatchedAttribute.size(); i++){
			int s = posLabeledMatchedAttribute.get(i).getSourceAttrIndex();
			int t = posLabeledMatchedAttribute.get(i).getTargetAttrIndex();
			double posScore = posLabeledMatchedAttribute.get(i).getMatchingScore();
			double negScore = negLabeledMatchedAttribute.get(i).getMatchingScore();
			
			matchedAttributes.add(new MatchedAttribute(s,t,(posScore+negScore)/2));
		}
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByAvgAndStd(){
		ArrayList<MatchedAttribute> matchedAttributes= new ArrayList<MatchedAttribute>();
		
		ArrayList<Double> sourceAttrsAverage = WekaUtils.getAverageFromAllAttributes(sourceInstances);
		ArrayList<Double> sourceAttrsSTD = WekaUtils.getSTDFromAllAttributes(sourceInstances);
		ArrayList<Double> targetAttrsAverage = WekaUtils.getAverageFromAllAttributes(targetInstances);
		ArrayList<Double> targetAttrsSTD = WekaUtils.getSTDFromAllAttributes(targetInstances);
		
		for(int s=0;s<sourceAttrsAverage.size();s++){
			
			for(int t=0;t<targetAttrsAverage.size();t++){
				//double averageDiff = 1/(Math.abs(sourceAttrsAverage.get(s)-targetAttrsAverage.get(t))+1);
				//double stdDiff = 1/(Math.abs(sourceAttrsSTD.get(s)-targetAttrsSTD.get(t))+1);
				double averageDiff = sourceAttrsAverage.get(s)<targetAttrsAverage.get(t)?sourceAttrsAverage.get(s)/targetAttrsAverage.get(t):targetAttrsAverage.get(t)/sourceAttrsAverage.get(s);
				double stdDiff = sourceAttrsSTD.get(s)<targetAttrsSTD.get(t)?sourceAttrsSTD.get(s)/targetAttrsSTD.get(t):targetAttrsSTD.get(t)/sourceAttrsSTD.get(s);
				
				matchedAttributes.add(new MatchedAttribute(s,t,(averageDiff+stdDiff)/2));
			}
		}
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByPercentile(){
		ArrayList<MatchedAttribute> matchedAttributes= new ArrayList<MatchedAttribute>();
		
		ArrayList<ArrayList<Double>> srcAttrsPercentiles = getPercentiles(sourceInstances);
		
		ArrayList<ArrayList<Double>> tarAttrsPercentiles = getPercentiles(targetInstances);
		
		for(int s=0;s<srcAttrsPercentiles.size();s++){
			
			for(int t=0;t<tarAttrsPercentiles.size();t++){
				
				double matchingScore = 0;
				ArrayList<Double> srcPercentiles = srcAttrsPercentiles.get(s);
				ArrayList<Double> tarPercentiles = tarAttrsPercentiles.get(t);
				for(int i=0;i<srcPercentiles.size();i++){
					if(srcPercentiles.get(i)>tarPercentiles.get(i)){
						matchingScore = matchingScore + tarPercentiles.get(i)/srcPercentiles.get(i);
					}
					else{
						double divideBy = tarPercentiles.get(i);
						if(divideBy==0)
							divideBy = 0.00001; // if 0. divided by resonably small number
						matchingScore = matchingScore + srcPercentiles.get(i)/divideBy;
					}
				}
				
				matchingScore = matchingScore / srcPercentiles.size();
				matchedAttributes.add(new MatchedAttribute(s,t,matchingScore));
			}
		}
		
		return matchedAttributes;
	}
	
	ArrayList<MatchedAttribute> analyzeByQuartiles(){
		ArrayList<MatchedAttribute> matchedAttributes= new ArrayList<MatchedAttribute>();
		
		ArrayList<ArrayList<Double>> srcAttrsQuartiles = getQuartiles(sourceInstances);
		
		ArrayList<ArrayList<Double>> tarAttrsQuartiles = getQuartiles(targetInstances);
		
		for(int s=0;s<srcAttrsQuartiles.size();s++){
			
			for(int t=0;t<tarAttrsQuartiles.size();t++){
				
				double matchingScore = 0;
				ArrayList<Double> srcQuartiles = srcAttrsQuartiles.get(s);
				ArrayList<Double> tarQuartiles = tarAttrsQuartiles.get(t);
				for(int i=0;i<srcQuartiles.size();i++){
					if(srcQuartiles.get(i)>tarQuartiles.get(i)){
						matchingScore = matchingScore + tarQuartiles.get(i)/srcQuartiles.get(i);
					}
					else{
						double divideBy = tarQuartiles.get(i);
						if(divideBy==0)
							divideBy = 0.00001; // if 0. divided by resonably small number
						matchingScore = matchingScore + srcQuartiles.get(i)/divideBy;
					}
				}
				
				matchingScore = matchingScore / srcQuartiles.size();
				matchedAttributes.add(new MatchedAttribute(s,t,matchingScore));
			}
		}
		
		return matchedAttributes;
	}
	
	ArrayList<ArrayList<Double>> getPercentiles(Instances instances){
		
		ArrayList<ArrayList<Double>> attrsPercentiles = new ArrayList<ArrayList<Double>>();
		
		for(int i=0;i<instances.numAttributes();i++){
			if(i==instances.classIndex())
				continue;
			
			DescriptiveStatistics stat = new DescriptiveStatistics(instances.attributeToDoubleArray(i));
			ArrayList<Double> percentiles = new ArrayList<Double>();
			for(int percentile = 10;percentile<=90;percentile=percentile+10){
				percentiles.add(stat.getPercentile(percentile));
			}
			attrsPercentiles.add(percentiles);
		}
		
		return attrsPercentiles;
	}
	
ArrayList<ArrayList<Double>> getQuartiles(Instances instances){
		
		ArrayList<ArrayList<Double>> attrsPercentiles = new ArrayList<ArrayList<Double>>();
		
		for(int i=0;i<instances.numAttributes();i++){
			if(i==instances.classIndex())
				continue;
			
			DescriptiveStatistics stat = new DescriptiveStatistics(instances.attributeToDoubleArray(i));
			ArrayList<Double> percentiles = new ArrayList<Double>();
			for(int percentile = 25;percentile<=75;percentile=percentile+25){
				percentiles.add(stat.getPercentile(percentile));
			}
			attrsPercentiles.add(percentiles);
		}
		
		return attrsPercentiles;
	}
	
	public static ArrayList<MatchedAttribute> matchedAttributeByRandom(Instances sourceInstances,Instances targetInstances, int numMatchedFeatures,int srcLabelIndex,int tgtLabelIndex){
		
		ArrayList<MatchedAttribute> matchedAttributesByRandom= new ArrayList<MatchedAttribute>();
		
		ArrayList<Integer> selectedSourceIndice = new ArrayList<Integer>();
		ArrayList<Integer> selectedTargetIndice = new ArrayList<Integer>();
		
		int numSourceFeatures = sourceInstances.numAttributes()-1; // ignore label attribute
		int numTargetFeautres = targetInstances.numAttributes()-1; // ignore label attribute
		
		Random rForSource = new Random();
		Random rForTarget = new Random();
		
		int selectedSourceIndex = -1;
		int selectedTargetIndex = -1;

		for(int i=0; i< numMatchedFeatures; i++){
			selectedSourceIndex = rForSource.nextInt(numSourceFeatures); // From 0 to sizeOfCandidates-1, random number generated
			if (srcLabelIndex==selectedSourceIndex)	// in case that class index is not the last
				selectedSourceIndex = numSourceFeatures;
			
			selectedTargetIndex = rForTarget.nextInt(numTargetFeautres);
			if (tgtLabelIndex==selectedTargetIndex) // in case that class index is not the last
				selectedTargetIndex = numTargetFeautres;
				
			if(selectedSourceIndice.contains(selectedSourceIndex) || selectedTargetIndice.contains(selectedTargetIndex) ){
				i--;
			}
			else{
				selectedSourceIndice.add(selectedSourceIndex);
				selectedTargetIndice.add(selectedTargetIndex);
				MatchedAttribute newMatchedAttribute = new MatchedAttribute(selectedSourceIndex,selectedTargetIndex,-1);
				matchedAttributesByRandom.add(newMatchedAttribute);
			}
		}
		
		return matchedAttributesByRandom;
	}
	
	void createNewSourceAndTargetdatasets(ArrayList<MatchedAttribute> matchedAttribute,boolean forRandom){
		
		// create attribute information
		ArrayList<Attribute> attributes = WekaUtils.createAttributeInfoForClassfication(matchedAttribute.size()+1); //for label +1
		
		Instances newSourceInstances, newTargetInstances;
		
		newSourceInstances = new Instances("newSource", attributes, 0);
		newTargetInstances = new Instances("newTarget", attributes, 0);
		
		for(Instance instance:sourceInstances){

			double[] vals = new double[attributes.size()];
			
			// process attribute values except for label
			for(int i=0; i<attributes.size()-1;i++){
				vals[i] = instance.value(matchedAttribute.get(i).getSourceAttrIndex());
			}
			// assign label value
			String currentInstaceLabel = instance.stringValue(sourceInstances.attribute(sourceLabelName));
			if(currentInstaceLabel.equals(sourceLabelPos))
				vals[attributes.size()-1] = WekaUtils.dblPosValue;
			else
				vals[attributes.size()-1] = WekaUtils.dblNegValue;
			
			newSourceInstances.add(new DenseInstance(1.0, vals));
		}
		
		for(Instance instance:targetInstances){

			double[] vals = new double[attributes.size()];
			
			// process attribute values except for label
			for(int i=0; i<attributes.size()-1;i++){
				vals[i] = instance.value(matchedAttribute.get(i).getTargetAttrIndex());
			}
			// assign label value
			String currentInstaceLabel = instance.stringValue(targetInstances.attribute(targetLabelName));
			if(currentInstaceLabel.equals(targetLabelPos))
				vals[attributes.size()-1] = WekaUtils.dblPosValue;
			else
				vals[attributes.size()-1] = WekaUtils.dblNegValue;
			
			newTargetInstances.add(new DenseInstance(1.0, vals));
		}
		
		ArrayList<Instance> labeledInstancesOfNewTarget = new ArrayList<Instance>();
		
		if(indexOfLabeledTargetInstances != null){
			for(int labeledInstanceIndex:indexOfLabeledTargetInstances){
				labeledInstancesOfNewTarget.add(newTargetInstances.get(labeledInstanceIndex));
			}
			
			// TODO don't add target instances for training
			// move 10% of labeled target instances to new source.
			newSourceInstances.addAll(labeledInstancesOfNewTarget);
			newTargetInstances.removeAll(labeledInstancesOfNewTarget);
		}
		
		newSourceInstances.setClass(newSourceInstances.attribute(WekaUtils.labelName));
		newTargetInstances.setClass(newTargetInstances.attribute(WekaUtils.labelName));
		
		if (!forRandom){
			this.newSourceInstances = newSourceInstances;
			this.newTargetInstances = newTargetInstances;
		}
		else{
			this.newRandomSourceInstances = newSourceInstances; //WekaUtils.randomizeByShufflingFeatureVector(newSourceInstances);
			this.newRandomTargetInstances = newTargetInstances;
		}
	}
	
	ArrayList<MatchedAttribute> analyzeByLabeledPearsonCorrealation(){
		
		Instances labeledTargetInstances = WekaUtils.getInstancesFromIndice(targetInstances,indexOfLabeledTargetInstances);
		
		Instances sourcePosInstancesToAnalyze = WekaUtils.getInstancesBySpecificLabel(sourceInstances,sourceLabelPos,true);
		Instances targetPosInstancesToAnalyze = WekaUtils.getInstancesBySpecificLabel(labeledTargetInstances,targetLabelPos,true);
		Instances sourceNegInstancesToAnalyze = WekaUtils.getInstancesBySpecificLabel(sourceInstances,sourceLabelPos,false);
		Instances targetNegInstancesToAnalyze = WekaUtils.getInstancesBySpecificLabel(labeledTargetInstances,targetLabelPos,false);
		
		ArrayList<MatchedAttribute> posLabelCorrelationScore = new ArrayList<MatchedAttribute>();
		posLabelCorrelationScore = getCorrelationScore(sourcePosInstancesToAnalyze,targetPosInstancesToAnalyze,Analyzer.PCoAnalyzer);
		
		ArrayList<MatchedAttribute> negLabelCorrelationScore = new ArrayList<MatchedAttribute>();
		negLabelCorrelationScore = getCorrelationScore(sourceNegInstancesToAnalyze,targetNegInstancesToAnalyze,Analyzer.PCoAnalyzer);
		
		ArrayList<MatchedAttribute> correlationScore = new ArrayList<MatchedAttribute>();
		
		// posLabelCorrelationScore == negLabelCorrelationScore, but posLabelCorrelationScore.size() can be 0 when there is no pos instances
		for(int i=0; i<negLabelCorrelationScore.size();i++){
			
			double posScore = posLabelCorrelationScore.get(i).getMatchingScore();
			double negScore = negLabelCorrelationScore.get(i).getMatchingScore();
			
			int s = negLabelCorrelationScore.get(i).getSourceAttrIndex();
			int t = negLabelCorrelationScore.get(i).getTargetAttrIndex();
			
			double matchingScore;
			
			if(Double.isNaN(posScore))
				matchingScore = negScore;
			else
				matchingScore = (posScore+negScore)/2;
			
			correlationScore.add(new MatchedAttribute(s,t,matchingScore));
		}
		
		return correlationScore;
	}
	
	ArrayList<MatchedAttribute> analyzeByPearsonCorrealation(boolean supposeTargetLabelKnown){

		ArrayList<MatchedAttribute> correlationScore = new ArrayList<MatchedAttribute>();
		if(!supposeTargetLabelKnown)
			correlationScore = getCorrelationScore(sourceInstances,targetInstances,Analyzer.PCoAnalyzer);
		else{
			ArrayList<MatchedAttribute> correlationScoreForPos = new ArrayList<MatchedAttribute>();
			ArrayList<MatchedAttribute> correlationScoreForNeg = new ArrayList<MatchedAttribute>();
			
			Instances sourcePosInstances = WekaUtils.getInstancesBySpecificLabel(sourceInstances, sourceLabelPos, true);
			Instances sourceNegInstances = WekaUtils.getInstancesBySpecificLabel(sourceInstances, sourceLabelPos, false);
			Instances targetPosInstances = WekaUtils.getInstancesBySpecificLabel(targetInstances, targetLabelPos, true);
			Instances targetNegInstances = WekaUtils.getInstancesBySpecificLabel(targetInstances, targetLabelPos, false);
			
			correlationScoreForPos = getCorrelationScoreUsingTargetLabel(sourcePosInstances,targetPosInstances,Analyzer.PCoAnalyzer);
			correlationScoreForNeg = getCorrelationScoreUsingTargetLabel(sourceNegInstances,targetNegInstances,Analyzer.PCoAnalyzer);
			
			for(int i=0; i<correlationScoreForPos.size();i++){
				
				double score = (correlationScoreForPos.get(i).getMatchingScore() + correlationScoreForNeg.get(i).getMatchingScore())/2;
				int sourceIndex = correlationScoreForPos.get(i).getSourceAttrIndex();
				int targetIndex = correlationScoreForPos.get(i).getTargetAttrIndex();
				
				correlationScore.add(new MatchedAttribute(sourceIndex,targetIndex,score));
			}	
		}
		
		
		
		return correlationScore;
	}
	
	ArrayList<MatchedAttribute> analyzeBySpearmansCorrealation(){

		ArrayList<MatchedAttribute> correlationScore = new ArrayList<MatchedAttribute>();
		correlationScore = getCorrelationScore(sourceInstances,targetInstances,Analyzer.SCoAnalyzer);
		
		return correlationScore;
	}

	ArrayList<MatchedAttribute> getFinalListOfMatchedAttributes(ArrayList<MatchedAttribute> matched,double cutoff){
		// sort a tuple by correlation in descending order.
		Collections.sort(matched);
		
		for(MatchedAttribute ma:matched){
			mapMatchedAttribute.put(ma.getSourceAttrIndex() + "-" + ma.getTargetAttrIndex(), ma);
		}
		
		// assign all matched to the HashMap
		
		ArrayList<Integer> selectedSourceAttributeIndex = new ArrayList<Integer>();
		ArrayList<Integer> selectedTargetAttributeIndex = new ArrayList<Integer>();
		
		ArrayList<MatchedAttribute> matchedAttrubites = new ArrayList<MatchedAttribute>();
		
		for(MatchedAttribute matchedAttribute:matched){
			
			int sourceIndex = matchedAttribute.getSourceAttrIndex();
			int targetIndex = matchedAttribute.getTargetAttrIndex();
			double correlationValue = matchedAttribute.getMatchingScore();
			
			// list is sorted. So we can stop when applyCutoff is true AND correlationValue <= cutoff 
			if(applyCutoff && correlationValue <= cutoff)
				break;
			
			if(!selectedSourceAttributeIndex.contains(sourceIndex)
					&& !selectedTargetAttributeIndex.contains(targetIndex)){
				matchedAttrubites.add(new MatchedAttribute(sourceIndex,targetIndex,matchedAttribute.getMatchingScore()));
				selectedSourceAttributeIndex.add(sourceIndex);
				selectedTargetAttributeIndex.add(targetIndex);
			}
		}
		
		return matchedAttrubites;
	}
	
	ArrayList<MatchedAttribute> getPValueOfTtest(Instances sourceInstances,Instances targetInstances){
		ArrayList<MatchedAttribute> pValues = new ArrayList<MatchedAttribute>();
		
		for(int s=0; s < sourceInstances.numAttributes(); s++){
			// skip label
			if(sourceInstances.attribute(sourceLabelName).index()==s)
				continue;
			
			double[] sourceAttrValues = sourceInstances.attributeToDoubleArray(s);
			
			for(int t=0; t < targetInstances.numAttributes(); t++){
				// ignore label attribute
				if(t==targetInstances.classIndex())
					continue;
				
				double[] targetAttrValues = targetInstances.attributeToDoubleArray(t);
				 
				 double pValue = TestUtils.tTest(sourceAttrValues, targetAttrValues);

				 pValues.add(new MatchedAttribute(s,t,pValue));
			}
		}
		
		return pValues;
	}
	
	ArrayList<MatchedAttribute> getPValueOfKSTest(Instances sourceInstances,Instances targetInstances,String srcLabeName){
		ArrayList<MatchedAttribute> pValues = new ArrayList<MatchedAttribute>();
		
		for(int s=0; s < sourceInstances.numAttributes(); s++){
			// skip label
			if(sourceInstances.attribute(srcLabeName).index()==s)
				continue;
			
			double[] sourceAttrValues = sourceInstances.attributeToDoubleArray(s);
			
			for(int t=0; t < targetInstances.numAttributes(); t++){
				// ignore label attribute
				if(t==targetInstances.classIndex())
					continue;
				
				double[] targetAttrValues = targetInstances.attributeToDoubleArray(t);
				 
				 //double pValue = TestUtils.tTest(sourceAttrValues, targetAttrValues);
				
				//double pValue= getKSPvalueFromR(sourceAttrValues, targetAttrValues);

				double pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(sourceAttrValues, targetAttrValues);
				pValues.add(new MatchedAttribute(s,t,pValue));
			}
		}
		
		return pValues;
	}

	// to user Rserve only once, make this method as static.
	// to avoid multiple threads access this method at the same time, synchronized
	RConnection c=null;
	double getKSPvalueFromR(double[] sourceAttrValues,
			double[] targetAttrValues) {
		
		double pValue=0.0;
		try {
			
			// connect once
			if(c==null){
				c = new RConnection();
			}
			
			c.assign("treated", sourceAttrValues);
			c.assign("control", targetAttrValues);
			System.err.println("exact=TRUE");
			RList l = c.eval("ks.test(control,treated,exact=TRUE)").asList();
			pValue = l.at("p.value").asDouble();
			
		} catch (RserveException e) {
			//e.printStackTrace();
			System.err.println("Turn on Rserve in R");
			/*for(double value:sourceAttrValues){
				System.out.print(value +  ",");
			}
			System.out.println();
			for(double value:targetAttrValues){
				System.out.print(value +  ",");
			}
			System.out.println();*/
			System.exit(0);
		} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return pValue;
	}
	
	ArrayList<MatchedAttribute> getPValueOfUtest(Instances sourceInstances,Instances targetInstances){
		ArrayList<MatchedAttribute> pValues = new ArrayList<MatchedAttribute>();
		
		for(int s=0; s < sourceInstances.numAttributes(); s++){
			// skip label
			if(sourceInstances.attribute(sourceLabelName).index()==s)
				continue;
			
			double[] sourceAttrValues = sourceInstances.attributeToDoubleArray(s);
			
			for(int t=0; t < targetInstances.numAttributes(); t++){
				// ignore label attribute
				if(t==targetInstances.classIndex())
					continue;
				
				double[] targetAttrValues = targetInstances.attributeToDoubleArray(t);
				 
				 //double pValue = TestUtils.tTest(sourceAttrValues, targetAttrValues);
				double pValue = new MannWhitneyUTest().mannWhitneyUTest(sourceAttrValues, targetAttrValues);

				 pValues.add(new MatchedAttribute(s,t,pValue));
			}
		}
		
		return pValues;
	}
	
	
	ArrayList<MatchedAttribute> getCorrelationScoreUsingTargetLabel(Instances sourceInstancesToAnalyze,Instances targetInstancesToAnalyze,Analyzer analyzer){
		
		Instances selectedAndSortedSrcInstances;// = new Instances(sourceInstancesToAnalyze,0);
		Instances selectedAndSortedTgrInstances;//= new Instances(targetInstancesToAnalyze,0);
		
		// select source and target instances in the same number
		// if source is bigger, we should randomly select values of an attribute from source, otherwise, from target.
		int numOfSourceInstances = sourceInstancesToAnalyze.numInstances();
		int numOfTargetInstances = targetInstancesToAnalyze.numInstances();
		boolean sourceBigger = numOfSourceInstances > numOfTargetInstances? true:false;
		if(sourceBigger){
			selectedAndSortedSrcInstances = new Instances(sourceInstancesToAnalyze,0,numOfTargetInstances);//WekaUtils.selectAndSortInsancesUsingPopIndex(sourceInstancesToAnalyze, numOfTargetInstances);
			selectedAndSortedTgrInstances = new Instances(targetInstancesToAnalyze,0,numOfTargetInstances);//WekaUtils.selectAndSortInsancesUsingPopIndex(targetInstancesToAnalyze, numOfTargetInstances);
		}
		else{
			selectedAndSortedSrcInstances = new Instances(sourceInstancesToAnalyze,0,numOfSourceInstances);//WekaUtils.selectAndSortInsancesUsingPopIndex(sourceInstancesToAnalyze, numOfSourceInstances);
			selectedAndSortedTgrInstances = new Instances(targetInstancesToAnalyze,0,numOfSourceInstances); //WekaUtils.selectAndSortInsancesUsingPopIndex(targetInstancesToAnalyze, numOfSourceInstances);
		}
		
		ArrayList<MatchedAttribute> correlationScore = new ArrayList<MatchedAttribute>();
		for(int s=0;s<selectedAndSortedSrcInstances.numAttributes();s++){
			// skip label
			if(selectedAndSortedSrcInstances.attribute(sourceLabelName).index()==s)
				continue;
						
			double[] orderedSourceValues = selectedAndSortedSrcInstances.attributeToDoubleArray(s);
			Arrays.sort(orderedSourceValues);
			
			
			for(int t=0;t<selectedAndSortedTgrInstances.numAttributes();t++){
				// skip label
				if(targetInstances.attribute(targetLabelName).index()==t)
					continue;
				
				double[] orderedTargetValues = selectedAndSortedTgrInstances.attributeToDoubleArray(t);
				Arrays.sort(orderedTargetValues);
				
				double correlationResult = Double.NaN;
				// Pearson's correlation
				if (analyzer == Analyzer.PCoAnalyzer){
					try{
						correlationResult = new PearsonsCorrelation().correlation(orderedSourceValues, orderedTargetValues);
					}
					catch(Exception e){
						correlationResult = Double.NaN;
					}
				}
				else if(analyzer == Analyzer.SCoAnalyzer)
					correlationResult = new SpearmansCorrelation().correlation(orderedSourceValues, orderedTargetValues);
					
				correlationScore.add(new MatchedAttribute(s,t,correlationResult));
			}
		}
		
		return correlationScore;
	}

	ArrayList<MatchedAttribute> getCorrelationScore(Instances sourceInstancesToAnalyze,Instances targetInstancesToAnalyze,Analyzer analyzer){
		
		Instances selectedAndSortedSrcInstances;// = new Instances(sourceInstancesToAnalyze,0);
		Instances selectedAndSortedTgrInstances;//= new Instances(targetInstancesToAnalyze,0);
		
		// select source and target instances in the same number
		// if source is bigger, we should randomly select values of an attribute from source, otherwise, from target.
		int numOfSourceInstances = sourceInstancesToAnalyze.numInstances();
		int numOfTargetInstances = targetInstancesToAnalyze.numInstances();
		boolean sourceBigger = numOfSourceInstances > numOfTargetInstances? true:false;
		if(sourceBigger){
			selectedAndSortedSrcInstances = new Instances(sourceInstancesToAnalyze,0,numOfTargetInstances);//WekaUtils.selectAndSortInsancesUsingPopIndex(sourceInstancesToAnalyze, numOfTargetInstances);
			selectedAndSortedTgrInstances = new Instances(targetInstancesToAnalyze,0,numOfTargetInstances);//WekaUtils.selectAndSortInsancesUsingPopIndex(targetInstancesToAnalyze, numOfTargetInstances);
		}
		else{
			selectedAndSortedSrcInstances = new Instances(sourceInstancesToAnalyze,0,numOfSourceInstances);//WekaUtils.selectAndSortInsancesUsingPopIndex(sourceInstancesToAnalyze, numOfSourceInstances);
			selectedAndSortedTgrInstances = new Instances(targetInstancesToAnalyze,0,numOfSourceInstances); //WekaUtils.selectAndSortInsancesUsingPopIndex(targetInstancesToAnalyze, numOfSourceInstances);
		}
		
		ArrayList<MatchedAttribute> correlationScore = new ArrayList<MatchedAttribute>();
		for(int s=0;s<selectedAndSortedSrcInstances.numAttributes();s++){
			// skip label
			if(selectedAndSortedSrcInstances.attribute(sourceLabelName).index()==s)
				continue;
						
			double[] orderedSourceValues = selectedAndSortedSrcInstances.attributeToDoubleArray(s);
			Arrays.sort(orderedSourceValues);
			
			
			for(int t=0;t<selectedAndSortedTgrInstances.numAttributes();t++){
				// skip label
				if(targetInstances.attribute(targetLabelName).index()==t)
					continue;
				
				double[] orderedTargetValues = selectedAndSortedTgrInstances.attributeToDoubleArray(t);
				Arrays.sort(orderedTargetValues);
				
				double correlationResult = Double.NaN;
				// Pearson's correlation
				if (analyzer == Analyzer.PCoAnalyzer){
					try{
						correlationResult = new PearsonsCorrelation().correlation(orderedSourceValues, orderedTargetValues);
					}
					catch(Exception e){
						correlationResult = Double.NaN;
					}
				}
				else if(analyzer == Analyzer.SCoAnalyzer)
					correlationResult = new SpearmansCorrelation().correlation(orderedSourceValues, orderedTargetValues);
					
				correlationScore.add(new MatchedAttribute(s,t,correlationResult));
			}
		}
		
		return correlationScore;
	}
	
	public Instances getNewSourceInstances(){
		return newSourceInstances;
	}
	
	public Instances getNewTargetInstances(){
		return newTargetInstances;
	}
	
	public ArrayList<Integer> getMatchedIndice(DataKind dataKind,boolean forRandom){
		ArrayList<Integer> selectedAttributesIndice = new ArrayList<Integer>();
		
		ArrayList<MatchedAttribute> matchedAttribute = null;
		
		if(!forRandom){
			matchedAttribute = this.matchedAttribute;
		}
		else{
			matchedAttribute = matchedAttributeByRandom;
		}
		
		for(MatchedAttribute matched:matchedAttribute){
			int selectedIndex = dataKind==DataKind.SOURCE?matched.getSourceAttrIndex():matched.getTargetAttrIndex();
			selectedAttributesIndice.add(selectedIndex);
		}
		
		return selectedAttributesIndice;
	}
}
