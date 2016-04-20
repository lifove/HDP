libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"
server=`hostname`
#for algorithm in $algorithms
#do
	java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.util.WinTieLossByUtest Results/skcpu7/20140812.05.txt Results/skcpu7/AUC_from_common_features.csv > Results/$server/20140812_0.05_wintieloss.txt
#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
