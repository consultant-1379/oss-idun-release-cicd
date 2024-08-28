#!/bin/bash

function clean_up_yum_data() {
    yum_path=$(command -v yum)
    if [[ -n ${yum_path} ]]; then
        echo "INFO: Found yum - cleaning up yum data."
        yum clean all
        # Remove yum cache for edgecases where yum clean doesn't.
        rm -rf /var/cache/yum
        # Remove yum user cache in case any non root user used yum.
        rm -rf /var/tmp/yum-*
    fi
}

########################
#     SCRIPT START     #
########################
clean_up_yum_data
