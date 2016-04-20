package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import hk.ust.cse.ipam.utils.FileUtil;

public class CoFeautureGeneratorFromLPResult {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CoFeautureGeneratorFromLPResult().run(args,0.00,"cofeature.txt");
	}

	void run(String[] args, double cutoff,String pathToSave){
		String pathForCofeaturesAllMatched = args[0]; // "data/cofeatures_20140729_PAnalyzer_FS_FILTER_All_Matched.txt";
		String dirForLPResults = args[1]; //"/Users/JC/Dropbox/Transfer/CDDP/LPSOLVER.tar/resultsFSP/";
		
		File createdDir = new File(dirForLPResults);
		if(!createdDir.exists()){
			if(!(new File(dirForLPResults).mkdirs())){
				System.err.println(dirForLPResults +" is not created");
				System.exit(0);
			}
		}
			
		
		ArrayList<String> lines = FileUtil.getLines(pathForCofeaturesAllMatched, false);
		
		ArrayList<String> outputLines = new ArrayList<String>();
		
		DecimalFormat dec = new DecimalFormat("0.00");
		
		for(String line:lines){
			String[] splitLine = line.split(":");
			
			String predComb = splitLine[0];
			String numFeatures = splitLine[1];
			if(splitLine.length==2){
				outputLines.add(predComb + ":" + numFeatures + ":");
				continue;
			}
				
			String[] edges = splitLine[2].split(",");
			
			ArrayList<String> resultLines = FileUtil.getLines(dirForLPResults + predComb + "_" + dec.format(cutoff) + ".txt", false);
			
			String matchedFeatures = predComb + ":" + numFeatures + ":";
			for(String resultLine:resultLines){
				if(resultLine.startsWith("x")){
					String[] results = resultLine.split("[\\s]+");
					int edgeIndex = Integer.parseInt(results[0].substring(1, results[0].length()));
					String result = results[1];
					if(result.equals("1"))
						matchedFeatures = matchedFeatures + edges[edgeIndex] + ",";
				}
			}
			outputLines.add(matchedFeatures);
			//System.exit(0);
		}
		
		FileUtil.writeAFile(outputLines, pathToSave);
	}
}
