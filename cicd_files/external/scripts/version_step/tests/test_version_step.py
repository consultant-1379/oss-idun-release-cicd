import pytest
import sys
sys.path.append('cicd_files/external/scripts/version_step')
from version_step import step_version

# pylint: disable=no-self-use
class TestVersionStep():
    """
    Class to run unit tests for version_step.py
    """

    def test_patch_version_step(self):
        assert step_version('3.0.1-20', 'PATCH') == ('3.0.2-1', '3.0.1-20')

    def test_minor_version_step(self):
        assert step_version('3.0.1-20', 'MINOR'), ('3.1.0-1', '3.0.1-20')

    def test_major_version_step(self):
        assert step_version('3.0.1-20', 'MAJOR'), ('4.0.0-1', '3.0.1-20')
