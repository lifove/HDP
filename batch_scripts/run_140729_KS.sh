date=20140729
postfix=_KS
cofeatures=cofeatures_20140729_LP_FS_FILTERED_KS.txt
server=`hostname`
numOfThreads=20
saveNewData=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"
java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
