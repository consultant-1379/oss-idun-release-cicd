#!/usr/bin/env groovy

pipeline {
  agent {
    label SLAVE
  }
  parameters {
    string(name: 'CHART_REPO', defaultValue: '', description: 'Repo URL where chart resides.')
    string(name: 'CHART_NAME', defaultValue: '', description: 'Name of the chart.')
    string(name: 'CHART_VERSION', defaultValue: '', description: 'Chart version to pass into the CBOS age tool.')
    string(name: 'HELM_DR_CHECK_VERSION', defaultValue: 'latest', description: 'Version of CBOS age tool to use.')
    string(name: 'SLAVE', defaultValue: '', description: 'Specify the slave label that you want the job to run on.')
  }
  stages {
    stage('Set CBOS Credentials') {
      steps {
        script {
          withCredentials([usernamePassword(credentialsId: 'spinnaker_cli_creds', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
            sh """
              cp resources/cbos_credentials.yaml .
              sed -i "s/USERNAME_PLACEHOLDER/'${USER}'/g" cbos_credentials.yaml
              sed -i "s/PASSWORD_PLACEHOLDER/'${PASS}'/g" cbos_credentials.yaml
            """
          }
        }
      }
    }
    stage('Run CBOS Age Tool') {
      steps {
        script {
          sh """
            docker run --rm --user \$(id -u):\$(id -g) --pull=always \
            --volume $WORKSPACE:$WORKSPACE \
            --workdir $WORKSPACE \
            armdocker.rnd.ericsson.se/proj-adp-cicd-drop/common-library-adp-helm-dr-check:${params.HELM_DR_CHECK_VERSION} \
            cbos-age-tool \
            --slogan="${params.CHART_NAME} CBO Report" \
            --helm-chart-repo ${params.CHART_REPO} \
            --helm-chart-name ${params.CHART_NAME} \
            --helm-chart-version ${params.CHART_VERSION} \
            --output $WORKSPACE --cbos-age -Dhelmdrck.credential.file.path=$WORKSPACE/cbos_credentials.yaml
          """
        }
      }
      post {
        success {
          echo "Archiving reports."
          archiveArtifacts '*.html'
          archiveArtifacts '*.json'
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