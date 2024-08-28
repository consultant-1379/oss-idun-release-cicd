import pytest
from scripts.select_site_values import get_path_to_site_values

# pylint: disable=no-self-use
class TestSiteValues:
    """
    Class to run unit tests for select_site_values.py
    """

    def test_select_site_values_version_first(self):
        assert get_path_to_site_values('2.0.0-336') == 'site-values-1.0.0.yaml'

    def test_select_site_values_version_second(self):
        assert get_path_to_site_values('2.0.0-505') == 'site-values-1.1.0.yaml'

    def test_select_site_values_version_third(self):
        assert get_path_to_site_values('2.0.0-506') == 'site-values-1.1.1.yaml'

    def test_select_site_values_version_fourth(self):
        assert get_path_to_site_values('2.0.0-790') == 'site-values-1.1.1.yaml'

    def test_select_site_values_version_fifth(self):
        assert get_path_to_site_values('2.0.0-336') == 'site-values-1.0.0.yaml'

    def test_select_site_values_version_sixth(self):
        assert get_path_to_site_values('2.0.0-1000') == 'site-values-1.1.2.yaml'

    def test_select_site_values_version_seventh(self):
        assert get_path_to_site_values('2.0.0-1011') == 'site-values-1.1.2.yaml'

    def test_select_site_values_version_eighth(self):
        assert get_path_to_site_values('2.0.0-791') == 'site-values-1.1.1.yaml'

    def test_select_site_values_version_ninth(self):
        assert get_path_to_site_values('2.0.0-792') == 'site-values-1.1.2.yaml'

    def test_select_site_values_version_tenth(self):
        assert get_path_to_site_values('2.0.0-1043') == 'site-values-1.1.2.yaml'

    def test_select_site_values_version_eleventh(self):
        assert get_path_to_site_values('2.0.0-1044') == 'site-values-2.0.0-1484.yaml'

    def test_select_site_values_version_twelfth(self):
        assert get_path_to_site_values('2.1.0-0') == 'site-values-2.0.0-1484.yaml'

    def test_select_site_values_version_thirteenth(self):
        assert get_path_to_site_values('2.1.0-1') == 'site-values-2.1.0-220.yaml'

    def test_select_site_values_version_fourteenth(self):
        assert get_path_to_site_values('2.2.0-2') == 'site-values-latest.yaml'

    def test_select_site_values_version_major(self):
        assert get_path_to_site_values('3.0.0-2000') == 'site-values-latest.yaml'

    def test_select_site_values_azure_version_minor(self):
        assert get_path_to_site_values('2.0.0-336') == 'site-values-1.0.0.yaml'

    def test_select_site_values_version_minor(self):
        assert get_path_to_site_values('2.1.0-2000') == 'site-values-2.1.0-220.yaml'

    def test_select_site_values_azure_version_major(self):
        assert get_path_to_site_values('3.0.0-2000') == 'site-values-latest.yaml'
