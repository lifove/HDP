libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*:/bigstore/msr3/jc/mybin/JCTools/lib/*"
server=`hostname`

rawdata=skcpu6/20141013_hetero_10random.txt

#for algorithm in $algorithms
#do
	java -Xmx13524m -cp  bin/CrossPredictor.jar$libs hk.ust.cse.ipam.jc.crossprediction.util.WinTieLossByUtest Results/$rawdata > Results/$server/20141013_wintieloss_hetero_random.txt
#done

mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
