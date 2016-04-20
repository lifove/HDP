date=20130827
postfix=_SemiPCoAS_all_cutoff_2fold_FS
#server=pishon
server=tigris

java -Xmx8024m -jar bin/CrossPredictor_$date$postfix.jar > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
