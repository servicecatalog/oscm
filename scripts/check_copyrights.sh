#!/usr/bin/env bash

echo "PROCEEDING WITH COPYRIGHT CHECK..."

git diff --stat --name-only $(echo $TRAVIS_COMMIT_RANGE) | grep "\.java$" | while read -r line ; do
    bash scripts/check_add_class_legal_notice.sh $line scripts/java_class_legal_notice.md
done