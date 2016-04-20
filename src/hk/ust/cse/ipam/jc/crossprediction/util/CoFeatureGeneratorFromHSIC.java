package hk.ust.cse.ipam.jc.crossprediction.util;

import hk.ust.cse.ipam.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;

public class CoFeatureGeneratorFromHSIC {
	final int ITER = 10;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CoFeatureGeneratorFromHSIC().run(args);
	}

	private void run(String[] args) {
		String dataPath = args.length==0?"data":args[0];
		String[] projects = {"Apache","Safe","Zxing",
									"EQ","JDT","LC","ML","PDE",
									"ar1","ar3","ar4","ar5","ar6",
									"cm1","mw1","pc1","pc3","pc4",
									"ant-1.3","poi-1.5","skarbonka","xalan-2.4","arc","tomcat","xerces-1.2","camel-1.0","redaktor","velocity-1.4"};
		
		//String[] projects = {"Apache","EQ"};
		
		ArrayList<String> finishedProject = new ArrayList<String>();
		
		for(int srcIdx = 0; srcIdx < projects.length;srcIdx++){
			for(int tarIdx = 0; tarIdx < projects.length;tarIdx++){
				// skip src and tar are the same project
				if(srcIdx==tarIdx)
					continue;
				// skip a project matched to all other projects once
				if(finishedProject.contains(projects[tarIdx]))
					continue;
				
				String src = projects[srcIdx];
				String tar = projects[tarIdx];

				// read file
				// iteration #, src feature index, tar feature index, testStat (HSIC), threshold
				ArrayList<String> lines,linesForNaN;
				
				lines = FileUtil.getLines(dataPath + File.separator + src + "_" + tar + "_FS.csv",false);
				
				if (lines.size()==0)
					continue;
				
				// final format: Q_Safe_ASAnalyzer:(13):0-0|0.11889309310071702,0-1|0.12204484752266301,0-2|0.10315340326919796,0-3|0.4026669809331229
				System.out.print(src + "_" + tar + "_MMD:(-1):");
				String reverseCombination = tar + "_" + src + "_MMD:(-1):";
				String curSrcFeatureIndex="0";
				String curTarFeatureIndex="0";
				double sumTestStat = 0;
				double sumThreshold = 0;
				for(String line:lines){
					String[] values = line.split(",");
					String srcFeatureIndex = values[1];
					String tarFeatureIndex = values[2];
					
					if(curSrcFeatureIndex.equals(srcFeatureIndex) && curTarFeatureIndex.equals(tarFeatureIndex)){
						double testStat = Double.parseDouble(values[3]);
						double threshold = Double.parseDouble(values[4]);
						
						sumTestStat += testStat;
						sumThreshold += threshold;
					}else{
						//System.out.print(curSrcFeatureIndex + "-" + curTarFeatureIndex +"|" + sumTestStat/ITER + "," + sumThreshold/ITER + ",");
						double avgTestStat = (Double.isNaN(sumTestStat/ITER)?0:sumTestStat/ITER);
						double avgThreshold = (Double.isNaN(sumThreshold/ITER)?0:sumThreshold/ITER);
						
						if(avgTestStat<=avgThreshold){
							avgTestStat = -1;
						}
							
						System.out.print(curSrcFeatureIndex + "-" + curTarFeatureIndex +"|" + avgTestStat + ",");
						reverseCombination += curTarFeatureIndex +"-" + curSrcFeatureIndex +"|" + avgTestStat + ",";
						sumTestStat = 0;
						sumThreshold = 0;
						
						
						
						sumTestStat += Double.parseDouble(values[3]);
						sumThreshold += Double.parseDouble(values[4]);
						
						curSrcFeatureIndex = srcFeatureIndex;
						curTarFeatureIndex = tarFeatureIndex;
					}
				}
				// for last pair
				//System.out.println(curSrcFeatureIndex + "-" + curTarFeatureIndex +"|" + sumTestStat/ITER + "," + sumThreshold/ITER + ",");	
				double avgTestStat = (Double.isNaN(sumTestStat/ITER)?0:sumTestStat/ITER);
				System.out.println(curSrcFeatureIndex + "-" + curTarFeatureIndex +"|" + avgTestStat + ",");	
				reverseCombination += curTarFeatureIndex + "-" + curSrcFeatureIndex +"|" + avgTestStat + ",";
				System.out.println(reverseCombination);
			}
			finishedProject.add(projects[srcIdx]);
		}
	}
}
