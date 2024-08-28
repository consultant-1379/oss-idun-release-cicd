def pipelineBeingGeneratedName = 'oss-idun-release-cicd_Copy_EIC_Site_Values_To_OST'

pipelineJob(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h3 style="text-align:left;">Job Description:</h3>
            <p>This job is used in <a href="https://spinnaker.rnd.gic.ericsson.se/#/applications/eic-release-e2e-cicd/executions?pipeline=eic-release-end-of-sprint-activities">eic-release-end-of-sprint-activities</a>.</p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_Copy_EIC_Site_Values_To_OST%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
        '''
    )

    parameters {
        stringParam('SLAVE_LABEL', 'RHEL7_GE_Docker_1', 'Agent label or name to run the job on.')
        stringParam('VERSION', '', 'The version of EIC helmfile to be copied.')
        stringParam('USER_AUTH', 'TB_DATA_STORE_AUTH', 'user:password for data storage cli.')
        stringParam('BUCKET_NAME', 'eic_site_values_versioned', 'The name of the OST bucket.')
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
                    }
                }
            }
            scriptPath('cicd_files/external/jenkins/files/fem7s11_eiffel216/CopyEicSiteValuesToOst.Jenkinsfile')
        }
    }
}
