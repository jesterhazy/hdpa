#!/bin/bash
d="$( dirname "$0" )"
java -Xmx6g -cp "$d/lib/*" $@
