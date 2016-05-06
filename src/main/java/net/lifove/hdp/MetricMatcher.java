package net.lifove.hdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.lifove.hdp.util.MWBMatchingAlgorithm;
import weka.core.Instances;

public class MetricMatcher {
	
	Instances source;
	Instances target;
	double cutoff;
	int threadPoolSize=10;
	
	public MetricMatcher(Instances source, Instances target, double cutoff, int threadPoolSize){
		this.source = source;
		this.target = target;
		this.cutoff = cutoff;
		this.threadPoolSize = threadPoolSize;
	}

	public void match() {
		ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
		
		ArrayList<Runnable> threads = new ArrayList<Runnable>();
		for(int srcAttrIdx = 0; srcAttrIdx < source.numAttributes(); srcAttrIdx++){
			if(srcAttrIdx==source.classIndex())
				continue;
			for(int tarAttrIdx = 0; tarAttrIdx < target.numAttributes(); tarAttrIdx++){
				if(tarAttrIdx==target.classIndex())
					continue;
				
				double[] sourceMetric=source.attributeToDoubleArray(srcAttrIdx);
				double[] targetMetric=target.attributeToDoubleArray(tarAttrIdx);
				
				Runnable ksAnalyzer = new KSAnalyzer(sourceMetric,targetMetric,srcAttrIdx+"-"+tarAttrIdx);
				threads.add(ksAnalyzer);
				executor.execute(ksAnalyzer);
			}
		}
		
		executor.shutdown();
		
		while (!executor.isTerminated()) {
			// waiting
        }
		
		HashMap<String,Double> matchingScores = new HashMap<String,Double>();
		for(Runnable runnable:threads){
			KSAnalyzer ksAnalyzer = (KSAnalyzer)runnable;
			if(ksAnalyzer.getMatchingScore()>cutoff){
				matchingScores.put(ksAnalyzer.getMachingID(), ksAnalyzer.getMatchingScore());
			}
			else{
				matchingScores.put(ksAnalyzer.getMachingID(), cutoff); // if the matching score is not greater than the cutoff, make it as -1 so that MWBMatchingAlgorithm can work correctly.
			}
		}
		
		// get maximum-weighted bipartite matching
		MWBMatchingAlgorithm mwb = new MWBMatchingAlgorithm(source.numAttributes()-1,target.numAttributes()-1);
		for(String key:matchingScores.keySet()){
			String[] pairs = key.split("-");
			mwb.setWeight(Integer.parseInt(pairs[0]), Integer.parseInt(pairs[1]),matchingScores.get(key));
		}
		int[] matching = mwb.getMatching(); // index is srcMetricIdx and value is tarMetricIdx
		for(int srcMetricIdx=0;srcMetricIdx < matching.length;srcMetricIdx++){
			String key = srcMetricIdx + "-" + matching[srcMetricIdx];
			if(matching[srcMetricIdx]>=0 && matchingScores.get(key) > cutoff )
				System.out.println(srcMetricIdx + "-" + matching[srcMetricIdx] + ": " + matchingScores.get(key));
		}
	}
}
