package net.lifove.hdp;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;


public class EffectSizeRScriptGeneratorTest {

	@Test
	public void testMain() {
		
		String pathToResults = System.getProperty("user.home") + "/Documents/HDP/Resutls/";
		ArrayList<String> linesHDP = getLines(pathToResults + "HDP_C0.05_ChiSquare.txt",false);
		ArrayList<String> linesIFS = getLines(pathToResults + "IFS_results.txt",false);
		
		HashMap<String,ArrayList<Prediction>> resultsHDP = new HashMap<String,ArrayList<Prediction>>();
		
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

class Prediction{
	int fold;
	int repeat;
	double AUC;
	
	public Prediction(int fold,int repeat,double AUC){
		this.fold = fold;
		this.repeat = repeat;
		this.AUC = AUC;
	}
}
