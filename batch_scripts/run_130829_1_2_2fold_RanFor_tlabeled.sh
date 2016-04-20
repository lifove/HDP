date=20130829
postfix=_1_2_2fold_RanFor_tlabeled
#server=zion
server=tigris

java -Xmx15524m -jar bin/CrossPredictor_$date$postfix.jar > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
