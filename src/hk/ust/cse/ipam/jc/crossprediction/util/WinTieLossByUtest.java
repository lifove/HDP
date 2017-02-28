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

public class WinTieLossByUtest {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		new WinTieLossByUtest().run(args);
	}
	
	HashMap<String,ArrayList<Double>> withinResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossResults = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> withinResultsF = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossResultsF = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> manualResults = new HashMap<String,ArrayList<Double>>();
	
	HashMap<String,ArrayList<Double>> srcPrecisions = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> tarPrecisions = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossPrecisions = new HashMap<String,ArrayList<Double>>();
	
	HashMap<String,ArrayList<Double>> srcRecalls = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> tarRecalls = new HashMap<String,ArrayList<Double>>();
	HashMap<String,ArrayList<Double>> crossRecalls = new HashMap<String,ArrayList<Double>>();
	HashMap<String,Double> manualResultsByPrediction = new HashMap<String,Double>();
	
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

		while (records.readRecord()){
			String key = records.get("Prediction");
			String manualAUC = records.get("AUC_manual");
			manualResultsByPrediction.put(key, Double.parseDouble(manualAUC));
		}		
	}

	private void conductUtest(){
		WilcoxonSignedRankTest statTest = new WilcoxonSignedRankTest();
		
		ArrayList<String> rSourceWitinAndCross = new ArrayList<String>();
		ArrayList<String> rSourceCross = new ArrayList<String>();
		
		ArrayList<String> rSourceWitinAndCrossF = new ArrayList<String>();
		
		for(String key:withinResults.keySet()){
			ArrayList<Double> withinAUCValues = withinResults.get(key);
			ArrayList<Double> crossAUCValues = crossResults.get(key);
			ArrayList<Double> manualAUCValues = manualResults.get(key);
			ArrayList<Double> withinFValues = withinResultsF.get(key);
			ArrayList<Double> crossFValues = crossResultsF.get(key);
			
			ArrayList<Double> srcPrecisionValues = srcPrecisions.get(key);
			ArrayList<Double> tarPrecisionValues = tarPrecisions.get(key);
			ArrayList<Double> crossPrecisionValues = crossPrecisions.get(key);
			ArrayList<Double> srcRecallValues = srcRecalls.get(key);
			ArrayList<Double> tarRecallValues = tarRecalls.get(key);
			ArrayList<Double> crossRecallValues = crossRecalls.get(key);

			
			DescriptiveStatistics statWithin = new DescriptiveStatistics(Doubles.toArray(withinAUCValues));
			DescriptiveStatistics statCross = new DescriptiveStatistics(Doubles.toArray(crossAUCValues));
			DescriptiveStatistics statManual = new DescriptiveStatistics(Doubles.toArray(manualAUCValues));
			DescriptiveStatistics statWithinF = new DescriptiveStatistics(Doubles.toArray(withinFValues));
			DescriptiveStatistics statCrossF = new DescriptiveStatistics(Doubles.toArray(crossFValues));
			
			DescriptiveStatistics statSrcPrecision = new DescriptiveStatistics(Doubles.toArray(srcPrecisionValues));
			DescriptiveStatistics statTarPrecision = new DescriptiveStatistics(Doubles.toArray(tarPrecisionValues));
			DescriptiveStatistics statCrossPrecision = new DescriptiveStatistics(Doubles.toArray(crossPrecisionValues));
			DescriptiveStatistics statSrcRecall = new DescriptiveStatistics(Doubles.toArray(srcRecallValues));
			DescriptiveStatistics statTarRecall = new DescriptiveStatistics(Doubles.toArray(tarRecallValues));
			DescriptiveStatistics statCrossRecall = new DescriptiveStatistics(Doubles.toArray(crossRecallValues));
			
			double wMedian = statWithin.getPercentile(50);
			double cMedian = statCross.getPercentile(50);
			double mMedian = statManual.getPercentile(50);
			double wMedianF = statWithinF.getPercentile(50);
			double cMedianF = statCrossF.getPercentile(50);
			
			double srcPrecisionMedian = statSrcPrecision.getPercentile(50);
			double tarPrecisionMedian = statTarPrecision.getPercentile(50);
			double crossPrecisionMedian = statCrossPrecision.getPercentile(50);
			double srcRecallMedian = statSrcRecall.getPercentile(50);
			double tarRecallMedian = statTarRecall.getPercentile(50);
			double crossRecallMedian = statCrossRecall.getPercentile(50);

			int numPredictions = withinAUCValues.size();
			
			double p=0.0;
			double pBtCM=0.0;
			double pF=0.0;
			try {
				RConnection c = new RConnection();
				c.assign("treated", Doubles.toArray(withinAUCValues));
				c.assign("control", Doubles.toArray(crossAUCValues));
				
				RList l = c.eval("wilcox.test(control,treated,paired=TRUE)").asList();
				p=l.at("p.value").asDouble();
				
				c.assign("treated", Doubles.toArray(manualAUCValues));
				c.assign("control", Doubles.toArray(crossAUCValues));
				l = c.eval("wilcox.test(control,treated,paired=TRUE)").asList();
				pBtCM=l.at("p.value").asDouble();
				
				c.assign("treated", Doubles.toArray(withinFValues));
				c.assign("control", Doubles.toArray(crossFValues));
				l = c.eval("wilcox.test(control,treated,paired=TRUE)").asList();
				pF=l.at("p.value").asDouble();
				
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
			//double pBtCM = statTest.wilcoxonSignedRankTest(Doubles.toArray(manualAUCValues), Doubles.toArray(crossAUCValues),false);
			//double pF = statTest.wilcoxonSignedRankTest(Doubles.toArray(withinFValues), Doubles.toArray(crossFValues),false);
			
			String wtlResultsBtWvsKS = "tie";
			
			if(cMedian>wMedian && p < 0.05)
				wtlResultsBtWvsKS = "win";
			else if(wMedian>cMedian && p < 0.05)
				wtlResultsBtWvsKS = "loss";
			
			String wtlResults = "tie";
			
			if(cMedian>mMedian && pBtCM < 0.05)
				wtlResults = "win";
			else if(mMedian>cMedian && pBtCM < 0.05)
				wtlResults = "loss";
			
			String wtlResultsBtWvsKSF = "tie";
			
			if(cMedianF>wMedianF && pF < 0.05)
				wtlResultsBtWvsKSF = "win";
			else if(wMedianF>cMedianF && pF < 0.05)
				wtlResultsBtWvsKSF = "loss";
			
			System.out.println(key + "," + srcPrecisionMedian + "," + tarPrecisionMedian + "," + crossPrecisionMedian + "," +
								srcRecallMedian + "," + tarRecallMedian + "," + crossRecallMedian + "," +
								wMedian + "," + mMedian + "," + cMedian + "," + numPredictions + "," + p + "," + wtlResultsBtWvsKS + "," + pBtCM + "," +
								wtlResults + "," + wMedianF + "," +cMedianF + "," + wtlResultsBtWvsKSF);
		
			/*rSourceWitinAndCross.add(key);
			rSourceWitinAndCross.add("treated<-c(" + getStringFromArray(Doubles.toArray(withinAUCValues)) +")");
			rSourceWitinAndCross.add("control<-c(" + getStringFromArray(Doubles.toArray(crossAUCValues)) +")");
			rSourceWitinAndCross.add("wilcox.test(control,treated,paired=TRUE)");
			
			rSourceCross.add(key);
			rSourceCross.add("treated<-c(" + getStringFromArray(Doubles.toArray(manualAUCValues)) +")");
			rSourceCross.add("control<-c(" + getStringFromArray(Doubles.toArray(crossAUCValues)) +")");
			rSourceCross.add("wilcox.test(control,treated,paired=TRUE)");
			
			rSourceWitinAndCrossF.add(key);
			rSourceWitinAndCrossF.add("treated<-c(" + getStringFromArray(Doubles.toArray(withinFValues)) +")");
			rSourceWitinAndCrossF.add("control<-c(" + getStringFromArray(Doubles.toArray(crossFValues)) +")");
			rSourceWitinAndCrossF.add("wilcox.test(control,treated,paired=TRUE)");*/
		}
		
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
			String key = group +"," + prediction + "," + source + "," +target + "," + analyzer + "," + classifier; //prediction.split(">>")[1];//records.get("target");
			//String key = prediction; //prediction.split(">>")[1];//records.get("target");
			//String key = analyzer;
			String withinAUC  = records.get("TargetWithinAUC");
			String crossAUC = records.get("CrossAUC");
			String withinF  = records.get("TargetWithinF");
			String crossF = records.get("CrossF");
			String srcPrecision = records.get("SourceWithinPrecision");
			String tarPrecision = records.get("TargetWithinPrecision");
			String crossPrecision = records.get("CrossPrecision");
			
			String srcRecall = records.get("SourceWithinRecall");
			String tarRecall = records.get("TargetWithinRecall");
			String crossRecall = records.get("CrossRecall");
			
			String adjustedPredictionForManualAUC = !fsOption.equals("")?prediction.replace(fsOption, ""):prediction;
			
			Double manualAUC = manualResultsByPrediction.size()==0?records.get("AUC_manual").equals("")?-1:Double.parseDouble(records.get("AUC_manual")):manualResultsByPrediction.containsKey(adjustedPredictionForManualAUC)?manualResultsByPrediction.get(adjustedPredictionForManualAUC):-1.0;
			
			if(!withinResults.containsKey(key)){
				ArrayList<Double> withinAUCValues = new ArrayList<Double>();
				withinAUCValues.add(Double.parseDouble(withinAUC));
				withinResults.put(key, withinAUCValues);
				
				ArrayList<Double> withinFValues = new ArrayList<Double>();
				withinFValues.add(Double.parseDouble(withinF));
				withinResultsF.put(key, withinFValues);
				
				ArrayList<Double> crossAUCValues = new ArrayList<Double>();
				crossAUCValues.add(Double.parseDouble(crossAUC));
				crossResults.put(key, crossAUCValues);
				
				ArrayList<Double> crossFValues = new ArrayList<Double>();
				crossFValues.add(Double.parseDouble(crossF));
				crossResultsF.put(key, crossFValues);
				
				ArrayList<Double> srcPrecisionValues = new ArrayList<Double>();
				srcPrecisionValues.add(Double.parseDouble(srcPrecision));
				srcPrecisions.put(key, srcPrecisionValues);
				ArrayList<Double> tarPrecisionValues = new ArrayList<Double>();
				tarPrecisionValues.add(Double.parseDouble(tarPrecision));
				tarPrecisions.put(key, tarPrecisionValues);
				ArrayList<Double> crossPrecisionValues = new ArrayList<Double>();
				crossPrecisionValues.add(Double.parseDouble(crossPrecision));
				crossPrecisions.put(key, crossPrecisionValues);
				
				ArrayList<Double> srcRecallValues = new ArrayList<Double>();
				srcRecallValues.add(Double.parseDouble(srcRecall));
				srcRecalls.put(key, srcRecallValues);
				ArrayList<Double> tarRecallValues = new ArrayList<Double>();
				tarRecallValues.add(Double.parseDouble(tarRecall));
				tarRecalls.put(key, tarRecallValues);
				ArrayList<Double> crossRecallValues = new ArrayList<Double>();
				crossRecallValues.add(Double.parseDouble(crossRecall));
				crossRecalls.put(key, crossRecallValues);
				
				ArrayList<Double> manualAUCValues = new ArrayList<Double>();
				manualAUCValues.add(manualAUC);
				manualResults.put(key, manualAUCValues);
			}
			else{
				withinResults.get(key).add(Double.parseDouble(withinAUC));
				crossResults.get(key).add(Double.parseDouble(crossAUC));
				manualResults.get(key).add(manualAUC);
				
				srcPrecisions.get(key).add(Double.parseDouble(srcPrecision));
				tarPrecisions.get(key).add(Double.parseDouble(tarPrecision));
				crossPrecisions.get(key).add(Double.parseDouble(crossPrecision));
				
				srcRecalls.get(key).add(Double.parseDouble(srcRecall));
				tarRecalls.get(key).add(Double.parseDouble(tarRecall));
				crossRecalls.get(key).add(Double.parseDouble(crossRecall));
				
				withinResultsF.get(key).add(Double.parseDouble(withinF));
				crossResultsF.get(key).add(Double.parseDouble(crossF));
			}
		}
	}

}
