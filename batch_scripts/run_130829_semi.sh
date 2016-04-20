date=20130829
postfix=_all_2fold_2semi
#server=zion
#server=tigris
server=pishon

java -Xmx15524m -jar bin/CrossPredictor_$date$postfix.jar > Results/$server/$date$postfix.txt
mail -s "$server $date$postfix finished!" jaechang.nam@gmail.com < message.txt
