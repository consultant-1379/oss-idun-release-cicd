#!/usr/bin/env groovy

def bob = "bob/bob -r \${WORKSPACE}/cicd_files/external/jenkins/rulesets/AutoAppsTransferZip.yaml"

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
        stage('Fetch Zip') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.FUNCTIONAL_USER_SECRET, usernameVariable: 'FUNCTIONAL_USER_USERNAME', passwordVariable: 'FUNCTIONAL_USER_PASSWORD')]) {
                    sh "${bob} download-zip"
                }
            }
        }
        stage('Upload Zip') {
            steps {
                withCredentials([usernamePassword(credentialsId: env.FUNCTIONAL_USER_SECRET, usernameVariable: 'FUNCTIONAL_USER_USERNAME', passwordVariable: 'FUNCTIONAL_USER_PASSWORD')]) {
                    sh "${bob} upload-zip"
                }
            }
        }
    }
    post {
        success {
            script {
                sh "echo 'CHART_NAME=${params.CHART_NAME}' > artifact.properties"
                sh "echo 'CHART_REPO=${params.CHART_REPO}' >> artifact.properties"
                sh "echo 'ZIP_DOWNLOAD_REPO=${params.ZIP_DOWNLOAD_REPO}' >> artifact.properties"
                sh "echo 'ZIP_UPLOAD_REPO=${params.ZIP_UPLOAD_REPO}' >> artifact.properties"
                sh "echo 'ZIP_STORAGE_INSTANCE=${params.ZIP_STORAGE_INSTANCE}' >> artifact.properties"
                archiveArtifacts 'artifact.properties'
            }
        }
        always {
            cleanWs()
        }
    }
}
