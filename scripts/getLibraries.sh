#!/usr/bin/env bash

 wget -P $TRAVIS_BUILD_DIR/libraries/apache-ant-contrib/javalib/ http://central.maven.org/maven2/org/apache/ivy/ivy/2.4.0/ivy-2.4.0.jar
 wget -P $TRAVIS_BUILD_DIR/libraries/apache-ant/ https://archive.apache.org/dist/ant/binaries/apache-ant-1.10.5-bin.zip
 unzip -o -d $TRAVIS_BUILD_DIR/libraries/apache-ant/ $TRAVIS_BUILD_DIR/libraries/apache-ant/apache-ant-1.10.5-bin.zip
