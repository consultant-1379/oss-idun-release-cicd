def returnParametersForDsl() {
    return [SLAVE: env.SLAVE]
}

def getPipelineJobs() {
    def pipelineJobList = []

    pipelineJobList.add('cicd_files/dsl/pipeline_operations/PipelineUpdater.groovy')

    pipelineJobList.add('cicd_files/dsl/pipeline_jobs/BuildAndPublish.groovy')
    pipelineJobList.add('cicd_files/dsl/pipeline_jobs/PreCodeReview.groovy')
    pipelineJobList.add('cicd_files/dsl/testing/SuccessOrFailure.groovy')
    pipelineJobList.add('cicd_files/external/dsl/fem5s11_eiffel052/AutoAppsBuildAndUploadCSAR.groovy')

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

        stage ('Update OSS Idun Release CICD List View') {
            steps {
                jobDsl targets: 'cicd_files/dsl/views/View.groovy'
            }
        }
    }
    post {
        always {
            cleanWs()
        }
    }
}
