package hk.ust.cse.ipam.jc.crossprediction.hemap;

import static org.junit.Assert.*;

import org.apache.commons.math3.linear.MatrixUtils;
import org.junit.Test;

public class HeMapTransformerTest {

	@Test
	public void testRun() {
		HeMapTransformer transformer = new HeMapTransformer();
		String[] args = {"EQ","data/AEEEM/EQ.arff", "class", "buggy", "cm1", "data/NASA/cm1.arff", "Defective","T","nonlinear"};
		transformer.run(args);
		assertEquals("size of T and S must be same", transformer.source.size(), transformer.target.size());
		assertEquals("Source matrix value in 0,0 is wrong", transformer.S[0][0], 3,0);
		assertEquals("Source matrix value in 0,1 is wrong", transformer.S[0][1], 0.002547,0);
		assertEquals("Source matrix value in 3,1 is wrong", transformer.S[3][1], 0.005642,0);
		assertTrue("Matrix A should be symmetric:",MatrixUtils.isSymmetric(MatrixUtils.createRealMatrix(transformer.A), 0));
		
	}

}
