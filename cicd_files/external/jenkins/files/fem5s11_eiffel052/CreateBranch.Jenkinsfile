pipeline {
    agent {
        label env.SLAVE_LABEL
    }
    environment {
        PROJECT_PATH = "OSS/com.ericsson.oss.eiae"
        HELMFILE_REPO = "eiae-helmfile"
    }
    parameters {
        string(name: 'SPRINT_NUMBER', defaultValue: '', description: 'Number of current sprint')
        string(name: 'SLAVE_LABEL', defaultValue: 'evo_docker_engine_athlone', description: 'Agent label or name to run the job on.')
    }
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Prepare Branch') {
            steps {
                sh """
                    git clone --depth 1 \${GERRIT_CENTRAL_HTTP}/${env.PROJECT_PATH}/${env.HELMFILE_REPO}
                    cd ${env.HELMFILE_REPO}
                    git fetch origin master
                    git checkout -b ${params.SPRINT_NUMBER}_track
                    git status
                """
          }
        }
        stage('Push Branch') {
            steps {
                sh """
                    cd ${env.HELMFILE_REPO}
                    git push origin HEAD:refs/heads/${params.SPRINT_NUMBER}_track
                """
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}