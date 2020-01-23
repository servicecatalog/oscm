#!/usr/bin/env bash

. ./scripts/common.sh

echo "PROCEEDING WITH GOOGLE JAVA FORMAT STEP..."

determine_files_to_process

if [[ ! -z "$GIT_DIFF_OUTPUT" ]] ; then
    if [ ! -f libraries/google-java-format.jar ] ; then 
        curl -o libraries/google-java-format.jar https://repo1.maven.org/maven2/com/google/googlejavaformat/google-java-format/1.7/google-java-format-1.7-all-deps.jar
        chmod 755 libraries/google-java-format.jar
    fi

    echo $GIT_DIFF_OUTPUT | tr " " "\n" | while read -r line ; do
        echo "FORMATING FILE $line..."
        java -jar libraries/google-java-format.jar -r $line
    done
else 
    echo "NOTHING TO FORMAT. ABORTING..."
fi

echo "DONE!"