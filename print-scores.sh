#!/bin/bash

cd /Users/adam/src/junk/nfl-scores;
/usr/bin/sbt run | grep -v info;
