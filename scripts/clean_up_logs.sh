#!/bin/bash

function clean_up_logs() {
    echo "INFO: Trimming Log Data"
    # This will truncate any *.log files in /var that are either older than 7 days and greater than 50M or older than 30 days.
    find /var -name "*.log" \( \( -size +50M -mtime +7 \) -o -mtime +30 \) -exec truncate {} --size 0 \;
}

########################
#     SCRIPT START     #
########################
clean_up_logs