#!/usr/bin/env bash

echo "PROCEEDING WITH GOOGLE JAVA FORMAT STEP..."

if [ ! -f libraries/google-java-format.jar ] ; then 
    curl -o libraries/google-java-format.jar http://central.maven.org/maven2/com/google/googlejavaformat/google-java-format/1.7/google-java-format-1.7-all-deps.jar
    chmod 755 libraries/google-java-format.jar
fi

git diff --stat --name-only $(echo $TRAVIS_COMMIT_RANGE) | grep "\.java$" | while read -r line ; do
    echo "FORMATING FILE $line..."
    java -jar libraries/google-java-format.jar -r $line
done

echo "DONE!"