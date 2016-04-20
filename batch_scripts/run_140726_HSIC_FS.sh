date=20140726
postfix=_HSIC_FS
server=`hostname`
numOfThreads=1
saveNewData=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"
java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
