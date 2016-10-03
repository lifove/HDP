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
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;

import com.google.common.primitives.Doubles;

import net.lifove.hdp.util.Utils.FeatureSelectors;


public class ResultTableGeneratorForEachCombination {
	
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
		sourceGroups.put("ZXing".toLowerCase(),"Relink");
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
		
		String pathToResults = System.getProperty("user.home") + "/Documents/HDP/Results/";
		
		DecimalFormat decForCutoff = new DecimalFormat("0.00");
		DecimalFormat dec = new DecimalFormat("0.000");
		
		//for(double cutoff=0.05;cutoff<0.06;cutoff=cutoff+0.05){
			
		generate(orderedProjectName, pathToResults,decForCutoff, dec,
				0.05,"KSAnalyzer","weka.classifiers.functions.SimpleLogistic",FeatureSelectors.GainRatio);
		
	}

	private void generate(ArrayList<String> orderedProjectName, String pathToResults, 
			DecimalFormat decForCutoff,
			DecimalFormat dec, double cutoff,String analyzer, String mlAlg,FeatureSelectors fSelector) {
		System.out.println("\n\n<!--====cutoff: " + decForCutoff.format(cutoff) + "_" + fSelector.name() + "_" + analyzer + "_" + mlAlg + "-->");
															
		ArrayList<String> linesHDP = getLines(pathToResults + "HDP_C" + decForCutoff.format(cutoff) + "_" + fSelector.name() + "_" + analyzer + "_" + mlAlg + ".txt",false);

		HashMap<String,ArrayList<Prediction>> resultsHDPForEachPredictonCombination = new HashMap<String,ArrayList<Prediction>>(); // key: source-target
		HashMap<String,String> mapMatchedMetrics = new HashMap<String,String>();
		// get HashMap from HDP results
		//int lineCount = 0;
		for(String line:linesHDP){
			
			String[] splitLine = line.split(",");
			String source = splitLine[2].split("/")[1].replace(".arff", "").toLowerCase();
			String target = splitLine[3].split("/")[1].replace(".arff", "").toLowerCase();
			String key = source + ":" + target;
			int repeat = Integer.parseInt(splitLine[0]);
			int fold =  Integer.parseInt(splitLine[1]);
			Double wAUC = Double.parseDouble(splitLine[8]);
			Double AUC = Double.parseDouble(splitLine[12]);
			String matchedMetrics = splitLine[13];
			if(!resultsHDPForEachPredictonCombination.containsKey(key)){
				ArrayList<Prediction> predictions = new ArrayList<Prediction>();
				predictions.add(new Prediction(source,target,fold,repeat,AUC,wAUC));
				resultsHDPForEachPredictonCombination.put(key, predictions);
				mapMatchedMetrics.put(key, matchedMetrics.replaceAll(" >> ", " : "));
			}else{
				resultsHDPForEachPredictonCombination.get(key).add(new Prediction(source,target,fold,repeat,AUC,wAUC));				
			}	
		}
		
		HashMap<String,ArrayList<Double>> aucsBySource = new HashMap<String,ArrayList<Double>>();
		ArrayList<String> matchedMetricsForEachCpmbination = new ArrayList<String>();
		
		SortedSet<String> keys = new TreeSet<String>(resultsHDPForEachPredictonCombination.keySet());
		for (String key : keys) { 
			
			String source = getFinalProjectName(key.split(":")[0]);
			String target = getFinalProjectName(key.split(":")[1]);
			
			ArrayList<Double> wAUCs = getWAUCs(resultsHDPForEachPredictonCombination.get(key));
			ArrayList<Double> AUCs = getAUCs(resultsHDPForEachPredictonCombination.get(key));
			
			Double hdpAUC = getMedian(AUCs);
			
			if(!aucsBySource.containsKey(source)){
				ArrayList<Double> hdpAUCs = new ArrayList<Double>();
				hdpAUCs.add(hdpAUC);
				aucsBySource.put(source, hdpAUCs);
			}else{
				aucsBySource.get(source).add(hdpAUC);
			}
			System.out.println("<tr>\n\t<td>" + source + " >> " + target + "</td>");
			System.out.println("\t<td>" + dec.format(getMedian(wAUCs)) + "</td>");
			System.out.println("\t<td>" + dec.format(hdpAUC) + "</td>");
			System.out.println("\t<td>" + getWTL(wAUCs,AUCs) + "</td>");
			System.out.println("\t<td>" + (mapMatchedMetrics.get(key).split("\\|").length) + "</td>");
			System.out.println("\t<td>\n\t" + getMatchedMetricsForHTML(source,target,mapMatchedMetrics.get(key)) + "</td>");
			System.out.println("</tr>");
			
			matchedMetricsForEachCpmbination.add(source + "," + target + "," + mapMatchedMetrics.get(key));
		}

		// print out
		int i=0;
		for(String source:orderedProjectName){
			source = getFinalProjectName(source);
			
			if(aucsBySource.get(source)!=null)
				if(i%2==0)
					System.out.print(source + "\t&" + dec.format(getMedian(aucsBySource.get(source))) + "\t&" + aucsBySource.get(source).size() + "\t&");
				else
					System.out.println(source + "\t&" + dec.format(getMedian(aucsBySource.get(source))) + "\t&" + aucsBySource.get(source).size() +"\t\\\\ \\hline");
			else
				if(i%2==0)
					System.out.print(source + "\t&n/a\t&0" +"\t&");
				else
					System.out.println(source + "\t&n/a\t&0" +"\t\\\\ \\hline");
			
			i++;
		}
		
		// print out 
		for(String line:matchedMetricsForEachCpmbination)
			System.out.println(line);
	}
	
	private String getMatchedMetricsForHTML(String srcName,String tarName,String mathecMetrics) {
		String[] arrMathedMetrics = mathecMetrics.split("\\|");
		
		String lines="";
		for(String matchedMetric:arrMathedMetrics){
			String[] splitString = matchedMetric.split(">>");
			String srcAttr = splitString[0];
			String tarAttr = splitString[1].substring(0,  splitString[1].indexOf("("));
			String imgName = srcName.toLowerCase() + "_" + tarName.toLowerCase() + "_" + srcAttr + "_" + tarAttr + ".png";
			//<a href=bplots/ant-1.3_apache_rfc_AvgLine.png target=_NEW onmouseover="hoverDivs(event,'bplots/ant-1.3_apache_rfc_AvgLine.png')" onmouseleave="leaveDivs(event)">rfc:AvgLine</a>
			lines = lines + "<a href=bplots/" + imgName  +
							" onmouseover=\"hoverDivs(event,'bplots/" + imgName + "')\" onmouseleave=\"leaveDivs(event)\"" +
							"target=_NEW>" +
					srcAttr + ":" + tarAttr + "</a><br>\n\t";
		}
		return lines;
	}

	private String getFinalProjectName(String string) {
		if (string.equals("eq"))
			return "EQ";
		if (string.equals("jdt"))
			return "JDT";
		if (string.equals("ml"))
			return "ML";
		if (string.equals("pde"))
			return "PDE";
		if (string.equals("lc"))
			return "LC";
		
		if (string.equals("apache"))
			return "Apache";
		if (string.equals("safe"))
			return "Safe";
		if (string.equals("zxing"))
			return "ZXing";
		
		return string;
	}

	private ArrayList<Double> getWAUCs(ArrayList<Prediction> results) {
		ArrayList<Double> aucs = new ArrayList<Double>();
		
		for(Prediction prediction:results){
				aucs.add(prediction.wAUC);
		}
		return aucs;
	}
	
	private  ArrayList<Double> getAUCs(ArrayList<Prediction> results) {
		ArrayList<Double> aucs = new ArrayList<Double>();
		
		for(Prediction prediction:results){
				aucs.add(prediction.AUC);
		}
		return aucs;
	}

	private String getWTL(ArrayList<Double> baselineAUCs, ArrayList<Double> hdpAUCs) {
		
		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(baselineAUCs), Doubles.toArray(hdpAUCs),false);

		Double medeanA = getMedian(baselineAUCs);
		Double medeanB = getMedian(hdpAUCs);
		
		
		// win
		if(p<0.05 && medeanA < medeanB){
			
			return "Win";
		}
		
		// loss
		if(p<0.05 && medeanA > medeanB){
			
			return "Loss";
		}
		
		return "Tie";
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
