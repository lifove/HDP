package hk.ust.cse.ipam.jc.crossprediction;

public class ProjectGroupInfo{
	String dirPath;
	String labelName;
	String posLabel;
	String[] projects;
	
	ProjectGroupInfo(String dirPath,String labelName,String posLabel,String[] projects){
		this.dirPath = dirPath;
		this.labelName = labelName;
		this.posLabel = posLabel;
		this.projects = projects;
	}
}
