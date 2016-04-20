date=20150613
postfix=_verbose_common_features
cofeatures=commonfeatures.txt
server=`hostname`
numOfThreads=20
saveNewData=false
verbose=true
analyzers="commonFatures"
#analyzers="KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"
#algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

#for algorithm in $algorithms
#do
	java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.DriverWithCommonFeatures $numOfThreads $saveNewData $cofeatures $analyzers 0.0 0.0 0.10 $algorithms $verbose > Results/$server/$date$postfix.txt
#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
