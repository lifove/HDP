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
		
		String dataRoot = System.getProperty("user.home") + "/Documents/UW/HDP+/";
		ArrayList<ProjectGroupInfo> pg = new ArrayList<ProjectGroupInfo>();
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
		ProjectGroupInfo projectGroupAEEEM = new ProjectGroupInfo(dataRoot + "data/AEEEM/", "class", "buggy", AEEEM);
		pg.add(projectGroupAEEEM);
		
		String[] ReLink = {"Apache","Safe","Zxing"};
		ProjectGroupInfo projectGroupRelink = new ProjectGroupInfo(dataRoot + "data/Relink/", "isDefective", "TRUE", ReLink);
		pg.add(projectGroupRelink);
		
		//String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		//ProjectGroupInfo projectGroupMIM = new ProjectGroupInfo("data/MIM/", "class", "buggy", MIM);
		//pg.add(projectGroupMIM);
		
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka",
			"tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		ProjectGroupInfo projectGroupPROMISE = new ProjectGroupInfo(dataRoot + "data/CK/", "bug", "buggy", PROMISE);
		pg.add(projectGroupPROMISE);
		
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		ProjectGroupInfo projectGroupNASA = new ProjectGroupInfo(dataRoot + "data/NASA/", "Defective", "Y", NASA);
		pg.add(projectGroupNASA);
		
		String[] NASA2 = {"jm1"};
		ProjectGroupInfo projectGroupNASA2 = new ProjectGroupInfo(dataRoot + "data/NASA/", "Defective", "Y", NASA2);
		pg.add(projectGroupNASA2);
		
		String[] NASA3 = {"pc2"};
		ProjectGroupInfo projectGroupNASA3 = new ProjectGroupInfo(dataRoot + "data/NASA/", "Defective", "Y", NASA3);
		pg.add(projectGroupNASA3);
		
		String[] NASA4 = {"pc5","mc1"};
		ProjectGroupInfo projectGroupNASA4 = new ProjectGroupInfo(dataRoot + "data/NASA/", "Defective", "Y", NASA4);
		pg.add(projectGroupNASA4);
		
		String[] NASA5 = {"mc2","kc3"};
		ProjectGroupInfo projectGroupNASA5 = new ProjectGroupInfo(dataRoot + "data/NASA/", "Defective", "Y", NASA5);
		pg.add(projectGroupNASA5);
		
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		ProjectGroupInfo projectGroupSOFTLAB = new ProjectGroupInfo(dataRoot + "data/SOFTLAB/", "defects", "true", SOFTLAB);
		pg.add(projectGroupSOFTLAB);
		
		/*String[] efforts = {"albrecht"};
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
		pg.add(projectWine);*/
		
		for(ProjectGroupInfo srcpg:pg){
			for(ProjectGroupInfo tarpg:pg){
				
				if(srcpg.equals(tarpg)) continue;
				
				
				
				System.out.println(srcpg.dirPath + " and " + tarpg.dirPath + " processing!...");
				
				for(String project:srcpg.projects){
					for(String tarProject:tarpg.projects){
		
						String[] dataInfo = {project,srcpg.dirPath,srcpg.labelName,srcpg.posLabel};
						String[] tarDataInfo = {tarProject,tarpg.dirPath,tarpg.labelName,tarpg.posLabel};
						
						Instances srcInstances = Utils.loadArff(srcpg.dirPath + File.separator + project + ".arff", srcpg.labelName);
						Instances tarInstances = Utils.loadArff(tarpg.dirPath + File.separator + tarProject + ".arff", tarpg.labelName);
						
						for(int srcAttrIdx = 0; srcAttrIdx < srcInstances.numAttributes();srcAttrIdx++){
							if(srcAttrIdx == srcInstances.classIndex()) continue;
							for(int tarAttrIdx = 0; tarAttrIdx <tarInstances.numAttributes();tarAttrIdx++){
								if(tarAttrIdx == tarInstances.classIndex()) continue;
						
								String srcAttrName = srcInstances.attribute(srcAttrIdx).name();
								String tarAttrName = tarInstances.attribute(tarAttrIdx).name();
								
								GeneratePlotDataFile(dataRoot, dataInfo, tarDataInfo,
										srcAttrName, tarAttrName);
							}
						}
					}
				}
			}
		}
		
	}

	public static void GeneratePlotDataFile(String dataRoot, String[] dataInfo, String[] tarDataInfo,
			String srcAttrName, String tarAttrName) {
		ArrayList<String> lines = new ArrayList<String>();
		lines.add("value,subject,Instances");
		lines = new RDataGenerator().run(dataInfo,lines,true,srcAttrName);
		lines = new RDataGenerator().run(tarDataInfo,lines,false,tarAttrName);
		FileUtil.writeAFile(lines, dataRoot + "bplotdata/bplotdata_" + dataInfo[0] + "_" + srcAttrName + "_ "+ tarDataInfo[0] + "_" + tarAttrName + ".csv");
	}

	private ArrayList<String> run(String[] dataInfo,ArrayList<String> lines,boolean isSource,String attrName){
		String projectName = dataInfo[0];
		String dirPath = dataInfo[1];
		String labelName = dataInfo[2];
		String buggyLabel = dataInfo[3];
		String dataPath = dirPath + File.separator + projectName + ".arff";
		
		String strKind = isSource? "Source":"Target";
		
		Instances srcInstances = Utils.loadArff(dataPath, labelName);
			
		// for source data
		for(int instIdx = 0; instIdx < srcInstances.numInstances();instIdx++){	
				double value = srcInstances.get(instIdx).value(srcInstances.attribute(attrName));
				String label = srcInstances.get(instIdx).classAttribute().value((int)srcInstances.get(instIdx).classValue()).equals(buggyLabel)? "buggy":"clean";
				
				//lines.add(value +"\t" + projectName + "_" + attrName + "_" + label);
				//lines.add(value +"\t" + projectName + "_" + attrName);
				lines.add(value +"," + strKind +": " + projectName + " (" + attrName + ")," + label);
				lines.add(value +"," + strKind +": " + projectName + " (" + attrName + "),all");
		}
	
		return lines;
	}
}
