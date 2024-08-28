"""
The operator for handling artifact properties files.
"""

import os
import logging

from idun_cicd_cli.src.etc.exceptions import (
    InDirectoryNotSetException,
    InFileNotSetException,
    KeyValuePairsNotSetException
)


class ArtifactProperties:
    """
    Class to handle artifact properties files.
    """
    def __init__(self, key_value_pairs=None, out_directory='/usr/src/app/out',
                 out_file='artifact.properties', mode='w'):
        self.key_value_pairs = key_value_pairs
        self.out_directory = out_directory
        self.out_file = out_file
        self.mode = mode

    def generate(self):
        """
        Method to generate an artifact properties file.
        """
        logging.info('Generating artifact.properties file')
        if not self.key_value_pairs:
            raise KeyValuePairsNotSetException('Attempted to generate artifact properties with no keys/values.')

        if not os.path.exists(self.out_directory):
            logging.debug(f'Creating directory "{self.out_directory}" as it does not exist.')
            os.makedirs(self.out_directory)

        file_path = f'{self.out_directory}/{self.out_file}'
        with open(file_path, self.mode, encoding="utf-8") as out_file:
            logging.debug(f'Writing to file "{file_path}".')
            for key in self.key_value_pairs:
                logging.info(f'Writing key/value to file: {key}={self.key_value_pairs[key]}')
                out_file.write(f'{key}={self.key_value_pairs[key]}\n')

    def read(self, in_directory='/usr/src/app/in', in_file='artifact.properties'):
        """
        Method to read in an artifact properties file.
        :param in_directory:
        :param in_file:
        :return: key_value_pairs
        :rtype: dict
        """
        logging.info('Reading in artifact properties file.')
        file_path = f'{in_directory}/{in_file}'

        if not in_directory:
            raise InDirectoryNotSetException('No in_directory specified.')
        if not in_file:
            raise InFileNotSetException('No in_file specified.')
        if not os.path.exists(in_directory):
            raise InDirectoryNotSetException('The specified in_directory does not exist.')
        if not os.path.isfile(file_path):
            raise InFileNotSetException('The specified in_file does not exist.')
        if self.key_value_pairs is None:
            self.key_value_pairs = {}

        with open(file_path, 'r', encoding="utf-8") as in_stream:
            logging.debug(f'Reading from file "{file_path}".')
            for line in in_stream:
                key_value_pair = line.split('=')
                logging.debug(f'Storing {key_value_pair[1]} at [{key_value_pair[0]}] in self.key_value_pairs.')
                self.key_value_pairs[key_value_pair[0]] = key_value_pair[1].rstrip()

        return self.key_value_pairs
