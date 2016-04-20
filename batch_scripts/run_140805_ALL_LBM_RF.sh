date=20140805
postfix=_NEW_FS_LBM_5Algs
cofeatures=cofeatures_20140802_LP_FS_LBMedian.txt
server=`hostname`
numOfThreads=1
saveNewData=false
#analyzers="PAnalyzer,UAnalyzer,ASAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
analyzers="KSAnalyzer"
#algorithms="weka.classifiers.functions.Logistic"
algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

algorithms="weka.classifiers.trees.J48"
algorithm="J48"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers$algorithm.txt

algorithms="weka.classifiers.trees.RandomForest"
algorithm="RF"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers$algorithm.txt

algorithms="weka.classifiers.bayes.NaiveBayes"
algorithm="NB"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers$algorithm.txt

algorithms="weka.classifiers.bayes.BayesNet"
algorithm="BN"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers$algorithm.txt

algorithms="weka.classifiers.functions.SMO"
algorithm="SMO"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers$algorithm.txt

algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.05 0.05 0.05 $algorithms > Results/$server/$date\_0.05_$postfix.txt 

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
