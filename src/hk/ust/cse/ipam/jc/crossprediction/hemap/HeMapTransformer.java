package hk.ust.cse.ipam.jc.crossprediction.hemap;

import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.AttributeStats;
import weka.core.DenseInstance;
import weka.core.Instances;
import hk.ust.cse.ipam.jc.crossprediction.util.Utils;
import hk.ust.cse.ipam.utils.WekaUtils;
import hk.ust.cse.ipam.utils.MatrixUtil;

public class HeMapTransformer {
	public Instances source;
	public Instances target;
	public double[][] T;
	public double[][] S;
	double[][] A;
	boolean smallerSource = false;
	int increaseBy = 0;
	ArrayList<Integer> indiceOfAddedInstances = new ArrayList<Integer>();
	
	Instances newSourceInstances, newTargetInstances;
	
	/**
	 * @param args[] 0: source project name, 1: source data path, 2: source data label name, 3: source buggy label
	 * 4: target project name, 5: target data path, 6: target data label name, 7: target buggy label, 8: transformation type
	 */
	public static void main(String[] args) {
		new HeMapTransformer().run(args);
	}

	/**
	 * HeMap runner
	 * @param args[] 0: source data path, 1: source data label name, 2: target data path, 3: target data label name
	 */
	public void run(String[] args) {
		//source = Utils.loadArff("data/AEEEM/EQ.arff", "class");
		//target = Utils.loadArff("data/NASA/cm1.arff", "Defective");
		// EQ data/AEEEM/EQ.arff class buggy cm1 data/NASA/cm1.arff Defective Y
		String sourceProjectName = args[0];
		String sourceDataPath = args[1];
		String sourceLabel = args[2];
		String srcBuggyLabel = args[3];
		
		String targetProjectName = args[4];
		String targetDataPath = args[5];
		String targetLabel = args[6];
		String tarBuggyLabel = args[7];
		
		String transformationType = args[8];
		Instances originalSoruce = Utils.loadArff(sourceDataPath, sourceLabel);
		Instances originalTarget = Utils.loadArff(targetDataPath, targetLabel);
		
		EigenDecomposition U = null;
		int beta = 1;
		int k = 1;
		
		// repeat until Eigenvector found (random seed changes)
		int seed=0;
		boolean displayed = false;
		for(int i=0;i<1000;i++){
			
			source = new Instances(originalSoruce);
			target = new Instances(originalTarget);
			
			preprocessing(seed);
			seed++;
			// post-condition: the number of instances of source and target is same.
			
			// input
			S = WekaUtils.getMatrixFromInstances(source);
			T = WekaUtils.getMatrixFromInstances(target);
			
			RealMatrix matrixS = MatrixUtils.createRealMatrix(S);
			RealMatrix matrixT = MatrixUtils.createRealMatrix(T);
			
			// 1. construct Matrix A
			if(transformationType.equals("linear"))
				A = constructMatrixAByLinearTransformation(matrixS, matrixT, beta);
			else if(transformationType.equals("nonlinear")){
				double theta = 0.5;
				A = constructMatrixAByNonLinearTransformation(matrixS, matrixT, beta, theta);
			}
			
			// reset the class label since it may be unset while clustering part
			target.setClass(target.attribute(targetLabel));
			
			// 2. calculate the top-k eigenvalues of A and their corresponding eigenvectors
			if(!displayed){
				System.out.println("Finding Eigenvectors...");
				displayed = true;
			}
			
			try{
				U = new EigenDecomposition(MatrixUtils.createRealMatrix(A));
				break;
			}catch(MaxCountExceededException e){
				
			}
			
		}
		
		if(U==null){
			System.out.println("Cannot compute Eigenvectors after 1000 iterations!");
			System.exit(0);
		}
		
		// 3. Compute the matrix B_T and B_S
		RealMatrix matrixB_t = getB(U,k,"target");
		RealMatrix matrixB_s = getB(U,k,"source");
		
		// Generalization: select source instances by using clustering

		// Finalizing new datasets
		generatingNewInstances(sourceProjectName, sourceLabel, srcBuggyLabel,
				targetProjectName, targetLabel, tarBuggyLabel, k, matrixB_t,
				matrixB_s, transformationType);
		// post-condition newSourceInstances and newTargetInstances were assigned with the transformed data
		
		newSourceInstances = selectSourceInstances(newSourceInstances,newTargetInstances);
		
		if(isTransferringSourceFeasible(newSourceInstances)){
			String pathToSaveNewData = "data/HeMapData/";
			WekaUtils.writeADataFile(newSourceInstances, pathToSaveNewData + sourceProjectName + "_to_" + targetProjectName + "_" + transformationType + "_S.arff");
			WekaUtils.writeADataFile(newTargetInstances, pathToSaveNewData + sourceProjectName + "_to_" + targetProjectName + "_" + transformationType + "_T.arff");
		}
		else
			System.out.println("Trasferring source is not feasible on this prediction combination!!!");
	}

	private boolean isTransferringSourceFeasible(Instances newSrcInstances) {
				
		if(newSrcInstances.numInstances()==0)
			return false;
		
		AttributeStats stats = newSrcInstances.attributeStats(newSrcInstances.attribute(WekaUtils.labelName).index());
				
		if(stats.distinctCount<2)
			return false;
		
		return true;
	}

	private Instances selectSourceInstances(Instances source,
			Instances target) {
		
		// select source Instances by using clustering
		Instances mergedInstances = new Instances(newSourceInstances);
		
		int srcStartIndex = 0;
		int srcEndIndex = newSourceInstances.numInstances()-1;
		int tarStartIndex = srcEndIndex+1;
		int tarEndIndex = tarStartIndex + newTargetInstances.numInstances() - 1;
		
		mergedInstances.addAll(newTargetInstances);
		
		// do clustering
		// run clustering algorithm
		String[] options = new String[5];
		options[0] = "-I";                 // max. iterations
		options[1] = "10";
		options[2] = "-c";
		options[3] = "" + mergedInstances.classIndex();
		options[4] = "-O";

		mergedInstances.setClassIndex(-1);
		
		int numClusters = 0;

		SimpleKMeans clusterer = new SimpleKMeans();   // new instance of clusterer
		try {
			clusterer.setOptions(options);
			//TODO magic number 10
			clusterer.setNumClusters(10);
			clusterer.buildClusterer(mergedInstances);  
			numClusters = clusterer.numberOfClusters();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} 
		
		// remove source instances that are not realted to target instances
		ArrayList<Integer> instancesToBeRemoved = new ArrayList<Integer>();
		for(int cluster=0;cluster<numClusters;cluster++){
			int clusterSize = clusterer.getClusterSizes()[cluster];
			ArrayList<Integer> indice = getInstanceIndexInCluster(clusterer,cluster,srcStartIndex,srcEndIndex);
			int numSrcInstanceInCluster = indice.size();
			
			// consider there is no target instances in the cluster
			if(numSrcInstanceInCluster==clusterSize){
				instancesToBeRemoved.addAll(indice);
			}
		}
		// remove here!
		newSourceInstances.removeAll(instancesToBeRemoved);
		
		return newSourceInstances;
		
	}

	private ArrayList<Integer> getInstanceIndexInCluster(SimpleKMeans clusterer,
			int cluster, int srcStartIndex, int srcEndIndex) {
		
		ArrayList<Integer> indice = new ArrayList<Integer>();
		
		int[] clusterAssignments = null;
		try {
			clusterAssignments = clusterer.getAssignments();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int count = 0;
		for(int i=0;i<=srcEndIndex;i++){
			if(clusterAssignments[i]==cluster)
				indice.add(i);
		}
		
		return indice;
	}

	private double[][] constructMatrixAByNonLinearTransformation(
			RealMatrix matrixS, RealMatrix matrixT, int beta, double theta) {
		
		// 1. Generate partition matrix C_T and C_S
		double[][] Ct = getPartitionMatrixForTarget(target);
		RealMatrix matrixCt = MatrixUtils.createRealMatrix(Ct);
		
		// C_S: two clusters (buggy and clean)
		double[][] Cs = getPartitionMatrixForSource(source);
		RealMatrix matrixCs = MatrixUtils.createRealMatrix(Cs);
		
		// 2. Construct the matrix AHat
		
		// AHat1 = 2*theta^2*TT^T+(beta^2/2)*SS^T+(1-theta)(beta+2*theta)*C_t*C_t^T
		RealMatrix AHat1 = matrixT.multiply(matrixT.transpose()).scalarMultiply(2*theta*theta).add(matrixCt.multiply(matrixCt.transpose()).scalarMultiply((1-theta)*(beta+2*theta)));
		// AHat2 = AHat3 = beta*theta(TT^T + SS^T)
		RealMatrix AHat2 = matrixT.multiply(matrixT.transpose()).add(matrixS.multiply(matrixS.transpose())).scalarMultiply(beta*theta);
		RealMatrix AHat3 = AHat2;
		// 
		RealMatrix AHat4 = matrixS.multiply(matrixS.transpose()).scalarMultiply(2*theta*theta).add(matrixCs.multiply(matrixCs.transpose()).scalarMultiply((1-theta)*(beta+2*theta)));
		
		double[][] arrayAHat1 = AHat1.getData();
		double[][] arrayAHat2 = AHat2.getData();
		double[][] arrayAHat3 = AHat3.getData();
		double[][] arrayAHat4 = AHat4.getData();
		
		// AHat = AHat1 AHat2
		//     	  AHat3 AHat4
		// Compute AHat
		double[][] AHat12 = MatrixUtil.matrixConcatLR(arrayAHat1, arrayAHat2);
		double[][] AHat34 = MatrixUtil.matrixConcatLR(arrayAHat3, arrayAHat4);
		
		return MatrixUtil.matrixConcatUL(AHat12, AHat34);
	}

	private double[][] getPartitionMatrixForTarget(Instances instances) {
		
		int numClusters = 0;
		int numRows = instances.numInstances();

		// run clustering algorithm
		String[] options = new String[4];
		options[0] = "-I";                 // max. iterations
		options[1] = "10";
		options[2] = "-c";
		options[3] = "" + instances.classIndex();
		
		instances.setClassIndex(-1);
		
		SimpleKMeans clusterer = new SimpleKMeans();   // new instance of clusterer
		try {
			clusterer.setOptions(options);
			//TODO magic number 10
			clusterer.setNumClusters(10);
			clusterer.buildClusterer(instances);  
			numClusters = clusterer.numberOfClusters();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		} 
		
		double[][] M = new double[numRows][numClusters];
		
		for(int row=0;row<numRows;row++){
			for(int cluster=0;cluster<numClusters;cluster++){
				M[row][cluster]=0;
			}
			try {
				M[row][clusterer.clusterInstance(instances.get(row))] = 1;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return M;
	}

	private double[][] getPartitionMatrixForSource(Instances instances) {
		
		int numLabelValues = instances.classAttribute().numValues();
		int numRows = instances.numInstances();
		double[][] M = new double[numRows][numLabelValues];
		
		for(int i=0;i<numRows;i++){
			for(int col = 0;col < numLabelValues;col++){
				M[i][col]=0;
			}
			Double labelValue = (Double)instances.get(i).classValue();
			M[i][labelValue.intValue()] = 1;
		}
		
		return M;
	}

	private void generatingNewInstances(String sourceProjectName,
			String sourceLabel, String srcBuggyLabel, String targetProjectName,
			String targetLabel, String tarBuggyLabel, int k,
			RealMatrix matrixB_t, RealMatrix matrixB_s,String transformationType) {
		
		// new matrixes corresponding to the original instances
		if(increaseBy>0){
			if(smallerSource){
				double[][] newData = new double[source.numInstances()-increaseBy][matrixB_s.getColumnDimension()];
				matrixB_s.copySubMatrix(0, source.numInstances()-increaseBy-1, 0, matrixB_s.getColumnDimension()-1, newData);
				matrixB_s = MatrixUtils.createRealMatrix(newData);
			}
			else{
				double[][] newData = new double[target.numInstances()-increaseBy][matrixB_t.getColumnDimension()];
				matrixB_t.copySubMatrix(0, target.numInstances()-increaseBy-1, 0, matrixB_t.getColumnDimension()-1, newData);
				matrixB_t = MatrixUtils.createRealMatrix(newData);
			}
		}
		
		// generate arff files.
		newSourceInstances = getNewInstancesFromMatrix(matrixB_s,k,source,sourceLabel,srcBuggyLabel,"newSource");
		newTargetInstances = getNewInstancesFromMatrix(matrixB_t,k,target,targetLabel,tarBuggyLabel,"newTarget");
		
		// write arff files.
		//String pathToSaveNewData = "data/HeMapData/";
		//WekaUtils.writeADataFile(newSourceInstances, pathToSaveNewData + sourceProjectName + "_to_" + targetProjectName + "_" + transformationType + "_S.arff");
		//WekaUtils.writeADataFile(newTargetInstances, pathToSaveNewData + sourceProjectName + "_to_" + targetProjectName + "_" + transformationType + "_T.arff");
	}

	private Instances getNewInstancesFromMatrix(RealMatrix matrix,int k, Instances instances,
			String label, String buggyLabel, String newDatasetName) {
		
		ArrayList<String> labels = WekaUtils.getArrayListOfLabels(instances,label,buggyLabel);

		// create attribute information
		ArrayList<Attribute> attributes = WekaUtils.createAttributeInfoForClassfication(k+1); //for label +1

		Instances newInstances = new Instances(newDatasetName, attributes, 0);

		for(int row=0;row<matrix.getRowDimension();row++){

			double[] vals = new double[attributes.size()];

			// process attribute values except for label
			for(int col=0; col<attributes.size()-1;col++){
				vals[col] = matrix.getEntry(row, col);
			}

			if(labels.get(row).equals(WekaUtils.strPos))
				vals[attributes.size()-1] = WekaUtils.dblPosValue;
			else
				vals[attributes.size()-1] = WekaUtils.dblNegValue;

			newInstances.add(new DenseInstance(1.0, vals));
		}

		return newInstances;
	}

	private RealMatrix getB(EigenDecomposition U, int k,
			String type) {
		
		RealMatrix matrix = U.getV(); // column of getV() is eigenvector.
		
		int startRow = 0;
		int endRow = 0;
		int l= matrix.getRowDimension();
		
		if(type.equals("target")){
			endRow = l/2-1;
		}
		else if(type.equals("source")){
			startRow = l/2;
			endRow = l-1;
		}
		
		RealMatrix matrixB = matrix.getSubMatrix(startRow, endRow, 0, k-1);
		
		return matrixB;
	}

	/**
	 * Construct Matrix A
	 * @param matrixS
	 * @param matrixT
	 * @param beta
	 * @return Matrix A
	 */
	private double[][] constructMatrixAByLinearTransformation(RealMatrix matrixS, RealMatrix matrixT, int beta) {
		// A1 = 2TT^T+beta^2/2(SS^T)
		RealMatrix A1 = matrixT.scalarMultiply(2).multiply(matrixT.transpose()).add(matrixS.scalarMultiply((beta*beta)/2).multiply(matrixS.transpose()));
		// A2 = beta(SS^T + TT^T)
		RealMatrix A2 = matrixS.multiply(matrixS.transpose()).add(matrixT.multiply(matrixT.transpose())).scalarMultiply(beta);
		// A3 = A2^T
		RealMatrix A3 = A2.transpose();
		// A4 = (beta^2/2)TT^T+2SS^T
		RealMatrix A4 = matrixT.scalarMultiply((beta*beta)/2).multiply(matrixT.transpose()).add(matrixS.scalarMultiply(2).multiply(matrixS.transpose()));
		
		double[][] arrayA1 = A1.getData();
		double[][] arrayA2 = A2.getData();
		double[][] arrayA3 = A3.getData();
		double[][] arrayA4 = A4.getData();
		
		// A = A1 A2
		//     A3 A4
		// Compute A
		double[][] A12 = MatrixUtil.matrixConcatLR(arrayA1, arrayA2);
		double[][] A34 = MatrixUtil.matrixConcatLR(arrayA3, arrayA4);
		
		return MatrixUtil.matrixConcatUL(A12, A34);
	}

	/**
	 * Preprocessing
	 */
	private void preprocessing(int seed) {
		// Preprocessing to make the number of instances bewteen source and target same.
		if(source.size()>target.size()){
			increaseBy = source.size()-target.size();
			target = sizePreprocessing(target,increaseBy,seed);
		}
		else if(source.size()<target.size()){
			increaseBy = target.size()-source.size();
			source = sizePreprocessing(source,increaseBy,seed);
			smallerSource=true;
		}
	}

	/**
	 * Make the number of instances between source and target.
	 * @param instances 
	 * @param increaseBy
	 * @return instances
	 */
	private Instances sizePreprocessing(Instances instances, int increaseBy,int seed) {
		
		int size = instances.size();
		Random r = new Random(seed);
		
		indiceOfAddedInstances.clear();
		   
		for(int i=0; i< increaseBy; i++){
			int selectedIndex = r.nextInt(size); // random integer From 0 to sizeOfCandidates-1
			instances.add(instances.instance(selectedIndex));
			indiceOfAddedInstances.add(selectedIndex);
		}
		
		return instances;
	}
}
