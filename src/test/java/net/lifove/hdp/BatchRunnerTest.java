package net.lifove.hdp;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.junit.Test;

import net.lifove.hdp.util.Utils;
import weka.core.Attribute;
import weka.core.Instances;

public class BatchRunnerTest {

	@Test
	public void testMain() {
		Runner runner = new Runner();
		
		String [] projects = {
				"ReLink/Safe.arff",
				"ReLink/Apache.arff",
				"ReLink/Zxing.arff",
				"NASA/MC2.arff", // new
				"NASA/PC5.arff", // new
				"NASA/PC1.arff",
				"NASA/PC2.arff", // new
				"NASA/JM1.arff", // new
				"NASA/PC4.arff",
				"NASA/KC3.arff", // new
				"NASA/PC3.arff",
				"NASA/MW1.arff",
				"NASA/CM1.arff",
				"NASA/MC1.arff", // new
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
				/*"CK/ivy-2.0.arff",
				"CK/jedit-4.1.arff",
				"CK/lucene-2.4.arff",
				"CK/poi-3.0.arff",
				"CK/synapse-1.2.arff",
				"CK/velocity-1.6.arff",
				"CK/xalan-2.6.arff",
				"CK/xerces-1.3.arff",
				"CK/prop-1.arff",
				"CK/prop-2.arff",
				"CK/prop-3.arff",
				"CK/prop-4.arff",
				"CK/prop-5.arff",
				"CK/prop-6.arff",
				"CK/sklebagd.arff",*/
				"AEEEM/PDE.arff",
				"AEEEM/EQ.arff",
				"AEEEM/LC.arff",
				"AEEEM/JDT.arff",
				"AEEEM/ML.arff"
				};
		
		String pathToDataset = System.getProperty("user.home") + "/Documents/HDP/data/";
		
		String cutoff = "0.05";

		Path path = Paths.get(System.getProperty("user.home") + "/Documents/HDP/Results/SingleHDP_C" + cutoff +".txt");
		
		try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
			
			for(String target:projects){
				for(String source:projects){
					if(source.equals(target))
						continue;
					
					String[] srclabelInfo = getLabelInfo(source);
					String[] tarlabelInfo = getLabelInfo(target);
					
					Instances sourceInstances = Utils.loadArff(pathToDataset +  source, srclabelInfo[0]);
					Instances targetInstances = Utils.loadArff(pathToDataset +  target, tarlabelInfo[0]);
					
					// Skip datasets with the same number of attrbiutes
					if(sameMetricSets(sourceInstances,targetInstances)){
						System.err.println("SKIP: the number of attributes is same.: " + source + "==> " + target);
						continue;
					}
					
					String[] args = {"-s", pathToDataset +  source, "-t",  pathToDataset + target,
							"-sl", srclabelInfo[0], "-sp", srclabelInfo[1],
							"-tl", tarlabelInfo[0], "-tp", tarlabelInfo[1],"-c",cutoff,"-r"}; 
					runner.getStringHDPResult(args);
					
					String result = runner.getStringHDPResult(args);
					
					if(result.equals(""))
						continue;
					
					writer.write(source + "," + target + "," + result + "\n" );
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			labelInfo[0] = "class";
			labelInfo[1] = "buggy";
		}
		
		return labelInfo;
	}
}
