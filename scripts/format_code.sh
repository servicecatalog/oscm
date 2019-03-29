#!/usr/bin/env bash

determine_files_to_process () {
    if [[ ! -z "$TRAVIS_COMMIT_RANGE" ]] ; then
        GIT_DIFF_OUTPUT=$(git diff --stat --name-only $(echo "$TRAVIS_COMMIT_RANGE") | grep "\.java$")
    else
        GIT_DIFF_OUTPUT=$(git diff-tree --no-commit-id --name-only -r $(echo "$TRAVIS_COMMIT") | grep "\.java$")
    fi
}


echo "PROCEEDING WITH GOOGLE JAVA FORMAT STEP..."

determine_files_to_process

if [[ ! -z "$GIT_DIFF_OUTPUT" ]] ; then
    if [ ! -f libraries/google-java-format.jar ] ; then 
        curl -o libraries/google-java-format.jar http://central.maven.org/maven2/com/google/googlejavaformat/google-java-format/1.7/google-java-format-1.7-all-deps.jar
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