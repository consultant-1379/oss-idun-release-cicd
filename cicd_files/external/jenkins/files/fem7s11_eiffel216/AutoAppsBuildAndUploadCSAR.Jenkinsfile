#!/usr/bin/env groovy

def bob = "bob/bob -r \${WORKSPACE}/cicd_files/external/jenkins/rulesets/AutoAppsBuildAndUploadCSAR.yaml"

pipeline {
    agent {
        label env.SLAVE_LABEL
    }
    stages {
        stage('Clean Workspace') {
            steps {
                sh 'git submodule sync'
                sh 'git submodule update --init --recursive --remote'
                sh "${bob} git-clean"
            }
        }
        stage('Inject Creds') {
            steps {
                sh "chmod 666 ${HOME}/.docker/config.json"
                withCredentials( [file(credentialsId: params.ARMDOCKER_USER_SECRET, variable: 'dockerConfig')]) {
                    sh "install -m 666 ${dockerConfig} ${HOME}/.docker/config.json"
                }
            }
        }
        stage('Clone Repo') {
            steps {
                sh "${bob} clone-repo"
            }
        }
        stage('Prepare CSAR Input Files') {
            steps {
                sh "${bob} csar-prep"
            }
        }
        stage('Fetch Chart') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.FUNCTIONAL_USER_SECRET, usernameVariable: 'FUNCTIONAL_USER_USERNAME', passwordVariable: 'FUNCTIONAL_USER_PASSWORD')]) {
                    sh "${bob} fetch-chart"
                }
                sh "${bob} move-chart"
            }
        }
        stage('Build the CSAR') {
            options { retry(2) }
            steps {
                sh "${bob} create-output-folder"
                withCredentials( [file(credentialsId: params.ARMDOCKER_USER_SECRET, variable: 'dockerConfig')]) {
                    sh "install -m 666 ${dockerConfig} ${HOME}/.docker/config.json"
                    sh "${bob} set-csar-dir-permissions"
                    sh "${bob} build-csar"
                }
            }
        }
        stage('Upload CSAR to Storage Location') {
            steps {
                sh "${bob} confirm-csar"
                withCredentials([usernamePassword(credentialsId: env.FUNCTIONAL_USER_SECRET, usernameVariable: 'FUNCTIONAL_USER_USERNAME', passwordVariable: 'FUNCTIONAL_USER_PASSWORD')]) {
                    sh "${bob} upload-csar"
                }
            }
        }
    }
    post {
        success {
            script {
                sh "rm -f ${env.WORKSPACE}/ci/csar/output_folder/${env.CHART_NAME}-${env.CHART_VERSION}.csar"
                currentBuild.description = "See published CSAR below:\n${params.CSAR_STORAGE_INSTANCE}/artifactory/${params.CSAR_STORAGE_REPO}/${env.CSAR_REPO_SUBPATH}/${env.CHART_NAME}/${env.CHART_VERSION}"
                sh "echo 'CHART_NAME=${params.CHART_NAME}' > artifact.properties"
                sh "echo 'CHART_REPO=${params.CHART_REPO}' >> artifact.properties"
                sh "echo 'CHART_VERSION=${params.CHART_VERSION}' >> artifact.properties"
                sh "echo 'CSAR_STORAGE_INSTANCE=${params.CSAR_STORAGE_INSTANCE}' >> artifact.properties"
                sh "echo 'CSAR_STORAGE_REPO=${params.CSAR_STORAGE_REPO}' >> artifact.properties"
                sh "echo 'CSAR_STORAGE_LOCATION=${env.CSAR_REPO_SUBPATH}/${env.CHART_NAME}/${env.CHART_VERSION}' >> artifact.properties"
                sh "echo 'CSAR_NAME=${env.CHART_NAME}-${env.CHART_VERSION}.csar' >> artifact.properties"
                sh "echo 'CSAR_PATH=${params.CSAR_STORAGE_INSTANCE}/artifactory/${params.CSAR_STORAGE_REPO}/${env.CSAR_REPO_SUBPATH}/${env.CHART_NAME}/${env.CHART_VERSION}/${env.CHART_NAME}-${env.CHART_VERSION}.csar' >> artifact.properties"
                archiveArtifacts 'artifact.properties'
            }
        }
        always {
            cleanWs()
        }
    }
}
