package hk.ust.cse.ipam.jc.crossprediction;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import hk.ust.cse.ipam.utils.FileUtil;

public class TCAPlusDecisionMaker {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new TCAPlusDecisionMaker().run(args);
	}
	
	String analyzer="AL1DAnalyzer_0.55";
	
	void run(String[] args){
		
		String dataNumInstances = "data" + File.separator + "tcaPlusNumInstances.txt";
		String sourceTCAPlus = "data" + File.separator + "tcaPlusSourceEachProject.txt";
		String targetTCAPlus = "data" + File.separator + "tcaPlusTargetEachProject.txt";
		
		ArrayList<String> infoNumInstances = FileUtil.getLines(dataNumInstances, false);
		ArrayList<String> infoSource = FileUtil.getLines(sourceTCAPlus, false);
		ArrayList<String> infoTarget = FileUtil.getLines(targetTCAPlus, false);
		
		HashMap<String,Integer> numInstances = getMapForNumInstances(infoNumInstances);
		HashMap<String,DataInfo> sourceInfo = getMapForDataInfo(infoSource);
		HashMap<String,DataInfo> targetInfo = getMapForDataInfo(infoTarget);
		
		String[] sources = {"AEEEM", "ReLink", "MIMAll", "PROMISE", "NASA", "SOFTLAB"};
		String[] AEEEM = {"EQ","JDT", "LC","ML","PDE"};
		ArrayList<String[]> targetGroups = new ArrayList<String[]>();
		targetGroups.add(AEEEM);
		String[] ReLink = {"Apache","Safe","Zxing"};
		targetGroups.add(ReLink);
		String[] MIM = {"MIMEtc","MIMMylyn","MIMTeam"};
		targetGroups.add(MIM);
		String[] PROMISE = {"ant-1.3","arc","camel-1.0","poi-1.5","redaktor","skarbonka","tomcat","velocity-1.4","xalan-2.4","xerces-1.2"};
		targetGroups.add(PROMISE);
		String[] NASA = {"cm1","mw1","pc1","pc3","pc4"};
		targetGroups.add(NASA);
		String[] SOFTLAB = {"ar1","ar3","ar4","ar5","ar6"};
		targetGroups.add(SOFTLAB);
		
		for (int srcGroupIdx = 0; srcGroupIdx<targetGroups.size();srcGroupIdx++){
			for(int tarGroupIdx=0;tarGroupIdx<targetGroups.size();tarGroupIdx++){
				if (srcGroupIdx==tarGroupIdx)
					continue;
				for(int srcIdx=0; srcIdx < targetGroups.get(srcGroupIdx).length;srcIdx++)
				for(int tarIdx=0; tarIdx < targetGroups.get(tarGroupIdx).length;tarIdx++){
					String source = targetGroups.get(srcGroupIdx)[srcIdx];
					String target = targetGroups.get(tarGroupIdx)[tarIdx];
					String key = source +"_to_" + target + "_" + analyzer;
					double srcMean = sourceInfo.get(key).mean;
					double srcMedian = sourceInfo.get(key).median;
					double srcMin = sourceInfo.get(key).min;
					double srcMax = sourceInfo.get(key).max;
					double srcStd = sourceInfo.get(key).std;
					int srcNumInstances = numInstances.get(source);
					
					double tarMean = targetInfo.get(key).mean;
					double tarMedian = targetInfo.get(key).median;
					double tarMin = targetInfo.get(key).min;
					double tarMax = targetInfo.get(key).max;
					double tarStd = targetInfo.get(key).std;
					int tarNumInstances = numInstances.get(target);
					
					String selectedNormOption = "N2";
					
					// Rule1: if mean and std is same >> NoN
					if((srcMean*0.9<=tarMean && tarMean <= srcMean*1.1) && (srcStd*0.9<=tarStd && tarStd <= srcStd*1.1))
						selectedNormOption = "NoN";
					
					// Rule2: if mean max numIstances are much less or much more
					if(tarMin<srcMin*0.4 || tarMin>srcMin*1.6 ||
							tarMax<srcMax*0.4 || tarMax>srcMax*1.6 ||
						tarNumInstances<srcNumInstances*0.4 || tarNumInstances>srcNumInstances*1.6
					)
						selectedNormOption = "N1";
					
					// Rule3: if std is much more or std is much less
					if((tarStd>srcStd*1.6 && tarNumInstances<srcNumInstances*0.9) || (tarStd<srcStd*0.4 && tarNumInstances>srcNumInstances*1.1))
						selectedNormOption = "N3";
					
					// Rule4: if std is much more or std is much less
					if((tarStd>srcStd*1.6 && tarNumInstances>srcNumInstances*1.1) || (tarStd>tarStd*1.6 && tarNumInstances<srcNumInstances*0.9))
						selectedNormOption = "N4";
					
					System.out.println(source + ">>" + target + "," +
							srcMean + "," + srcMedian + "," + srcMin + "," + srcMax + "," + srcStd + "," + srcNumInstances + "," + 
							tarMean + "," + tarMedian + "," + tarMin + "," + tarMax + "," + tarStd + "," + tarNumInstances + "," + selectedNormOption);
				}
			}
		}
	}
	
	HashMap<String,Integer> getMapForNumInstances(ArrayList<String> lines){
		HashMap<String,Integer> map = new HashMap<String,Integer>();
		
		for(String line:lines){
			String[] splits = line.split(",");
			map.put(splits[0],Integer.parseInt(splits[1])+Integer.parseInt(splits[2]));
		}
		return map;
	}
	
	HashMap<String,DataInfo> getMapForDataInfo(ArrayList<String> lines){
		HashMap<String,DataInfo> map = new HashMap<String,DataInfo>();
		
		for(String line:lines){
			String[] splits = line.split(",");
			map.put(splits[0],new DataInfo(splits));
		}
		return map;
	}

}

class DataInfo{
	String projectName;
	double std;
	double mean;
	double max;
	double min;
	double median;
	
	DataInfo(String[] info){
		this.projectName = info[0];
		
		this.mean = Double.parseDouble(info[1]);
		this.median = Double.parseDouble(info[2]);
		this.min = Double.parseDouble(info[3]);
		this.max = Double.parseDouble(info[4]);
		this.std = Double.parseDouble(info[5]);
	}
}
