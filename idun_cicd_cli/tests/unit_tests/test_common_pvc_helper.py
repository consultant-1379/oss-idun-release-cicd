"""
Unit tests for common_pvc_helper.py
"""
import unittest
from subprocess import PIPE, CalledProcessError, TimeoutExpired
from unittest.mock import Mock, mock_open, patch
from idun_cicd_cli.src.helpers.pvc.common_pvc_helper import CommonPvcHelper


class TestCommonPvcHelper(unittest.TestCase):
    """
    Class to run unit tests for common_pvc_helper.py
    """

    @patch("idun_cicd_cli.src.helpers.pvc.common_pvc_helper.run")
    def test_run_kube_command(self, mock_run):
        """
        Tests test_run_kube_command success
        :param mock_run:
        """
        common_pvc_helper = CommonPvcHelper()
        mock_run.return_value = Mock(stdout='command output', stderr='')
        kubectl_command = 'kubectl get pods'
        cli_command = '--namespace default'
        expected_command = f"{kubectl_command} --kubeconfig=kube_config/config {cli_command}"

        result = common_pvc_helper.run_kube_command(kubectl_command, cli_command)
        mock_run.assert_called_once_with(expected_command, shell=True, stdout=-1, stderr=-1, check=True, text=True)
        self.assertEqual(result, 'command output')

    @patch("idun_cicd_cli.src.helpers.pvc.common_pvc_helper.run")
    def test_run_kube_command_raises_called_process_error(self, mock_run):
        """
        Tests test_run_kube_command failure raises calledProcessError
        :param mock_run:
        """
        common_pvc_helper = CommonPvcHelper()
        mock_run.return_value = Mock(stdout='command output', stderr='')
        kubectl_command = 'kubectl get pods'
        cli_command = '--namespace default'
        expected_command = f"{kubectl_command} --kubeconfig=kube_config/config {cli_command}"

        mock_run.side_effect = CalledProcessError(1, 'failed command', output='error output')

        with self.assertRaises(CalledProcessError) as err:
            common_pvc_helper.run_kube_command(kubectl_command, cli_command)
        mock_run.assert_called_with(expected_command, shell=True, stdout=-1, stderr=-1, check=True, text=True)
        self.assertEqual(str(err.exception), 'Command \'failed command\' returned non-zero exit status 1.')

    @patch("idun_cicd_cli.src.helpers.pvc.common_pvc_helper.run")
    def test_run_kube_command_raises_timeout_expired(self, mock_run):
        """
        Tests test_run_kube_command failure raises TimeoutExpired
        :param mock_run:
        """
        common_pvc_helper = CommonPvcHelper()
        mock_run.return_value = Mock(stdout='command output', stderr='')
        kubectl_command = 'kubectl get pods'
        cli_command = '--namespace default'
        expected_command = f"{kubectl_command} --kubeconfig=kube_config/config {cli_command}"

        mock_run.side_effect = TimeoutExpired('command', 60)

        with self.assertRaises(TimeoutExpired) as error:
            common_pvc_helper.run_kube_command(kubectl_command, cli_command)
        mock_run.assert_called_with(expected_command, shell=True, stdout=-1, stderr=-1, check=True, text=True)
        self.assertEqual(str(error.exception), 'Command \'command\' timed out after 60 seconds')

    @patch("idun_cicd_cli.src.helpers.pvc.common_pvc_helper.run")
    def test_run_kube_command_failure(self, mock_run):
        """
        Tests test_run_kube_command failure
        :param mock_run:
        """
        common_pvc_helper = CommonPvcHelper()
        expected_error = CalledProcessError(
            1, "invalid_command", "Command 'invalid_command' not found"
        )
        mock_run.side_effect = expected_error

        with self.assertRaises(CalledProcessError):
            common_pvc_helper.run_kube_command("invalid_command")

    @patch.object(CommonPvcHelper, "get_kafka_statefulset_name")
    @patch.object(CommonPvcHelper, "run_kube_command")
    def test_get_pvc_size(
        self, mock_run_kube_command, mock_get_kafka_statefulset_name
    ):
        """
        Tests test_get_pvc_size runs succesfully
        :param mock_run_kube_command:
        :param mock_get_kafka_statefulset_name:
        """
        common_pvc_helper = CommonPvcHelper()
        kafka_type = "adp"
        namespace = "my-namespace"
        mock_get_kafka_statefulset_name.return_value = "my-kafka-statefulset"
        mock_run_kube_command.return_value = (
            '{"spec":{"volumeClaimTemplates":[{"spec":'
            '{"resources":{"requests":{"storage":"10Gi"}}}}]}}'
        )
        expected_value = "10Gi"

        result = common_pvc_helper.get_pvc_size(kafka_type, namespace)

        self.assertEqual(result, expected_value)
        mock_get_kafka_statefulset_name.assert_called_once_with(kafka_type)
        mock_run_kube_command.assert_called_once_with(
            "kubectl get sts my-kafka-statefulset -n my-namespace -o json"
        )

    def test_compare_pvc_same_value(self):
        """
        Tests test_compare_pvc_same_value with same values
        """
        common_pvc_helper = CommonPvcHelper()
        pvc_size_on_env = "10Gi"
        pvc_size_in_dmm_chart = "10Gi"

        update_pvc = common_pvc_helper.compare_kafka_pvc_against_new_pvc(
            pvc_size_on_env, pvc_size_in_dmm_chart
        )

        self.assertFalse(update_pvc)

    def test_compare_pvc_different_value(self):
        """
        Tests test_compare_pvc_same_value with different values
        """
        common_pvc_helper = CommonPvcHelper()
        pvc_size_on_env = "10Gi"
        pvc_size_in_dmm_chart = "20Gi"

        update_pvc = common_pvc_helper.compare_kafka_pvc_against_new_pvc(
            pvc_size_on_env, pvc_size_in_dmm_chart
        )

        self.assertTrue(update_pvc)

    @staticmethod
    @patch.object(CommonPvcHelper, "run_kube_command")
    def test_delete_current_dmm_statefulset(mock_run_kube_command):
        """
        Tests test_delete_current_dmm_statefulset is called
        """
        common_pvc_helper = CommonPvcHelper()
        common_pvc_helper.delete_current_dmm_statefulset("my-namespace", "adp")

        expected_command = (
            "kubectl delete statefulset.apps/"
            "eric-oss-dmm-data-message-bus-kf "
            "-n my-namespace --cascade=false"
        )
        mock_run_kube_command.assert_called_once_with(expected_command)

    @patch.object(CommonPvcHelper, "run_kube_command")
    def test_delete_crashed_dmm_pods_and_pvc(self, mock_run_kube_command):
        """
        Tests test_delete_crashed_dmm_pods_and_pvc all commands called
        """
        common_pvc_helper = CommonPvcHelper()
        namespace = "test-namespace"
        kafka_type = "adp"
        expected_pod = "eric-oss-dmm-data-message-bus-kf"
        expected_pvc = "datadir-eric-oss-dmm-data-message-bus-kf"

        common_pvc_helper.delete_crashed_dmm_pods_and_pvc(namespace, kafka_type)

        self.assertEqual(mock_run_kube_command.call_count, 6)
        expected_commands = [
            f"kubectl delete pod {expected_pod}-0 -n {namespace}",
            f"kubectl delete pod {expected_pod}-1 -n {namespace}",
            f"kubectl delete pod {expected_pod}-2 -n {namespace}",
            f"kubectl delete pvc {expected_pvc}-0 -n {namespace}",
            f"kubectl delete pvc {expected_pvc}-1 -n {namespace}",
            f"kubectl delete pvc {expected_pvc}-2 -n {namespace}",
        ]
        actual_commands = [args[0][0] for args in mock_run_kube_command.call_args_list]
        self.assertListEqual(actual_commands, expected_commands)

    @staticmethod
    @patch(
        "idun_cicd_cli.src.helpers.pvc.common_pvc_helper.open",
        new_callable=mock_open,
        read_data="test_yaml_file_contents",
    )
    @patch("idun_cicd_cli.src.helpers.pvc.common_pvc_helper.Popen")
    def test_recreate_statefulset_success(mock_popen, mock_open):
        """
        Tests test_recreate_statefulset_success all commands called
        """
        common_pvc_helper = CommonPvcHelper()
        namespace = "test-namespace"
        kafka_type = "adp"
        expected_kubectl_cmd = [
            "kubectl",
            "create",
            "-f",
            "-",
            "-n",
            namespace,
            "--kubeconfig",
            "kube_config/config",
        ]

        common_pvc_helper.recreate_statefulset(namespace, kafka_type)

        mock_open.assert_called_once_with(
            "BACKUP_ADP_STATEFULSET_FILE.yaml", "r", encoding="utf8"
        )
        mock_popen.assert_called_once_with(
            expected_kubectl_cmd, shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE
        )
        mock_popen.return_value.communicate.assert_called_once_with(
            "test_yaml_file_contents".encode()
        )

    @patch.object(CommonPvcHelper, "get_kafka_statefulset_name")
    @patch.object(CommonPvcHelper, "run_kube_command")
    def test_check_are_dmm_pods_running(
        self, mock_run_kube_command, mock_get_kafka_statefulset_name
    ):
        """
        Tests test_run_kube_command
        """
        common_pvc_helper = CommonPvcHelper()
        namespace = "test-namespace"
        kafka_type = "test-kafka-type"
        statefulset = "test-kafka-statefulset"
        mock_output = (
            f"{statefulset}-0    1/1     Running     0          1m\n"
            f"{statefulset}-1    1/1     Running     0          1m\n"
            f"{statefulset}-2    1/1     Running     0          1m\n"
        )
        mock_get_kafka_statefulset_name.return_value = statefulset
        mock_run_kube_command.return_value = mock_output

        common_pvc_helper.check_are_dmm_pods_running(namespace, kafka_type)
        expected_kubectl_command = f"kubectl get pods -n {namespace}"
        expected_cli_command = f"| grep {statefulset}"
        mock_run_kube_command.assert_called_once_with(
            expected_kubectl_command, expected_cli_command
        )

        self.assertTrue(mock_run_kube_command.called)
        self.assertTrue(mock_run_kube_command.call_count == 1)
        self.assertTrue("Running" in mock_output)
        self.assertTrue(statefulset in mock_output)

    @patch.object(CommonPvcHelper, "get_kafka_statefulset_name")
    @patch.object(CommonPvcHelper, "run_kube_command")
    def test_check_pvc_capacity(
        self, mock_run_kube_command, mock_get_kafka_statefulset_name
    ):
        """
        Tests test_check_pvc_capacity
        """
        common_pvc_helper = CommonPvcHelper()
        namespace = "test-namespace"
        kafka_type = "test-kafka-type"
        statefulset = "test-kafka-statefulset"
        new_pvc_value = "2Gi"
        mock_get_kafka_statefulset_name.return_value = statefulset
        mock_run_kube_command.return_value = "pvc1 1Gi RWX\n"

        expected_output = (
            "PVC of 2Gi for test-kafka-statefulset has not been found in line pvc1 "
            "1Gi RWX PVC value has not been updated"
        )
        with self.assertRaisesRegex(AssertionError, expected_output):
            common_pvc_helper.check_pvc_capacity(namespace, kafka_type, new_pvc_value)

    def test_get_kafka_statefulset_name_adp(self):
        """
        Tests test_run_kube_command
        """
        kafka_type = "adp"
        expected_result = "eric-oss-dmm-data-message-bus-kf"
        common_pvc_helper = CommonPvcHelper()
        result = common_pvc_helper.get_kafka_statefulset_name(kafka_type)
        self.assertEqual(result, expected_result)

    def test_get_kafka_statefulset_name_strimzi(self):
        """
        Tests test_run_kube_command
        """
        kafka_type = "strimzi"
        expected_result = "eric-oss-dmm-kf-op-sz-kafka"
        common_pvc_helper = CommonPvcHelper()
        result = common_pvc_helper.get_kafka_statefulset_name(kafka_type)
        self.assertEqual(result, expected_result)

    def test_get_kafka_statefulset_name_invalid(self):
        """
        Tests test_run_kube_command
        """
        kafka_type = "invalid"
        common_pvc_helper = CommonPvcHelper()
        with self.assertRaises(ValueError):
            common_pvc_helper.get_kafka_statefulset_name(kafka_type)
