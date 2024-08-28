pipeline {
    agent {
        label params.SLAVE_LABEL
    }
    stages {
        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }
        stage('Prepare and Push Branch') {
            steps {
                script {
                    env.REPO_PATH = params.GERRIT_PROJECT
                }
                sh """
                    #!/bin/bash +x
                    git clone --depth 1 \${GERRIT_CENTRAL_SSH}/\${REPO_PATH}
                    PROJECT_NAME=\$(basename \${REPO_PATH})
                    cd \${PROJECT_NAME}
                    git fetch origin master
                    git fetch --all --tags
                    if [ "${params.VERSION}" =  "latest" ]
                    then
                        echo "Branch created using the latest tag"
                        CREATE_BRANCH_VERSION=\$(git describe --tags \$(git rev-list --tags --max-count=1))
                        CREATE_BRANCH_YYWW=\$(date +"%y%V")
                        echo "VERSION=\${CREATE_BRANCH_VERSION}" >> artifact.properties
                        echo "BRANCH_NAME=\${CREATE_BRANCH_VERSION}_\${CREATE_BRANCH_YYWW}_track" >> artifact.properties
                        mv artifact.properties ../artifact.properties
                        git checkout -b \${CREATE_BRANCH_VERSION}_\${CREATE_BRANCH_YYWW}_track
                        git status
                        git push origin HEAD:refs/heads/\${CREATE_BRANCH_VERSION}_\${CREATE_BRANCH_YYWW}_track
                    else
                        echo "Branch created from the specified helmfile version"
                        CREATE_BRANCH_YYWW=\$(date +"%y%V")
                        echo "VERSION=${params.VERSION}" >> artifact.properties
                        echo "BRANCH_NAME=${params.VERSION}_\${CREATE_BRANCH_YYWW}_track" >> artifact.properties
                        mv artifact.properties ../artifact.properties
                        git checkout tags/${params.VERSION} -b ${params.VERSION}_\${CREATE_BRANCH_YYWW}_track
                        git status
                        git push origin HEAD:refs/heads/${params.VERSION}_\${CREATE_BRANCH_YYWW}_track
                    fi
                """
                archiveArtifacts artifacts: 'artifact.properties', onlyIfSuccessful: true
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}