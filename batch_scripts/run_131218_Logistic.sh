date=20131218
postfix=_2folds_PCoAEDAL1D_static_LR_NoN
#server=zion
server=tigris
#server=pishon
#server=zion
numOfThreads=26
saveNewData=false

java -Xmx13524m -cp  bin/CrossPredictor_$date$postfix.jar hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt