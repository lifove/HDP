date=20140804
postfix=_NEW_FS_Filters
#cofeatures=cofeatures_20140802_LP_FS_LBMedian.txt
cofeatures=cofeatures_20140804_LP_FS_NEW_FILTERS.txt
server=`hostname`
numOfThreads=8
saveNewData=false
analyzers="PAnalyzer,UAnalyzer,ASAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

analyzers="PAnalyzer"
#java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers.txt

analyzers="UAnalyzer"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers.txt

analyzers="ASAnalyzer"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers.txt

analyzers="SCoAnalyzer"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers.txt

analyzers="PIAnalyzer"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers.txt

analyzers="KSAnalyzer"
java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 0.10 $algorithms > Results/$server/$date$postfix$analyzers.txt

analyzers="PAnalyzer,UAnalyzer,ASAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.05 0.05 0.05 $algorithms > Results/$server/$date\_0.05_$postfix.txt 

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
