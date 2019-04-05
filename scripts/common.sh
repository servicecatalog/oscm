#!/usr/bin/env bash

determine_files_to_process () {
    if [[ ! -z "$TRAVIS_COMMIT_RANGE" ]] ; then
        GIT_DIFF_OUTPUT=$(git diff --stat --name-only $(echo "$TRAVIS_COMMIT_RANGE") | grep "\.java$")
    else
        GIT_DIFF_OUTPUT=$(git diff-tree --no-commit-id --name-only -r $(echo "$TRAVIS_COMMIT") | grep "\.java$")
    fi
}