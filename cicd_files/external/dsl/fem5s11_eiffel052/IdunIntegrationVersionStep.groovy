def pipelineBeingGeneratedName = 'idun-integration-version-step'

pipelineJob(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h3 style="text-align:left;">Job Description:</h3>
            <p>This job is used in <a href="https://spinnaker.rnd.gic.ericsson.se/#/applications/eic-release-e2e-cicd/executions?pipeline=eic-release-end-of-sprint-activities">eic-release-end-of-sprint-activities</a>.</p>
            <p>The job increments MAJOR, MINOR, PATCH version for the helmfile.</p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=idun-integration-version-step%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
        '''
    )

    parameters {
        stringParam('CHART_PATH', 'helmfile', 'The path to helm chart')
        stringParam('VERSION_TYPE', 'MINOR', 'Enter \'MAJOR\', \'MINOR\' or \'PATCH\'')
        stringParam('SLAVE_LABEL', 'evo_docker_engine_athlone', 'Slave label to use')
    }

    disabled(false)

    keepDependencies(false)

    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    definition {
        cpsScm {
            scm {
                git {
                    branch('master')
                    remote {
                        url('${GERRIT_CENTRAL_HTTP}/OSS/com.ericsson.oss.eiae/eiae-helmfile')
                        credentials('eoadm100-user-credentials')
                    }
                }
            }
            scriptPath('ci/jenkins/files/versionStep.Jenkinsfile')
        }
    }
}
