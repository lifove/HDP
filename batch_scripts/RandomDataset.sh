dataPath=$1
groupName=$2
projects=$3
posLabel=$4
classAttrName=$5

libs=":/bigstore/msr3/jc/mybin/JCTools/dist/JCTools.jar:bin/CrossPredictionSimulator/CrossPredictionSimulator/lib/*"

for project in $projects
do
	java -Xmx2048m -cp  bin/CrossPredictor.jar:$libs hk.ust.cse.ipam.jc.crossprediction.util.RandomDatasetGenerator $dataPath/$groupName $project $posLabel $classAttrName

done

mail -s "`hostname` Random dataset generated $groupName" jaechang.nam@gmail.com < /dev/null
