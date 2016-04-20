package hk.ust.cse.ipam.jc.crossprediction.data;

public class ProjectGroupInfo{
	public String dirPath;
	public String labelName;
	public String posLabel;
	public String[] projects;
	
	public ProjectGroupInfo(String dirPath,String labelName,String posLabel,String[] projects){
		this.dirPath = dirPath;
		this.labelName = labelName;
		this.posLabel = posLabel;
		this.projects = projects;
	}
}