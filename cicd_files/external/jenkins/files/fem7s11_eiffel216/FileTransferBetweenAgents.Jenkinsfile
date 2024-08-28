#!/usr/bin/env groovy

def bob = "bob/bob -r \${WORKSPACE}/cicd_files/external/jenkins/rulesets/FileTransferBetweenAgents.yaml"

pipeline {
    agent {
        label env.SLAVE_LABEL
    }
    stages {
        stage('Prepare') {
            steps {
                sh "git submodule sync"
                sh "git submodule update --init --recursive --remote"
                sh "${bob} git-clean"
            }
        }
        stage('Prepare Working Directory'){
            steps {
                withCredentials([file(credentialsId: params.ARMDOCKER_USER_SECRET, variable: 'DOCKERCONFIG')]) {
                    sh "install -m 600 ${DOCKERCONFIG} ${HOME}/.docker/config.json"
                    sh "${bob} fetch-site-values"
                }
            }
        }
        stage('Copy Certificates & keys to workspace') {
            steps {
                script {
                    sh "${bob} copy-certificate-files"
                }
            }
        }
        stage('Create archive') {
            steps {
                script {
                    sh "${bob} create-archive-to-transfer"
                }
            }
        }
        stage('Transfer archive (PrivateKey)') {
            when {
                expression { params.TYPE_OF_CREDENTIAL_FOR_SCP == "sshUserPrivateKey" }
            }
            steps {
                script {
                    withCredentials([sshUserPrivateKey(credentialsId: env.FUNCTIONAL_SCP_USER_SECRET, keyFileVariable: 'KEY_FILE', usernameVariable: 'SCP_USER')]) {
                        sh "${bob} transfer-archive-to-vm-privatekey"
                    }
                }
            }
        }
        stage('Transfer archive (UsernamePassword)') {
            when {
                expression { params.TYPE_OF_CREDENTIAL_FOR_SCP == "UsernamePassword" }
            }
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: env.FUNCTIONAL_SCP_USER_SECRET, usernameVariable: 'SCP_USER', passwordVariable: 'SCP_USER_PASSWORD')]) {
                        sh "${bob} transfer-archive-to-vm-userpass"
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