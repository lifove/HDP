date=20140729
postfix=
cofeatures=cofeatures_20140729_LP_P.txt
server=`hostname`
numOfThreads=8
saveNewData=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"
java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures PAnalyzer 0.00 0.90 > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
