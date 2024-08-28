def returnParametersForDsl() {
    return [SLAVE: env.SLAVE]
}

def getPipelineJobs() {
    def pipelineJobList = []

    pipelineJobList.add('cicd_files/dsl/pipeline_operations/fem7s11_eiffel216/PipelineUpdater.groovy')

    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaDeploy.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaDeployInternalTesting.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/UnpackAndPushImages.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/UnpackAndPushImagesInternalTesting.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/FileTransferBetweenAgents.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/GetDiffBetweenAppVersions.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/SetPathToSiteValues.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/CreateBranch.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/CopyEicSiteValuesToOst.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaHealthCheck.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaHealthCheckInternalTesting.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaCleanImagesInDockerRegistry.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/JenkinsAgentCleanup.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/IdunIntegrationSiteValuesUpdate.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/IdunIntegrationVersionStep.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaInternalTestingPreOperations.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/ManaInternalTestingFilesTransfer.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/AutoAppsBuildAndUploadCSAR.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem7s11_eiffel216/AutoAppsTransferZip.groovy')

    return pipelineJobList.join('\n')
}

pipeline {
    agent {
        node {
            label SLAVE
        }
    }

    environment {
        DSL_CLASSPATH = 'cicd_files/dsl'
    }

    stages {
        stage ('Validate required parameters set') {
            when {
                expression {
                    env.SLAVE == null
                }
            }

            steps {
                error ('Required parameter(s) not set. Please provide a value for all required parameters')
            }
        }

        stage ('Generate OSS Idun Release CICD pipeline jobs') {
            steps {
                jobDsl targets: getPipelineJobs(),
                additionalParameters: returnParametersForDsl(),
                additionalClasspath: env.DSL_CLASSPATH
            }
        }

        stage ('Update IDUN Thunderbee List View') {
            steps {
                jobDsl targets: 'cicd_files/dsl/views/fem7s11_eiffel216/View.groovy'
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
