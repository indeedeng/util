#!/bin/bash

export MAVEN_OPTS="-Xmx64M -XX:+PrintGCDetails -verbose:gc"

if [[ "Indeed" == $1 ]] 
then
INDEED_ARG="-Dexec.arguments=ind";
fi

if [ ! -f src/test/resources/logentries.txt.gz ]; then
    echo "Downloading benchmark data from AWS, this could take a while"
    wget -P src/test/resources 'https://s3.amazonaws.com/indeed-open-source/logentries.txt.gz'
fi

mvn clean package
mvn exec:java -Dexec.mainClass="com.indeed.util.urlparsing.benchmark.KeyValueParsingBenchmark" -Dexec.classpathScope="test" $INDEED_ARG


