#!/usr/bin/env bash

determine_files_to_process () {
    if [[ ! -z "$TRAVIS_COMMIT_RANGE" ]] ; then
        GIT_DIFF_OUTPUT=$(git diff --stat --name-only $(echo "$TRAVIS_COMMIT_RANGE") | grep "\.java$")
    else
        GIT_DIFF_OUTPUT=$(git diff-tree --no-commit-id --name-only -r $(echo "$TRAVIS_COMMIT") | grep "\.java$")
    fi
}


echo "PROCEEDING WITH COPYRIGHT CHECK..."

determine_files_to_process

if [[ ! -z "$GIT_DIFF_OUTPUT" ]] ; then
    echo $GIT_DIFF_OUTPUT | tr " " "\n" | while read -r line ; do
        bash scripts/check_add_class_legal_notice.sh $line scripts/java_class_legal_notice.md
    done
else
    echo "NOTHING TO PROCESS. ABORTING..."
    echo "DONE!"
fi