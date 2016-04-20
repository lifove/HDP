package hk.ust.cse.ipam.jc.crossprediction.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import hk.ust.cse.ipam.utils.FileUtil;

public class LpSolveModelGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new LpSolveModelGenerator().run(args,0.00);

	}
	
	void run(String args[],double cutoff){
		String name = args[0];
		String dirName = args[1] + File.separator + name;
		String fullDirName = dirName; //"LPModelsP";
		String cofeatureFile = args[2];// "data/cofeatures_20140729_PAnalyzer_FS_FILTER_All_Matched.txt";
		String dirForLPResults = args[3];// resultsFSP
		String shFile = args[4] + File.separator + name +".sh"; // lp run shell file
		
		File createdDir = new File(dirName);
		if(!createdDir.exists()){
			if(!(new File(dirName).mkdirs())){
				System.err.println(dirName +" is not created");
				System.exit(0);
			}
		}
		
		createdDir = new File(dirForLPResults);
		if(!createdDir.exists()){
			if(!(new File(dirForLPResults).mkdirs())){
				System.err.println(dirForLPResults +" is not created");
				System.exit(0);
			}
		}
		
		ArrayList<String> lines = FileUtil.getLines(cofeatureFile, false);
		
		DecimalFormat dec = new DecimalFormat("0.00");
		
		try {
			File file= new File(shFile);
			FileOutputStream fos = new FileOutputStream(file);
			DataOutputStream dos=new DataOutputStream(fos);
		 
			int processCount=1;
			for(String line:lines){
				String[] splitLine = line.split(":");
				String predCombi = splitLine[0];
				
				line = "./lp_solve -S3 " + fullDirName + File.separator + predCombi + "_" + dec.format(cutoff) + ".lp > " + dirForLPResults + File.separator + predCombi + "_" + dec.format(cutoff) + ".txt";
				dos.write((line+"\n").getBytes());
				
				if(processCount%1000 == 1)
					System.out.println(processCount);
				
				++processCount;
				
				ArrayList<String> LPModelLines = new ArrayList<String>();
				
				boolean existMatched = splitLine.length>2;
				// ignore when there is no matched features
				if(!existMatched)
					continue;
				
				String[] edges = splitLine[2].split(",");
				
				ArrayList<String> c = new ArrayList<String>();
				HashMap<String,ArrayList<Integer>> edgesIncidentToVertices = new HashMap<String,ArrayList<Integer>>(); // String vertexID
				
				for(int i=0;i<edges.length;i++){
					String[] splitEdgeString = edges[i].split("\\|");
					
					String edgeID = splitEdgeString[0];
					
					Double matchingScore = Double.parseDouble(splitEdgeString[1]);
					
					// only consider matching scores greater than a cutoff
					if(matchingScore<=cutoff)
						continue;
					
					c.add(matchingScore + "");
					
					String[] vertexIDs = edgeID.split("-");
					String srcVertexID = "s" + vertexIDs[0];
					String tarVertexID = "t" + vertexIDs[1];
					
					if(edgesIncidentToVertices.containsKey(srcVertexID))
						edgesIncidentToVertices.get(srcVertexID).add(i);
					else{
						edgesIncidentToVertices.put(srcVertexID, new ArrayList<Integer>());
						edgesIncidentToVertices.get(srcVertexID).add(i);
					}
					
					if(edgesIncidentToVertices.containsKey(tarVertexID))
						edgesIncidentToVertices.get(tarVertexID).add(i);
					else{
						edgesIncidentToVertices.put(tarVertexID, new ArrayList<Integer>());
						edgesIncidentToVertices.get(tarVertexID).add(i);
					}		
				}
				
				String objectiveFunction = "max: ";
				for(int i=0;i<c.size();i++){
					objectiveFunction = objectiveFunction + " " + c.get(i) + " x" + i ;
					
					if(i<c.size()-1)
						objectiveFunction = objectiveFunction + " + ";
				}
				objectiveFunction = objectiveFunction + ";";
				
				LPModelLines.add(objectiveFunction);
				
				String constraints = "";
				
				for(String key:edgesIncidentToVertices.keySet()){
					ArrayList<Integer> incidentIndices = edgesIncidentToVertices.get(key);
					
					for(int i=0; i< incidentIndices.size(); i++){
						constraints = constraints + "x" + incidentIndices.get(i);
						
						if(i<incidentIndices.size()-1)
							constraints = constraints + " + ";
					}
					constraints = constraints + "<=1;\n";
				}
				
				LPModelLines.add(constraints);
				
				String otherConstraints = "";
				for(int i=0;i<edges.length;i++){
					otherConstraints = otherConstraints + "x" + i + ">=0;\n";
				}
				
				LPModelLines.add(otherConstraints);
				
				FileUtil.writeAFile(LPModelLines, dirName + "/" + predCombi + "_" + dec.format(cutoff) + ".lp");
			}
			
			dos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
