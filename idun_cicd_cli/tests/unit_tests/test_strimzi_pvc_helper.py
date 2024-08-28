"""
Unit tests for strimzi_pvc_helper.py
"""
import unittest
from unittest.mock import patch
from idun_cicd_cli.src.helpers.pvc.common_pvc_helper import CommonPvcHelper
from idun_cicd_cli.src.helpers.pvc.strimzi_pvc_helper import StrimziPvcHelper


class TestStrimziPvcHelper(unittest.TestCase):
    """
    Class to run unit tests for strimzi_pvc_helper.py
    """
    @staticmethod
    @patch.object(CommonPvcHelper, "run_kube_command")
    def test_scale_current_szkf_cluster_operator(mock_run_kube_command):
        """
        Tests test_scale_current_szkf_cluster_operator
        runs scale up and scale down command
        :param mock_run:
        """
        strimzi_pvc_helper = StrimziPvcHelper()
        strimzi_pvc_helper.scale_current_szkf_cluster_operator("my_namespace", 3)
        expected_command = (
            "kubectl scale deployment "
            "eric-oss-dmm-kf-op-sz-cluster-operator "
            "-n my_namespace --replicas 3"
        )
        mock_run_kube_command.assert_called_once_with(expected_command)
        mock_run_kube_command.reset_mock()
        strimzi_pvc_helper.scale_current_szkf_cluster_operator("my_namespace", 1)
        expected_command = (
            "kubectl scale deployment "
            "eric-oss-dmm-kf-op-sz-cluster-operator "
            "-n my_namespace --replicas 1"
        )
        mock_run_kube_command.assert_called_once_with(expected_command)
