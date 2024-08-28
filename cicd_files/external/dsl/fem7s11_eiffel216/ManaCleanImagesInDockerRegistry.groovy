def pipelineBeingGeneratedName = "oss-idun-release-cicd_MANA_Clean_Images_In_Docker_Registry"

pipelineJob(pipelineBeingGeneratedName) {
description(
"""
<div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
    <h2>DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
    <h3 style="text-align:left;">Job Description:</h3>
    <p>This job cleans the images from the docker registry attached to the environment.</p>
    <p>This is a DSL generated job.</p>
    <p>Repository: OSS/com.ericsson.oss.cicd/oss-idun-release-cicd</p>
    <p>Groovy file location: cicd_files/external/dsl/fem7s11_eiffel216/ManaCleanImagesInDockerRegistry.groovy</p>
    <br/>
    <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b>
    <br/>
    <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_MANA_Clean_Images_In_Docker_Registry%20Job">&#128231; Send Mail to provide feedback</a></p>
</div>
""")

    parameters {
        stringParam("NAMESPACE", "", "Namespace on the cluster that the deployment is installed into.")
        stringParam("KUBECONFIG_FILE", "", "ID of the Kubernetes config file that has been stored on the Jenkins server in the credentials area as a file.")
        stringParam("SLAVE_LABEL", "", "Label of the Jenkins slave where this jenkins job should be executed.")
        stringParam("REGISTRY_CREDENTIALS", "", "ID of the registry credentails that has been stored on the Jenkins server.")
        booleanParam("DELETE_ALL_IMAGES", false, "If set to true, all images in the registry of the environemnt are cleaned up including those of in use images.")
    }
    disabled(false)
    keepDependencies(false)
    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    definition {
        cps {
            script('''
pipeline {
    agent {
        label params.SLAVE_LABEL
    }
    stages {
        stage("Write docker registry cleanup python script") {
            steps {
                writeFile file: "external_registry_cleanup.py", text: \'\'\'import argparse
from collections import defaultdict
import logging
import json
import os
from subprocess import Popen, PIPE
import time

class Image(object):
    """
    Object used to store information about a docker image.
    """
    def __init__(self, repo, tag="latest"):
        self.repo = repo
        self.tag = tag

    def __str__(self):
        return self.repo + ":" + self.tag

    def __hash__(self):
        return hash(self.__str__())

    def get_name(self):
        return self.repo.split('/', 1)[1]

    def get_registry(self):
        return self.repo.split('/')[0]

    def get_tag(self):
        return self.tag

    def __eq__(self, other):
        if isinstance(other, Image):
            return self.__str__() == other.__str__()
        else:
            return False

def import_and_install_packages(package_name, package_version):
    """
    This will import the necessary python packages to the system if they are not present
    :param package_name
    :param package_version
    """
    try:
        globals()[package_name] = __import__(package_name)
    except ImportError:
        os.system('pip3 install -v ' + package_name + '==' + package_version)

def retrieve_in_use_images_from_docker_registry(namespace, charts_in_namespace):
    """
    This function will get all the images being used by in the specified namespace
    :param namespace
    :return in_use_images
    :rtype: set
    """
    in_use_images = set()
    for chart_in_namespace in charts_in_namespace:
        chart_information = retrieve_chart_information_from_release(chart_in_namespace, namespace)
        in_use_images.update(parse_image_information_from_yaml(chart_information))
    return in_use_images

def get_charts_in_namespace(namespace):
    """
    Gets all helm charts in the specified namespace
    :param namespace
    :return: list_of_chart_names
    :rtype: list
    """
    get_charts_command = 'helm ls -a -n ' + namespace + ' -o json'
    helm_template_output = run_command(get_charts_command)

    helm_template_output = json.loads(helm_template_output)
    list_of_chart_names = []

    if helm_template_output:
        for chart in helm_template_output:
            list_of_chart_names.append(chart['name'])
        logging.info('Detected the following releases in ' + namespace + ' namespace: ' + str(list_of_chart_names))
        return list_of_chart_names
    raise Exception('No charts detected in namespace ' + namespace)

def retrieve_chart_information_from_release(chart, namespace):
    """
    Retrieves information about the helm release from the chart name passed in
    :param chart
    :param namespace
    :return chart_information
    :rtype: str
    """
    get_all_release_charts = 'helm get all ' + chart + ' -n ' + namespace
    chart_information = run_command(get_all_release_charts)
    return chart_information

def run_command(command):
        """
        Executes a command against a k8s cluster
        :return: output
        :rtype: str
        """
        logging.info('Running the following cli command: ' + command)
        command = command + ' --kubeconfig=kube_config/config'
        cmd = Popen(command, shell=True, stdin=PIPE, stdout=PIPE, stderr=PIPE)
        output, err = cmd.communicate()
        return output

def parse_image_information_from_yaml(yaml_to_parse):
    """
    This will parse the inputted yaml and retrieve all instances of the word image.
    It will then set these images to the Image object
    :param yaml_to_parse
    :return image_list
    :rtype: list
    """
    image_list = list()
    invalid_characters_in_helm_annotations = ['{{', '@']
    for line_of_yaml in yaml_to_parse.split('\\\\n'):
        if 'image: ' in line_of_yaml:
            if not any(item in line_of_yaml for item in invalid_characters_in_helm_annotations):
                image_information = line_of_yaml.split('image: ')[1]
                parsed_image_information = image_information.replace('"', '')
                image_key_value_pair = parsed_image_information.split(':', 1)
                if len(image_key_value_pair) > 1:
                    image = Image(repo=image_key_value_pair[0], tag=image_key_value_pair[1])
                else:
                    image = Image(repo=image_key_value_pair[0])
                if '/' in image.repo:
                    image_list.append(image)
    return image_list

def determine_registry_and_verify_connectivity(registry_username, registry_password, namespace):
    """
    This will determine the docker registry URL based on the retrieved in the script.
    Once it has the docker registry URL, it will verify connectivity between the environment and the registry
    :param registry_username
    :param registry_password
    :param namespace
    :return retrieved_registry_url
    :rtype: str
    """
    chart = 'eric-oss-app-mgr'
    get_docker_registry_command = 'helm get values ' + chart + ' -n ' + namespace + ' -o json'
    helm_template_output = run_command(get_docker_registry_command)
    helm_values_json = json.loads(helm_template_output)
    docker_registry_domain = helm_values_json['global']['registry']['url']
    retrieved_registry_url = 'https://' + docker_registry_domain + '/v2/'

    logging.info('Verifying connectivity to ' + retrieved_registry_url)
    try:
        response = requests.get(retrieved_registry_url, verify=False, auth=(registry_username, registry_password))
        if response.status_code == 200:
            logging.info('Successfully connected to docker registry ' + retrieved_registry_url)
        elif response.status_code == 401:
            raise Exception('Unauthorized to access docker registry ' + retrieved_registry_url + '. Please validate credentials provided.')
    except requests.exceptions.RequestException:
        raise Exception('Could not connect to Docker Registry ' + retrieved_registry_url + '. Please investigate.')
    return retrieved_registry_url

def find_used_image_tags_in_docker_repo(in_use_images_from_docker_registry, docker_registry_url,
                                        registry_username, registry_password):
    """
    This will find any image tags which are present in the docker repo. We may have more
    image tags than those being used as they may have been used in previous instances of namespace.
    In this function, we create an defaultdict where the keys are the image name, and the values are all
    image tags in the repo associated with the image
    :param in_use_images_from_docker_registry
    :param docker_registry_url
    :param registry_username
    :param registry_password
    :return images_in_repo
    :rtype: defaultdict(set)
    """
    images_in_repo = defaultdict(set)

    logging.info('Getting all used docker images in the docker repo')
    for image in in_use_images_from_docker_registry:
        logging.info('Getting image tag information for image: ' + image.get_name())
        response = requests.get(
            docker_registry_url + image.get_name() + '/tags/list', verify=False, auth=(registry_username,
                                                                                       registry_password))
        if 'tags' in response.json() and response.json()['tags']:
            images_in_repo[image.get_name()].update(response.json()['tags'])
    return images_in_repo

def find_in_use_images_and_tags_from_registry(in_use_images_from_docker_registry):
    """
    This will use all images from the docker registry, in order to create a dictionary where the key
    is the image name, and the value is a set of image tags
    :param in_use_images_from_docker_registry
    :return images_in_use
    :rtype: defaultdict(set)
    """
    images_in_use = defaultdict(set)
    for image in in_use_images_from_docker_registry:
        images_in_use[image.get_name()].add(image.get_tag())
    return images_in_use

def delete_images_that_are_not_in_use(in_use_images_and_tags, docker_registry_url, registry_username,
                                      registry_password, images_in_docker_repo):
    """
    This will loop every manifest of every image on the system and will delete any images that are not currently in
    use by pods etc.
    We run a one minute sleep to allow users of the script time to terminate the delete if they need
    :param in_use_images_and_tags
    :param docker_registry_url
    :param registry_username
    :param registry_password
    :param images_in_docker_repo
    """
    logging.info('Deleting all manifests of images that are not in use from docker repo.')
    manifests_to_keep = get_manifests_to_keep(in_use_images_and_tags, docker_registry_url,
                                              registry_username, registry_password)
    manifests_to_delete = dict()
    for image_in_docker_repo in images_in_docker_repo:
        for docker_image_tags in images_in_docker_repo[image_in_docker_repo]:
            manifest_of_required_tags = get_docker_manifest_for_image(docker_registry_url, image_in_docker_repo,
                                                                      docker_image_tags, registry_username,
                                                                      registry_password)
            if manifest_of_required_tags and manifest_of_required_tags not in manifests_to_keep:
                manifests_to_delete.__setitem__(manifest_of_required_tags, image_in_docker_repo)

    logging.info('Deleting required images that are not in use.')
    logging.info('Will begin deleting ' + str(len(manifests_to_delete)) + ' images after a 1 minute sleep!')
    time.sleep(60)

    for manifest_to_delete in manifests_to_delete:
        delete_manifest(manifest_to_delete, manifests_to_delete[manifest_to_delete], docker_registry_url, registry_username, registry_password)

def delete_all_manifests_of_all_images(images_in_docker_repo, docker_registry_url,
                                       registry_username, registry_password):
    """
    This will loop every manifest of every image on the system and delete everything.
    This will ensure there is nothing on your system
    If there are still applications in use, running this function may cause instability of the system
    We run a one minute sleep to allow users of the script time to terminate the delete if they need
    :param images_in_docker_repo
    :param docker_registry_url
    :param registry_username
    :param registry_password
    """
    manifests_to_delete = dict()
    logging.info('Deleting all manifests of all images in docker repo.')
    for image_in_docker_repo in images_in_docker_repo:
        for docker_image_tags in images_in_docker_repo[image_in_docker_repo]:
            manifest_of_required_tags = get_docker_manifest_for_image(
                docker_registry_url, image_in_docker_repo, docker_image_tags, registry_username, registry_password)
            if manifest_of_required_tags:
                manifests_to_delete.__setitem__(manifest_of_required_tags, image_in_docker_repo)

    logging.info('Deleting all images. Deleting in use images may cause instability on your system.')
    logging.info('Will begin deleting ' + str(len(manifests_to_delete)) + ' images after a 1 minute sleep!')
    time.sleep(60)

    for manifest_to_delete in manifests_to_delete:
        delete_manifest(manifest_to_delete, manifests_to_delete[manifest_to_delete], docker_registry_url, registry_username, registry_password)

def get_manifests_to_keep(in_use_images_from_docker_registry, docker_registry_url,
                          registry_username, registry_password):
    """
    This function will create a set which contains all docker manifests currently being used which are
    stored in your docker registry. This is useful as this will prevent us from cleaning up images which
    are currently being used by our application.
    :param in_use_images_from_docker_registry
    :param docker_registry_url
    :param registry_username
    :param registry_password
    :return manifests_to_keep
    :rtype: set
    """
    manifests_to_keep = set()
    for image_in_use, tags_in_use in in_use_images_from_docker_registry.items():
        for tag_in_use in tags_in_use:
            manifest_of_image_being_used = get_docker_manifest_for_image(
                docker_registry_url, image_in_use, tag_in_use, registry_username, registry_password)
            if manifest_of_image_being_used:
                manifests_to_keep.add(manifest_of_image_being_used)
    return manifests_to_keep

def get_docker_manifest_for_image(docker_registry_url, image_name, image_tag, registry_username, registry_password):
    """
    This will retrieve the docker manifest for the given image
    :param docker_registry_url
    :param image_name
    :param image_tag
    :param registry_username
    :param registry_password
    :return manifest
    :rtype: str
    """
    logging.info('Getting manifest information for docker image ' + image_name + ':' + image_tag)
    get_manifest_headers = {
        'Accept': 'application/vnd.docker.distribution.manifest.v2+json',
    }
    response = requests.get(docker_registry_url + image_name + '/manifests/' + image_tag,
                            verify=False,
                            headers=get_manifest_headers,
                            auth=(registry_username, registry_password))
    manifest_content = response.headers.get('Docker-Content-Digest')
    return manifest_content

def delete_manifest(manifest_to_delete, manifest_image, docker_registry_url,
                    registry_username, registry_password):
    """
    This function will delete specified manifest of the specified image. This is what will clear up space
    on our docker registry after these manifests are cleaned
    :param manifest_to_delete
    :param manifest_image
    :param docker_registry_url
    :param registry_username
    :param registry_password
    :rtype: boolean
    """
    logging.info('Deleting manifest ' + manifest_to_delete + ' associated with image ' + manifest_image)
    delete_manifest_headers = {
        'Accept': 'application/vnd.docker.distribution.manifest.v2+json',
    }
    retries = 5

    while retries > 0:
        response = requests.delete(docker_registry_url + manifest_image + '/manifests/' + manifest_to_delete,
                                   headers=delete_manifest_headers, verify=False, auth=(registry_username,
                                                                                        registry_password))
        if response.status_code == 202:
            logging.info('Successfully deleted manifest for image ' + manifest_image)
            return True
        else:
            time.sleep(10)
            retries = retries - 1
    raise Exception('Failed to deleted manifest for image ' + manifest_image + '. Exiting...')

def format_arguments():
    """
    Parse arguments passed into script
    :return parser.parse_args()
    """
    parser = argparse.ArgumentParser(
        formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument('-d', '--delete_all', help='', action='store_true')
    parser.add_argument('-n', '--namespace', help='', required=True)
    parser.add_argument('-u', '--registry_username', help='', required=True)
    parser.add_argument('-p', '--registry_password', help='', required=True)

    return parser.parse_args()

def main(args):
    logging.basicConfig(level = logging.INFO)

    import_and_install_packages('requests', '2.25.1')
    import_and_install_packages('urllib3', '1.26.4')

    logging.getLogger("requests").setLevel(logging.WARNING)
    logging.getLogger("urllib3").setLevel(logging.CRITICAL)
    urllib3.disable_warnings(urllib3.exceptions.InsecureRequestWarning)
    urllib3.disable_warnings()

    charts_in_namespace = get_charts_in_namespace(args.namespace)
    in_use_images_from_docker_registry = retrieve_in_use_images_from_docker_registry(args.namespace, charts_in_namespace)
    registry_url = determine_registry_and_verify_connectivity(args.registry_username, args.registry_password, args.namespace)
    all_images_in_docker_repo = find_used_image_tags_in_docker_repo(in_use_images_from_docker_registry,
                                                                    registry_url, args.registry_username, args.registry_password)

    if args.delete_all:
        delete_all_manifests_of_all_images(all_images_in_docker_repo, registry_url, args.registry_username, args.registry_password)
    else:
        in_use_images_and_tags = find_in_use_images_and_tags_from_registry(in_use_images_from_docker_registry)
        delete_images_that_are_not_in_use(in_use_images_and_tags, registry_url, args.registry_username, args.registry_password, all_images_in_docker_repo)

if __name__ == '__main__':
    main(format_arguments())
\'\'\'
            }
        }
        stage('Retrieve environment registry credentials') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: env.REGISTRY_CREDENTIALS, passwordVariable: 'REGISTRY_PASSWORD', usernameVariable: 'REGISTRY_USERNAME')]) {
                        env.registryUsername = "${REGISTRY_USERNAME}"
                        env.registryPassword = "${REGISTRY_PASSWORD}"
                    }
                }
            }
        }
        stage('Run script') {
            steps {
                script {
                    env.parsedRegistryPassword = registryPassword.replaceAll('\\\\\\\\', '')
                    withCredentials([file(credentialsId: env.KUBECONFIG_FILE, variable: "KUBECONFIG")]) {
                        sh """
                            if [[ -d "./kube_config" ]]; then
                                rm -r ./kube_config
                            fi
                        """
                        sh "mkdir ./kube_config"
                        sh 'install -m 600 $KUBECONFIG ./kube_config/config'
                        sh """
                            if [[ ${DELETE_ALL_IMAGES} = true ]]; then
                                python external_registry_cleanup.py -n ${NAMESPACE} -u ${registryUsername} -p ${parsedRegistryPassword} -d
                            else
                                python external_registry_cleanup.py -n ${NAMESPACE} -u ${registryUsername} -p ${parsedRegistryPassword}
                            fi
                        """
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
'''
            )
            sandbox()
        }
    }
}