"""
Unit tests for artifact_properties.py
"""

import os
import pytest

from idun_cicd_cli.src.etc.artifact_properties import ArtifactProperties
from idun_cicd_cli.src.etc.exceptions import (
    InDirectoryNotSetException,
    InFileNotSetException,
    KeyValuePairsNotSetException
)


# pylint: disable=no-self-use
class TestArtifactProperties:
    """
    Class to run unit tests for artifact_properties.py
    """
    def test_generate_no_key_value_pairs(self):
        """
        Tests the generate function when no key value
        pairs are passed to the ArtifactProperties class. (negative case)
        """
        artifact_properties = ArtifactProperties({})
        with pytest.raises(KeyValuePairsNotSetException) as exception:
            artifact_properties.generate()
        expected_exception_value = \
            'Key Value Pairs Not Set Exception, Attempted to generate artifact properties with no keys/values.'
        assert str(exception.value) == expected_exception_value

    def test_generate_no_out_directory(self, tmp_path):
        """
        Tests the generate function when the specified
        out directory has not been created first. (semi positive case)
        :param tmp_path:
        """
        test_out_dir = tmp_path / 'inexistant_directory'
        str_path = str(test_out_dir)
        artifact_properties = ArtifactProperties({'myKey': 'myValue'}, out_directory=str_path)
        artifact_properties.generate()
        assert os.path.exists(str_path)

    def test_generate_with_key_value_pairs_and_out_directory(self, tmp_path):
        """
        Tests the generate function when the out directory
        exists and key value pairs are provided. (positive case)
        :param tmp_path:
        """
        test_out_dir = tmp_path / 'existing_directory'
        test_out_dir.mkdir(parents=True)
        str_path = str(test_out_dir)
        artifact_properties = ArtifactProperties({'myKey': 'myValue'}, out_directory=str_path)
        artifact_properties.generate()
        assert os.path.exists(str_path)

    def test_read_if_no_in_directory_specified(self):
        """
        Tests the read method when the in directory
        hasn't been specified.
        """
        artifact_properties = ArtifactProperties()
        with pytest.raises(InDirectoryNotSetException) as exception:
            artifact_properties.read(in_directory=None)
        expected_exception_value = \
            'In Directory Not Set Exception, No in_directory specified.'
        assert str(exception.value) == expected_exception_value

    def test_read_if_no_in_file_specified(self, tmp_path):
        """
        Tests the read method when the in file
        hasn't been specified.
        :param tmp_path:
        """
        in_directory = tmp_path / 'test_dir'
        artifact_properties = ArtifactProperties()
        with pytest.raises(InFileNotSetException) as exception:
            artifact_properties.read(in_directory, in_file=None)
        expected_exception_value = \
            'In File Not Set Exception, No in_file specified.'
        assert str(exception.value) == expected_exception_value

    def test_read_if_in_directory_does_not_exist(self):
        """
        Tests the read method when the in directory
        doesn't exist.
        """
        artifact_properties = ArtifactProperties()
        with pytest.raises(InDirectoryNotSetException) as exception:
            artifact_properties.read()
        expected_exception_value = \
            'In Directory Not Set Exception, The specified in_directory does not exist.'
        assert str(exception.value) == expected_exception_value

    def test_read_if_in_file_does_not_exist(self, tmp_path):
        """
        Tests the read method when the in file
        doesn't exist.
        :param tmp_path:
        """
        in_directory = tmp_path / 'test_dir'
        os.makedirs(str(in_directory))
        artifact_properties = ArtifactProperties()
        with pytest.raises(InFileNotSetException) as exception:
            artifact_properties.read(in_directory)
        expected_exception_value = \
            'In File Not Set Exception, The specified in_file does not exist.'
        assert str(exception.value) == expected_exception_value

    def test_read_if_everything_provided_and_exists(self, tmp_path):
        """
        Tests the read method when the in directory
        and in file are provided and exist.
        :param tmp_path:
        """
        in_directory = tmp_path / 'test_dir'
        os.makedirs(str(in_directory))
        in_file = 'artifact.properties'

        with open(f'{in_directory}/{in_file}', 'w', encoding="utf-8") as tmp_file:
            tmp_file.write('KEY=VALUE')

        artifact_properties = ArtifactProperties()
        key_value_pairs = artifact_properties.read(str(in_directory), in_file)
        assert key_value_pairs['KEY'] == 'VALUE'
