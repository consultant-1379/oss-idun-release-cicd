"""
This is the CLI for the Thunderbee IDUN CICD jobs.
"""

import logging
import click
from idun_cicd_cli.src.etc import logging_utils
from idun_cicd_cli.src.operators.retrigger_aas_e2e_flows import RetriggerAASE2EFlows
from idun_cicd_cli.src.operators.release_pvc_update import ReleasePvcUpdate


def log_verbose_option(func):
    """A decorator for the log verbose command line argument."""
    return click.option('-v', '--verbose', type=click.BOOL, is_flag=True, required=False,
                        help='Increase output verbosity')(func)


def namespace_option(func):
    """A decorator for the namespace command line argument."""
    return click.option("-n", "--namespace", type=click.STRING, required=True,
                        help="Namespace of environment.")(func)


def username_option(func):
    """A decorator for the username command line argument."""
    return click.option('-u', '--username', type=click.STRING, required=True,
                        help='Username for accessing any internal tool/artifactory.')(func)


def password_option(func):
    """A decorator for the password command line argument."""
    return click.option('-p', '--password', type=click.STRING, required=True,
                        help='Password for any internal tool/artifactory.')(func)


def dmm_version_option(func):
    """A decorator for the dmm version command line argument."""
    return click.option("-dmm", "--dmm_version", type=click.STRING, required=True,
                        help="Version of the DMM chart to check PVC value in.")(func)


def autoapp_option(func):
    """A decorator for the autoapp command line argument."""
    return click.option('-a', '--autoapp', type=click.STRING, required=False,
                        help='autoapp name.')(func)


def app_instance_option(func):
    """A decorator for the app instance command line argument."""
    return click.option('-appi', '--app_instance', type=click.STRING, required=False,
                        help='App instance base URL.')(func)


def get_id_option(func):
    """A decorator for the get ID command line argument."""
    return click.option('-id', '--get_id', type=click.BOOL, is_flag=True, required=False,
                        help='Option to get the app ID of the autoapp with latest version')(func)


def generate_artifact_properties_option(func):
    """A decorator for the generate_artifact_properties command line argument."""
    return click.option('-gap', '--generate_artifact_properties', type=click.BOOL, is_flag=True, required=False,
                        help='Option to generate the artifact.properties file for use by '
                             'Spinnaker pipelines.')(func)


@click.group()
def cli_main():
    """
    The entry-point to the idun cli tool.
    Please see available options below.
    """


@cli_main.command()
@log_verbose_option
@username_option
@password_option
def retrigger_aas_e2e_flows(username, password, verbose):
    """
    Retrigger AAS E2E Flows Command
    :param username:
    :param password:
    :return:
    """
    logging_utils.initialize_logging(verbose)
    retrigger_instance = RetriggerAASE2EFlows(username, password)
    failed_pipeline_names = retrigger_instance.get_names_of_failed_pipelines()
    retrigger_instance.retrigger_pipelines(failed_pipeline_names)


@cli_main.command()
@log_verbose_option
@namespace_option
@username_option
@password_option
@dmm_version_option
def release_pvc_update(
    namespace, username, password, dmm_version, verbose
):
    """
    Check does DMM Kafka PVC values need expanding
    :param namespace:
    :param username:
    :param password:
    :param dmm_version:
    :param generate_artifact_properties:
    """
    try:
        logging_utils.initialize_logging(verbose)
        ReleasePvcUpdate(namespace, username, password, dmm_version).release_pvc_update()
    except Exception as err:
        logging.error(err)
        raise
