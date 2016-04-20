libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"
server=`hostname`

rawdata=skcpu6/20140902_wo_lbm_all_0.00.txt

#for algorithm in $algorithms
#do
	java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.util.WinTieLossByUtest Results/$rawdata Results/skcpu7/AUC_from_common_features.csv > Results/$server/20140902_wintieloss_by_analyzer_all.txt
#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
