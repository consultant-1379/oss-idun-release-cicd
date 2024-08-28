#!/bin/bash

function bump-version() {
    WORKSPACE=$1
    old_version=$(cat "${WORKSPACE}"/VERSION)
    version_as_list=( ${old_version//./ } )
    path_to_artifact_properties=${WORKSPACE}'/artifact.properties'
    new_version=${version_as_list[0]}'.'${version_as_list[1]}'.'$((version_as_list[2] + 1 ))

    echo "$new_version" > "$path_to_artifact_properties"
}


bump-version "$1"

