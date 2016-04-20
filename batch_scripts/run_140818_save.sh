date=20140818
postfix=_save
cofeatures=cofeatures_20140802_LP_FS_LBMedian.txt
server=`hostname`
numOfThreads=1
saveNewData=true
verbose=false
#analyzers="PAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
analyzers="KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"
#algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

#for algorithm in $algorithms
#do
#	java -Xmx13524m -cp  bin/CrossPredictor_20140818_save.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.90 0.90 0.10 $algorithms $verbose > Results/$server/$date$postfix_0.90.txt
#done
cutoffs="0.05" #"0.10 0.20 0.30 0.40 0.50 0.60 0.70 0.80"
for cutoff in $cutoffs 
do

java -Xmx13524m -cp  bin/CrossPredictor_20140818_save.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers $cutoff $cutoff 0.05 $algorithms $verbose > Results/$server/$date$postfix\_$cutoff.txt 
done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
