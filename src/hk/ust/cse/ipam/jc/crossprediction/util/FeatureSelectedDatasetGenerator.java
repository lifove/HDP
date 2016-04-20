package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import weka.core.Instances;
import hk.ust.cse.ipam.utils.WekaUtils;

public class FeatureSelectedDatasetGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		 
		BufferedReader reader;
		try {
			
			String[] paths ={"data/AEEEM/EQ.arff","data/Severity/pitsB.arff",
							"data/Medical/ovarian_61902.arff"};
			
			String[] newPaths ={"data/AEEEM/EQ_FS.arff","data/Severity/pitsB_FS.arff",
			"data/Medical/ovarian_61902_FS.arff"};
			
			for(int i=0;i<paths.length;i++){
				System.out.println("FEATURE SELECTING: " + paths[i]);
				reader = new BufferedReader(new FileReader(paths[i]));
				Instances instances = new Instances(reader);
				reader.close();
				
				instances.setClass(instances.attribute(instances.numAttributes()-1));
				
				instances = WekaUtils.featrueSelectionByCfsSubsetEval(instances);
				
				WekaUtils.writeADataFile(instances, newPaths[i]);
				
				System.out.println("Finished: " + newPaths[i]);
			}
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
