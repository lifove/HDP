package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

import com.csvreader.CsvReader;
import com.google.common.primitives.Doubles;

public class UtestForAnalyzers {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new UtestForAnalyzers().run(args);
	}
	
	HashMap<String,ArrayList<Double>> withinResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> manualResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> withinResultsF = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossResultsF = new HashMap<String,ArrayList<Double>>();
	
	boolean displayRScript = false;
	String keyType = "Prediction";
	
	public void run(String[] args) throws IOException {
		String dataFilePath = args[0];
		keyType = args[1];
		displayRScript = Boolean.parseBoolean(args[2]);
		
		
		loadData(dataFilePath);
		
		conductUtest();
		
	}
	
	private void conductUtest(){
		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		
		ArrayList<String> rSourceWitinAndCross = new ArrayList<String>();
		ArrayList<String> rSourceCross = new ArrayList<String>();
		
		for(String key:withinResults.keySet()){
			ArrayList<Double> withinAUCValues = withinResults.get(key);
			ArrayList<Double> crossAUCValues = crossResults.get(key);
			ArrayList<Double> manualAUCValues = manualResults.get(key);
			ArrayList<Double> withinFValues = withinResultsF.get(key);
			ArrayList<Double> crossFValues = crossResultsF.get(key);
			
			DescriptiveStatistics statWithin = new DescriptiveStatistics(Doubles.toArray(withinAUCValues));
			DescriptiveStatistics statCross = new DescriptiveStatistics(Doubles.toArray(crossAUCValues));
			DescriptiveStatistics statManual = new DescriptiveStatistics(Doubles.toArray(manualAUCValues));
			DescriptiveStatistics statWithinF = new DescriptiveStatistics(Doubles.toArray(withinFValues));
			DescriptiveStatistics statCrossF = new DescriptiveStatistics(Doubles.toArray(crossFValues));
			
			double wMedian = statWithin.getPercentile(50);
			double cMedian = statCross.getPercentile(50);
			double mMedian = statManual.getPercentile(50);
			double wMedianF = statWithinF.getPercentile(50);
			double cMedianF = statCrossF.getPercentile(50);
			int numPredictions = withinAUCValues.size();
			
			
			
			double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(withinAUCValues), Doubles.toArray(crossAUCValues),false);
			double pBtCM = statTest.wilcoxonSignedRankTest(Doubles.toArray(manualAUCValues), Doubles.toArray(crossAUCValues),false);
			
			System.out.println(key + "," + wMedian + "," + mMedian + "," + cMedian + "," + numPredictions + "," + p + "," + pBtCM + "," + wMedianF + "," +cMedianF);
			
			
			rSourceWitinAndCross.add(key);
			rSourceWitinAndCross.add("treated<-c(" + getStringFromArray(Doubles.toArray(withinAUCValues)) +")");
			rSourceWitinAndCross.add("control<-c(" + getStringFromArray(Doubles.toArray(crossAUCValues)) +")");
			rSourceWitinAndCross.add("wilcox.test(control,treated,paired=TRUE)");
			
			rSourceCross.add(key);
			rSourceCross.add("treated<-c(" + getStringFromArray(Doubles.toArray(manualAUCValues)) +")");
			rSourceCross.add("control<-c(" + getStringFromArray(Doubles.toArray(crossAUCValues)) +")");
			rSourceCross.add("wilcox.test(control,treated,paired=TRUE)");
			
		}
		
		if(displayRScript){
			for(String line:rSourceWitinAndCross)
				System.out.println(line);
			
			for(String line:rSourceCross)
				System.out.println(line);
		}
	}

	private String getStringFromArray(double[] values){
		
		String strValues = "";
		for(double value:values){
			strValues+=value + ",";
		}
		
		strValues = strValues.substring(0, strValues.length()-1);
		return strValues;
	}
	
	private void loadData(String dataFilePath) throws FileNotFoundException,
			IOException {
		CsvReader records = new CsvReader(dataFilePath);
		
		records.readHeaders();

		while (records.readRecord()){
			String itrtCount = records.get("ItrtCount");
			String analyzer = records.get("Analyzer");
			if(!itrtCount.equals("1000") || analyzer.equals("Analyzer")) continue;
			
			//String key = records.get("Group");
			//String key = records.get("Analyzer");
			//String key = records.get("Target");
			String key = records.get(keyType);
			String withinAUC  = records.get("TargetWithinAUC");
			String crossAUC = records.get("CrossAUC");
			String manualAUC = records.get("AUC_manual");
			String withinF  = records.get("TargetWithinF");
			String crossF = records.get("CrossF");
			
			if(!withinResults.containsKey(key)){
				ArrayList<Double> withinAUCValues = new ArrayList<Double>();
				withinAUCValues.add(Double.parseDouble(withinAUC));
				withinResults.put(key, withinAUCValues);
				
				ArrayList<Double> crossAUCValues = new ArrayList<Double>();
				crossAUCValues.add(Double.parseDouble(crossAUC));
				crossResults.put(key, crossAUCValues);
				
				ArrayList<Double> manualAUCValues = new ArrayList<Double>();
				manualAUCValues.add(Double.parseDouble(manualAUC));
				manualResults.put(key, manualAUCValues);
				
				ArrayList<Double> withinFValues = new ArrayList<Double>();
				withinFValues.add(Double.parseDouble(withinF));
				withinResultsF.put(key, withinFValues);
				
				ArrayList<Double> crossFValues = new ArrayList<Double>();
				crossFValues.add(Double.parseDouble(crossF));
				crossResultsF.put(key, crossFValues);				
				
			}
			else{
				withinResults.get(key).add(Double.parseDouble(withinAUC));
				crossResults.get(key).add(Double.parseDouble(crossAUC));
				manualResults.get(key).add(Double.parseDouble(manualAUC));
				withinResultsF.get(key).add(Double.parseDouble(withinF));
				crossResultsF.get(key).add(Double.parseDouble(crossF));
			}
		}
	}

}
