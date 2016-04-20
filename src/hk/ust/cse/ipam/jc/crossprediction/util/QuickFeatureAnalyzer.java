package hk.ust.cse.ipam.jc.crossprediction.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.google.common.primitives.Doubles;

import hk.ust.cse.ipam.jc.crossprediction.data.ProjectGroupInfo;
import hk.ust.cse.ipam.utils.FileUtil;
import hk.ust.cse.ipam.utils.WekaUtils;
import weka.core.Instances;

public class QuickFeatureAnalyzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new QuickFeatureAnalyzer().run();
	}

	HashMap<String,ArrayList<String>> projectInfo = new HashMap<String,ArrayList<String>>();
	
	private void run() {
		
		setProjectInfo();
		
		System.out.println("prediction,cofeatures,AUC,srcFeatureName,tarFeatureName,srcCleanMean,srcCleanGeoMean,srcCleanSTD,srcCleanSK,srcCleanPK,srcBuggyMean,srcBuggyGeoMean,srcBuggySTD,srcBuggySK,srcBuggyPK,srcMean,srcGeoMean,srcSTD,srcSK,srcPK,tarMean,tarGeoMean,tarSTD,tarSK,tarPK");
		
		ArrayList<String> cofeatureInfo = FileUtil.getLines("data/UFS_0.90_cofeatures.txt", false);
		
		for(String line:cofeatureInfo){
			String[] splitLine = line.split("\t");
			String prediction = splitLine[0];
			String srcProjectName = prediction.split(">>")[0];
			String tarProjectName = prediction.split(">>")[1];
			String coFeatures = splitLine[1];
			String AUC = splitLine[2];
			
			Pattern pattern = Pattern.compile("\\((([a-zA-Z0-9_:-])+)\\)");
			Matcher matcher = pattern.matcher(coFeatures);
			
			while (matcher.find()) {
				String srcFeature = matcher.group(1);
				matcher.find();
				String tarFeature = matcher.group(1);
				
				//JDT>>ar5	UAnalyzer_0.90	1	|19(CvsExpEntropy)>>12(halstead_level)(0.91)
				String srcName = srcProjectName;
				String tarName = tarProjectName;
				
				String srcFeatureName = srcFeature;
				String tarFeatureName = tarFeature;
				
				String srcPath = projectInfo.get(srcProjectName).get(0) + srcName + ".arff";
				String srcLabelName = projectInfo.get(srcProjectName).get(1);
				String srcPosLabel = projectInfo.get(srcProjectName).get(2);
				
				String tarPath = projectInfo.get(tarProjectName).get(0) + tarName + ".arff";
				String tarLabelName = projectInfo.get(tarProjectName).get(1);
				
				Instances srcInstances = Utils.loadArff(srcPath, srcLabelName);
				Instances tarInstances = Utils.loadArff(tarPath, tarLabelName);
				
				int srcAttrIndex = srcInstances.attribute(srcFeatureName).index();
				double[] srcFeatureAllValues = srcInstances.attributeToDoubleArray(srcAttrIndex);
				double[] srcFeatureBuggyValues = WekaUtils.getValuesByClassValue(srcInstances, srcAttrIndex,srcPosLabel,true);
				double[] srcFeatureCleanValues = WekaUtils.getValuesByClassValue(srcInstances, srcAttrIndex,srcPosLabel,false);
				
				int tarAttrIndex = tarInstances.attribute(tarFeatureName).index();
				double[] tarFeatureAllValues = tarInstances.attributeToDoubleArray(tarAttrIndex);
				
				DescriptiveStatistics srcAllStat = new DescriptiveStatistics(srcFeatureAllValues);
				DescriptiveStatistics srCleanStat = new DescriptiveStatistics(srcFeatureCleanValues);
				DescriptiveStatistics srcBuggyStat = new DescriptiveStatistics(srcFeatureBuggyValues);
				DescriptiveStatistics tarAllStat = new DescriptiveStatistics(tarFeatureAllValues);
				
				double srcMean = srcAllStat.getMean();
				double srcGeoMean = srcAllStat.getGeometricMean();
				double srcSTD = srcAllStat.getStandardDeviation();
				double srcSK = srcAllStat.getSkewness();
				double srcPK = srcAllStat.getKurtosis();
				
				double srcCleanMean = srCleanStat.getMean() ;
				double srcCleanGeoMean = srCleanStat.getGeometricMean();
				double srcCleanSTD = srCleanStat.getStandardDeviation();
				double srcCleanSK = srCleanStat.getSkewness();
				double srcCleanPK = srCleanStat.getKurtosis();
				
				double srcBuggyMean = srcBuggyStat.getMean();
				double srcBuggyGeoMean = srcBuggyStat.getGeometricMean();
				double srcBuggySTD = srcBuggyStat.getStandardDeviation();
				double srcBuggySK = srcBuggyStat.getSkewness();
				double srcBuggyPK = srcBuggyStat.getKurtosis();
				
				double tarMean = tarAllStat.getMean();
				double tarGeoMean = tarAllStat.getGeometricMean();
				double tarSTD = tarAllStat.getStandardDeviation();
				double tarSK = tarAllStat.getSkewness();
				double tarPK = tarAllStat.getKurtosis();
				
				double[] srcUpperValues = getValuesByMean(srcMean, srcFeatureAllValues,true);
				double[] srcBelowValues = getValuesByMean(srcMean, srcFeatureAllValues,false);
				
				double[] tarUpperValues = getValuesByMean(tarMean, tarFeatureAllValues,true);
				double[] tarBelowValues = getValuesByMean(tarMean, tarFeatureAllValues,false);
				
				DescriptiveStatistics srcUpperStat = new DescriptiveStatistics(srcUpperValues);
				DescriptiveStatistics srcBelowStat = new DescriptiveStatistics(srcBelowValues);
				
				DescriptiveStatistics tarUpperStat = new DescriptiveStatistics(tarUpperValues);
				DescriptiveStatistics tarBelowStat = new DescriptiveStatistics(tarBelowValues);
				
				
				double srcUpperMean = srcUpperStat.getMean();
				double srcBelowMean = srcBelowStat.getMean();
				double tarUpperMean = tarUpperStat.getMean();
				double tarBelowMean = tarBelowStat.getMean();
				
				double upperScore = srcUpperMean>tarUpperMean?tarUpperMean/srcUpperMean:srcUpperMean/tarUpperMean;
				double belowScore = srcBelowMean>tarBelowMean?tarBelowMean/srcBelowMean:srcBelowMean/tarBelowMean;
				
				double twoModeMeanComparisonScore =  (upperScore + belowScore)/2;
				if(Double.isNaN(belowScore) || Double.isInfinite(belowScore))
					twoModeMeanComparisonScore = upperScore;
				
				//System.out.println("prediction,cofeatures,AUC,srcMean,srcGeoMean,srcSTD,srcSK,srcPK,tarMean,tarGeoMean,tarSTD,tarSK,tarPK");
				System.out.println(prediction + "," + coFeatures + "," + AUC +  "," +
									srcFeatureName + "," +
									tarFeatureName + "," +
									srcCleanMean + "," +
									srcCleanGeoMean + "," +
									srcCleanSTD + "," +
									srcCleanSK + "," +
									srcCleanPK + "," +
									srcBuggyMean + "," +
									srcBuggyGeoMean + "," +
									srcBuggySTD + "," +
									srcBuggySK + "," +
									srcBuggyPK + "," +
									srcMean + "," +
									srcGeoMean + "," +
									srcSTD + "," +
									srcSK + "," +
									srcPK + "," +
									tarMean + "," +
									tarGeoMean + "," +
									tarSTD + "," +
									tarSK + "," +
									tarPK + "," +
									twoModeMeanComparisonScore
						);
				
			}
		}
	}

	private double[] getValuesByMean(double mean,double[] values, boolean isUpper) {
	
		ArrayList<Double> newArray = new ArrayList<Double>();
		for(double value:values){
			if(isUpper && mean < value)
				newArray.add(value);
			if(!isUpper && mean >= value)
				newArray.add(value);
		}
		
		return Doubles.toArray(newArray);
	}

	private void setProjectInfo() {
		
		ArrayList<ProjectGroupInfo> pg = new ArrayList<ProjectGroupInfo>();
		
		String[] AEEEM = {"EQ", "JDT","LC","ML","PDE"};
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
		pg.add(projectGroupSOFTLAB);
		
		for(ProjectGroupInfo info:pg){
			String[] projects = info.projects;
			ArrayList<String> dataInfo = new ArrayList<String>();
			dataInfo.add(info.dirPath);
			dataInfo.add(info.labelName);
			dataInfo.add(info.posLabel);

			for(String project:projects){
				projectInfo.put(project, dataInfo);
			}
		}
	}

	private double computeFactor(DescriptiveStatistics stat) {
		return stat.getSkewness();
	}
}
