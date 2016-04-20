date=20150606
server=`hostname`
numOfThreads=20
fsOption=none #shiv_s0.15 #shiv_gr0.25 #none auc aac f-meas gr shiv_gr

redirect=data/cofeatures_$date\_All_Matched_for_fs_$fsOption\_2fold.txt
#analyzers="UAnalyzer,PAnalyzer,PIAnalyzer,KSAnalyzer,SCoAnalyzer"
analyzers="KSAnalyzer,PAnalyzer,SCoAnalyzer"
useLBMFilter=false
useDMFilter=false

allData=data/cofeatures_20140826_All_Matched.txt #data/cofeatures_20150209_All_Matched_for_fs_none.txt

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"

java -Xmx10524m -cp  bin/CoFeatureGenerator.jar:$libs hk.ust.cse.ipam.jc.crossprediction.CoFeatureGenerator $numOfThreads $analyzers $useLBMFilter $useDMFilter 0.00 $fsOption $allData > $redirect 
mail -s "$server CoFeatureGenerator $date finished!" jaechang.nam@gmail.com < message.txt
