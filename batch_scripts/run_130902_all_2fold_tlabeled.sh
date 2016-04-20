date=20130902
postfix=_all_2fold_tlabeled
#server=zion
server=tigris
numOfThreads=30

java -Xmx15524m -cp bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
