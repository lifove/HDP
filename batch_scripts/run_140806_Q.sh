date=20140806
postfix=_Q
cofeatures=cofeatures_20140806_LP_Q.txt
server=`hostname`
numOfThreads=1
saveNewData=false
#analyzers="PAnalyzer,UAnalyzer,ASAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
analyzers="QAnalyzer"
algorithms="weka.classifiers.functions.Logistic"
#algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

for algorithm in $algorithms
do
	java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithm > Results/$server/$date$postfix$analyzers$algorithm.txt
done


java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.05 0.05 0.05 $algorithms > Results/$server/$date\_0.05_$postfix.txt 

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
