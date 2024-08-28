#!/usr/bin/env python3
"""
Python script to convert different content types to string.
It will also post the datafile to OST.
"""
import argparse
import logging
import requests
from base64 import b64encode
import yaml
import json
import os
import ast


def read_file_contents(filepath):
    """
    Read and returns the contents of a file
    :param filepath:
    """
    try:
        with open(filepath, "r") as file:
            content = file.read()
        return content
    except FileNotFoundError:
        return f"File not found: {filepath}"
    except Exception as e:
        return f"Error reading file: {e}"


def convert_content_to_string(content, content_type):
    """
    Function to handle data type conversion.
    :param content: content to convert
    :param type: type of content. E.g. yaml, json
    :return content_str:
    :rtype: str
    """
    if content_type == "yaml":
        data = yaml.safe_load(content)
        yaml_string = repr(yaml.dump(data, default_flow_style=False, sort_keys=False, default_style=""))
        return yaml_string.strip()

    if content_type == "json":
        json_data_string = repr(json.dumps(content, sort_keys=False, indent=2))
        data = ast.literal_eval(json_data_string)
        return data

    else:
        raise Exception("Invalid filetype. Only yaml and json files are converted!")


def post_datafile_to_ost(data, file_name, file_extension, bucket_name, username, password):
    """
    This function retrieves the bucket id and post the datafile to OST
    :param data: datafile content
    :param file_name: name of the file
    :param file_extension: extension of the file
    :param bucket_name: name of the bucket where the datafile will be stored
    :param username: username to access OST
    :param password: password for the user used
    """
    try:
        ost_base_url = "https://atvost.athtem.eei.ericsson.se/api"
        bucket_info_endpoint = f"buckets/name/{bucket_name}"
        create_datafile_endpoint = f"dataFiles"

        headers_list = {
            "Accept": "*/*",
            "Authorization": f"Basic {b64encode(f'{username}:{password}'.encode('utf-8')).decode('ascii')}",
            "Content-Type": "application/json"
        }

        bucket_response = requests.request(
            "GET", f"{ost_base_url}/{bucket_info_endpoint}", data="",  headers=headers_list)
        if bucket_response.status_code != 200:
            raise Exception(f"Issue while retrieving the bucket information! Error: {bucket_response.text}")
        json_response = bucket_response.json()
        bucket_id = json_response.get("_id")

        payload_data = json.dumps({
            "name": file_name,
            "type": file_extension,
            "content": yaml.load(data, Loader=yaml.FullLoader),
            "bucket_id": bucket_id
        })
        datafile_repsonse = requests.request(
            "POST", f"{ost_base_url}/{create_datafile_endpoint}", data=payload_data,  headers=headers_list)
        if datafile_repsonse.status_code != 201:
            raise Exception(f"Issue when posting the datafile to OST! Error: {datafile_repsonse.text}")
    except Exception as err:
        logging.error(err)
        raise


def main():
    """
    Driver function
    """
    parser = argparse.ArgumentParser(description="Convert the content to string")
    parser.add_argument("filepath", help="location of the file to read")
    parser.add_argument("bucket_name", help="name of the bucket in which datafile will be stored")
    parser.add_argument("username", help="user that has access to the OST bucket")
    parser.add_argument("password", help="password for the username used")
    args = parser.parse_args()
    filepath = args.filepath
    bucket_name = args.bucket_name
    username = args.username
    password = args.password
    full_filename = os.path.basename(filepath)
    file_name, file_extension = os.path.splitext(full_filename)
    content = read_file_contents(filepath)
    data = convert_content_to_string(content, file_extension[1:])
    post_datafile_to_ost(data, file_name, file_extension[1:], bucket_name, username, password)


if __name__ == "__main__":
    main()
