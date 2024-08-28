def pipelineBeingGeneratedName = 'oss-idun-release-cicd_MANA_Internal_Testing_Pre_Operations'

pipelineJob(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h3 style="text-align:left;">Job Description:</h3>
            <p>This job is used in CaaP TEST pipeline. This job performs the pre-requisite for install/upgrade of EIAE helmfile.</p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_MANA_Internal_Testing_Pre_Operations%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
        '''
    )

    parameters {
        stringParam("SLAVE_LABEL", "fem5dockerslave8", "Label of the Jenkins slave where this jenkins job should be executed.")
        stringParam("ERIC_OSS_ADC_PRODUCT_NO", "CXF1010181_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_ADC_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_ERICSSON_ADAPTATION_PRODUCT_NO", "CXF1010182_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_ERICSSON_ADAPTATION_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_APP_MGR_PRODUCT_NO", "CXF1010183_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_APP_MGR_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_CONFIG_HANDLING_PRODUCT_NO", "CXF1010184_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_CONFIG_HANDLING_VERSION", "", "CSAR version to package")
        stringParam("ERIC_TOPOLOGY_HANDLING_PRODUCT_NO", "CXF1010185_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_TOPOLOGY_HANDLING_VERSION", "", "CSAR version to package")
        stringParam("ERIC_CLOUD_NATIVE_BASE_PRODUCT_NO", "CXF1010186_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CLOUD_NATIVE_BASE_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_DMM_PRODUCT_NO", "CXF1010187_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_DMM_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_COMMON_BASE_PRODUCT_NO", "CXF1010188_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_COMMON_BASE_VERSION", "", "CSAR version to package")
        stringParam("EIAE_DEPLOYMENT_HELM_RELEASE_PACKAGE_PRODUCT_NO", "CXF1010192-", "Please append the R-state to the default value in the field.")
        stringParam("EIAE_DEPLOYMENT_HELM_RELEASE_VERSION", "", "CSAR version to package")
        stringParam("OSS_DEPLOYMENT_MANAGER_RELEASE_PACKAGE_PRODUCT_NO", "CXF1010193-", "Please append the R-state to the default value in the field.")
        stringParam("OSS_DEPLOYMENT_MANAGER_RELEASE_VERSION", "", "CSAR version to package")
        stringParam("ERIC_CLOUD_NATIVE_SERVICE_MESH_PRODUCT_NO", "CXF1010246_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CLOUD_NATIVE_SERVICE_MESH_VERSION", "", "CSAR version to package")
        stringParam("ERIC_CNCS_OSS_CONFIG_PRODUCT_NO", "CXF1010333_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CNCS_OSS_CONFIG_VERSION", "", "CSAR version to package")
        stringParam("ERIC_CNBASE_OSS_CONFIG_PRODUCT_NO", "CXF1010334_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CNBASE_OSS_CONFIG_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_ORAN_SUPPORT_PRODUCT_NO", "CXF1010222_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_ORAN_SUPPORT_VERSION", "", "CSAR version to package")
        stringParam("ERIC_OSS_TASK_AUTOMATION_AE_PRODUCT_NO", "CXF1010224_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_TASK_AUTOMATION_AE_VERSION", "", "CSAR version to package")
        stringParam("CSAR_DOWNLOAD_LOCATION_PATH", "", "Path on server where CSAR packages will be downloaded. Please ensure the filesystem has space available for the path provided. E.g. /proj/cloudman")
        stringParam("USER_CREDENTIAL_ID", "", "Credential ID stored in Jenkins for the user (Secret Text).")
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
                    }
                    extensions {
                        cleanBeforeCheckout()
                        localBranch 'master'
                    }
                }
            }
            scriptPath('cicd_files/external/jenkins/files/fem5s11_eiffel052/ManaInternalTestingPreOperations.Jenkinsfile')
        }
    }
}
