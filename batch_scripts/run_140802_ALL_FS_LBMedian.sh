date=20140811
postfix=_ALL_FS_LBMedian
cofeatures=cofeatures_20140802_LP_FS_LBMedian.txt
server=`hostname`
numOfThreads=1
saveNewData=false
analyzers="PAnalyzer,UAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"
verbose=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms $verbose > Results/$server/$date$postfix.txt

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.05 0.05 0.05 $algorithms $verbose >> Results/$server/$date$postfix.txt 

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
