"""
Module contains the common PVC helper class
"""
import json
import logging
import tarfile
import os
import shutil
import tempfile
import time
from subprocess import CalledProcessError, Popen, PIPE, TimeoutExpired, run
import requests
from idun_cicd_cli.src.helpers.yaml.yaml_helper import YamlHelper


class CommonPvcHelper:
    """
    Helper class containing common methods used when modifying
    the PVC size for DMM statefulset files
    """

    def __init__(self):
        self.pod_range = 3
        self.yaml_helper = YamlHelper()

    @staticmethod
    def run_kube_command(kubectl_command: str, cli_command: str = "") -> str:
        """
        Executes a kubectl command against a Kubernetes cluster.
        :param kubectl_command: The kubectl command to execute as a string.
        :param cli_command: (optional) The CLI command to execute as a string.
        :return: The output of the command as a string.
        :raises: subprocess.CalledProcessError if the command returns a non-zero exit code.
        :rtype: str
        """
        full_command = f"{kubectl_command} --kubeconfig=kube_config/config {cli_command}"
        logging.info(f"Running the following CLI command: {full_command}")
        try:
            result = run(full_command, shell=True, stdout=PIPE, stderr=PIPE, check=True, text=True)
            return result.stdout
        except CalledProcessError as err:
            logging.error(f"Error executing command: {err}")
            raise err
        except TimeoutExpired as err:
            logging.error(f"Command timed out: {err}")
            raise err

    def get_pvc_size(self, kafka_type: str, namespace: str) -> str:
        """
        Check the size of the Persistent Volume Claim
        for a given DMM Kafka on the Environment
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :param namespace: The Kubernetes namespace where the deployment is running.
        :return: The size of the Persistent Volume Claim as a string.
        :raises: ValueError if the specified kafka_type is invalid.
        :rtype: str
        """
        try:
            logging.info(f"Checking PVC size for {kafka_type}")
            statefulset = self.get_kafka_statefulset_name(kafka_type)
            get_pvc_command_for_dmm_kafka = (
                f"kubectl get sts {statefulset} -n {namespace} -o json"
            )
            dmm_kafka_pvc_value = self.run_kube_command(get_pvc_command_for_dmm_kafka)
            dmm_kafka_pvc_value_json = json.loads(dmm_kafka_pvc_value)
            dmm_kafka_pvc_value = dmm_kafka_pvc_value_json["spec"][
                "volumeClaimTemplates"
                ][0]["spec"]["resources"]["requests"]["storage"]
            logging.info(f"PVC size for {statefulset} is {dmm_kafka_pvc_value}")
            return dmm_kafka_pvc_value
        except Exception as err:
            logging.error(f"Error occurred while checking PVC size: {str(err)}")
            raise err

    def get_pvc_size_in_dmm_chart(
        self, username: str, password: str, dmm_version: str, kafka_type: str, kafka_statefulset: str
    ) -> str:
        """
        Check the pvc size of a specific kafka statefulset in the dmm chart used in the EIAE helmfile
        :param username: The username to use when authenticating with the DMM chart.
        :param password: The password to use when authenticating with the DMM chart.
        :param dmm_version: The version of the DMM chart to use.
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :param kafka_statefulset: The name of the Kafka statefulset to check the PVC size for.
        :return: The size of the PVC associated with the specified Kafka statefulset.
        :raises: RequestException
        :raises: TarError
        :raises: ValueError if the specified kafka_type is invalid.
        :rtype: str
        """
        logging.info(f"Checking PVC size in DMM chart eric-oss-dmm-{dmm_version}")
        dmm_tgz_url = (
            "https://arm.seli.gic.ericsson.se/artifactory/proj-eric-oss-drop-helm"
            + f"/eric-oss-dmm/eric-oss-dmm-{dmm_version}.tgz"
        )
        dir_path = "eric-oss-dmm"

        try:
            logging.info(f"Downloading eric-oss-dmm-{dmm_version} from artifactory")
            response = requests.get(dmm_tgz_url, auth=(username, password), timeout=10)
            response.raise_for_status()

            logging.info("Writing the contents of the tgz file to a temporary file")
            with tempfile.NamedTemporaryFile(delete=False) as temp_file:
                temp_file.write(response.content)
                temp_file_path = temp_file.name
                temp_file.close()

            logging.info("Extracting the conents of the tgz file")
            with tarfile.open(temp_file_path) as tar:
                tar.extractall()
                tar.close()

            logging.info("Loading the contents of the yaml file")
            values_file_path = os.path.join(dir_path, "values.yaml")
            contents = self.yaml_helper.load_yaml_file(values_file_path)
            logging.info(f"Accessing the PVC size for {kafka_type} in DMM version file")
            if contents.get(kafka_statefulset) is not None:
                if kafka_type == "adp":
                    pvc_value = contents[kafka_statefulset]["persistence"][
                        "persistentVolumeClaim"
                        ]["size"]
                elif kafka_type == "strimzi":
                    pvc_value = contents[kafka_statefulset]["kafka-cluster"]["kafka"][
                        "jbod"
                        ]["size"]
                else:
                    raise ValueError(f"Unknown kafka type: {kafka_type}")
            else:
                pvc_value = None
            logging.info(f"PVC size for {kafka_statefulset} in DMM chart eric-oss-dmm-{dmm_version} is {pvc_value}")
            return pvc_value
        except (
            requests.exceptions.RequestException,
            tarfile.TarError,
            ValueError,
        ) as err:
            logging.error("Error while checking PVC size in DMM chart: %s", err)
            raise err
        finally:
            os.remove(temp_file_path)
            if os.path.exists(dir_path):
                shutil.rmtree(dir_path)

    @staticmethod
    def compare_kafka_pvc_against_new_pvc(pvc_size_on_env: str, pvc_size_in_dmm_chart: str) -> bool:
        """
        Compare PVC value on ENV against new PVC value in the DMM chart
        :param pvc_size_on_env: The size of the PVC associated with the Kafka deployment on the environment.
        :param pvc_size_in_dmm_chart: The new size of the PVC specified in the DMM chart.
        :return: True if the PVC size in the DMM chart is different from the PVC size on the environment, else False
        :rtype: bool
        """
        logging.info("Compare PVC value on ENV against new PVC value in the DMM chart ")
        update_pvc_in_statefulset_file = False
        message = "PVC on Environment is the same as in DMM chart, no PVC adjustment needed"
        if pvc_size_on_env != pvc_size_in_dmm_chart:
            update_pvc_in_statefulset_file = True
            message = "PVC on Environment is different then that in the DMM chart, PVC adjustment needed"
        logging.info(message)
        return update_pvc_in_statefulset_file

    def backup_of_existing_statefulset_kafka_file(self, kafka_type: str, namespace: str):
        """
        Backup existing statefulset kafka files
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :param namespace: The Kubernetes namespace where the deployment is running.
        :raises: exception
        """
        logging.info(f"Creating backup existing sts kafka file for {kafka_type}")
        try:
            statefulset = self.get_kafka_statefulset_name(kafka_type)
            backup_statefulset_file_command = (
                f"kubectl get statefulset {statefulset} -n {namespace} -o yaml"
            )
            backup_yaml_data = self.run_kube_command(backup_statefulset_file_command)
            with open(
                f"BACKUP_{kafka_type.upper()}_STATEFULSET_FILE.yaml",
                "w",
                encoding="utf8",
            ) as file:
                file.write(backup_yaml_data)
        except Exception as err:
            logging.error(
                f"Error backing up {kafka_type.upper()} statefulset file: {str(err)}"
            )
            raise err

    def delete_current_dmm_statefulset(self, namespace: str, kafka_type: str):
        """
        Delete current dmm statefulset
        :param namespace: The Kubernetes namespace where the deployment is running.
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :type kafka_type: str
        :raises: exception
        """
        try:
            logging.info(f"Deleting the current statefulset for {kafka_type}")
            statefulset = self.get_kafka_statefulset_name(kafka_type)
            delete_current_statefulset_command = (
                f"kubectl delete statefulset.apps/{statefulset}"
                f" -n {namespace} --cascade=false"
            )
            self.run_kube_command(delete_current_statefulset_command)
        except Exception as err:
            logging.error(
                f"An error occurred while deleting the current statefulset for {kafka_type}: {err}"
            )
            raise err

    def delete_crashed_dmm_pods_and_pvc(self, namespace: str, kafka_type: str):
        """
        Delete dmm pods and pvc
        :param namespace: The Kubernetes namespace where the deployment is running.
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :raises: Exception
        """
        try:
            statefulset = self.get_kafka_statefulset_name(kafka_type)
            if kafka_type == "adp":
                pvc = f"datadir-{statefulset}"
            else:
                pvc = f"data-0-{statefulset}"

            logging.info(f"Deleting DMM crashed pods and pvc for {kafka_type}")
            for index in range(self.pod_range):
                delete_pods_command = f"kubectl delete pod {statefulset}-{index} -n {namespace}"
                self.run_kube_command(delete_pods_command)

            for index in range(self.pod_range):
                delete_pvc_of_pods_command = (
                    f"kubectl delete pvc {pvc}-{index} -n {namespace}"
                )
                self.run_kube_command(delete_pvc_of_pods_command)
        except Exception as err:
            logging.error(
                f"Error deleting DMM crashed pods and pvc for {kafka_type}: {str(err)}"
            )
            raise err

    @staticmethod
    def recreate_statefulset(namespace: str, kafka_type: str):
        """
        Recreate message bus kafka stateful set using backup statefulset file
        :param namespace: The Kubernetes namespace where the deployment is running.
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :raises: CalledProcessError
        """
        logging.info("Recreating message bus Kafka stateful set...")
        filename = f"BACKUP_{kafka_type.upper()}_STATEFULSET_FILE.yaml"

        logging.info('Reading the contents of the YAML file')
        file = open(filename, "r", encoding="utf8")
        yaml_file_contents = file.read()

        create_resources_kubectl_cmd = ['kubectl', 'create', '-f', '-', '-n', namespace,
                                        '--kubeconfig', 'kube_config/config']

        try:
            logging.info('Create a Popen object to execute the command on the remote server')
            kubectl_proc = Popen(create_resources_kubectl_cmd, shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
            logging.info('Passing the contents of the YAML file to the commands standard input')
            kubectl_proc.communicate(yaml_file_contents.encode())
        except CalledProcessError as err:
            logging.error(f"Error occurred while executing command: {err.stderr.decode()}")
            raise err

    def check_are_dmm_pods_running(self, namespace: str, kafka_type: str):
        """
        Check if the message bus kafka pods are back up and running
        :param namespace: The Kubernetes namespace where the deployment is running.
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :return: True if all DMM pods are running, False otherwise.
        :raises: Exception
        """
        try:
            logging.info("Checking if the message bus kafka pods are back up and running")
            statefulset = self.get_kafka_statefulset_name(kafka_type)
            start_time = time.time()
            timeout = 60
            pod_0_running = False
            pod_1_running = False
            pod_2_running = False
            check_pods_are_running_kubectl_command = (
                f"kubectl get pods -n {namespace}"
            )
            check_pods_is_running_cli_command = f"| grep {statefulset}"
            while not (pod_0_running and pod_1_running and pod_2_running):
                output = self.run_kube_command(
                    check_pods_are_running_kubectl_command, check_pods_is_running_cli_command
                    ).split("\n")
                for line in output[:-1]:
                    if "Running" in line and statefulset in line:
                        pod_name = line.split()[0]
                        logging.info(f"Pod {pod_name} is Running")
                        if pod_name.endswith("-0"):
                            pod_0_running = True
                        elif pod_name.endswith("-1"):
                            pod_1_running = True
                        elif pod_name.endswith("-2"):
                            pod_2_running = True
                    else:
                        logging.info(f"Pod status: {line}")
                        if time.time() - start_time >= timeout:
                            raise TimeoutError(
                                "Timed out waiting for message bus kafka pods to be Running"
                            )
                        time.sleep(5)
        except Exception as err:
            logging.error(
                f"Error checking if the message bus kafka pods are back up and running: {str(err)}"
            )
            raise err

    def check_pvc_capacity(self, namespace: str, kafka_type: str, new_pvc_value: str):
        """
        Check the PVC capacity to ensure it matches that of the new PVC value
        :param namespace: The namespace where the Kafka deployment is located.
        :param kafka_type: The type of Kafka deployment (e.g. "strimzi", "confluent") to check the PVC capacity for.
        :param new_pvc_value: The expected new capacity of the PVC.
        :raises: Exception
        """
        try:
            logging.info(f"Checking the PVC capacity for {kafka_type}")
            statefulset = self.get_kafka_statefulset_name(kafka_type)
            check_pvc_kubectl_command = f"kubectl get pvc -n {namespace}"
            check_pvc_cli_command = f"| grep {statefulset}"
            output = self.run_kube_command(check_pvc_kubectl_command, check_pvc_cli_command).split("\n")
            for line in output[:-1]:
                if new_pvc_value in line and statefulset in line:
                    actual_pvc_value = line.split()[3]
                    if actual_pvc_value == new_pvc_value:
                        logging.info(f"PVC has been updated, PVC value: {actual_pvc_value}")
                    else:
                        raise AssertionError(
                            f"Actual PVC value ({actual_pvc_value}) does not match expected PVC value ({new_pvc_value})"
                        )
                else:
                    raise AssertionError(
                        f"PVC of {new_pvc_value} for {statefulset} has not been found in line {line} "
                        "PVC value has not been updated"
                    )
        except Exception as err:
            logging.error(
                f"Error checking the PVC capacity for {kafka_type}: {str(err)}"
            )
            raise err

    @staticmethod
    def get_kafka_statefulset_name(kafka_type: str) -> str:
        """
        Get the name of the Kafka statefulset for a given type of Kafka
        :param kafka_type: The type of DMM Kafka deployment to check (e.g. "adp" or "strimzi").
        :return: The name of the Kafka statefulset for the specified Kafka deployment type.
        :raises: ValueError
        :rtype: str
        """
        if kafka_type == "adp":
            return "eric-oss-dmm-data-message-bus-kf"
        if kafka_type == "strimzi":
            return "eric-oss-dmm-kf-op-sz-kafka"
        raise ValueError(f"Invalid kafka_type: {kafka_type}")
