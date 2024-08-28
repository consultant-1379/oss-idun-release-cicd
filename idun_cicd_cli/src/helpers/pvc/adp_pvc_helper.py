"""
Module contains the ADP PVC helper class.
"""

import logging
from idun_cicd_cli.src.helpers.yaml.yaml_helper import YamlHelper


class AdpPvcHelper:
    """
    Helps manage PVCs for the ADP platform.
    """

    def __init__(self):
        self.yaml_helper = YamlHelper()

    def update_storage_value_in_backup_mbkf_statefulset_file(self, kafka_type: str, new_pvc_size: str):
        """
        Updates the PVC storage value in the backup mbkf statefulset file
        to the new size.
        :param kafka_type: The type of Kafka deployment being updated (e.g. "adp" or "strimzi").
        :param new_pvc_size: The new size of the PVC.
        :raises YAMLError:
        """
        logging.info(
            "Updating PVC value to %s in backup mbkf statefulset file",
            new_pvc_size
        )
        filename = f"BACKUP_{kafka_type.upper()}_STATEFULSET_FILE.yaml"
        yaml_obj = self.yaml_helper.load_yaml_file(filename)
        yaml_obj["spec"]["volumeClaimTemplates"][0][
            "spec"]["resources"]["requests"]["storage"] = new_pvc_size

        logging.info(f"Save {filename} YAML file")
        self.yaml_helper.save_yaml_file(filename, yaml_obj)
