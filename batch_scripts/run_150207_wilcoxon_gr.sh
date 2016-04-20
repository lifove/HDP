libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"
server=`hostname`
date="20150210"
fsOptions="shiv_cs shiv_s shiv_wr shiv_rf shiv_cs15 shiv_s15 shiv_wr15 shiv_rf15"
fsOptions="shiv_cs shiv_s shiv_wr shiv_rf"

for fsOption in $fsOptions
do

rawdata=skcpu6/20150210_fs_$fsOption.txt

#for algorithm in $algorithms
#do
	java -Xmx13524m -cp  bin/Tester.jar$libs hk.ust.cse.ipam.jc.crossprediction.util.WinTieLossByUtest Results/$rawdata Results/skcpu7/AUC_from_common_features.csv $fsOption > Results/$server/$date\_wintieloss_fs_$fsOption.txt
#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
done
