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
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

import com.google.common.primitives.Doubles;


public class ResultTableGenerator {
	
	DecimalFormat dec = new DecimalFormat("0.000");
	
	@Test
	public void testMain() {
		
		ArrayList<String> orderedProjectName = new ArrayList<String>();
		orderedProjectName.add("EQ");
		orderedProjectName.add("JDT");
		orderedProjectName.add("LC");
		orderedProjectName.add("ML");
		orderedProjectName.add("PDE");
		orderedProjectName.add("Apache");
		orderedProjectName.add("Safe");
		orderedProjectName.add("Zxing");
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
		orderedProjectName.add("CM1");
		orderedProjectName.add("MW1");
		orderedProjectName.add("PC1");
		orderedProjectName.add("PC3");
		orderedProjectName.add("PC4");
		orderedProjectName.add("JM1");
		orderedProjectName.add("PC2");
		orderedProjectName.add("PC5");
		orderedProjectName.add("MC1");
		orderedProjectName.add("MC2");
		orderedProjectName.add("KC3");
		orderedProjectName.add("ar1");
		orderedProjectName.add("ar3");
		orderedProjectName.add("ar4");
		orderedProjectName.add("ar5");
		orderedProjectName.add("ar6");
		
		
		String pathToResults = System.getProperty("user.home") + "/Documents/HDP/Results/";
		ArrayList<String> linesHDP = getLines(pathToResults + "HDP_C0.05_ChiSquare.txt",false);
		ArrayList<String> linesIFS = getLines(pathToResults + "IFS_results.txt",false);
		ArrayList<String> linesCM = getLines(pathToResults + "HDP_common_metrics.txt",false);
		
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsHDP = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		HashSet<String> validHDPPrediction = new HashSet<String>(); // value: source target repeat folder
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsWPDP = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		
		/*int millis = 1000;
		long startTime = System.currentTimeMillis();
		System.out.println("Start process HDP results: " + (int)startTime/millis + "s");*/
		
		// get HashMap from HDP results
		//int lineCount = 0;
		for(String line:linesHDP){
			
			String[] splitLine = line.split(",");
			String source = splitLine[2].split("/")[1].replace(".arff", "");
			String target = splitLine[3].split("/")[1].replace(".arff", "");
			int repeat = Integer.parseInt(splitLine[0]);
			int fold =  Integer.parseInt(splitLine[1]);
			Double wAUC = Double.parseDouble(splitLine[8]);
			Double AUC = Double.parseDouble(splitLine[12]);
			
			if(!resultsHDP.containsKey(target)){
				HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
				ArrayList<Prediction> prediction = new ArrayList<Prediction>();
				prediction.add(new Prediction(source,target,fold,repeat,AUC));
				resultsHDPBySource.put(source,prediction);
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
				ArrayList<Prediction> prediction = new ArrayList<Prediction>();
				prediction.add(new Prediction(source,target,fold,repeat,wAUC));
				resultsHDPBySource.put(source,prediction);
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
		//System.out.println("End process HDP results: " + (int)(System.currentTimeMillis()-startTime)/millis + "s");
		
		// get HashMap from CM results
		//System.out.println("Start process CM results: " + (int)(System.currentTimeMillis()-startTime)/millis + "s");
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsCM = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		//lineCount = 0;
		for(String line:linesCM){
			String[] splitLine = line.split(",");
			String source = splitLine[2].split("/")[1].replace(".arff", "");
			String target = splitLine[3].split("/")[1].replace(".arff", "");
			int repeat = Integer.parseInt(splitLine[0]);
			int fold =  Integer.parseInt(splitLine[1]);
			Double AUC = Double.parseDouble(splitLine[12]);
			
			//++lineCount;
			
			// only consider valid prediction from HDP results
			if(!validHDPPrediction.contains((source+target+repeat+ "," + fold))) continue;
			
			if(!resultsCM.containsKey(target)){
				HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
				ArrayList<Prediction> prediction = new ArrayList<Prediction>();
				prediction.add(new Prediction(source,target,fold,repeat,AUC));
				resultsHDPBySource.put(source,prediction);
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
		//System.out.println("End process CM results: " + (int)(System.currentTimeMillis()-startTime)/millis + "s");
		
		// get HashMap from IFS results
		HashMap<String,HashMap<String,ArrayList<Prediction>>> resultsIFS = new HashMap<String,HashMap<String,ArrayList<Prediction>>>(); // key: target, second key: source
		//System.err.println(linesIFS.size());
		int repeat=0;
		int fold = 0;
		for(String line:linesIFS){
			String[] splitLine = line.split(",");
			if(splitLine[0].trim().equals("A")) continue;
		
			String source = splitLine[1].split(">>")[0];
			String target = splitLine[1].split(">>")[1];
			Double AUC = Double.parseDouble(splitLine[9]);
			
			// only consider valid prediction from HDP results
			if(!validHDPPrediction.contains((source+target+repeat+ "," + fold))) continue;
			
			if(!resultsIFS.containsKey(target)){
				HashMap<String,ArrayList<Prediction>> resultsHDPBySource = new HashMap<String,ArrayList<Prediction>>();
				ArrayList<Prediction> prediction = new ArrayList<Prediction>();
				prediction.add(new Prediction(source,target,fold,repeat,AUC));
				resultsHDPBySource.put(source,prediction);
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
		
		// get medians
		HashMap<String, ArrayList<Double>> mediansHDP = getMapMedians(resultsHDP);
		HashMap<String, ArrayList<Double>> mediansWPDP = getMapMedians(resultsWPDP);
		HashMap<String, ArrayList<Double>> mediansCM = getMapMedians(resultsCM);
		HashMap<String, ArrayList<Double>> mediansIFS = getMapMedians(resultsIFS);
		
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
			for(String key: resultsHDP.keySet()){
				HashMap<String,ArrayList<Prediction>> predicitonsHDPBySource = resultsHDP.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsWPDPBySource = resultsWPDP.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsCMBySource = resultsCM.get(key);
				HashMap<String,ArrayList<Prediction>> predicitonsIFSBySource = resultsIFS.get(key);
				
				// treatment
				ArrayList<Double> treatment = new ArrayList<Double>();
				for(String srcKey:predicitonsHDPBySource.keySet()){
					ArrayList<Prediction> predicitonsHDP = predicitonsHDPBySource.get(srcKey);
					
					for(int i=0; i<predicitonsHDP.size();i++){
						treatment.add(predicitonsHDP.get(i).AUC);
					}
				}
				
				// WPDP
				ArrayList<Double> wpdp = new ArrayList<Double>();
				for(String srcKey:predicitonsWPDPBySource.keySet()){
					ArrayList<Prediction> predicitonsWPDP = predicitonsWPDPBySource.get(srcKey);
					
					for(int i=0; i<predicitonsWPDP.size();i++){
						wpdp.add(predicitonsWPDP.get(i).AUC);
					}
				}
				
				c.assign("treatment", Doubles.toArray(treatment));
				c.assign("control", Doubles.toArray(wpdp));
				RList lWPDP = c.eval("cliff.delta(treatment,control)").asList();
				
				// CM
				ArrayList<Double> cm = new ArrayList<Double>();
				for(String srcKey:predicitonsCMBySource.keySet()){
					ArrayList<Prediction> predicitonsCM = predicitonsCMBySource.get(srcKey);
					
					for(int i=0; i<predicitonsCM.size();i++){
						cm.add(predicitonsCM.get(i).AUC);
					}
				}
				c.assign("control", Doubles.toArray(cm));
				RList lCM = c.eval("cliff.delta(treatment,control)").asList();
				
				// IFS
				ArrayList<Double> ifs = new ArrayList<Double>();
				for(String srcKey:predicitonsIFSBySource.keySet()){
					ArrayList<Prediction> predicitonsIFS = predicitonsIFSBySource.get(srcKey);
					
					for(int i=0; i<predicitonsIFS.size();i++){
						ifs.add(predicitonsIFS.get(i).AUC);
					}
				}
				c.assign("control", Doubles.toArray(ifs));
				RList lIFS = c.eval("cliff.delta(treatment,control)").asList();
				
				Double wAUC = getMedian(mediansWPDP.get(key));
				Double wAUCCliffDelta = lWPDP.at("estimate").asDouble();
				Double cmAUC = getMedian(mediansCM.get(key));
				Double cmAUCCliffDelta = lCM.at("estimate").asDouble();
				Double ifsAUC = getMedian(mediansIFS.get(key));
				Double ifsAUCCliffDelta = lIFS.at("estimate").asDouble();
				
				String wAUCMagnitute = getCliffsDeltaMagnitute(wAUCCliffDelta);
				String cmAUCMagnitute = getCliffsDeltaMagnitute(cmAUCCliffDelta);
				String ifsAUCMagnitute = getCliffsDeltaMagnitute(ifsAUCCliffDelta);
				
				System.out.println(orderedProjectName.indexOf(key) + "\t" + 
							key.replace(".arff","") + "\t&" +
							dec.format(wAUC) + " (" + dec.format(wAUCCliffDelta) + "," + wAUCMagnitute + ")\t&" + 
							dec.format(cmAUC) + " (" + dec.format(cmAUCCliffDelta) + "," + cmAUCMagnitute + ")\t&" + 
							dec.format(ifsAUC) + " (" + dec.format(ifsAUCCliffDelta) + "," + ifsAUCMagnitute + ")\t&" + 
							dec.format(getMedian(mediansHDP.get(key))) + " \\\\ \\hline");
			}
			System.out.println("-\tMedian\t" + 
					dec.format(getMedian(resultsWPDP)) + "\t" + 
					"\t" +
					dec.format(getMedian(resultsCM)) + "\t" +
					"\t" +
					dec.format(getMedian(resultsIFS)) + "\t" +
					"\t" +
					dec.format(getMedian(resultsHDP)));
			
			
			
		} catch (REngineException | REXPMismatchException e) {
			e.printStackTrace();
		}
	}

	private String getCliffsDeltaMagnitute(Double wAUCCliffDelta) {
		
		Double absValue = Math.abs(wAUCCliffDelta);
		
		if(absValue>=0.474)
			return "L";
		if(absValue>=0.33)
			return "M";
		if(absValue>=0.147)
			return "S";

		return "N";
	}

	private Double getMedian(HashMap<String, HashMap<String, ArrayList<Prediction>>> results) {
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
		
		return getMedian(medians);
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
	
	public Prediction(String srcName,String tarName,int fold,int repeat,double AUC){
		this.sourceName = srcName;
		this.targetName = tarName;
		this.fold = fold;
		this.repeat = repeat;
		this.AUC = AUC;
	}
}
