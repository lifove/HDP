package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.RList;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import hk.ust.cse.ipam.utils.FileUtil;
import hk.ust.cse.ipam.utils.WekaUtils;
import weka.core.Instances;


public class RDataGeneratorTest {

	HashMap<String,String> groupNames = new HashMap<String,String>();

	@Test
	public void test() {

		groupNames.put("EQ".toLowerCase(),"AEEEM");
		groupNames.put("JDT".toLowerCase(),"AEEEM");
		groupNames.put("LC".toLowerCase(),"AEEEM");
		groupNames.put("ML".toLowerCase(),"AEEEM");
		groupNames.put("PDE".toLowerCase(),"AEEEM");
		groupNames.put("Apache".toLowerCase(),"ReLink");
		groupNames.put("Safe".toLowerCase(),"ReLink");
		groupNames.put("Zxing".toLowerCase(),"ReLink");
		groupNames.put("ant-1.3","CK");
		groupNames.put("arc","CK");
		groupNames.put("camel-1.0","CK");
		groupNames.put("poi-1.5","CK");
		groupNames.put("redaktor","CK");
		groupNames.put("skarbonka","CK");
		groupNames.put("tomcat","CK");
		groupNames.put("velocity-1.4","CK");
		groupNames.put("xalan-2.4","CK");
		groupNames.put("xerces-1.2","CK");
		groupNames.put("CM1".toLowerCase(),"NASA");
		groupNames.put("MW1".toLowerCase(),"NASA");
		groupNames.put("PC1".toLowerCase(),"NASA");
		groupNames.put("PC3".toLowerCase(),"NASA");
		groupNames.put("PC4".toLowerCase(),"NASA");
		groupNames.put("JM1".toLowerCase(),"NASA");
		groupNames.put("PC2".toLowerCase(),"NASA");
		groupNames.put("PC5".toLowerCase(),"NASA");
		groupNames.put("MC1".toLowerCase(),"NASA");
		groupNames.put("MC2".toLowerCase(),"NASA");
		groupNames.put("KC3".toLowerCase(),"NASA");
		groupNames.put("ar1","SOFTLAB");
		groupNames.put("ar3","SOFTLAB");
		groupNames.put("ar4","SOFTLAB");
		groupNames.put("ar5","SOFTLAB");
		groupNames.put("ar6","SOFTLAB");

		String dataRoot = System.getProperty("user.home") + "/Documents/HDP/data/";
		
		/*String sourceGroup = "CK";
		String srcName = "ant-1.3";
		String targetGroup = "SOFTLAB";
		String tarName = "ar5";
		String matchedMetric = "rfc>>unique_operands()|";
		String[] srcLabelInfo =  getLabelInfo(sourceGroup);
		String[] tarLabelInfo =  getLabelInfo(targetGroup);
		String[] srcDataInfo = {srcName,dataRoot + sourceGroup,srcLabelInfo[0],srcLabelInfo[1]};
		String[] tarDataInfo = {tarName,dataRoot + targetGroup,tarLabelInfo[0],tarLabelInfo[1]};
		DrawBPlot(dataRoot, srcName, tarName, sourceGroup,targetGroup, srcDataInfo, tarDataInfo, matchedMetric);*/

		ArrayList<String> lines = FileUtil.getLines(dataRoot + "matched_metrics_KS_0.05_GainRatio.txt", false);

		for(String line:lines){

			String[] splitLine = line.split(",");
			String srcProject = splitLine[0];
			String tarProject = splitLine[1];

			String[] matchedMetrics = splitLine[2].split("\\|");

			String srcGroupName = groupNames.get(srcProject.toLowerCase());
			String tarGroupName = groupNames.get(tarProject.toLowerCase());

			String[] srcLabelInfo =  getLabelInfo(srcGroupName);
			String[] tarLabelInfo =  getLabelInfo(tarGroupName);


			String[] srcDataInfo = {srcProject,dataRoot + srcGroupName,srcLabelInfo[0],srcLabelInfo[1]};
			String[] tarDataInfo = {tarProject,dataRoot + tarGroupName,tarLabelInfo[0],tarLabelInfo[1]};

			for(String matchedMetric:matchedMetrics){
				DrawBPlot(dataRoot, srcProject, tarProject, srcGroupName, tarGroupName, srcDataInfo, tarDataInfo,
						matchedMetric);
			}
		}
	}

	private void DrawBPlot(String dataRoot, String srcProject, String tarProject, String srcGroupName,
			String tarGroupName, String[] srcDataInfo, String[] tarDataInfo, String matchedMetric) {
		String srcAttrName = matchedMetric.substring(0, matchedMetric.indexOf(">"));
		String tarAttrName = matchedMetric.substring(matchedMetric.lastIndexOf(">")+1, matchedMetric.indexOf("("));

		// TODO commented a already generated
		//RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo, tarDataInfo, srcAttrName, tarAttrName);

		String RDataFilePath =  dataRoot + "bplotdata/bplotdata_" + srcDataInfo[0] + "_" + srcAttrName + "_ "+ tarDataInfo[0] + "_" + tarAttrName + ".csv";
		String RPlotFilePath =  dataRoot + "bplots/" + srcDataInfo[0].toLowerCase() + "_" + tarDataInfo[0].toLowerCase() + "_" + srcAttrName + "_" + tarAttrName + ".png";
		
		long maxYAxis = getMaxYAxis(dataRoot + srcGroupName + "/" + srcProject,dataRoot + tarGroupName  +"/" + tarProject, srcAttrName, tarAttrName);
		
		// draw BPlots
		RConnection c;
		try {
			c = new RConnection();
			//REXP x = c.eval("R.version.string");
			//System.out.println(x.asString());
			c.eval("library(\"ggplot2\")");
			c.eval("random <- read.csv(\"" + RDataFilePath + "\", header=T)");
			
			//System.out.println("random <- read.csv(\"" + RDataFilePath + "\", header=T)");
			
			//c.eval("dev.copy(png,'myplot.png')");

			c.eval("pl <-qplot(subject, value, data = random, geom = \"boxplot\", fill=Instances)"
					+ " + xlab(\"Distribution\")"
					+ " + ylab(\"Metric values\")"
					+ " + ylim(values = c(0," + maxYAxis + "))"
					+ " + scale_fill_manual(values = c(\"#BBBBBB\", \"#000000\",\"#FFFFFF\"))"
					+ " + theme_bw()");
			//c.eval("print(pl)");
			c.eval("ggsave(\"" +RPlotFilePath +"\", plot = pl)");

		} catch (RserveException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		//} catch (REXPMismatchException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		} catch (REngineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private long getMaxYAxis(String src, String tar, String srcAttrName, String tarAttrName) {
		
		Instances srcInstances = WekaUtils.loadArff(src + ".arff");
		Instances tarInstances = WekaUtils.loadArff(tar + ".arff");
		
		double[] srcAttributeValues = srcInstances.attributeToDoubleArray(srcInstances.attribute(srcAttrName).index());
		double[] tarAttributeValues = tarInstances.attributeToDoubleArray(tarInstances.attribute(tarAttrName).index());
		
		DescriptiveStatistics srcStat = new DescriptiveStatistics(srcAttributeValues);	
		DescriptiveStatistics tarStat = new DescriptiveStatistics(tarAttributeValues);	
		double srcMax  = srcStat.getPercentile(100);
		double tarMax  = tarStat.getPercentile(100);
		
		double srcQ3  = srcStat.getPercentile(75);
		double tarQ3  = tarStat.getPercentile(75);
		
		long max = -1;
		if(srcMax>tarMax){
			max = Math.round(srcQ3*4);
			if(max>srcMax)
				max=Math.round(srcMax);
		}
		else{
			max = Math.round(tarQ3*4);
			if(max>tarMax)
				max=Math.round(tarMax);
		}
			
		return max==0?1:max;
	}

	private String[] getLabelInfo(String groupName) {

		String[] labelInfo = new String[2];

		String group = groupName;

		if(group.equals("ReLink")){
			labelInfo[0] = "isDefective";
			labelInfo[1] = "TRUE";
		}

		if(group.equals("NASA")){
			labelInfo[0] = "Defective";
			labelInfo[1] = "Y";
		}

		if(group.equals("AEEEM")){
			labelInfo[0] = "class";
			labelInfo[1] = "buggy";
		}

		if(group.equals("SOFTLAB")){
			labelInfo[0] = "defects";
			labelInfo[1] = "true";
		}

		if(group.equals("CK")){
			labelInfo[0] = "bug"; // bug or class
			labelInfo[1] = "buggy";
		}

		if(group.equals("MIM")){
			labelInfo[0] = "class"; // bug or class
			labelInfo[1] = "buggy";
		}

		return labelInfo;
	}

}
