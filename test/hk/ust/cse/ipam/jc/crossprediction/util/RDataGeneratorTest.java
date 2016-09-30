package hk.ust.cse.ipam.jc.crossprediction.util;

import static org.junit.Assert.*;

import org.junit.Test;

public class RDataGeneratorTest {

	@Test
	public void test() {
		
		String dataRoot = System.getProperty("user.home") + "/Documents/UW/HDP+/data/";
		
		String[] srcDataInfo = {"mc1",dataRoot + "NASA","Defective","Y"};
		String[] tarDataInfo = {"Safe",dataRoot + "Relink","isDefective","TRUE"};
		
		
		String srcAttrName = "HALSTEAD_VOLUME";
		String tarAttrName = "CountSemicolon";
		
		RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo, tarDataInfo, srcAttrName, tarAttrName);
		
		String[] tarDataInfo2 = {"ar3",dataRoot + "SOFTLAB","defects","true"};
		
		srcAttrName = "HALSTEAD_VOLUME";
		tarAttrName = "executable_loc";
		RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo, tarDataInfo2, srcAttrName, tarAttrName);
		
		String[] tarDataInfo3 = {"ar5",dataRoot + "SOFTLAB","defects","true"};
		RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo, tarDataInfo3, srcAttrName, tarAttrName);
		
		//ar1 >> camel-1.0	0.550	0.485	Loss	unique_operators>>amc(0.089)
		//ar1 >> cm1	0.653	0.280	Loss	halstead_level>>HALSTEAD_ERROR_EST(0.074)
		//ar1 >> kc3	0.609	0.320	Loss	halstead_level>>HALSTEAD_ERROR_EST(0.087)
		
		String[] srcDataInfo2 = {"ar1",dataRoot + "SOFTLAB","defects","true"};
		String[] tarDataInfo4 = {"camel-1.0",dataRoot + "CK","bug","buggy"};
		
		srcAttrName = "unique_operators";
		tarAttrName = "amc";
		RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo2, tarDataInfo4, srcAttrName, tarAttrName);
		
		String[] tarDataInfo5 = {"cm1",dataRoot + "NASA","Defective","Y"};
		
		srcAttrName = "halstead_level";
		tarAttrName = "HALSTEAD_ERROR_EST";
		RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo2, tarDataInfo5, srcAttrName, tarAttrName);
		
		String[] tarDataInfo6 = {"kc3",dataRoot + "NASA","Defective","Y"};
		
		srcAttrName = "halstead_level";
		tarAttrName = "HALSTEAD_ERROR_EST";
		RDataGenerator.GeneratePlotDataFile(dataRoot, srcDataInfo2, tarDataInfo6, srcAttrName, tarAttrName);
		
	}

}
