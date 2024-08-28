pipeline {
    agent {
        label env.SLAVE_LABEL
    }
    environment {
        PROJECT_PATH = "OSS/com.ericsson.oss.eiae"
        HELMFILE_REPO = "eiae-helmfile"
    }
    parameters {
        string(name: 'CHART_PATH', defaultValue: 'helmfile', description: 'The path to helm chart in eiae-helmfile repo')
        string(name: 'VERSION_TYPE', defaultValue: '', description: 'Enter \'MAJOR\', \'MINOR\' or \'PATCH\'')
        string(name: 'SLAVE_LABEL', defaultValue: 'RHEL7_GE_Docker_1', description: 'Slave label to use')
    }
    stages {
        stage('Clone Repository') {
            steps {
                sh """
                git clone --depth 1 ${GERRIT_CENTRAL_SSH}/${env.PROJECT_PATH}/${env.HELMFILE_REPO}
                git fetch origin master
                git fetch --all
                """
            }
        }
        stage('Build Image') {
            steps {
                sh '''
                docker build -f ${PWD}/cicd_files/external/scripts/version_step/Dockerfile-version-step -t helmfile-version-step .
                '''
            }
        }
        stage('Version Step') {
            steps {
                sh '''
                docker run --user "$(id -u):$(id -g)" -v ${PWD}:/usr/src/app --rm helmfile-version-step ${VERSION_TYPE} ${HELMFILE_REPO}/${CHART_PATH}
                '''
            }
        }
        stage('Push to master') {
            steps {
                sh """
                cd ${HELMFILE_REPO}
                git status
                git add -u
                git commit -m "NOJIRA - Automatic version step [${VERSION_TYPE}] for next release"
                git push origin HEAD:refs/heads/master
                """
            }
        }
        stage('Archiving artifact.properties') {
            steps {
                script {
                    archiveArtifacts artifacts: 'artifact.properties', onlyIfSuccessful: true
                }
            }
        }
    }
    post {
        always {
            cleanWs()
            sh '''
            docker rmi -f helmfile-version-step || true
            '''
        }
    }
}
