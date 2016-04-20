libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"
server=`hostname`

rawdata=skcpu6/20150206_fs_wrapper_acc.txt

#for algorithm in $algorithms
#do
	java -Xmx13524m -cp  bin/Tester.jar$libs hk.ust.cse.ipam.jc.crossprediction.util.WinTieLossByUtest Results/$rawdata Results/skcpu7/AUC_from_common_features.csv > Results/$server/20150206_wintieloss_fs_wrapper_acc.txt
#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
