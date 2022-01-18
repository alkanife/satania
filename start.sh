#!/bin/bash

clear

while true
do
        java -jar satania.jar
        echo "[Auto] Restart in 5 seconds."
        for i in 5 4 3 2 1
        do
                echo "[Auto] $i..."
                sleep 1
        done
        echo "[Auto] Restarting"
done