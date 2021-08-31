#!/bin/sh -x

artifact_url=$1
artifact_path=$2
file_path=$3

if [[ -z $artifact_url ]]; then
    echo "artifact url can not be empty"
    exit 1
fi

if [[ -z $artifact_path ]]; then
    echo "artifact path can not be empty"
    exit 1
fi

if [[ -z $file_path ]]; then
    echo "path to the file to be uploaded can not be empty"
    exit 1
fi

if [[ ! -f $file_path ]]; then
    echo "file doest not exist"
    exit 1
fi

if [[ -z $artifactory_user_id ]]; then
    echo "artifactory username is empty"
    exit 1
fi

if [[ -z $artifactory_password ]]; then
    echo "artifactory password is empty"
    exit 1
fi


curl -v -u $artifactory_user_id:$artifactory_password -X PUT -T $file_path "${artifact_url}/${artifact_path}"
