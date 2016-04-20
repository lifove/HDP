package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class UtestForAnalyzersTest {

	@Test
	public void test() {
		// the second parameter is for the field name in csv file such as Analyzer, Target, Group
		String args[] = {"/Users/JC/Dropbox/Transfer/CDDP/results/20150216_fs_s15_0.05_0.90_all.csv", "Analyzer", "true"};
		try {
			new UtestForAnalyzers().run(args);
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
