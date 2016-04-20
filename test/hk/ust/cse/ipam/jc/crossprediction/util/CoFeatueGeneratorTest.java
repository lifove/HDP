package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;
import hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator;
import hk.ust.cse.ipam.jc.crossprediction.CoFeatureGeneratorNoneThreads;

import java.io.IOException;

import org.junit.Test;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

public class CoFeatueGeneratorTest {

	@Test
	public void test() {
		String[] args = {"4", "KSAnalyzer", "false", "false","0.00","shiv_s0.15", "data/cofeatures__All_Matched_for_fs_shiv_s0.15_2_fold_ing.txt", "2","500"};

		//CoFeatureGeneratorNoneThreads.main(args);
		CoFeatureGenerator.main(args);
	}
}
