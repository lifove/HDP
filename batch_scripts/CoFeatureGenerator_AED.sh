date=20131217
#server=zion
#server=pishon
#server=tigris
server=zion
numOfThreads=26

java -Xmx10524m -cp  bin/CoFeatureGenerator_AED.jar hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator $numOfThreads > data/cofeatures_AEDAnalyzer.txt
mail -s "$server CoFeatureGenerator $date finished!" jaechang.nam@gmail.com < message.txt
