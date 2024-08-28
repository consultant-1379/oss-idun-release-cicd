"""
Release PVC Update Module
"""
import logging
from idun_cicd_cli.src.helpers.pvc.adp_pvc_helper import AdpPvcHelper
from idun_cicd_cli.src.helpers.pvc.common_pvc_helper import CommonPvcHelper
from idun_cicd_cli.src.helpers.pvc.strimzi_pvc_helper import StrimziPvcHelper


class ReleasePvcUpdate:
    """
    Release PVC Update Class
    """

    def __init__(self, namespace: str, seli_username: str, seli_password: str, dmm_version: str):
        self.namespace = namespace
        self.seli_username = seli_username
        self.seli_password = seli_password
        self.dmm_version = dmm_version
        self.adp_pvc_helper = AdpPvcHelper()
        self.common_pvc_helper = CommonPvcHelper()
        self.strimzi_pvc_helper = StrimziPvcHelper()

    def update_pvc_value_for_adp(self, namespace: str, kafka_type: str, new_pvc_value: str):
        """
        Updates the Persistent Volume Claim (PVC) value for a DMM ADP Kafka in the specified namespace.
        :param namespace: The namespace in which the DMM ADP Kafka cluster is running.
        :param kafka_type: The type of DMM ADP Kafka cluster to update the PVC value for.
        :param new_pvc_value: The new PVC value to set for the Kafka cluster, in the format `10Gi`.
        :raises: Any exceptions that occur while updating PVC size for DMM adp.
        """
        try:
            logging.info(f"Updating PVC value to {new_pvc_value} for ADP")
            self.common_pvc_helper.backup_of_existing_statefulset_kafka_file(
                kafka_type, namespace
            )
            self.adp_pvc_helper.update_storage_value_in_backup_mbkf_statefulset_file(
                kafka_type, new_pvc_value
            )
            self.common_pvc_helper.delete_current_dmm_statefulset(namespace, kafka_type)
            self.common_pvc_helper.delete_crashed_dmm_pods_and_pvc(namespace, kafka_type)
            self.common_pvc_helper.recreate_statefulset(namespace, kafka_type)
            self.common_pvc_helper.check_are_dmm_pods_running(namespace, kafka_type)
            self.common_pvc_helper.check_pvc_capacity(namespace, kafka_type, new_pvc_value)
        except Exception as err:
            raise err

    def update_pvc_value_for_strimzi(self, namespace: str, kafka_type: str, new_pvc_value: str):
        """
        Updates the Persistent Volume Claim (PVC) value for a DMM Strimzi Kafka in the specified namespace.
        :param namespace: The namespace in which the DMM Strimzi Kafka cluster is running.
        :param kafka_type: The type of DMM Strimzi Kafka cluster to update the PVC value for.
        :param new_pvc_value: The new PVC value to set for the Kafka cluster, in the format `10Gi`.
        :raises: Any exceptions that occur while updating PVC size for DMM strimzi.
        """
        try:
            logging.info(f"Updating PVC value to {new_pvc_value} for Strimzi")
            self.common_pvc_helper.backup_of_existing_statefulset_kafka_file(
                kafka_type, namespace
            )
            self.strimzi_pvc_helper.update_storage_value_in_backup_szkf_statefulset_file(
                kafka_type, new_pvc_value
            )
            self.strimzi_pvc_helper.scale_current_szkf_cluster_operator(namespace, 0)
            self.common_pvc_helper.delete_current_dmm_statefulset(namespace, kafka_type)
            self.common_pvc_helper.delete_crashed_dmm_pods_and_pvc(namespace, kafka_type)
            self.common_pvc_helper.recreate_statefulset(namespace, kafka_type)
            self.common_pvc_helper.check_are_dmm_pods_running(namespace, kafka_type)
            self.common_pvc_helper.check_pvc_capacity(namespace, kafka_type, new_pvc_value)
            self.strimzi_pvc_helper.scale_current_szkf_cluster_operator(namespace, 1)
        except Exception as err:
            raise err

    def is_pvc_update_needed(self, kafka_type: str, statefulset_in_dmm_chart: str) -> tuple[bool, str]:
        """
        Get PVC value on ENV and in DMM chart and compare to see does the PVC need to be updated
        :param kafka_type: The type of Kafka cluster for which to check the PVC size.
        :param statefulset_in_dmm_chart: The name of the statefulset for the Kafka cluster in the DMM chart.
        :return: A tuple containing a boolean value indicating whether the PVC needs to be updated
                 and the PVC size specified in the DMM chart.
        :rtype: tuple[bool, str]
        :raises: Any exceptions that occur while retrieving the PVC sizes.
        """
        try:
            pvc_size_in_dmm_chart = self.common_pvc_helper.get_pvc_size_in_dmm_chart(
                self.seli_username,
                self.seli_password,
                self.dmm_version,
                kafka_type,
                statefulset_in_dmm_chart,
            )
            if pvc_size_in_dmm_chart is not None:
                pvc_size_on_env = self.common_pvc_helper.get_pvc_size(
                    kafka_type, self.namespace
                )
                update_pvc = self.common_pvc_helper.compare_kafka_pvc_against_new_pvc(
                    pvc_size_on_env, pvc_size_in_dmm_chart
                )
            else:
                update_pvc = False
            return update_pvc, pvc_size_in_dmm_chart
        except Exception as err:
            raise err

    def release_pvc_update(self):
        """
        Check does PVC need to be updated for DMM ADP Kafka and DMM Strimzi Kafka
        :raises: Any exceptions that occur while running release pvc update.
        """
        try:
            kafka_types = ["adp", "strimzi"]
            statefulsets_in_dmm_chart = {"adp": "eric-oss-dmm-data-message-bus-kf", "strimzi": "eric-oss-dmm-kf-op-sz"}

            for kafka_type in kafka_types:
                update_pvc, new_pvc_value = self.is_pvc_update_needed(
                    kafka_type, statefulsets_in_dmm_chart[kafka_type]
                )
                if update_pvc:
                    getattr(self, "update_pvc_value_for_" + kafka_type)(self.namespace, kafka_type, new_pvc_value)
        except Exception as err:
            raise err
