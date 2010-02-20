#!/bin/bash
cd ${0%/*}
export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:lib/rxtx32:lib/rxtx64
java -jar -splash:lib/armotool.png -Xmx256m "JaKoTo.jar"
