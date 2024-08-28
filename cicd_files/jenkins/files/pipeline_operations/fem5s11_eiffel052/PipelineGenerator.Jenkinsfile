def returnParametersForDsl() {
    return [SLAVE: env.SLAVE]
}

def getPipelineJobs() {
    def pipelineJobList = []

    pipelineJobList.add('cicd_files/dsl/pipeline_operations/fem5s11_eiffel052/PipelineUpdater.groovy')

    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaDeploy.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaDeployInternalTesting.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/UnpackAndPushImages.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/UnpackAndPushImagesInternalTesting.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/FileTransferBetweenAgents.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/GetDiffBetweenAppVersions.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/SetPathToSiteValues.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/CreateBranch.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaHealthCheck.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaHealthCheckInternalTesting.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaCleanImagesInDockerRegistry.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/JenkinsAgentCleanup.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/IdunIntegrationSiteValuesUpdate.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/IdunIntegrationVersionStep.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaInternalTestingPreOperations.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/ManaInternalTestingFilesTransfer.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/AutoAppsBuildAndUploadCSAR.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/CleanImagesInDockerRegistry.groovy')

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
                jobDsl targets: 'cicd_files/dsl/views/fem5s11_eiffel052/View.groovy'
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
