#!/usr/bin/env bash

determine_files_to_process () {
    if [[ ! -z "$TRAVIS_COMMIT_RANGE" ]] ; then
        GIT_DIFF_OUTPUT=$(git diff --stat --name-only $(echo "$TRAVIS_COMMIT_RANGE") | grep "\.java$")
    else
        GIT_DIFF_OUTPUT=$(git diff-tree --no-commit-id --name-only -r $(echo "$TRAVIS_COMMIT") | grep "\.java$")
    fi
}


echo "PUSHING FORMATTED CODE BACK TO THE REPOSITORY..."

git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis CI"
git config --global push.default simple
git remote set-url origin https://${GITHUB_TOKEN}@github.com/servicecatalog/oscm.git

determine_files_to_process

if [[ ! -z "$GIT_DIFF_OUTPUT" ]] ; then
    git add *.java
    git commit -m "Applied code formatting [ci skip]"
    git push origin HEAD:$TRAVIS_BRANCH
else
    echo "NOTHING TO PUSH. ABORTING..."
fi

echo "DONE!"