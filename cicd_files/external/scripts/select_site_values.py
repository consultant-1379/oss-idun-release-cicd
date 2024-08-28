"""
This script selects which site values file should be used
This is stored as an artifact properties file as PATH_TO_SITE_VALUES_FILE

Please note this is a temporary solution for IDUN-14873 and will be replaced with the deliverable from IDUN-14705.
"""
import os
def get_path_to_site_values(int_chart_version: str):
    """
    Decides which site values file to use
    :param int_chart_version: The integration chart version being used
    :param platform_type: The type of platform used on the test environment
    :return: site values file name
    :rtype: string
    """
    # int_chart_version < 2.0.0-338 -> site-values-1.0.0.yaml
    if is_version_less_than(int_chart_version, '2.0.0-338'):
        return 'site-values-1.0.0.yaml'
    # int_chart_version < 2.0.0-506 -> site-values-1.1.0.yaml
    elif is_version_less_than(int_chart_version, '2.0.0-506'):
        return 'site-values-1.1.0.yaml'
    # int_chart_version < 2.0.0-792 -> site-values-1.1.1.yaml
    elif is_version_less_than(int_chart_version, '2.0.0-792'):
        return 'site-values-1.1.1.yaml'
    # int_chart_version < 2.0.0-1044 -> site-values-1.1.2.yaml
    elif is_version_less_than(int_chart_version, '2.0.0-1044'):
        return 'site-values-1.1.2.yaml'
    # int_chart_version < 2.1.0-1 -> site-values-2.0.0-1484.yaml
    elif is_version_less_than(int_chart_version, '2.1.0-1'):
        return 'site-values-2.0.0-1484.yaml'
    # int_chart_version < 2.2.0-1 -> site-values-2.1.0-220.yaml
    elif is_version_less_than(int_chart_version, '2.2.0-1'):
        return 'site-values-2.1.0-220.yaml'
    # else site-values-latest.yaml
    else:
        return 'site-values-latest.yaml'

def get_version_string_as_list(chart_version: str) -> list:
    """
    Returns the int chart version as a list of strings
    :param chart_version: The version to be converted
    :return: chart version as list of strings
    :rtype: list
    """
    split_int_chart_version = chart_version.split('.')
    epoch_split = split_int_chart_version[2].split('-')
    split_int_chart_version.pop()
    split_int_chart_version = split_int_chart_version + epoch_split
    return split_int_chart_version

def is_version_less_than(int_chart_version: str, version_to_compare: str) -> bool:
    """
        Returns comparison of versions
        :param int_chart_version: The int chart version
        :param version_to_compare: The version to be compared
        :return: if chart version is less than or equal version to compare return true
        :rtype: bool
        """
    int_chart_version_list = get_version_string_as_list(int_chart_version)
    version_to_compare_list = get_version_string_as_list(version_to_compare)
    if int(int_chart_version_list[0]) < int(version_to_compare_list[0]):
        return True
    elif int(int_chart_version_list[0]) == int(version_to_compare_list[0]):
        if int(int_chart_version_list[1]) < int(version_to_compare_list[1]):
            return True
        elif int(int_chart_version_list[1]) == int(version_to_compare_list[1]):
            if int(int_chart_version_list[2]) < int(version_to_compare_list[2]):
                return True
            elif int(int_chart_version_list[2]) == int(version_to_compare_list[2]):
                if int(int_chart_version_list[3]) < int(version_to_compare_list[3]):
                    return True
                else:
                    return False
            else:
                return False
        else:
            return False
    else:
        return False


if __name__ == '__main__':
    with open('artifact.properties', 'w', encoding='utf8') as artifact_file:
        artifact_file.write('PATH_TO_SITE_VALUES_FILE='+str(get_path_to_site_values(os.getenv('INT_CHART_VERSION'))))
        artifact_file.close()
