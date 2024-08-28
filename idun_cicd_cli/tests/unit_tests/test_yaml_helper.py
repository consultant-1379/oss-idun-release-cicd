"""
Unit tests for yaml_helper.py
"""

import unittest
import os
from idun_cicd_cli.src.helpers.yaml.yaml_helper import YamlHelper


class TestYamlHelper(unittest.TestCase):
    """
    Class to run unit tests for yaml_helper.py
    """

    def test_load_yaml_file(self):
        """
        Tests test_load_yaml_file
        runs yaml load command
        :param mock_run:
        """
        yaml_helper = YamlHelper()
        sample_data = {"foo": "bar"}
        yaml_helper.save_yaml_file("sample.yaml", sample_data)

        result = yaml_helper.load_yaml_file("sample.yaml")

        self.assertEqual(result, sample_data)
        os.remove("sample.yaml")

    def test_save_yaml_file(self):
        """
        Tests test_save_yaml_file
        runs yaml dump command
        """
        yaml_helper = YamlHelper()
        sample_data = {"foo": "bar"}
        yaml_helper.save_yaml_file("sample.yaml", sample_data)

        result = yaml_helper.load_yaml_file("sample.yaml")

        self.assertEqual(result, sample_data)
        os.remove("sample.yaml")
