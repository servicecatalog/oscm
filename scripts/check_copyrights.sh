#!/usr/bin/env bash

. ./scripts/common.sh

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