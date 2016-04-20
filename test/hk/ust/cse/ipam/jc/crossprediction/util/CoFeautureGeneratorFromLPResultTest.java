package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import java.text.DecimalFormat;

import org.junit.Test;

public class CoFeautureGeneratorFromLPResultTest {

	@Test
	public void testRun() {
		String args[] = {"data/cofeatures_20150610_All_Matched_for_fs_shiv_s0.15_2_fold.txt",
				"/Users/JC/Documents/HKUST/Research/CDDP/LPSOLVER.tar/resultsS15_0.050/"};
		String pathToSave = "/Users/JC/Documents/HKUST/Research/CDDP/LPSOLVER.tar/cofeatures/20150610_FS_S15_";
		double cutoff = 0.05;
		new CoFeautureGeneratorFromLPResult().run(args,cutoff,pathToSave+ cutoff + ".txt");
		/*DecimalFormat dec = new DecimalFormat("0.00");
		for(int i=1;i<=9;i++){
			cutoff = 0.1*i;
			new CoFeautureGeneratorFromLPResult().run(args,cutoff,pathToSave+ dec.format(cutoff) + ".txt");
		}*/
	}
}
