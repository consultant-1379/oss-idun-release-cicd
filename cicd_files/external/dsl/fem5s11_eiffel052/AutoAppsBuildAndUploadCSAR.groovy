def pipelineBeingGeneratedName = 'EIAP-AUTO-APP-CSAR-Builder'

pipelineJob(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h2>DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
            <h3 style="text-align:left;">Job Description:</h3>
            <p>EIAP-AUTO-APP-CSAR-Builder job builds a CSAR and uploads to an artifactory.</p>
            <p>This is a DSL generated job.</p>
            <p> Repository: <a href="https://gerrit-gamma.gic.ericsson.se/#/admin/projects/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd">OSS/com.ericsson.oss.cicd/oss-idun-release-cicd</a></p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=EIAP-AUTO-APP-CSAR-Builder%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>''')

    keepDependencies(false)

    parameters {
        stringParam('CHART_NAME', '', 'Chart Name of Chart to build from. NOTE: The CSAR name will be \'CHART_NAME-CHART_VERSION\'.')
        stringParam('CHART_VERSION', '', 'Version of the Chart to build from.')
        stringParam('CHART_REPO', '', 'Repo to fetch the chart from.')
        stringParam('SSH_REPO_URL', '', 'SSH URL to the repo that will contain the input template files for the CSAR build.')
        stringParam('INPUT_FOLDER_LOCATION', '', 'This path of the folder which will contain the input template files for the CSAR build.')
        stringParam('ADDITIONAL_VALUES_LOCATION', '', 'This path of the folder which will contain the additional values files for the CSAR build.')
        stringParam('CSAR_STORAGE_INSTANCE', 'https://arm.seli.gic.ericsson.se', 'Storage Instance to push the CSARs to. NOTE: Use Default if unsure.')
        stringParam('CSAR_STORAGE_REPO', 'proj-eric-oss-dev-generic-local', 'Storage directory to push the CSARs to. NOTE: Use Default if unsure.')
        stringParam('CSAR_REPO_SUBPATH', 'csars/rapps', 'Storage sub-directory to push the CSARs to. NOTE: Use Default if unsure (production directory).')
        stringParam('ARMDOCKER_USER_SECRET', 'cloudman-docker-auth-config', 'ARM Docker secret')
        stringParam('FUNCTIONAL_USER_SECRET', 'cloudman-user-creds','Jenkins secret ID for ARM Registry Credentials.')
        stringParam('SLAVE_LABEL', 'evo_docker_engine', 'Specify the slave label that you want the job to run on.')
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('${GERRIT_MIRROR}/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd')
                    }
                    branch('master')
                }
            }
            scriptPath('cicd_files/external/jenkins/files/fem5s11_eiffel052/AutoAppsBuildAndUploadCSAR.Jenkinsfile')
        }
    }

    quietPeriod(5)

    disabled(false)
}
