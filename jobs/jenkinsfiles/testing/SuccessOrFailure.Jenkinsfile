#!/usr/bin/env groovy

pipeline {
    agent {
        node {
            label SLAVE
        }
    }
    parameters {
        string(
            name: 'SUCCESS',
            defaultValue: 'true',
            description: "Set it to 'true' for successful job. Set it to 'false' for unsuccessful job."
        )
    }
    stages {
        stage('Check Success or Failure') {
            steps {
                sh """
                if [[ "${env.SUCCESS}" != "true" ]]; then
                    echo "FAILURE";
                    exit 1
                else
                    echo "SUCCESS";
                    exit 0
                fi
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
