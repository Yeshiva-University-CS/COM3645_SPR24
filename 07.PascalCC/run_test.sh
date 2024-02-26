#!/usr/bin/env bash

inputDir=src/test/resources/input
resultDir=src/test/resources/output
outputRoot=target/testruns
jarfile=target/pascalCC-1-jar-with-dependencies.jar
single=$1

# remove old output
rm -f -r $outputRoot

mkdir -p $outputRoot/pascal-output
mkdir -p $outputRoot/conversion-output
mkdir -p $outputRoot/java-output



# iterate over input pascal files in $inputDir

for FILE in $inputDir/*.pas; do
	filename=$(basename -- "$FILE")
	#extension="${filename##*.}"
	prog="${filename%.*}"

	echo
	echo === BEGIN: "$prog"

	echo execute the pascal program
	java -jar $jarfile -execute $FILE > $outputRoot/pascal-output/$prog.txt

	echo run conversion to java file
	java -jar $jarfile -convert $FILE > $outputRoot/conversion-output/$prog.java

	echo compile the java file
	javac $outputRoot/conversion-output/$prog.java

	echo execute the java program
	java -cp $outputRoot/conversion-output $prog > $outputRoot/java-output/$prog.txt

	echo === END: "$prog"

done
echo
echo :::::::::::::::::::::::::::::::::
echo diff the pascal execution output
echo :::::::::::::::::::::::::::::::::
diff -q -b --strip-trailing-cr $resultDir $outputRoot/pascal-output

echo
echo :::::::::::::::::::::::::::::::
echo diff the java execution output
echo :::::::::::::::::::::::::::::::
diff -q -b --strip-trailing-cr $resultDir $outputRoot/java-output
