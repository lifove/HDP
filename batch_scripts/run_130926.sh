date=20130926
postfix=_2fold_all
#server=zion
#server=tigris
server=pishon
numOfThreads=26

java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
