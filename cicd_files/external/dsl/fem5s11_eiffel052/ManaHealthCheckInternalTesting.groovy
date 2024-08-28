def pipelineBeingGeneratedName = 'oss-idun-release-cicd_MANA_Health_Check_Internal_Testing'

pipelineJob(pipelineBeingGeneratedName) {
description(
"""
<div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
    <h2>DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
    <h3 style="text-align:left;">Job Description:</h3>
    <p>This job checks the status of HELM deployments on MANA environments.</p>
    <p>This is test pipeline and should only be used when running it against an internal test environment.</p>
    <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
    <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_MANA_Health_Check_Internal_Testing%20Job">&#128231; Send Mail to provide feedback</a></p>
</div>
""")

    parameters {
        stringParam("NAMESPACE", "", "Namespace on the cluster that the deployment is installed into.")
        stringParam("KUBECONFIG_FILE", "", "ID of the Kubernetes config file that has been stored on the Jenkins server in the credentials area as a file.")
        stringParam("SLAVE_LABEL", "fem5dockerslave8", "Label of the Jenkins slave where this jenkins job should be executed.")
        stringParam("OSS_DEPLOYMENT_MANAGER_RELEASE_PACKAGE_PRODUCT_NO", "CXF1010193-", "The release package product number of Deployment Manager to use.")
        stringParam("OSS_DEPLOYMENT_MANAGER_VERSION", "", "The version of Deployment Manager to use.")
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
        PACKAGE_DIR = 'swgw_download_dir'
    }
    stages {
        stage("Extract the CXF files") {
            environment {
                EIAP_FILES_LOCATION = sh(script: "echo ${params.CSAR_DOWNLOAD_LOCATION_PATH}/${env.PACKAGE_DIR}", returnStdout: true).trim()
            }
            steps {
                sh "unzip -o ${env.EIAP_FILES_LOCATION}/${params.OSS_DEPLOYMENT_MANAGER_RELEASE_PACKAGE_PRODUCT_NO}.zip"
            }
        }
        stage("Unpack and init Deployment Manager") {
            steps {
                sh "unzip -o ${env.DEPLOYMENT_MANAGER_IMAGE}*.zip"
                sh "docker load --input ${env.DEPLOYMENT_MANAGER_IMAGE}.tar"
            }
        }
        stage("Executing Health Check using Deployment Manager") {
            steps {
                script {
                    withCredentials( [file(credentialsId: env.KUBECONFIG_FILE, variable: "KUBECONFIG")]) {
                        sh """
                            if [ -d "./kube_config" ]
                            then
                                rm -r ./kube_config
                            fi
                        """
                        sh "mkdir ./kube_config"
                        sh 'install -m 600 $KUBECONFIG ./kube_config/config'
                        sh "docker run --network=host -u \\$(id -u ${USER}):\\$(id -g ${USER}) --init --rm -v /var/run/docker.sock:/var/run/docker.sock -v ${env.WORKSPACE}:/workdir -v /etc/hosts:/etc/hosts --user 0:0 -e KUBECONFIG=/workdir/kube_config/config --workdir ${env.WORKSPACE} ${env.DEPLOYMENT_MANAGER_IMAGE}:${params.OSS_DEPLOYMENT_MANAGER_VERSION} health-check all --namespace ${params.NAMESPACE}"
                    }
                }
            }
        }
        stage("Checking output") {
            steps {
                script {
                    try {
                        // Fails with non-zero exit if string does not exist in log file
                        def dir1 = sh(
                            script:
                                'cd logs; ' +
                                'cat "$(ls -1rt | grep healthcheck | tail -n1)" | grep "healthcheck all ' +
                                'command completed successfully with no failures"',
                            returnStdout:true).trim()
                    } catch (Exception ex) {
                        println("Healthcheck didn't succeed: ${ex}")
                        currentBuild.result = 'FAILED'
                    }
                }
            }
        }
    }
    post {
        always {
            cleanWs(
                deleteDirs: true,
                disableDeferredWipeout: true,
                notFailBuild: true
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