date=20130902
postfix=_all_2fold_2semi
#server=zion
#server=tigris
server=pishon
numOfThreads=8

java -Xmx15524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
