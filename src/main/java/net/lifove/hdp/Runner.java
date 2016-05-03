package net.lifove.hdp;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Runner {
	
	String sourcePath;
	String targetPath;
	String labelName;
	String posLabelValue;
	double cutoff = 0.05;
	boolean help = false;
	boolean suppress = false;

	public static void main(String[] args) {
		new Runner().runner(args);
	}

	void runner(String[] args) {
		
		Options options = createOptions();
		
		if(parseOptions(options, args)){
			if (help){
				printHelp(options);
				return;
			}
		}
	}
	
	Options createOptions(){
		
		// create Options object
		Options options = new Options();
		
		// add options
		options.addOption(Option.builder("s").longOpt("source")
		        .desc("Source arff file path to train a prediciton model")
		        .hasArg()
		        .argName("file")
		        .required()
		        .build());
		
		options.addOption(Option.builder("t").longOpt("target")
		        .desc("Target arff file path to predict")
		        .hasArg()
		        .argName("file")
		        .required()
		        .build());
		
		options.addOption(Option.builder("h").longOpt("help")
		        .desc("Help")
		        .build());
		
		options.addOption(Option.builder("c").longOpt("cutoff")
		        .desc("Cutoff for KSAnalyzer. Default is 0.05.")
		        .hasArg()
		        .argName("cutoff")
		        .build());
		
		options.addOption(Option.builder("r").longOpt("suppress")
		        .desc("Suppress detailed prediction results. Only works when the arff data is labeled.")
		        .build());
		
		options.addOption(Option.builder("l").longOpt("lable")
		        .desc("Label (Class attrubite) name")
		        .hasArg()
		        .argName("attribute name")
		        .required()
		        .build());
		
		options.addOption(Option.builder("p").longOpt("poslabel")
		        .desc("String value of buggy label. Since CLA/CLAMI works for unlabeld data (in case of weka arff files, labeled as '?',"
		        		+ " it is not necessary to use this option. "
		        		+ "However, if the data file is labeled, "
		        		+ "it will show prediction results in terms of precision, recall, and f-measure for evaluation puerpose.")
		        .hasArg()
		        .argName("attribute value")
		        .build());

		return options;
	}
	
	boolean parseOptions(Options options,String[] args){

		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			sourcePath = cmd.getOptionValue("s");
			targetPath = cmd.getOptionValue("t");
			labelName = cmd.getOptionValue("l");
			posLabelValue = cmd.getOptionValue("p");
			if(cmd.getOptionValue("c") != null)
				cutoff = Double.parseDouble(cmd.getOptionValue("c"));
			help = cmd.hasOption("h");
			suppress = cmd.hasOption("r");

		} catch (Exception e) {
			printHelp(options);
			return false;
		}

		return true;
	}
	
	private void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		String header = "Execute heterogeneous defect prediction. On Windows, use HDP.bat instead of ./HDP";
		String footer ="\nPlease report issues at https://github.com/lifove/HDP/issues";
		formatter.printHelp( "./HDP", header, options, footer, true);
	}
}
