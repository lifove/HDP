dataPath=data

sh RandomDataset.sh $dataPath effort "albrecht china maxwell miyazaki94" buggy label &
sh RandomDataset.sh $dataPath Wine winequality buggy quality &
sh RandomDataset.sh $dataPath Medical breast_cancer M Class &
#sh RandomDataset.sh $dataPath Severity pitsB_FS buggy "@@class@@" &
