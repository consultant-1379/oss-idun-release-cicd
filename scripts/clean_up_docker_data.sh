#!/bin/bash

function clean_up_docker_data() {
    running=$(docker ps -q | wc -l)
    if [[ ${running} -gt 0 ]]; then
        echo "INFO: Stopping ${running} containers."
        docker stop $(docker ps -q)
    fi
    echo "INFO: Purging Docker Data."
    docker system prune -a -f
}

########################
#     SCRIPT START     #
########################
clean_up_docker_data