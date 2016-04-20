date=20131217
postfix=_2folds_PCo_static_RF
server=zion
#server=tigris
#server=pishon
#server=zion
numOfThreads=4
saveNewData=true

java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
