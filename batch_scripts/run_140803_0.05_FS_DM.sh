date=20140803
postfix=_0.05_FS_noFilters
#cofeatures=cofeatures_20140802_LP_FS_DM.txt
cofeatures=cofeatures_20140730_LP_FS_NO_FILTER_ALL.txt
server=`hostname`
numOfThreads=16
saveNewData=false
analyzers="PAnalyzer,UAnalyzer,ASAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

#java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix.txt

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.05 0.05 0.05 $algorithms > Results/$server/$date$postfix.txt

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
