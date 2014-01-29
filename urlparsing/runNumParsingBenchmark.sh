#!/bin/bash

export MAVEN_OPTS="-Xmx64M -XX:+PrintGCDetails"

if [[ "Indeed" == $1 ]]
then
INDEED_ARG="-Dexec.arguments=ind";
fi
mvn clean package 

mvn exec:java -Dexec.mainClass="com.indeed.util.urlparsing.benchmark.NumberParsingBenchmark" -Dexec.classpathScope="test" $INDEED_ARG

