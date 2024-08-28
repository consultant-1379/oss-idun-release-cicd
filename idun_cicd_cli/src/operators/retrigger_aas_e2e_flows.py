"""
Retrigger AAS E2E flows Module
"""
import logging
from json import JSONDecodeError

from idun_cicd_cli.src.etc.request_retry import request_retry


class RetriggerAASE2EFlows:
    """
    Retrigger AAS E2E Flows Class
    """
    def __init__(self, username, password):
        self.username = username
        self.password = password
        self.parameters = {'limit': 1}
        self.get_url = 'https://spinnaker-api.rnd.gic.ericsson.se/applications/idun-aas-e2e-cicd/pipelines'
        self.post_url = 'https://spinnaker-api.rnd.gic.ericsson.se/pipelines/idun-aas-e2e-cicd/'
        self.__retry_amount = 5

    @staticmethod
    def __convert_response_to_json__(response):
        """
        Converts the response to JSON and catches any possible failures
        :param response:
        :return: json_response
        :rtype: dict
        """
        try:
            logging.debug('Attempting to convert response to JSON')
            json_response = response.json()
        except JSONDecodeError as json_error:
            logging.error('Response content:')
            logging.error(str(response.text))
            raise Exception('Got invalid response from request and could not convert it to JSON!') \
                from json_error
        return json_response

    def get_names_of_failed_pipelines(self):
        """
        Get names of failed pipelines
        :return: failed_pipeline_names
        :rtype: list
        """
        logging.info(f'Running GET request towards: {self.get_url}')
        response = request_retry(type_of_request='GET', url=self.get_url,
                                 max_retry=self.__retry_amount,
                                 auth=(self.username, self.password),
                                 params=self.parameters)
        json_data = self.__convert_response_to_json__(response)
        failed_pipeline_names = []
        for pipeline in json_data:
            if pipeline['name'][-12:] == "AAS-E2E-Flow" and pipeline['status'] == "TERMINAL":
                failed_pipeline_names.append(pipeline['name'])
        return failed_pipeline_names

    def retrigger_pipelines(self, pipeline_names):
        """
        Retrigger pipelines
        :param pipeline_names: List of pipeline names
        """
        for pipeline_name in pipeline_names:
            response = request_retry(type_of_request='POST',
                                     url=self.post_url+pipeline_name,
                                     max_retry=self.__retry_amount,
                                     auth=(self.username, self.password))
            logging.info(f'Pipeline Execution : {self.__convert_response_to_json__(response)}')
