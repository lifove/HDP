package hk.ust.cse.ipam.jc.crossprediction;

import static org.junit.Assert.*;

import org.junit.Test;

public class DriverTest {

	@Test
	public void testRun() {
		//$numOfThreads $saveNewData $cofeature_prefix\_$cutoff.txt $analyzers $cutoff $cutoff 0.05 $algorithms $fsOption $verbose
		String[] args = {"1","false","../../../LPSOLVER.tar/cofeatures/20150610_FS_S15_0.05.txt","KSAnalyzer","0.05","0.05","0.05","weka.classifiers.functions.Logistic","shiv_s0.15","true"};
		Driver.main(args);
	}

}
