#!/usr/bin/env bash

. ./scripts/common.sh

echo "PUSHING FORMATTED CODE BACK TO THE REPOSITORY..."


git config --global user.email "oscm.automaton@gmail.com"
git config --global user.name "OSCM Automaton"
git config --global push.default simple
git remote set-url origin https://${GH_TOKEN}@github.com/servicecatalog/oscm.git

determine_files_to_process

if [[ ! -z "$GIT_DIFF_OUTPUT" ]] ; then
    git add *.java
    git commit -m "Applied code formatting [ci skip]"
    git push origin HEAD:$BRANCH
else
    echo "NOTHING TO PUSH. ABORTING..."
fi

echo "DONE!"
