package hk.ust.cse.ipam.jc.crossprediction.util;

import hk.ust.cse.ipam.utils.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import weka.core.Instances;

public class RScriptGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ArrayList<String> lines = FileUtil.getLines("data/cofeatures_ASWF_0.9.txt", false);
		
		System.out.println("library(beanplot)");
		System.out.println("data <- read.table(\"featureRdata.txt\", header=T)");
		
		for(String line:lines){
			String[] splitLine = line.split("\t");
			String srcProject = splitLine[0].split(">>")[0];
			String tarProject = splitLine[0].split(">>")[1];
			String cofeatures = splitLine[1];
			String crossAUC = splitLine[2];
			new RScriptGenerator().run(srcProject,tarProject, cofeatures, crossAUC);
		}
	}

	private void run(String srcProjectName,String tarProjectName, String cofeatures,String crossAUC) {
		String src = srcProjectName;	
		String tar = tarProjectName;
	
				
		Pattern pattern = Pattern.compile("\\((([a-zA-Z0-9_:-])+)\\)");
		Matcher matcher = pattern.matcher(cofeatures);
		
		Pattern pattern2 = Pattern.compile("\\((([0-9\\.])+)\\)");
		Matcher matcher2 = pattern2.matcher(cofeatures);
		
		System.out.println("pdf(file=\"" + crossAUC + "_" + src + "_" +tar + "_beanplots.pdf\", w=10, h=10)");
		System.out.println("par(mar=c(7.1,4,2,1))");
		
		int i=1;
		while (matcher.find()) {
			String srcFeature = matcher.group(1);
			matcher.find();
			String tarFeature = matcher.group(1);
			matcher2.find();
			String cutoff = matcher2.group(1);
			
			System.out.println("ordered = c(\"" + src + "_" + srcFeature + "_clean\",\"" +
									tar + "_" + tarFeature + "_clean\",\"" +
									src + "_" + srcFeature + "_buggy\",\"" +
									tar + "_" + tarFeature + "_buggy\",\"" +
									src + "_" + srcFeature + "\",\"" +
									tar + "_" + tarFeature + "\")");
			System.out.println("d <- data");
			System.out.println("d$Type <- factor(d$Type, levels=ordered)");
			
			System.out.println("p <- beanplot(Value ~ Type, data=d, bw=\"bcv\", what=c(0,1,1,0)," +
									"border=NA, side=\"both\", col=list(\"#DDDDDD\", \"#BBBBBB\"), show.names=F)");
			
			
			System.out.println("labels= c(\"" + src + "_" + tar + "_" + srcFeature + "_" + tarFeature + "_clean\", \"" + src + "_" + tar + "_" + srcFeature + "_" + tarFeature + "_buggy\",\"" +
									srcFeature + "_" + tarFeature + "_" + src + ">>" + tar +"\")");
			
			System.out.println("for(i in 1:length(labels)){");
			System.out.println("	 text(0.9+(i-1), -10.0, labels=c(labels[i]), adj=1, srt=45, xpd=T)");
			System.out.println("}\n");
			
			
			i++;
		}
		
		System.out.println("dev.off()");
	}
}