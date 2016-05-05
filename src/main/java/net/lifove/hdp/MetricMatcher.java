package net.lifove.hdp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import weka.core.Instances;

public class MetricMatcher {
	
	Instances source;
	Instances target;
	int threadPoolSize=10;
	
	public MetricMatcher(Instances source, Instances target, int threadPoolSize){
		this.source = source;
		this.target = target;
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
			matchingScores.put(ksAnalyzer.getMachingID(), ksAnalyzer.getMatchingScore());
			System.out.println(ksAnalyzer.getMachingID() + ": " + ksAnalyzer.getMatchingScore());
		}
	}
}
