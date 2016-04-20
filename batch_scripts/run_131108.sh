date=20131108
postfix=_2folds_PCo_static
#server=zion
#server=tigris
server=pishon
numOfThreads=20
saveNewData=false

java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
