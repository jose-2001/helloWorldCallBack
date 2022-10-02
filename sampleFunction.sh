#!/bin/bash
for (( count=$1; count>0; count-- ))
do
java -jar ./client/build/libs/client.jar $2 &
done