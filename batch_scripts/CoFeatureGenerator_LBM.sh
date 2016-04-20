date=20140802
server=`hostname`
numOfThreads=18
redirect=data/cofeatures_$date\_ALL_FS_NEW_LBMedian_FILTER_All_Matched.txt
analyzers="UAnalyzer,PAnalyzer,PIAnalyzer,ASAnalyzer,KSAnalyzer,SCoAnalyzer"
#analyzers="SCoAnalyzer"
useLBMFilter=true
useDMFilter=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"

java -Xmx10524m -cp  bin/CoFeatureGenerator.jar:$libs hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator $numOfThreads $analyzers $useLBMFilter $useDMFilter > $redirect 
mail -s "$server CoFeatureGenerator $date finished!" jaechang.nam@gmail.com < message.txt
