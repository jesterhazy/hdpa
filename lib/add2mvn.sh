#!/bin/sh
_dir=$(dirname $0)
mvn install:install-file -Dfile=$_dir/timestools.jar -DgroupId=com.nytlabs -DartifactId=corpus -Dversion=2008 -Dpackaging=jar