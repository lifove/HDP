package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class WinTieLossByUtestTest {

	@Test
	public void test() {
		String args[] = {"/Users/JC/Dropbox/Transfer/CDDP/results/20140926_wintieloss_hetro_10random.txt",
								"/Users/JC/Dropbox/Transfer/CDDP/results/AUC_from_common_features.csv"};
		try {
			new WinTieLossByUtest().run(args);
			
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
