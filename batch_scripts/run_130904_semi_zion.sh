date=20130904
postfix=_SemiPCo_2fold_RandFor
server=zion
#server=tigris
#server=pishon
numOfThreads=4

java -Xmx15524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
