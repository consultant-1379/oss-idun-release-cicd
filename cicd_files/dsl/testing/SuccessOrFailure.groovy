import common_classes.CommonSteps
import common_classes.CommonParameters
import common_classes.ExternalParameters

CommonSteps commonSteps = new CommonSteps()
CommonParameters commonParams = new CommonParameters()
ExternalParameters externalParams = new ExternalParameters()


def pipelineBeingGeneratedName = 'SUCCESS_OR_FAILURE (DO NOT DELETE - FOR TESTING PURPOSES)'

pipelineJob(pipelineBeingGeneratedName) {
    description(commonSteps.defaultJobDescription(pipelineBeingGeneratedName,
        """<p>The ${pipelineBeingGeneratedName} job is <b>for Thunderbee testing purposes only</b>.
        It is used, during testing of e2e pipeline modifications, to replace production jobs,
        setting the result of a particular stage to a failure or success, as required.</p>""",
        "cicd_files/dsl/testing/SuccessOrFailure.groovy",
        "jobs/jenkinsfiles/testing/SuccessOrFailure.Jenkinsfile"))
    keepDependencies(false)
    logRotator(commonSteps.defaultLogRotatorValues())
    parameters {
        stringParam(commonParams.slave())
        stringParam(externalParams.success())
    }
    definition {
        cpsScm {
            scm {
                git {
                    branch('master')
                        remote {
                            name('gcn')
                            url(commonParams.repoUrl())
                        }
                        extensions {
                            cleanBeforeCheckout()
                            localBranch 'master'
                        }
                }
            }
            scriptPath('jobs/jenkinsfiles/testing/SuccessOrFailure.Jenkinsfile')
        }
    }
    quietPeriod(5)
    disabled(false)
}
