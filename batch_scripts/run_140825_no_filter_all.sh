date=20140825
postfix=_no_filter
cofeature_prefix=20140825_FS_NO_FILTERS
server=`hostname`
numOfThreads=1
saveNewData=false
verbose=true
analyzers="PAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
#analyzers="KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"
#algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

#for algorithm in $algorithms
#do
#	java -Xmx13524m -cp  bin/CrossPredictor_20140818_save.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.90 0.90 0.10 $algorithms $verbose > Results/$server/$date$postfix_0.90.txt
#done
cutoffs="0.05 0.10 0.20 0.30 0.40 0.50 0.60 0.70 0.80 0.90"
for cutoff in $cutoffs 
do

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeature_prefix\_$cutoff.txt $analyzers $cutoff $cutoff 0.05 $algorithms $verbose >> Results/$server/$date$postfix.txt 
done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
