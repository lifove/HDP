package net.lifove.hdp;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.lifove.hdp.util.MWBMatchingAlgorithm;
import net.lifove.hdp.util.Utils;
import weka.core.Instances;

public class MetricMatcher {
	
	Instances source;
	Instances target;
	double cutoff;
	int threadPoolSize=10;
	HashMap<String,Double> matchingScores = new HashMap<String,Double>();
	
	public static void main(String[] args) {
		String sourceDataPath = args[0];
		String srcLableName = args[1];
		String targetDataPath = args[2];
		String tarLableName = args[3];
		int threadPoolSize = Integer.parseInt(args[4]);
		Instances srcInstances = Utils.loadArff(sourceDataPath, srcLableName);
		Instances tarInstances = Utils.loadArff(targetDataPath, tarLableName);
		
		String sourceName = sourceDataPath.substring(sourceDataPath.lastIndexOf(File.separator)+1,sourceDataPath.length());
		String targetName = targetDataPath.substring(targetDataPath.lastIndexOf(File.separator)+1,targetDataPath.length());
		
		ArrayList<String> lines = new MetricMatcher(srcInstances,tarInstances,0.05,threadPoolSize).match();
		
		for(String line:lines){
			System.out.println(sourceName + ":" + targetName + "," + line);
		}
	}
	
	public MetricMatcher(Instances source, Instances target, double cutoff, int threadPoolSize){
		this.source = source;
		this.target = target;
		this.cutoff = cutoff;
		this.threadPoolSize = threadPoolSize;
	}

	public MetricMatcher(Instances source, Instances target, double cutoff,
			HashMap<String, Double> matchingScores) {
		this.source = source;
		this.target = target;
		this.cutoff = cutoff;
		this.matchingScores = matchingScores;
	}

	public ArrayList<String> match() {
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		
		if(matchingScores.size()==0){
		
			ArrayList<Runnable> threads = new ArrayList<Runnable>();
			for(int srcAttrIdx = 0; srcAttrIdx < source.numAttributes(); srcAttrIdx++){
				if(srcAttrIdx==source.classIndex())
					continue;
				for(int tarAttrIdx = 0; tarAttrIdx < target.numAttributes(); tarAttrIdx++){
					if(tarAttrIdx==target.classIndex())
						continue;
					
					double[] sourceMetric=source.attributeToDoubleArray(srcAttrIdx);
					double[] targetMetric=target.attributeToDoubleArray(tarAttrIdx);
					
					Runnable ksAnalyzer = new KSAnalyzer(sourceMetric,targetMetric,source.attribute(srcAttrIdx).name()+"-"+target.attribute(tarAttrIdx).name());
					threads.add(ksAnalyzer);
					executor.execute(ksAnalyzer);
				}
			}
			
			executor.shutdown();
			
			while (!executor.isTerminated()) {
				// waiting
	        }
			
			for(Runnable runnable:threads){
				KSAnalyzer ksAnalyzer = (KSAnalyzer)runnable;
				if(ksAnalyzer.getMatchingScore()>cutoff){
					matchingScores.put(ksAnalyzer.getMachingID(), ksAnalyzer.getMatchingScore());
				}
				else{
					matchingScores.put(ksAnalyzer.getMachingID(), -1.0); // if the matching score is not greater than the cutoff, make it as cutoff so that MWBMatchingAlgorithm can work correctly.
				}
			}
		}else{
			// change existing matching scores based on cutoff. For correct MWB, all scores <=cutoff are replaced with -1.
			for(String key:matchingScores.keySet()){
				if(matchingScores.get(key)<=cutoff)
					matchingScores.put(key, -1.0);
			}
		}
		
		// get maximum-weighted bipartite matching
		MWBMatchingAlgorithm mwb = new MWBMatchingAlgorithm(source.numAttributes()-1,target.numAttributes()-1);
		for(String key:matchingScores.keySet()){
			String[] pairs = key.split("-");
			int srcMetricIdx = source.attribute(pairs[0]).index();
			int tarMetricIdx = target.attribute(pairs[1]).index();
			mwb.setWeight(srcMetricIdx, tarMetricIdx,matchingScores.get(key));
		}
		
		ArrayList<String> matchedMetrics = new ArrayList<String>();
		int[] matching = mwb.getMatching(); // index is srcMetricIdx and value is tarMetricIdx
		for(int srcMetricIdx=0;srcMetricIdx < matching.length;srcMetricIdx++){
			if(matching[srcMetricIdx]>=0){
				String key = getAttributeName(srcMetricIdx,source) + "-" + getAttributeName(matching[srcMetricIdx],target);
				if(matchingScores.get(key) > cutoff )
					matchedMetrics.add(key + "(" +  matchingScores.get(key) + ")");
			}
		}
		
		return matchedMetrics;
	}
	
	private String getAttributeName(int attrIdx,Instances instances){
		return instances.attribute(attrIdx).name();
	}
	static public String getStrMatchedMetrics(Instances origSource, Instances origTarget, ArrayList<String> matchedMetrics) {
		
		String strMatchedMetrics = "";
		
		for(String matchedMetric: matchedMetrics){
			String[] splitMetricInfo = matchedMetric.split("\\(");
			String[] metrics = splitMetricInfo[0].split("-");
			
			int srcMetricIdx = Integer.parseInt(metrics[0]);
			int tarMetricIdx = Integer.parseInt(metrics[1]);
			
			String srcMetricName = origSource.attribute(srcMetricIdx).name();
			String tarMetricName = origTarget.attribute(tarMetricIdx).name();
			
			DecimalFormat dec = new DecimalFormat("0.000");
			Double matcingScore = Double.parseDouble(splitMetricInfo[1].substring(0, splitMetricInfo[1].length()-1));
			
			strMatchedMetrics += srcMetricName + ">>" + tarMetricName + "(" + dec.format(matcingScore) + ")|";	
		}
		
		return strMatchedMetrics;
	}
}
