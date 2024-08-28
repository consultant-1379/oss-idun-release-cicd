import common_classes.CommonSteps
import common_classes.CommonParameters

CommonSteps commonSteps = new CommonSteps()
CommonParameters commonParams = new CommonParameters()

def pipelineBeingGeneratedName = 'oss-idun-release-cicd_Build_And_Publish'

pipelineJob(pipelineBeingGeneratedName) {
    description(commonSteps.defaultJobDescription(pipelineBeingGeneratedName,
        """This ${pipelineBeingGeneratedName} job packages the OSS Idun CICD CLI helm chart.
        It then publishes the package to the armdocker JFrog artifactory.""",
        "cicd_files/dsl/pipeline_jobs/BuildAndPublish.groovy",
        "cicd_files/jenkins/files/pipeline_jobs/BuildAndPublish.Jenkinsfile"))
    keepDependencies(false)
    parameters {
        stringParam(commonParams.slave())
    }
    blockOn('''oss-idun-release-cicd_Pre_Code_Review
oss-idun-release-cicd_Pipeline_Updater''', {
        blockLevel('GLOBAL')
        scanQueueFor('DISABLED')
    })

    triggers {
        gerrit {
            project(commonParams.repo(), "master")
            events {
                changeMerged()
            }
        }
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        name('gcn')
                        url(commonParams.repoUrl())
                    }
                    branch('master')
                }
            }
            scriptPath('cicd_files/jenkins/files/pipeline_jobs/BuildAndPublish.Jenkinsfile')
        }
    }

    quietPeriod(10)
    disabled(false)
}
