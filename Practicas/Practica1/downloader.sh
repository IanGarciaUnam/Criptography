#!/bin/bash

#sleep 2; while true; do spd-say -w 'get back to work'; done
for i in {1..5000}; do if [i =0]; then speaker-test -t sine -f 900 -l 1; fi; done

#python downloader.py & shutdown -h 5
