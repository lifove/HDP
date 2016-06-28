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

public class BatchRunnerTest {

	@Test
	public void testMain() {
		Runner runner = new Runner();
		
		String [] projects = {
				"ReLink/Safe.arff",
				"ReLink/Apache.arff",
				"ReLink/Zxing.arff",
				"NASA/MC2.arff",
				"NASA/PC5.arff",
				"NASA/PC1.arff",
				"NASA/PC2.arff",
				"NASA/JM1.arff",
				"NASA/PC4.arff",
				"NASA/KC3.arff",
				"NASA/PC3.arff",
				"NASA/MW1.arff",
				"NASA/CM1.arff",
				"NASA/MC1.arff",
				"SOFTLAB/ar5.arff",
				"SOFTLAB/ar3.arff",
				"SOFTLAB/ar4.arff",
				"SOFTLAB/ar1.arff",
				"SOFTLAB/ar6.arff",
				"CK/log4j-1.0.arff",
				"CK/log4j-1.2.arff",
				"CK/xerces-1.4.arff",
				"CK/xerces-1.2.arff",
				"CK/termoproject.arff",
				"CK/velocity-1.5.arff",
				"CK/zuzel.arff",
				"CK/poi-2.5.arff",
				"CK/szybkafucha.arff",
				"CK/lucene-2.0.arff",
				"CK/e-learning.arff",
				"CK/arc.arff",
				"CK/kalkulator.arff",
				"CK/jedit-4.2.arff",
				"CK/prop-6.arff",
				"CK/systemdata.arff",
				"CK/forrest-0.8.arff",
				"CK/redaktor.arff",
				"CK/ant-1.5.arff",
				"CK/tomcat.arff",
				"CK/skarbonka.arff",
				"CK/intercafe.arff",
				"CK/synapse-1.0.arff",
				"CK/ivy-2.0.arff",
				"CK/ivy-1.4.arff",
				"CK/xerces-1.3.arff",
				"CK/jedit-4.3.arff",
				"CK/pbeans2.arff",
				"CK/lucene-2.2.arff",
				"CK/nieruchomosci.arff",
				"CK/synapse-1.1.arff",
				"CK/berek.arff",
				"CK/pbeans1.arff",
				"CK/log4j-1.1.arff",
				"CK/camel-1.0.arff",
				"CK/jedit-4.0.arff",
				"CK/lucene-2.4.arff",
				"CK/poi-1.5.arff",
				"CK/camel-1.2.arff",
				"CK/prop-5.arff",
				"CK/forrest-0.6.arff",
				"CK/workflow.arff",
				"CK/ant-1.6.arff",
				"CK/poi-2.0.arff",
				"CK/jedit-3.2.arff",
				"CK/prop-3.arff",
				"CK/forrest-0.7.arff",
				"CK/synapse-1.2.arff",
				"CK/xalan-2.4.arff",
				"CK/prop-1.arff",
				"CK/ant-1.7.arff",
				"CK/camel-1.4.arff",
				"CK/ant-1.3.arff",
				"CK/xalan-2.6.arff",
				"CK/pdftranslator.arff",
				"CK/camel-1.6.arff",
				"CK/xalan-2.5.arff",
				"CK/ant-1.4.arff",
				"CK/prop-4.arff",
				"CK/ckjm.arff",
				"CK/jedit-4.1.arff",
				"CK/wspomaganiepi.arff",
				"CK/xerces-init.arff",
				"CK/poi-3.0.arff",
				"CK/prop-2.arff",
				"CK/serapion.arff",
				"CK/ivy-1.1.arff",
				"CK/velocity-1.4.arff",
				"CK/xalan-2.7.arff",
				"CK/velocity-1.6.arff",
				"CK/sklebagd.arff",
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
					
					String[] args = {"-s", pathToDataset +  source, "-t",  pathToDataset + target,
							"-sl", srclabelInfo[0], "-sp", srclabelInfo[1],
							"-tl", tarlabelInfo[0], "-tp", tarlabelInfo[1],"-c",cutoff,"-r"}; 
					runner.getStringHDPResult(args);
					
					String result = runner.getStringHDPResult(args);
					
					writer.write(source + "," + target + "," + result + "\n" );
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
