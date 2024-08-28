"""
Unit tests for retrigger_aas_e2e_flows.py
"""

import pytest
from idun_cicd_cli.src.operators.retrigger_aas_e2e_flows import RetriggerAASE2EFlows
from idun_cicd_cli.tests.unit_tests.mock_response import MockResponse

VALID_JSON_STRING = '''{
    "key": "value"
}'''

INVALID_JSON_STRING = '''{
    "key": "value"
    "another": "one"
}'''


# pylint: disable=no-self-use,unused-argument
class TestRetriggerAASE2EFlows:
    """
    Class to run unit tests for retrigger_aas_e2e_flows.py
    """

    def test_convert_response_to_json(self):
        """
        Test that we can convert a response to JSON successfully
        """
        retrigger_instance = RetriggerAASE2EFlows("user", "pass")
        mock_response = MockResponse(VALID_JSON_STRING, 200)
        actual_response = retrigger_instance.__convert_response_to_json__(mock_response)
        expected_response = {
            "key": "value"
        }
        assert actual_response == expected_response

    def test_convert_response_to_json_invalid(self):
        """
        Test that we handle the scenario where we get an invalid response from a request
        """
        retrigger_instance = RetriggerAASE2EFlows("user", "pass")
        mock_response = MockResponse(INVALID_JSON_STRING, 200)
        with pytest.raises(Exception) as exception:
            retrigger_instance.__convert_response_to_json__(mock_response)
        expected_exception_value = \
            'Got invalid response from request and could not convert it to JSON!'
        assert str(exception.value) == expected_exception_value
