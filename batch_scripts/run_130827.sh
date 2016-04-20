date=20130827
postfix=_SemiPCoAS_all_cutoff_2fold
server=pishon

java -Xmx8024m -jar bin/CrossPredictor_$date$postfix.jar > Results/tigris/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
