"""
Module contains the Yaml helper class.
"""

import logging
import yaml


class YamlHelper:
    """
    Helps manage Yaml file operations.
    """

    @staticmethod
    def load_yaml_file(filename: str) -> any:
        """
        Loads the yaml file using the filename passed in.
        :param filename: name of YAML file
        :raises FileNotFoundError, YAMLError:
        """
        try:
            with open(filename, "r", encoding="utf-8") as file:
                return yaml.safe_load(file)
        except FileNotFoundError as err:
            logging.error(f"Could not find file {filename}: {str(err)}")
            raise err
        except yaml.YAMLError as err:
            logging.error(f"Could not parse YAML file {filename}: {str(err)}")
            raise err

    @staticmethod
    def save_yaml_file(filename: str, yaml_obj: any):
        """
        Save the yaml file using the filename passed in.
        :param filename: name of YAML file
        :param yaml_obj: yaml data to save to file
        :raises FileNotFoundError, YAMLError:
        """
        try:
            with open(filename, "w", encoding="utf-8") as file:
                yaml.dump(yaml_obj, file)
        except FileNotFoundError as err:
            logging.error(f"Could not find file {filename}: {str(err)}")
            raise err
        except yaml.YAMLError as err:
            logging.error(f"Could not dump YAML data to file {filename}: {str(err)}")
            raise err
