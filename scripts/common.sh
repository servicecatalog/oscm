#!/usr/bin/env bash

determine_files_to_process () {
    if [[ ! -z "$COMMIT_RANGE" ]] ; then
        GIT_DIFF_OUTPUT=$(git diff --stat --name-only $(echo "$COMMIT_RANGE") | grep "\.java$")
    else
        GIT_DIFF_OUTPUT=$(git diff-tree --no-commit-id --name-only -r $(echo "$COMMIT") | grep "\.java$")
    fi
}

