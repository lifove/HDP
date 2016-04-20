package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import com.csvreader.CsvReader;
import com.google.common.primitives.Doubles;

public class WinTieLossByUtestForIFS {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new WinTieLossByUtestForIFS().run(args);
	}
	
	HashMap<String,ArrayList<Double>> withinResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> ifsResults = new HashMap<String,ArrayList<Double>>();
	
	String fsOption = "";
	boolean outputRSciprt = false;
	
	public void run(String[] args) throws IOException {
		String dataFilePath = args[0];
		String dataFilePath2 = "";
		if(args.length>=2)
			dataFilePath2 = args[1];
		
		if(!dataFilePath2.equals(""))
			loadData2(dataFilePath2);
		
		fsOption = args[2].equals("none")? "":"_FS_" + args[2];
		outputRSciprt = Boolean.parseBoolean(args[3]);
		
		loadData(dataFilePath);
		
		
		conductUtest();
		
	}
	
	private void loadData2(String dataFilePath) throws FileNotFoundException,
	IOException {
		CsvReader records = new CsvReader(dataFilePath);

		records.readHeaders();
		
		int i=1; // always 1000 iteration for each prediction combination
		ArrayList<Double> resultValues = new ArrayList<Double>();
		while (records.readRecord()){
			String key = records.get("Prediction"); // 
			Double ifsAUC = Double.parseDouble(records.get("cAUC"));
			
			resultValues.add(ifsAUC);
			
			if(i==1000){
				ifsResults.put(key,resultValues);
				resultValues = new ArrayList<Double>(); //generate new arraylist
				i=1;
			}
			else
				i++;
		}		
	}

	private void conductUtest(){
		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		
		ArrayList<String> rSourceWitinAndCross = new ArrayList<String>();
		ArrayList<String> rSourceCross = new ArrayList<String>();
		
		ArrayList<String> rSourceWitinAndCrossF = new ArrayList<String>();
		
		for(String key:withinResults.keySet()){
			
			//String key = prediction + "," + source + "," +target + "," + analyzer + "," + classifier;
			String[] splitKey = key.split(",");
			
			// only consider KSAnalyzer 0.05 and logistic
			if(!(splitKey[3].equals("KSAnalyzer_0.05") && splitKey[4].equals("weka.classifiers.functions.Logistic")))
				continue;
			String ifsKey = (splitKey[1] + ">>" + splitKey[2]).replace(fsOption, "");
			
			ArrayList<Double> withinAUCValues = withinResults.get(key);
			ArrayList<Double> crossAUCValues = crossResults.get(key);
			ArrayList<Double> ifsAUCValues = ifsResults.get(ifsKey);
			
			DescriptiveStatistics statWithin = new DescriptiveStatistics(Doubles.toArray(withinAUCValues));
			DescriptiveStatistics statCross = new DescriptiveStatistics(Doubles.toArray(crossAUCValues));
			DescriptiveStatistics statIFS = new DescriptiveStatistics(Doubles.toArray(ifsAUCValues));
			
			double wMedian = statWithin.getPercentile(50);
			double cMedian = statCross.getPercentile(50);
			double ifsMedian = statIFS.getPercentile(50);
			

			int numPredictions = withinAUCValues.size();
			
			double p=0.0;
			double pBtCandIFS=0.0;
			try {
				RConnection c = new RConnection();
				c.assign("treated", Doubles.toArray(withinAUCValues));
				c.assign("control", Doubles.toArray(crossAUCValues));
				
				RList l = c.eval("wilcox.test(control,treated,paired=TRUE)").asList();
				p=l.at("p.value").asDouble();
				
				c.assign("treated", Doubles.toArray(ifsAUCValues));
				c.assign("control", Doubles.toArray(crossAUCValues));
				l = c.eval("wilcox.test(control,treated,paired=TRUE)").asList();
				pBtCandIFS=l.at("p.value").asDouble();
				
			} catch (RserveException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REngineException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (REXPMismatchException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//double p = statTest.wilcoxonSignedRankTest(Doubles.toArray(withinAUCValues), Doubles.toArray(crossAUCValues),false);
			//double pBtCandIFS = statTest.wilcoxonSignedRankTest(Doubles.toArray(ifsAUCValues), Doubles.toArray(crossAUCValues),false);
			
			String wtlResultsBtWvsKS = "tie";
			
			if(cMedian>wMedian && p < 0.05)
				wtlResultsBtWvsKS = "win";
			else if(wMedian>cMedian && p < 0.05)
				wtlResultsBtWvsKS = "loss";
			
			String wtlResultsBtCandIFS = "tie";
			
			if(cMedian>ifsMedian && pBtCandIFS < 0.05)
				wtlResultsBtCandIFS = "win";
			else if(ifsMedian>cMedian && pBtCandIFS < 0.05)
				wtlResultsBtCandIFS = "loss";
			
			
			System.out.println(key + "," + 
								wMedian + "," + ifsMedian + "," + cMedian + "," + numPredictions + "," + p + "," + wtlResultsBtWvsKS + "," + pBtCandIFS + "," +
								wtlResultsBtCandIFS);
		
			//relcaced by Rserve
			/*rSourceWitinAndCross.add(key);
			rSourceWitinAndCross.add("treated<-c(" + getStringFromArray(Doubles.toArray(withinAUCValues)) +")");
			rSourceWitinAndCross.add("control<-c(" + getStringFromArray(Doubles.toArray(crossAUCValues)) +")");
			rSourceWitinAndCross.add("wilcox.test(control,treated,paired=TRUE)");
			
			rSourceCross.add(key);
			rSourceCross.add("treated<-c(" + getStringFromArray(Doubles.toArray(ifsAUCValues)) +")");
			rSourceCross.add("control<-c(" + getStringFromArray(Doubles.toArray(crossAUCValues)) +")");
			rSourceCross.add("wilcox.test(control,treated,paired=TRUE)");*/
		}
		
		//replcaced by Rserve
		/*if(outputRSciprt){
			for(String line:rSourceWitinAndCross)
				System.out.println(line);
			
			for(String line:rSourceCross)
				System.out.println(line);
			
			for(String line:rSourceWitinAndCrossF)
				System.out.println(line);
		}*/
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
			if(!records.get("Type").equals("D")) continue;
			
			String itrtCount = records.get("ItrtCount");
			String analyzer = records.get("Analyzer");
			//if(!itrtCount.equals("1") || analyzer.equals("Analyzer")) continue;
			String prediction = records.get("Prediction");
			String group = records.get("Group");
			String source = records.get("Source");
			String target = records.get("Target");
			String classifier = records.get("Classifier");
			String key = prediction + "," + source + "," +target + "," + analyzer + "," + classifier; //prediction.split(">>")[1];//records.get("target");
			//String key = group +"," + prediction + "," + source + "," +target + "," + analyzer + "," + classifier; //prediction.split(">>")[1];//records.get("target");
			//String key = prediction; //prediction.split(">>")[1];//records.get("target");
			//String key = analyzer;
			String withinAUC  = records.get("TargetWithinAUC");
			String crossAUC = records.get("CrossAUC");
			
			if(!withinResults.containsKey(key)){
				ArrayList<Double> withinAUCValues = new ArrayList<Double>();
				withinAUCValues.add(Double.parseDouble(withinAUC));
				withinResults.put(key, withinAUCValues);
				
				ArrayList<Double> crossAUCValues = new ArrayList<Double>();
				crossAUCValues.add(Double.parseDouble(crossAUC));
				crossResults.put(key, crossAUCValues);
				
			}
			else{
				withinResults.get(key).add(Double.parseDouble(withinAUC));
				crossResults.get(key).add(Double.parseDouble(crossAUC));
			}
		}
	}

}
