package hk.ust.cse.ipam.jc.crossprediction;

import static org.junit.Assert.*;
import hk.ust.cse.ipam.jc.crossprediction.ifs.IFSTransformer;

import org.junit.Test;

public class IFSTest {

	@Test
	public void testRun() {
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo("data/AEEEM/", "class", "buggy", AEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", ReLink);
		
		String[] Apache = {"Apache"};
		ProjectGroupInfo apache = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", Apache);
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		ProjectGroupInfo projectGroupPROMISE = new ProjectGroupInfo("data/promise/", "bug", "buggy", PROMISE);
		
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		ProjectGroupInfo projectGroupNASA = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", SOFTLAB);
		
		
		batchRunner(projectGroupAEEEM);
		batchRunner(projectGroupRelink);
		batchRunner(projectGroupPROMISE);
		batchRunner(projectGroupNASA);
		batchRunner(projectGroupSOFTLAB);
	}

	private void batchRunner(ProjectGroupInfo projectGroup) {
		for(String project:projectGroup.projects){
			String[] args = {projectGroup.dirPath, project +".arff",projectGroup.labelName,projectGroup.posLabel}; 
		IFSTransformer.main(args);
		}
	}

}
