date=20140730
postfix=_ALL_FS_NO_FILTER
cofeatures=cofeatures_20140730_LP_FS_NO_FILTER_ALL.txt
server=`hostname`
numOfThreads=22
saveNewData=false
analyzers="PAnalyzer,UAnalyzer,ASAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.00 0.90 > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
