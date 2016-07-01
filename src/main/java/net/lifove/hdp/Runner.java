package net.lifove.hdp;

import java.util.ArrayList;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import net.lifove.hdp.util.Utils;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.core.Instances;

public class Runner {

	String sourcePath;
	String targetPath;
	String srclabelName;
	String srcPosLabelValue;
	String tarlabelName;
	String tarPosLabelValue;
	double cutoff = 0.05;
	boolean help = false;
	boolean suppress = false;
	int numThreads = 4;

	String resultString = "";

	public static void main(String[] args) {
		new Runner().runner(args);
	}

	void runner(String[] args) {
		conductHDP(args,true);
	}

	private void conductHDP(String[] args,boolean printOutResult) {
		Options options = createOptions();

		if(parseOptions(options, args)){
			if (help){
				printHelp(options);
				return;
			}

			// load source and target arff files
			// load an arff file
			Instances source = Utils.loadArff(sourcePath, srclabelName);
			Instances target = Utils.loadArff(targetPath, tarlabelName);

			resultString = doHDP(printOutResult, source, target,srclabelName,srcPosLabelValue,tarlabelName,tarPosLabelValue,cutoff,suppress);
		}
	}

	public String doHDP(boolean printOutResult, Instances origSource, Instances origTarget,
			String srclabelName, String srcPosLabelValue,
			String tarlabelName, String tarPosLabelValue, ArrayList<String> matchedMetrics, double cutoff, boolean suppress) {
		String resultString = "";
		if(origSource!=null && origTarget!=null){
			origSource = new MetricSelector(origSource).getNewInstances();

			resultString = doHDP(printOutResult, origSource, origTarget, srclabelName, srcPosLabelValue, tarlabelName,
					tarPosLabelValue, matchedMetrics, suppress, resultString);
		}

		return resultString;
	}

	private String doHDP(boolean printOutResult, Instances origSource, Instances origTarget, String srclabelName,
			String srcPosLabelValue, String tarlabelName, String tarPosLabelValue, ArrayList<String> matchedMetrics,
			boolean suppress, String resultString) {
		// generate new datasets
		if(matchedMetrics.size()>0){
			Instances source = Utils.getNewInstancesByMatchedMetrics(origSource, matchedMetrics, true, srclabelName, srcPosLabelValue);
			Instances target = Utils.getNewInstancesByMatchedMetrics(origTarget, matchedMetrics, false, tarlabelName, tarPosLabelValue);

			//String mlAlgorithm = "weka.classifiers.functions.Logistic";
			int posClassValueIndex = source.attribute(source.classIndex()).indexOfValue(Utils.strPos);
			try {
				Classifier classifier = (Classifier) new Logistic();//weka.core.Utils.forName(Classifier.class, mlAlgorithm, null);
				classifier.buildClassifier(source);

				if(target.attributeStats(target.classIndex()).nominalCounts[1]!=0){			

					if(!suppress && printOutResult)
						Utils.printPredictionResultForEachInstance(target, classifier);

					Evaluation eval = new Evaluation(source);
					eval.evaluateModel(classifier, target);

					if(printOutResult){
						System.out.println("AUC: " + eval.areaUnderROC(posClassValueIndex));
						System.out.println("Precision: " + eval.precision(posClassValueIndex));
						System.out.println("Recall: " + eval.recall(posClassValueIndex));
						System.out.println("F1: " + eval.fMeasure(posClassValueIndex));
					}

					resultString = eval.precision(posClassValueIndex) + "," + eval.recall(posClassValueIndex) + "," +
							eval.fMeasure(posClassValueIndex) + "," + eval.areaUnderROC(posClassValueIndex) + "," + 
							MetricMatcher.getStrMatchedMetrics(origSource,origTarget,matchedMetrics);

				}else{
					Utils.printPredictionResultForEachInstance(target, classifier);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else{
			System.err.println("There are no matched metrics! Source and target datasets are too different to do HDP!" + sourcePath +"," + targetPath);
		}
		return resultString;
	}

	public String doHDP(boolean printOutResult, Instances origSource, Instances origTarget,
			String srclabelName, String srcPosLabelValue,
			String tarlabelName, String tarPosLabelValue, double cutoff, boolean suppress) {
		String resultString = "";
		if(origSource!=null && origTarget!=null){
			origSource = new MetricSelector(origSource).getNewInstances();
			ArrayList<String> matchedMetrics = new MetricMatcher(origSource,origTarget,cutoff,numThreads).match();

			resultString = doHDP(printOutResult, origSource, origTarget, srclabelName, srcPosLabelValue, tarlabelName,
					tarPosLabelValue, matchedMetrics, suppress, resultString);
		}

		return resultString;
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

		options.addOption(Option.builder("n").longOpt("numthread")
				.desc("The number of threads when computing metric matching scores. Default is 4.")
				.hasArg()
				.argName("number")
				.build());

		options.addOption(Option.builder("r").longOpt("suppress")
				.desc("Suppress detailed prediction results. Only works when the arff data is labeled.")
				.build());

		options.addOption(Option.builder("sl").longOpt("srclable")
				.desc("Label (Class attrubite) name")
				.hasArg()
				.argName("source attribute name")
				.required()
				.build());

		options.addOption(Option.builder("tl").longOpt("tarlable")
				.desc("Label (Class attrubite) name")
				.hasArg()
				.argName("target attribute name")
				.required()
				.build());

		options.addOption(Option.builder("sp").longOpt("srcposlabel")
				.desc("String value of buggy label in source data.")
				.hasArg()
				.required()
				.argName("attribute value")
				.build());

		options.addOption(Option.builder("tp").longOpt("tarposlabel")
				.desc("String value of buggy label in taget data. If the data file is labeled, "
						+ "it will show prediction results in terms of precision, recall, and f-measure for evaluation puerpose.")
				.hasArg()
				.required()
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
			srclabelName = cmd.getOptionValue("sl");
			srcPosLabelValue = cmd.getOptionValue("sp");
			tarlabelName = cmd.getOptionValue("tl");
			tarPosLabelValue = cmd.getOptionValue("tp");
			if(cmd.getOptionValue("c") != null)
				cutoff = Double.parseDouble(cmd.getOptionValue("c"));
			if(cmd.getOptionValue("n") != null)
				numThreads = Integer.parseInt(cmd.getOptionValue("n"));

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

	public String getStringHDPResult(String[] args) {
		conductHDP(args,false);
		return resultString;
	}
}
