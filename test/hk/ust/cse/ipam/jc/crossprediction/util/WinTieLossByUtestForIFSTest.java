package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

public class WinTieLossByUtestForIFSTest {

	@Test
	public void test() {
		String args[] = {"/Volumes/Grace/DownHDD/Dropbox/Transfer/CDDP/results/20150216_fs_s15.txt",
							"/Volumes/Grace/DownHDD/Dropbox/Transfer/CDDP/results/IFS_results.txt","shiv_s0.15","true"};
		
		try {
			new WinTieLossByUtestForIFS().run(args);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
