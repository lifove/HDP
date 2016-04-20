date=20131218
#server=zion
#server=pishon
server=tigris
#server=zion
numOfThreads=26

java -Xmx10524m -cp  bin/CoFeatureGenerator_NoN.jar hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator $numOfThreads > data/cofeatures_NoN_PCO_AED_AL1DAnalyzer.txt
mail -s "$server CoFeatureGenerator $date finished!" jaechang.nam@gmail.com < message.txt
