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
import java.util.Random;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.junit.Test;

import net.lifove.hdp.util.Utils;
import weka.core.Instances;

public class KSTestTest {

	@Test
	public void testMain() {
		
		String sourcePath = System.getProperty("user.home") + "/Documents/CDDP/CDDP/data/NASA/pc1_FS_shiv_s0.15.arff";
		String targetPath = System.getProperty("user.home") + "/Documents/HDP/data/ReLink/Safe.arff";
		
		Instances source = Utils.loadArff(sourcePath, "Defective");
		Instances target = Utils.loadArff(targetPath, "isDefective");
		
		for(int srcAttrIdx=0;srcAttrIdx<source.numAttributes();srcAttrIdx++){
			
			if(srcAttrIdx==source.classIndex())
				continue;
			
			for(int tarAttrIdx=0;tarAttrIdx<target.numAttributes();tarAttrIdx++){
				
				if(tarAttrIdx==target.classIndex())
					continue;
				
				double[] srcAttrValues = source.attributeToDoubleArray(srcAttrIdx);
				double[] tarAttrValues = target.attributeToDoubleArray(tarAttrIdx);
				
				double matchingScore = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(srcAttrValues, tarAttrValues);
				
				System.out.println(srcAttrIdx + "-" + tarAttrIdx + " " + matchingScore);
				
			}
			
		}
		
	}
}
