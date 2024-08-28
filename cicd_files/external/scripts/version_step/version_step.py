#!/usr/bin/env python3
import argparse
import os
import yaml
class Chart:
    """
    Helm chart class
    """
    def __init__(self, helm_chart_folder):
        """
        :arg helm_chart_folder path to helm chart
        """
        yaml_file = "%s/metadata.yaml" % helm_chart_folder
        if not os.path.exists(yaml_file):
            raise AttributeError("Could not find %s" % yaml_file)
        with open(yaml_file, 'r') as stream:
            doc = yaml.load(stream, Loader=yaml.FullLoader)
        self.yaml_file = yaml_file
        self.chart = doc
    def get_version(self):
        """
        Get helm chart version
        """
        return self.chart['version']
    def set_version(self, new_version):
        """
        Update helm chart version
        """
        self.chart['version'] = new_version
    def save(self, file=None):
        """
        :arg file: the file path to save requirements.yaml
        default is the current file
        """
        if file is None:
            file = self.yaml_file
        with open(f'/usr/src/app/{file}', 'w') as stream:
            yaml.dump(self.chart, stream, default_flow_style=False)
def step_version(version,version_type="MINOR"):
    """
    Step version. Increment MAJOR, MINOR, PATCH version or Build Number
    :arg version: version to step
    :arg version_type: MAJOR, MINOR or PATCH
    """
    old_version = version
    taglist = old_version.split(".")
    if len(taglist) == 3:
        major = taglist[0]
        minor = taglist[1]
        patch = taglist[2]
        new_major = major
        new_minor = minor
        new_patch = patch
        if version_type == "MAJOR":
            new_major = (str(int(major) + 1))
            new_minor = "0"
            new_patch = "0-1"
        elif version_type == "MINOR":
            new_minor = (str(int(minor) + 1))
            new_patch = "0-1"
        elif version_type == "PATCH":
            new_patch = "0"
            _patch = patch.split('-')[0]
            new_patch = str(int(_patch) + 1)+"-1"
            version = "%s.%s.%s" % (major, minor, new_patch)
        else:
            raise AttributeError("Unknown version type: %s. "
                                 "Should be '0.0.0' format" % version_type)
        version = "%s.%s.%s" % (new_major, new_minor, new_patch)
    print("Stepped version from " + old_version + " to " + version)
    return version, old_version

def write_to_artifact_file(old_version):
    """
    Write the old version to the artifact.properties file
    :arg old_version: old version before increment
    """
    #create artifact.properties with data needed for version handling automation
    with open('/usr/src/app/artifact.properties', 'w') as f:
      f.write('CHART_VERSION='+old_version+ '\n')

def main():
    parser = argparse.ArgumentParser(description='provide version to update')
    parser.add_argument('version_type', choices=['MAJOR', 'MINOR', 'PATCH'])
    parser.add_argument('chart_path', help="path to App chart")
    args = parser.parse_args()
    version_type = args.version_type
    chart_path = args.chart_path
    i_chart = Chart(chart_path)
    i_version = i_chart.get_version()
    new_version, old_version = step_version(i_version, version_type)
    write_to_artifact_file(old_version)
    i_chart.set_version(new_version)
    i_chart.save()

if __name__ == '__main__':
    main()