date=20130904
postfix=_SemiPCo_pop_2fold_only_source
#server=zion
#server=tigris
server=pishon
numOfThreads=24

java -Xmx25524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
