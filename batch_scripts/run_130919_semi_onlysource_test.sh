date=20130919
postfix=_SemiPCo_2fold_only_source_medical
#server=zion
server=tigris
#server=pishon
numOfThreads=2

java -Xmx3524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
