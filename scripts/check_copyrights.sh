#!/usr/bin/env bash

echo "PROCEEDING WITH COPYRIGHT CHECK..."

GIT_DIFF_OUTPUT=$(git diff --stat --name-only $(echo $TRAVIS_COMMIT_RANGE) | grep "\.java$")

if [[ ! -z "$GIT_DIFF_OUTPUT" ]] ; then
    echo $GIT_DIFF_OUTPUT | while read -r line ; do
        bash scripts/check_add_class_legal_notice.sh $line scripts/java_class_legal_notice.md
    done
else
    echo "NOTHING TO PROCESS. ABORTING..."
    echo "DONE!"
fi