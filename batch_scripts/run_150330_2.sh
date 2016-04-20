date=20150330
postfix=_fs_s15
cofeature_prefix=20150326_FS_S15
server=`hostname`
numOfThreads=1
saveNewData=false
fsOption=shiv_s0.15 # none acc auc f-meas
verbose=true

outputRScript=true

#analyzers="PAnalyzer,SCoAnalyzer,PIAnalyzer,KSAnalyzer"
analyzers="KSAnalyzer"
algorithms="weka.classifiers.functions.Logistic"
#algorithms="weka.classifiers.trees.J48,weka.classifiers.trees.RandomForest,weka.classifiers.bayes.NaiveBayes,weka.classifiers.bayes.BayesNet,weka.classifiers.functions.SMO"

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"

#for algorithm in $algorithms
#do
#	java -Xmx13524m -cp  bin/CrossPredictor_20140818_save.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeatures $analyzers 0.90 0.90 0.10 $algorithms $verbose > Results/$server/$date$postfix_0.90.txt
#done
cutoffs="0.05" # 0.10 0.20 0.30 0.40 0.50 0.60 0.70 0.80 0.90"
for cutoff in $cutoffs 
do

java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.Driver $numOfThreads $saveNewData $cofeature_prefix\_$cutoff.txt $analyzers $cutoff $cutoff 0.05 $algorithms $fsOption $verbose > Results/$server/$date$postfix\_$cutoff.txt 

mail -s "$server $cutoff $date$postfix finished!" jaechang.nam@gmail.com < message.txt

done

#cat Results/$server/$date$postfix\_*.txt > Results/$server/$date$postfix.txt

#for cutoff in $cutoffs
#do
	rawdata=$server/$date$postfix.txt
        java -Xmx13524m -cp  bin/Tester.jar$libs hk.ust.cse.ipam.jc.crossprediction.util.WinTieLossByUtest Results/$rawdata Results/skcpu7/AUC_from_common_features.csv $fsOption $outputRScript > Results/$server/$date\_wintieloss$postfix\_R_$outputRScript.txt

#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
