package hk.ust.cse.ipam.jc.crossprediction;

import static org.junit.Assert.*;

import java.io.File;

import hk.ust.cse.ipam.jc.crossprediction.ifs.IFSTransformer;
import hk.ust.cse.ipam.utils.WekaUtils;

import org.junit.Test;

import weka.core.Instances;

public class CrossPredictionWitIFSDatasets {

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
		
		
		batchRunner(projectGroupAEEEM,projectGroupRelink);
		batchRunner(projectGroupAEEEM,projectGroupPROMISE);
		batchRunner(projectGroupAEEEM,projectGroupNASA);
		batchRunner(projectGroupAEEEM,projectGroupSOFTLAB);
		
		batchRunner(projectGroupRelink,projectGroupAEEEM);
		batchRunner(projectGroupRelink,projectGroupPROMISE);
		batchRunner(projectGroupRelink,projectGroupNASA);
		batchRunner(projectGroupRelink,projectGroupSOFTLAB);
		
		batchRunner(projectGroupPROMISE,projectGroupAEEEM);
		batchRunner(projectGroupPROMISE,projectGroupRelink);
		batchRunner(projectGroupPROMISE,projectGroupNASA);
		batchRunner(projectGroupPROMISE,projectGroupSOFTLAB);
		
		batchRunner(projectGroupNASA,projectGroupAEEEM);
		batchRunner(projectGroupNASA,projectGroupRelink);
		batchRunner(projectGroupNASA,projectGroupPROMISE);
		batchRunner(projectGroupNASA,projectGroupSOFTLAB);
		
		batchRunner(projectGroupSOFTLAB,projectGroupAEEEM);
		batchRunner(projectGroupSOFTLAB,projectGroupRelink);
		batchRunner(projectGroupSOFTLAB,projectGroupPROMISE);
		batchRunner(projectGroupSOFTLAB,projectGroupNASA);
	}

	private void batchRunner(ProjectGroupInfo sourceGroup,ProjectGroupInfo targetGroup) {
		
		for(String source:sourceGroup.projects){
			for(String target:targetGroup.projects){
				String predictionInfo = source + ">>" + target;
				String sourcePath = sourceGroup.dirPath + "ifs_" + source +".arff";
				String targetPath = targetGroup.dirPath + "ifs_" + target +".arff";
				String classAttributeName = WekaUtils.labelName;
				String posLabel = WekaUtils.strPos;
				int repeat = 500;
				int folds = 2;
				Instances sourceInstances = WekaUtils.loadArff(sourcePath, classAttributeName);
				Instances targetInstances = WekaUtils.loadArff(targetPath, classAttributeName);
				
				WekaUtils.crossPredictionOnTheSameSplit(predictionInfo,
						sourceInstances, targetInstances, posLabel, repeat, folds);

			}
		}
	}

}
