date=20130927
postfix=_2fold_all
#server=zion
server=tigris
#server=pishon
numOfThreads=30
saveNewData=true

java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
