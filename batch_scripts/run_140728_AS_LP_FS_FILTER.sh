date=20140728
postfix=_AS_LP_FS_FILTER
server=`hostname`
numOfThreads=4
saveNewData=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"
java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData cofeatures_20140228_LP_FSAllFiltered.txt > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
