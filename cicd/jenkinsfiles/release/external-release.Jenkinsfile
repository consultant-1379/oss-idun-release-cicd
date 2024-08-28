#!/usr/bin/env groovy

def bob = "bob/bob -r \${WORKSPACE}/cicd/rulesets/release.yaml"

pipeline {
    parameters {
        string(name: 'AGENT_LABEL',
                defaultValue: 'IDUN_CICD',
                description: 'Jenkins agent to use')
        string( name: 'ARMDOCKER_CONFIG_SECRET',
                defaultValue: 'tbadm-docker-auth-config',
                description: 'ARM Docker config.json secret ID')
    }
    agent {
        node {
            label params.AGENT_LABEL
        }
    }
    stages {
        stage('Clean Git Repo') {
            steps {
                sh 'git clean -xdff'
                sh 'git submodule sync'
                sh 'git submodule update --init --recursive'
            }
        }
        stage('Prepare Working Directory') {
            steps {
                withCredentials([file(credentialsId: params.ARMDOCKER_CONFIG_SECRET, variable: 'DOCKERCONFIG')]) {
                    sh "install -m 600 ${DOCKERCONFIG} ${HOME}/.docker/config.json"
                }
            }
        }
        stage('Set Environment Variables') {
            steps {
                sh "${bob} set-oss-idun-cicd-cli-env-variables tmp-expose-bumped-version"
                script {
                    env.IMAGE_VERSION = readFile('artifact.properties').trim()
                }
            }
        }
        stage('Build IDUN CICD CLI') {
            steps {
                sh "${bob} build-docker-image-new-repo"
            }
        }
        stage('Publish IDUN CICD CLI') {
            steps {
                sh "${bob} publish-docker-image-new-repo"
            }
        }
        stage('Archive artifact properties file') {
            steps {
                archiveArtifacts artifacts: 'artifact.properties', onlyIfSuccessful: true
            }
        }
    }
    post {
        always {
            cleanWs()
            sh "docker rmi armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/oss-idun-cicd-cli:latest || true"
        }
    }
}