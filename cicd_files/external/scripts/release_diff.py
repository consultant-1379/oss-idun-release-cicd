"""
This script gets the difference between two integration chart versions
"""
import os


def compare_dict(current, previous):
    """
    Compares two dictionaries and returns the differences
    :param current
    :param previous
    :return: diff
    :rtype: dict
    """
    diff = {}
    current = convert_str_to_dict(current)
    previous = convert_str_to_dict(previous)
    for key in current:
        if (key not in previous or current[key] != previous[key]):
            diff[key] = current[key]
    return diff


def convert_str_to_dict(input_string):
    """
    Converts a string to a dictionary
    :param input_string
    :return: app_diff_dict
    :rtype: dict
    """
    input_string = input_string.replace('{', "")
    input_string = input_string.replace('}', "")
    app_diff_dict = dict(item.split("=") for item in input_string.split(", "))
    return app_diff_dict


def modify_dict(current, previous, helmfile_version, is_dm_released, dm_version):
    """
    Adds EIAE helmfile and/or deployment manager to the dictionary
    :param current
    :param previous
    :param helmfile_version
    :param is_dm_released
    :param dm_version
    :return: release
    :rtype: dict
    """
    release = compare_dict(current, previous)
    if is_dm_released.upper() == 'TRUE':
        release['eric-eiae-helmfile'] = helmfile_version
        release['deployment-manager'] = dm_version
    release['eric-eiae-helmfile'] = helmfile_version
    return release


if __name__ == '__main__':
    with open("artifact.properties", "w", encoding='utf8') as f:
        f.write('APP_TO_RELEASE='+str(modify_dict(os.getenv("LATEST_RELEASE_INT_CHART_VERSION"),
                os.getenv("PREVIOUS_RELEASE_INT_CHART_VERSION"), os.getenv("INT_CHART_VERSION"),
                os.getenv("IS_DM_RELEASED"), os.getenv("OSS_DEPLOYMENT_MANAGER_VERSION"))))
        f.close()
