package net.lifove.hdp;

import static org.junit.Assert.*;

import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.junit.Test;

public class RunnerTest {

	@Test
	public void testMain() {
		Runner runner = new Runner();
		runner.runner(null);
		
		assertEquals(runner.sourcePath,null);
		
		runner = new Runner();
		String[] args = {"-s", "test.arff"}; 
		runner.runner(args);
		assertEquals(runner.sourcePath,null); // because of missed options
		
		
		String[] args2 = {"-s", "source.arff", "-t", "target.arff", "-sl", "class", "-sp", "buggy","-tl", "class", "-tp", "buggy","-c","0.05"," "}; 
		runner.runner(args2);
		
		assertEquals(runner.sourcePath,args2[1]);
		assertEquals(runner.targetPath,args2[3]);
		assertEquals(runner.srclabelName,args2[5]);
		assertEquals(runner.srcPosLabelValue,args2[7]);
		assertEquals(runner.tarlabelName,args2[9]);
		assertEquals(runner.tarPosLabelValue,args2[11]);
		assertEquals(runner.cutoff,0.05,0);
		assertEquals(runner.help,false);
		assertEquals(runner.suppress,false);
		
		args2[1] = "data/sample.arff";
		args2[3] = "data/sample2.arff";
		
		runner.runner(args2);
		
		args2[14] = "-r";
		runner.runner(args2);
		
		args2[3] = "data/sample3.arff";
		runner.runner(args2);
		
		
		args2[1] = "../../Documents/HKUST/Research/CDDP/workspace/CrossPredictionSimulator/data/promise/ant-1.3.arff";
		args2[5] = "bug";
		
		args2[3] = "../../Documents/HKUST/Research/CDDP/workspace/CrossPredictionSimulator/data/SOFTLAB/ar5.arff";
		args2[9] = "defects";
		args2[11] = "true";
		
		
		args2[13] = "0.05";
		args2[14] = "";
		runner.runner(args2);
		
		
		double[] sample1 ={1,2,3,4,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20};
		double[] sample2 ={19,20,3,4,8,7,6,1,10,11,12,13,14,15,16,17,18,9,2};
		System.out.println(new KolmogorovSmirnovTest().kolmogorovSmirnovTest(sample1, sample2));
		
		double[] sample3 ={1,3,20,15,5,7,9,11,13};
		System.out.println(new KolmogorovSmirnovTest().kolmogorovSmirnovTest(sample1, sample3));
	}

}
