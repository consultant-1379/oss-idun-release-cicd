#!/bin/bash

function clear_home_directory_caches() {
    echo "INFO: Purging home directory caches."
    for home_directory in /home/*; do
        rm -rf ${home_directory}/.ansible
        rm -rf ${home_directory}/.aws/cli/cache
        rm -rf ${home_directory}/.cache
        rm -rf ${home_directory}/jenkins/cache
        rm -rf ${home_directory}/.kube/cache
        rm -rf ${home_directory}/.kube/http-cache
        rm -rf ${home_directory}/.m2/repository
        rm -rf ${home_directory}/.npm
    done
}

function clear_root_user_caches() {
    echo "INFO: Purging root user caches."
    rm -rf /root/.ansible
    rm -rf /root/.aws/cli/cache
    rm -rf /root/.cache
    rm -rf /root/jenkins/cache
    rm -rf /root/.kube/cache
    rm -rf /root/.kube/http-cache
    rm -rf /root/.m2/repository
    rm -rf /root/.npm
}

########################
#     SCRIPT START     #
########################
clear_home_directory_caches
clear_root_user_caches