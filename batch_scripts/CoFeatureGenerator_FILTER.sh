date=20140729
server=`hostname`
numOfThreads=20
redirect=data/cofeatures_$date\_ALL_FS_FILTER_All_Matched.txt
analyzers="UAnalyzer,PAnalyzer,PIAnalyzer,ASAnalyzer,KSAnalyzer,SCoAnalyzer"
useFilter=true

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"

java -Xmx10524m -cp  bin/CoFeatureGenerator.jar:$libs hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator $numOfThreads $analyzers $useFilter > $redirect 
mail -s "$server CoFeatureGenerator $date finished!" jaechang.nam@gmail.com < message.txt
