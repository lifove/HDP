package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

import com.google.common.primitives.Doubles;

import weka.core.Instances;

public class Utils {
	
	static public Instances loadArff(String path,String labelName){
		
		try {
			// load sourceData
			BufferedReader reader = new BufferedReader(new FileReader(path));
			Instances instances = new Instances(reader);
			reader.close();

			instances.setClass(instances.attribute(labelName));
			
			return instances;
		
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("A file does not exist! (both or each) Please, check path and upper/lower cases~");
			System.out.println("Source path: " + path);
			System.exit(0);
		}
				
		return null;
	}
	
	static double[] removeOutliers(double[] values) {
		
		Arrays.sort(values);
		int arraySize = values.length;
		
		double sectionSize = arraySize/4;
		
		ArrayList<Double> valuesWithoutOutlier = new ArrayList<Double>();
		
		for(int i=0;i<arraySize;i++){
			if( Math.round(sectionSize)<=i && i <= Math.round(sectionSize*3))
				valuesWithoutOutlier.add(values[i]);
		}
		
		return Doubles.toArray(valuesWithoutOutlier);
	}
}
