"""
Unit tests for release_pvc_update.py
"""
import unittest
from unittest.mock import patch
from idun_cicd_cli.src.operators.release_pvc_update import ReleasePvcUpdate
from idun_cicd_cli.src.helpers.pvc.common_pvc_helper import CommonPvcHelper


class TestReleasePVCUpdate(unittest.TestCase):
    """
    Class to run unit tests for release_pvc_update.py
    """
    def setUp(self):
        self.namespace = "test_namespace"
        self.seli_username = "test_username"
        self.seli_password = "test_password"
        self.dmm_version = "test_version"
        self.release_pvc_update = ReleasePvcUpdate(
            self.namespace, self.seli_username, self.seli_password, self.dmm_version
        )

    @patch.object(CommonPvcHelper, "get_pvc_size_in_dmm_chart")
    @patch.object(CommonPvcHelper, "get_pvc_size")
    @patch.object(ReleasePvcUpdate, "update_pvc_value_for_adp")
    @patch.object(ReleasePvcUpdate, "update_pvc_value_for_strimzi")
    def test_release_pvc_with_update(
        self,
        mock_update_pvc_value_for_strimzi,
        mock_update_pvc_value_for_adp,
        mock_get_pvc_size,
        mock_get_pvc_size_in_dmm_chart,
    ):
        """
        Tests test_release_pvc_with_update
        """
        mock_get_pvc_size.return_value = "20Gi"
        mock_get_pvc_size_in_dmm_chart.return_value = "200Gi"
        self.release_pvc_update.release_pvc_update()
        mock_update_pvc_value_for_adp.assert_called_with(self.namespace, "adp", "200Gi")
        mock_update_pvc_value_for_strimzi.assert_called_with(self.namespace, "strimzi", "200Gi")

    @patch.object(CommonPvcHelper, "get_pvc_size_in_dmm_chart")
    @patch.object(CommonPvcHelper, "get_pvc_size")
    @patch.object(ReleasePvcUpdate, "update_pvc_value_for_adp")
    @patch.object(ReleasePvcUpdate, "update_pvc_value_for_strimzi")
    def test_release_pvc_without_update(
        self,
        mock_update_pvc_value_for_strimzi,
        mock_update_pvc_value_for_adp,
        mock_get_pvc_size,
        mock_get_pvc_size_in_dmm_chart,
    ):
        """
        Tests test_release_pvc_without_update
        """
        mock_get_pvc_size.return_value = "200Gi"
        mock_get_pvc_size_in_dmm_chart.return_value = "200Gi"
        self.release_pvc_update.release_pvc_update()
        mock_update_pvc_value_for_adp.assert_not_called()
        mock_update_pvc_value_for_strimzi.assert_not_called()

    @patch.object(CommonPvcHelper, "get_pvc_size_in_dmm_chart")
    @patch.object(CommonPvcHelper, "get_pvc_size")
    def test_is_pvc_update_needed_true(self, mock_get_pvc_size, mock_get_pvc_size_in_dmm_chart):
        """
        Tests test_is_pvc_update_needed_true
        """
        mock_get_pvc_size.return_value = "200Gi"
        mock_get_pvc_size_in_dmm_chart.return_value = "200Gi"
        expected_result = (False, "200Gi")
        result = self.release_pvc_update.is_pvc_update_needed('test-kafka', 'test-statefulset')
        self.assertEqual(result, expected_result)

    @patch.object(CommonPvcHelper, "get_pvc_size_in_dmm_chart")
    @patch.object(CommonPvcHelper, "get_pvc_size")
    def test_is_pvc_update_needed_false(self, mock_get_pvc_size, mock_get_pvc_size_in_dmm_chart):
        """
        Tests test_is_pvc_update_needed_false
        """
        mock_get_pvc_size.return_value = "20Gi"
        mock_get_pvc_size_in_dmm_chart.return_value = "200Gi"
        expected_result = (True, "200Gi")
        result = self.release_pvc_update.is_pvc_update_needed('test-kafka', 'test-statefulset')
        self.assertEqual(result, expected_result)

    @patch.object(CommonPvcHelper, "compare_kafka_pvc_against_new_pvc")
    @patch.object(CommonPvcHelper, "get_pvc_size_in_dmm_chart")
    @patch.object(CommonPvcHelper, "get_pvc_size")
    def test_adp_kafka_not_found_in_dmm_chart(
        self,
        mock_get_pvc_size,
        mock_get_pvc_size_in_dmm_chart,
        mock_compare_kafka_pvc_against_new_pvc
    ):
        """
        Tests test_adp_kafka_not_found_in_dmm_chart
        """
        mock_get_pvc_size_in_dmm_chart.return_value = None
        self.release_pvc_update.release_pvc_update()
        mock_get_pvc_size.assert_not_called()
        mock_compare_kafka_pvc_against_new_pvc.assert_not_called()
