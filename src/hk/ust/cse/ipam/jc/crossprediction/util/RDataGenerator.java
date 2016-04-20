package hk.ust.cse.ipam.jc.crossprediction.util;

import hk.ust.cse.ipam.jc.crossprediction.data.ProjectGroupInfo;
import hk.ust.cse.ipam.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

import weka.core.Instances;

public class RDataGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArrayList<ProjectGroupInfo> pg = new ArrayList<ProjectGroupInfo>();
		
		/*String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo("data/AEEEM/", "class", "buggy", AEEEM);
		pg.add(projectGroupAEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo("data/Relink/", "isDefective", "TRUE", ReLink);
		pg.add(projectGroupRelink);
		
		String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		ProjectGroupInfo projectGroupMIM = new ProjectGroupInfo("data/MIM/", "class", "buggy", MIM);
		pg.add(projectGroupMIM);
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		ProjectGroupInfo projectGroupPROMISE = new ProjectGroupInfo("data/promise/", "bug", "buggy", PROMISE);
		pg.add(projectGroupPROMISE);
		
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		ProjectGroupInfo projectGroupNASA = new ProjectGroupInfo("data/NASA/", "Defective", "Y", NASA);
		pg.add(projectGroupNASA);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo("data/SOFTLAB/", "defects", "true", SOFTLAB);
		pg.add(projectGroupSOFTLAB);*/
		
		String[] efforts = {"albrecht"};
		ProjectGroupInfo projectGroupEffort = new ProjectGroupInfo("data/effort/", "label", "buggy", efforts);
		pg.add(projectGroupEffort);
		
		//String[] severity = {"pitsB"};
		//ProjectGroupInfo projectGroupSeverity = new ProjectGroupInfo("data/Severity/", "@@class@@", "buggy", severity);
		//pg.add(projectGroupSeverity);
		
		String[] medical = {"breast_cancer"};
		ProjectGroupInfo projectMedical = new ProjectGroupInfo("data/Medical/", "Class", "M", medical); //Cancer
		pg.add(projectMedical);
		
		String[] wine = {"winequality"};
		ProjectGroupInfo projectWine = new ProjectGroupInfo("data/Wine/", "quality", "buggy", wine);
		pg.add(projectWine);
		
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("Value\tType");
		for(ProjectGroupInfo pgi:pg){
			System.out.println(pgi.dirPath + "processing!...");
			for(String project:pgi.projects){
	
				String[] dataInfo = {project,pgi.dirPath,pgi.labelName,pgi.posLabel};
					
				lines = new RDataGenerator().run(dataInfo,lines);
			}
		}
		FileUtil.writeAFile(lines, "data/featureRdata.txt");
	}

	private ArrayList<String> run(String[] dataInfo,ArrayList<String> lines){
		String projectName = dataInfo[0];
		String dirPath = dataInfo[1];
		String labelName = dataInfo[2];
		String buggyLabel = dataInfo[3];
		String dataPath = dirPath + File.separator + projectName + ".arff";
		
		Instances srcInstances = Utils.loadArff(dataPath, labelName);
			
		// for source data
		for(int instIdx = 0; instIdx < srcInstances.numInstances();instIdx++){
			for(int attrIdx = 0; attrIdx < srcInstances.numAttributes();attrIdx++){
				if(attrIdx == srcInstances.classIndex())
					continue;
				
				double value = srcInstances.get(instIdx).value(srcInstances.attribute(attrIdx));
				String label = srcInstances.get(instIdx).classAttribute().value((int)srcInstances.get(instIdx).classValue()).equals(buggyLabel)? "buggy":"clean";
				String attrName = srcInstances.attribute(attrIdx).name();
				lines.add(value +"\t" + projectName + "_" + attrName + "_" + label);
				lines.add(value +"\t" + projectName + "_" + attrName);
			}
			
		}
	
		return lines;
	}
}
