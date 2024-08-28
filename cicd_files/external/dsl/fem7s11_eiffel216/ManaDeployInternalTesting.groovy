def pipelineBeingGeneratedName = "oss-idun-release-cicd_MANA_Deploy_Internal_Testing"

pipelineJob(pipelineBeingGeneratedName) {
    description(
"""
<div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
    <h2> DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
    <h3 style="text-align:left;">Job Description:</h3>
    <p>This job is used to install/upgrade the EIAP on an environment.</p>
    <p>This is test pipeline and should only be used when running it against an internal test environment.</p>
    <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
    <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_MANA_Deploy_Internal_Testing%20Job">&#128231; Send Mail to provide feedback</a></p>
</div>
"""
    )

    parameters {
        stringParam("SLAVE_LABEL", "DUMMY", "Label of the Jenkins slave where this jenkins job should be executed for internal testing.") // Update the GE name
        stringParam("DEPLOYMENT_TYPE", "upgrade", "Deployment Type, set 'install' or 'upgrade'")
        stringParam("HELM_TIMEOUT", "3600", "Time in seconds for the Deployment Manager to wait for the deployment to execute, default 1800.")
        stringParam("NAMESPACE", "", "Namespace on the cluster that the deployment is installed into.")
        stringParam("KUBECONFIG_FILE", "", "ID of the Kubernetes config file that has been stored on the Jenkins server in the credentials area as a file.")
        stringParam("CRD_NAMESPACE", "crd-namespace", "Namespace which was used to deploy the CRD.")
        stringParam("ERIC_OSS_ADC_PRODUCT_NO", "CXF1010181_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_ERICSSON_ADAPTATION_PRODUCT_NO", "CXF1010182_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_APP_MGR_PRODUCT_NO", "CXF1010183_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_CONFIG_HANDLING_PRODUCT_NO", "CXF1010184_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_TOPOLOGY_HANDLING_PRODUCT_NO", "CXF1010185_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CLOUD_NATIVE_BASE_PRODUCT_NO", "CXF1010186_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_DMM_PRODUCT_NO", "CXF1010187_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_COMMON_BASE_PRODUCT_NO", "CXF1010188_1-", "Please append the R-state to the default value in the field.")
        stringParam("EIAE_DEPLOYMENT_HELM_RELEASE_PACKAGE_PRODUCT_NO", "CXF1010192-", "Please append the R-state to the default value in the field.")
        stringParam("OSS_DEPLOYMENT_MANAGER_RELEASE_PACKAGE_PRODUCT_NO", "CXF1010193-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CLOUD_NATIVE_SERVICE_MESH_PRODUCT_NO", "CXF1010246_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CNCS_OSS_CONFIG_PRODUCT_NO", "CXF1010333_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_CNBASE_OSS_CONFIG_PRODUCT_NO", "CXF1010334_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_ORAN_SUPPORT_PRODUCT_NO", "CXF1010222_1-", "Please append the R-state to the default value in the field.")
        stringParam("ERIC_OSS_TASK_AUTOMATION_AE_PRODUCT_NO", "CXF1010224_1-", "Please append the R-state to the default value in the field.")
        stringParam("OSS_DEPLOYMENT_MANAGER_VERSION", "", "The version of Deployment Manager to use.")
        stringParam("DESTINATION_SERVER_PACKAGE_LOCATION", "/tmp", "Location of the package on the destination internal server")
        stringParam("CSAR_DOWNLOAD_LOCATION_PATH", "", "Path on server where the working directory is available. E.g. /proj/cloudman. The working directory 'swgw_download_dir' will automatically be created.")
    }

    disabled(false)

    keepDependencies(false)

    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    definition {
        cps {
            script('''
pipeline {
    agent {
        label params.SLAVE_LABEL
    }
    environment {
        DEPLOYMENT_MANAGER_IMAGE = "deployment-manager"
        PACKAGE_DIR = "swgw_download_dir"
    }
    stages {
        stage("Extract the CXF files") {
            environment {
                EIAP_FILES_LOCATION = sh(script: "echo ${params.CSAR_DOWNLOAD_LOCATION_PATH}/${env.PACKAGE_DIR}", returnStdout: true).trim()
            }
            steps {
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_ADC_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_ERICSSON_ADAPTATION_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_APP_MGR_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_CONFIG_HANDLING_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_TOPOLOGY_HANDLING_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_CLOUD_NATIVE_BASE_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_DMM_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_COMMON_BASE_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_CNCS_OSS_CONFIG_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_CNBASE_OSS_CONFIG_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_CLOUD_NATIVE_SERVICE_MESH_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.EIAE_DEPLOYMENT_HELM_RELEASE_PACKAGE_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.OSS_DEPLOYMENT_MANAGER_RELEASE_PACKAGE_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_ORAN_SUPPORT_PRODUCT_NO}.zip"
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.ERIC_OSS_TASK_AUTOMATION_AE_PRODUCT_NO}.zip"
            }
        }
        stage("Unpack Files from Package") {
            steps {
                sh "tar -xvf ${params.DESTINATION_SERVER_PACKAGE_LOCATION}/package.tar.gz"
            }
        }
        stage("Prepare Working Directory") {
            steps {
                sh "mkdir -p ./kube_config ./certificates"
                sh "mv ${env.WORKSPACE}/package/certificates/* ${env.WORKSPACE}/certificates"
                sh "mv ${env.WORKSPACE}/package/site*.yaml ${env.WORKSPACE}"
            }
        }
        stage("Untar Helmfile") {
            steps {
                sh "tar -xvf ${env.WORKSPACE}/eric-eiae-helmfile*.tgz"
            }
        }
        stage("Unpack and init Deployment Manager") {
            steps {
                sh "unzip ${env.WORKSPACE}/${env.DEPLOYMENT_MANAGER_IMAGE}*.zip"
                sh "docker load --input ${env.DEPLOYMENT_MANAGER_IMAGE}.tar"
                sh "docker run -u \\$(id -u ${USER}):\\$(id -g ${USER}) -v /var/run/docker.sock:/var/run/docker.sock -v ${env.WORKSPACE}:/workdir -v /etc/hosts:/etc/hosts ${env.DEPLOYMENT_MANAGER_IMAGE}:${params.OSS_DEPLOYMENT_MANAGER_VERSION} init"
            }
        }
        stage("Retrieve Kubeconfig File") {
            steps {
                withCredentials([file(credentialsId: env.KUBECONFIG_FILE, variable: "KUBECONFIG")]) {
                    sh 'install -m 600 $KUBECONFIG ./kube_config/config'
                }
            }
        }
        stage("Helmfile Deploy") {
            steps {
                sh "docker run --network=host -u \\$(id -u ${USER}):\\$(id -g ${USER}) --init --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${env.WORKSPACE}:/workdir -v /etc/hosts:/etc/hosts --user 0:0 --workdir ${env.WORKSPACE} ${env.DEPLOYMENT_MANAGER_IMAGE}:${params.OSS_DEPLOYMENT_MANAGER_VERSION} ${params.DEPLOYMENT_TYPE} --namespace ${params.NAMESPACE} --helm-timeout ${params.HELM_TIMEOUT} --crd-namespace ${params.CRD_NAMESPACE} --docker-timeout 600"
            }
            post {
                failure {
                    script {
                        withCredentials([file(credentialsId: env.KUBECONFIG_FILE, variable: "KUBECONFIG")]) {
                            sh 'install -m 600 $KUBECONFIG ./admin.conf'
                            sh "docker run --network=host -u \\$(id -u ${USER}):\\$(id -g ${USER}) --init --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${env.WORKSPACE}:/workdir -v /etc/hosts:/etc/hosts --workdir ${env.WORKSPACE} ${env.DEPLOYMENT_MANAGER_IMAGE}:${params.OSS_DEPLOYMENT_MANAGER_VERSION} collect-logs --namespace ${params.NAMESPACE} || true"
                            archiveArtifacts artifacts: "logs_*.tgz, logs/*", allowEmptyArchive: true
                        }
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs(
                deleteDirs: true,
                disableDeferredWipeout: true
            )
        }
    }
}
'''
            )
            sandbox()
        }
    }
}
