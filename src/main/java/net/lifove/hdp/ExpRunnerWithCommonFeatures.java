package net.lifove.hdp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import net.lifove.hdp.util.Utils;
import net.lifove.hdp.util.Utils.FeatureSelectors;
import weka.core.Instances;

public class ExpRunnerWithCommonFeatures {

	String [] projects = {
			//"MIM/MIMEtc.arff",
			//"MIM/MIMMylyn.arff",
			//"MIM/MIMTeam.arff",
			"ReLink/Safe.arff",
			"ReLink/Apache.arff",
			"ReLink/Zxing.arff",
			"NASA/mc2.arff", // new
			"NASA/pc5.arff", // new
			"NASA/pc1.arff",
			"NASA/pc2.arff", // new
			"NASA/jm1.arff", // new
			"NASA/pc4.arff",
			"NASA/kc3.arff", // new
			"NASA/pc3.arff",
			"NASA/mw1.arff",
			"NASA/cm1.arff",
			"NASA/mc1.arff", // new
			"SOFTLAB/ar5.arff",
			"SOFTLAB/ar3.arff",
			"SOFTLAB/ar4.arff",
			"SOFTLAB/ar1.arff",
			"SOFTLAB/ar6.arff",
			"CK/ant-1.3.arff",
			"CK/arc.arff",
			"CK/camel-1.0.arff",
			"CK/poi-1.5.arff",
			"CK/redaktor.arff",
			"CK/skarbonka.arff",
			"CK/tomcat.arff",
			"CK/velocity-1.4.arff",
			"CK/xalan-2.4.arff",
			"CK/xerces-1.2.arff",
			"AEEEM/PDE.arff",
			"AEEEM/EQ.arff",
			"AEEEM/LC.arff",
			"AEEEM/JDT.arff",
			"AEEEM/ML.arff",
	};

	public static void main(String[] args) {
		new ExpRunnerWithCommonFeatures().run(args);
	}

	public void run(String[] args){

		String pathToDataset = args[0];
		String pathToSaveResults = args[1];
		String commonMetricsDataFile = args[2];
		String mlAlg = args[3]; //"weka.classifiers.functions.SimpleLogistic"
		
		Runner runner = new Runner();

		Path path = Paths.get(pathToSaveResults + "/HDP_common_metrics_" + mlAlg + ".txt");

		ArrayList<String> commonMetrics = getLines(pathToDataset + File.separator + commonMetricsDataFile,false);

		HashMap<String,ArrayList<String>> mapMatchedMetrics = new HashMap<String,ArrayList<String>>();

		HashMap<String,String> withinResults = new HashMap<String,String>();
		
		OpenOption openOpt = StandardOpenOption.CREATE;
		HashSet<String> existingPrediciton = new HashSet<String>(); //"repeat,fold,Group/data.arff,group/data2.arff"
		if(Files.exists(path, LinkOption.NOFOLLOW_LINKS)){
			openOpt = StandardOpenOption.APPEND;
			ArrayList<String> lines = getLines(path.toString(),false);
			for(int i=0;i<lines.size()-1;i++){ // ignore the last line which might be corrupted.
				String[] splitLine=lines.get(i).split(",");
				existingPrediciton.add(splitLine[0]+","+splitLine[1]+","+splitLine[2]+","+splitLine[3]);
			}
		}

		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8,openOpt)) {

			for(String target:projects){
				for(String source:projects){
					if(source.equals(target))
						continue;

					String[] srclabelInfo = getLabelInfo(source);
					String[] tarlabelInfo = getLabelInfo(target);

					Instances sourceInstances = Utils.loadArff(pathToDataset +  source, srclabelInfo[0]);
					Instances targetInstances = Utils.loadArff(pathToDataset +  target, tarlabelInfo[0]);

					// Skip datasets with the same number of attributes
					if(sameMetricSets(sourceInstances,targetInstances)){
						System.err.println("SKIP: the number of attributes is same.: " + source + "==> " + target);
						continue;
					}

					ArrayList<String> strMatchedMetrics;

					String keyForMatchedMetrics =source + target;

					if(mapMatchedMetrics.containsKey(keyForMatchedMetrics))
						strMatchedMetrics = mapMatchedMetrics.get(keyForMatchedMetrics);
					else{
						strMatchedMetrics = getStrMatchedMetrics(commonMetrics,source,target);
						mapMatchedMetrics.put(keyForMatchedMetrics, strMatchedMetrics);
					}

					int numRuns = 500;
					int folds =2;

					for(int repeat=0;repeat < numRuns;repeat++){

						// randomize with different seed for each iteration
						targetInstances.randomize(new Random(repeat)); 
						targetInstances.stratify(folds);

						for(int fold = 0; fold < folds; fold++){
							
							if(existingPrediciton.contains(repeat + "," +fold + "," + source + "," + target)) continue;
							
							String withinResult = "";

							String key = target + repeat + "," + fold; 
							if(withinResults.containsKey(key))
								withinResult = withinResults.get(key);
							else{
								withinResult = Utils.doCrossPrediction(targetInstances.trainCV(folds, fold), 
										targetInstances.testCV(folds, fold),
										tarlabelInfo[1],
										mlAlg,false,FeatureSelectors.None);
								withinResults.put(key,withinResult);		
							}


							String result = runner.doHDP(false, sourceInstances, targetInstances.testCV(folds, fold), srclabelInfo[0], srclabelInfo[1],
									tarlabelInfo[0], tarlabelInfo[1], strMatchedMetrics, 0, true,FeatureSelectors.None,source,target,mlAlg);

							if(result.equals(""))
								continue;

							writer.write(repeat + "," +fold + "," + source + "," + target + "," + 
									source + target + "," +
									withinResult + ","+ result + "\n" );
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}


	}

	private ArrayList<String> getStrMatchedMetrics(ArrayList<String> commonMetrics, String source, String target) {
		ArrayList<String> strMatchedMetrics = new ArrayList<String>();

		String srcGroupName = source.split("/")[0];
		String tarGroupName = target.split("/")[0];

		for(String line:commonMetrics){
			if(line.startsWith(srcGroupName + ">>" + tarGroupName)){
				String[] matechedMetrics = line.replace(srcGroupName + ">>" + tarGroupName + ":", "").split("\\|");

				for(String matchedMetric:matechedMetrics){
					String[] splitString  = matchedMetric.split(">>");
					String srcMetric = splitString[0];
					String tarMetric = splitString[1];

					strMatchedMetrics.add(srcMetric + "-" + tarMetric + "(1)");
				}
				break;
			}

			if(line.startsWith(tarGroupName + ">>" + srcGroupName)){
				String[] matechedMetrics = line.replace(tarGroupName + ">>" + srcGroupName + ":", "").split("\\|");

				for(String matchedMetric:matechedMetrics){
					String[] splitString  = matchedMetric.split(">>");
					String srcMetric = splitString[1];
					String tarMetric = splitString[0];

					strMatchedMetrics.add(srcMetric + "-" + tarMetric + "(1)");
				}
				break;
			}
		}

		return strMatchedMetrics;
	}

	private boolean sameMetricSets(Instances sourceInstances, Instances targetInstances) {

		if(sourceInstances.numAttributes()!=targetInstances.numAttributes())
			return false;

		for(int attrIdx = 0; attrIdx < sourceInstances.numAttributes();attrIdx++){
			if(!sourceInstances.attribute(attrIdx).name().equals(targetInstances.attribute(attrIdx).name()))
				return false;
		}

		return true;
	}

	private String[] getLabelInfo(String path) {

		String[] labelInfo = new String[2];

		String group = path.substring(0, path.indexOf("/"));

		if(group.equals("ReLink")){
			labelInfo[0] = "isDefective";
			labelInfo[1] = "TRUE";
		}

		if(group.equals("NASA")){
			labelInfo[0] = "Defective";
			labelInfo[1] = "Y";
		}

		if(group.equals("AEEEM")){
			labelInfo[0] = "class";
			labelInfo[1] = "buggy";
		}

		if(group.equals("SOFTLAB")){
			labelInfo[0] = "defects";
			labelInfo[1] = "true";
		}

		if(group.equals("CK")){
			labelInfo[0] = "bug"; // bug or class
			labelInfo[1] = "buggy";
		}

		return labelInfo;
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
}
