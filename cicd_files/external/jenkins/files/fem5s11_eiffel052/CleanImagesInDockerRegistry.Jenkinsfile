#!/usr/bin/env groovy

pipeline {
    agent {
        label params.SLAVE_LABEL
    }
    environment {
        DOCKER_CONFIG_JSON = "${WORKSPACE}/.docker/"
    }
    stages {
      stage('Cleaning Git Repo') {
        steps {
          sh 'git clean -xdff'
          sh 'git submodule sync'
        }
      }
      stage('Install Docker config.json') {
        steps {
          withCredentials([file(credentialsId:'reluser-creds-docker', variable: 'DOCKER_CONFIG')]) {
            sh "mkdir ${DOCKER_CONFIG_JSON}"
            sh "install -vD -m 600 ${DOCKER_CONFIG} ${DOCKER_CONFIG_JSON}"
          }
        }
      }
      stage('Fetch Kube Config using OST') {
        when {
          not {
            environment ignoreCase:true, name: 'ENV_FILES_BUCKET_NAME', value: 'none'
          }
        }
        environment {
          BUCKET_NAME = "${params.ENV_FILES_BUCKET_NAME}"
          BUCKET_OUTPUT_DIR = '.'
        }
        steps {
          withCredentials([usernamePassword(credentialsId:params.FUNCTIONAL_USER_SECRET, usernameVariable: 'FUNCTIONAL_USER_USERNAME', passwordVariable: 'FUNCTIONAL_USER_PASSWORD')]) {
            sh """docker --config ${DOCKER_CONFIG_JSON} \
                            run \
                              --rm \
                              --user "\$(id -u):\$(id -g)" \
                              --init \
                              --volume ~/.docker:/root/.docker \
                              --volume /var/run/docker.sock:/var/run/docker.sock \
                              --volume ${env.PWD}/${BUCKET_OUTPUT_DIR}:/usr/src/app/out \
                              armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/data-store-client:latest  \
                                download-all-files-in-ost-bucket \
                                  --bucket_name ${BUCKET_NAME} \
                                  --auth_user ${FUNCTIONAL_USER_USERNAME} \
                                  --auth_pass ${FUNCTIONAL_USER_PASSWORD} \
                                  --generate_artifact_properties"""
            sh 'mkdir -p kube_config'
            sh "mv ${env.PWD}/${BUCKET_OUTPUT_DIR}/${env.KUBE_CONFIG} ${WORKSPACE}/kube_config/config"
            sh "chmod 600 ${WORKSPACE}/kube_config/config"
          }
        }
      }
      stage('Fetch Kube Config From Jenkins Credentials') {
        when {
          environment ignoreCase:true, name: 'ENV_FILES_BUCKET_NAME', value: 'none'
        }
        steps {
          withCredentials([file(credentialsId:env.KUBE_CONFIG, variable: 'KUBECONFIG')]) {
            sh "install -vD -m 600 ${KUBECONFIG} ${WORKSPACE}/kube_config/config"
          }
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
      stage('Build Clean Images in Docker Registry Docker Iamge') {
        steps {
          sh """docker build -f ${WORKSPACE}/cicd_files/external/scripts/clean_images_in_docker_registry/Dockerfile-clean_images_in_docker_registry -t clean_images_in_docker_registry ."""
        }
      }
      stage('Run Clean Images in Docker Registry Docker Iamge') {
          steps {
              script {
                  env.parsedRegistryPassword = registryPassword.replaceAll('\\\\\\\\', '')
                  sh'''
                      if [[ ${DELETE_ALL_IMAGES} = true ]]; then
                          docker --config ${DOCKER_CONFIG_JSON} \
                              run --user "$(id -u):$(id -g)" \
                                  -v ${PWD}:/usr/src/app \
                                  --rm clean_images_in_docker_registry \
                                  -d -n ${NAMESPACE} -u ${registryUsername} -p ${parsedRegistryPassword}
                      else
                          docker --config ${DOCKER_CONFIG_JSON} \
                              run --user "$(id -u):$(id -g)" \
                                  -v ${PWD}:/usr/src/app \
                                  --rm clean_images_in_docker_registry \
                                  -n ${NAMESPACE} -u ${registryUsername} -p ${parsedRegistryPassword}
                      fi
                  '''
              }
          }
      }
    }
    post {
        always {
          sh 'docker rmi clean_images_in_docker_registry || true'
          sh 'docker rmi armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/data-store-client:latest || true'
          cleanWs()
        }
    }
}
