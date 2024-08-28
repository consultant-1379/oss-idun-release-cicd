def pipelineBeingGeneratedName = 'oss-idun-release-cicd_Files_Transfer_Between_Agents'

pipelineJob(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h3 style="text-align:left;">Job Description:</h3>
            <p>This job is used in <a href="https://spinnaker.rnd.gic.ericsson.se/#/applications/idun-release-e2e-cicd/executions?pipeline=idun-connected-release-e2e-Flow">CaaP pipeline</a>. This job is used to transfer the files from one VM to another.</p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_Files_Transfer_Between_Agents%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
        '''
    )

    parameters {
        stringParam('INT_CHART_VERSION', '', 'The version of base platform to install or upgrade')
        stringParam('PATH_TO_CERTIFICATES_FILES', '', 'Path within the Repo to the location of the certificates directory')
        stringParam('PATH_TO_SITE_VALUES_FILE', '', 'Full path within the Repo to the site_values.yaml file')
        stringParam('TYPE_OF_CREDENTIAL_FOR_SCP', '', 'Type of credential to use for SCP. Please enter "UsernamePassword" or "sshUserPrivateKey"')
        stringParam('FUNCTIONAL_SCP_USER_SECRET', 'None', 'Jenkins secret credentials ID')
        stringParam('SLAVE_LABEL', 'DUMMY', 'Specify the slave label that you want the job to run on') // Update the GE name
        stringParam('ARMDOCKER_USER_SECRET', 'cloudman-docker-auth-config', 'ARM Docker secret')
        stringParam('DESTINATION_SERVER_HOSTNAME_OR_IP', 'None', 'IP address or hostname for the Destination Server')
        stringParam('DESTINATION_SERVER_PACKAGE_LOCATION', '/tmp', 'Location of the package on the destination server')
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
                        url('${GERRIT_MIRROR}/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd')
                        credentials("GERRIT_PASSWORD")
                    }
                    extensions {
                        cleanBeforeCheckout()
                        localBranch 'master'
                    }
                }
            }
            scriptPath('cicd_files/external/jenkins/files/fem7s11_eiffel216/FileTransferBetweenAgents.Jenkinsfile')
        }
    }
}
