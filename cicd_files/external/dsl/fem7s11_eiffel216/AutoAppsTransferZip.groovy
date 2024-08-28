import common_classes.CommonParameters

CommonParameters commonParams = new CommonParameters()

def pipelineBeingGeneratedName = 'EIC-AUTO-APP-Transfer-ZIP'
def defaultSlaveLabel = "RHEL7_GE_Docker_1"

pipelineJob(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h2>DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
            <h3 style="text-align:left;">Job Description:</h3>
            <p>EIC-AUTO-APP-Transfer-ZIP job copies a zip file of a tested artifact after a successful E2E run and uploads to a separate artifactory that exclusivly contains fully tested versions.</p>
            <p>This is a DSL generated job.</p>
            <p> Repository: <a href="https://gerrit-gamma.gic.ericsson.se/#/admin/projects/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd">OSS/com.ericsson.oss.cicd/oss-idun-release-cicd</a></p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=EIC-AUTO-APP-Transfer-ZIP%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>''')

    keepDependencies(false)

    parameters {
        stringParam('CHART_NAME', '', 'Chart Name of Chart to build from. NOTE: The ZIP name will be \'CHART_NAME-CHART_VERSION\'.')
        stringParam('CHART_VERSION', '', 'Version of the Chart to build from.')
        stringParam('ZIP_DOWNLOAD_REPO', '', 'The path of the folder which will contain the input template files for the ZIP build.')
        stringParam('ZIP_UPLOAD_REPO', '', 'The path of the folder which will contain the output template files for the ZIP build.')
        stringParam('ZIP_STORAGE_INSTANCE', 'https://arm.seli.gic.ericsson.se', 'Storage Instance to push the CSARs to.')
        stringParam('FUNCTIONAL_USER_SECRET', 'cloudman-user-creds','Jenkins secret ID for ARM Registry Credentials.')
        stringParam(commonParams.slave('RHEL7_GE_Docker_1'))
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
            scriptPath('cicd_files/external/jenkins/files/fem7s11_eiffel216/AutoAppsTransferZip.Jenkinsfile')
        }
    }

    quietPeriod(5)

    disabled(false)
}
