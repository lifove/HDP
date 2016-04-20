date=20130828
postfix=_All_2fold_RanFor_tlabeled_new
server=zion
#server=tigris

java -Xmx8024m -jar bin/CrossPredictor_$date$postfix.jar > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
