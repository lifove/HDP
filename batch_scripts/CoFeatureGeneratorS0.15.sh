date=20150326
server=`hostname`
numOfThreads=8
fsOption=shiv_s0.15 #none auc aac f-meas gr shiv_gr

redirect=data/cofeatures_$date\_All_Matched_for_fs_$fsOption.txt
#analyzers="UAnalyzer,PAnalyzer,PIAnalyzer,KSAnalyzer,SCoAnalyzer"
analyzers="KSAnalyzer,PAnalyzer,SCoAnalyzer""
useLBMFilter=false
useDMFilter=false

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"

java -Xmx10524m -cp  bin/CoFeatureGenerator.jar:$libs hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator $numOfThreads $analyzers $useLBMFilter $useDMFilter 0.00 $fsOption > $redirect 
mail -s "$server CoFeatureGenerator $date finished!" jaechang.nam@gmail.com < message.txt
