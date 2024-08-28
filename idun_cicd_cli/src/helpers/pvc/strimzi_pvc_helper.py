"""
Module contains the Strimzi PVC helper class
"""

import logging
import json
import yaml
from idun_cicd_cli.src.helpers.pvc.common_pvc_helper import CommonPvcHelper
from idun_cicd_cli.src.helpers.yaml.yaml_helper import YamlHelper


class StrimziPvcHelper:
    """
    Helps run kubectl by providing a wrapper around the functionality that changes how we run
    kubectl commands based on what platform we are running against.
    """

    def __init__(self):
        self.common_pvc_helper = CommonPvcHelper()
        self.yaml_helper = YamlHelper()

    @staticmethod
    def update_storage_value_in_backup_szkf_statefulset_file(kafka_type: str, new_pvc_size: str):
        """
        Update PVC value to new PVC size in backup szkf file
        :param kafka_type: The type of Kafka deployment being updated (e.g. "adp" or "strimzi").
        :param new_pvc_size: The new size of the PVC.
        :return: None
        """
        logging.info(f"Updating PVC value to {new_pvc_size} in backup szkf statefulset file")
        filename = f"BACKUP_{kafka_type.upper()}_STATEFULSET_FILE.yaml"

        logging.info("Loading backup statefulset YAML file")
        try:
            with open(filename, "r", encoding="utf-8") as file:
                yaml_obj = yaml.safe_load(file)
        except yaml.YAMLError as err:
            logging.error(f"Could not parse YAML file {filename}: {str(err)}")
            raise err

        logging.info("Change PVC storage value in backup yaml file")
        input_str = yaml_obj['metadata']['annotations']['strimzi.io/storage']
        input_dict = json.loads(input_str)
        input_dict["volumes"][0]["size"] = new_pvc_size
        output_str = json.dumps(input_dict)
        yaml_obj['metadata']['annotations']['strimzi.io/storage'] = output_str

        input_str = yaml_obj['spec']['template']['metadata']['annotations']['strimzi.io/storage']
        input_dict = json.loads(input_str)
        input_dict["volumes"][0]["size"] = new_pvc_size
        output_str = json.dumps(input_dict)
        yaml_obj['spec']['template']['metadata']['annotations']['strimzi.io/storage'] = output_str

        yaml_obj['spec']['volumeClaimTemplates'][0]["spec"]["resources"]["requests"]["storage"] = new_pvc_size

        logging.info(f"Save {filename} YAML file")
        try:
            with open(filename, "w", encoding="utf-8") as file:
                yaml.dump(yaml_obj, file)
        except yaml.YAMLError as err:
            logging.error(f"Could not dump YAML data to file {filename}: {str(err)}")
            raise err

    def scale_current_szkf_cluster_operator(self, namespace: str, scale_value: int):
        """
        Scaling the cluster-operator down or up depending on input parameters
        :param namespace: The Kubernetes namespace where the SZKF cluster-operator is located.
        :param scale_value: The desired number of replicas for the SZKF cluster-operator.
        :return: None
        """
        try:
            logging.info(f"Scaling current szkf cluster-operator replicas to {scale_value}")
            scale_down_current_mbkf_statefulset_command = (
                "kubectl scale deployment "
                + "eric-oss-dmm-kf-op-sz-cluster-operator"
                + f" -n {namespace} --replicas {scale_value}"
            )
            output = self.common_pvc_helper.run_kube_command(scale_down_current_mbkf_statefulset_command)
            logging.info(output)
        except Exception as err:
            logging.error(
                f"Error scaling current szkf cluster-operator replicas to {scale_value}: {str(err)}"
            )
