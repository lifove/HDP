package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class LpSolveModelGeneratorTest {

	@Test
	public void testRun() {
		String args[] = {"LPModel0.05S15",
							"/Users/JC/Documents/HKUST/Research/CDDP/workspace/CrossPredictionSimulator/data",
							"data/cofeatures_20150610_All_Matched_for_fs_shiv_s0.15_2_fold.txt",
							"/Users/JC/Documents/HKUST/Research/CDDP/LPSOLVER.tar/resultsS15_0.050",
							"/Users/JC/Documents/HKUST/Research/CDDP/LPSOLVER.tar"};
		new LpSolveModelGenerator().run(args,0.05);
		
		/*for(int i=1; i<=9;i++){
			new LpSolveModelGenerator().run(args,i*0.1);
		}*/
	}

}
