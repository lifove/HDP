date=20131003
#server=zion
server=tigris
#server=pishon
folds=2
repeat=50
numOfThreads=20

java -Xmx13524m -cp  bin/SimulatorForWithinPrediction.jar hk.ust.cse.ipam.jc.crossprediction.SimulatorForTargetWithin $folds $repeat $numOfThreads > data/WithinPredictionResults_folds$folds\_$repeat.txt
mail -s "$server $date Within Predicton finished!" jaechang.nam@gmail.com < message.txt
