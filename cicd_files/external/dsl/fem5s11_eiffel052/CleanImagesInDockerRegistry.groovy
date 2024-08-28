def pipelineBeingGeneratedName = "oss-idun-release-cicd_Clean_Images_In_Docker_Registry"

pipelineJob(pipelineBeingGeneratedName) {
description(
"""
<div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
    <h2>DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
    <h3 style="text-align:left;">Job Description:</h3>
    <p>This job cleans the images from the docker registry attached to the environment.</p>
    <p>This is a DSL generated job.</p>
    <p>Repository: OSS/com.ericsson.oss.cicd/oss-idun-release-cicd</p>
    <p>Groovy file location: cicd_files/external/dsl/fem5s11_eiffel052/CleanImagesInDockerRegistry.groovy</p>
    <p>Jenkinsfile location: cicd_files/external/jenkins/files/fem5s11_eiffel052/CleanImagesInDockerRegistry.Jenkinsfile</p>
    <br/>
    <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b>
    <br/>
    <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_Clean_Images_In_Docker_Registry%20Job">&#128231; Send Mail to provide feedback</a></p>
</div>
""")

    parameters {
        stringParam("ENV_FILES_BUCKET_NAME", "None", "Name of the Environment file OST bucket that stores the kube config file for the environment. ONLY USED if environment data is stored in OST.")
        stringParam("FUNCTIONAL_USER_SECRET", "TB_DATA_STORE_AUTH", "Jenkins secret ID for accessing DIT/OST")
        stringParam("NAMESPACE", "", "Namespace on the cluster that the deployment is installed into.")
        stringParam("KUBE_CONFIG", "", "ID of the Kubernetes config file that has been stored on the Jenkins server in the credentials area as a file.")
        stringParam("SLAVE_LABEL", "", "Label of the Jenkins slave where this jenkins job should be executed.")
        stringParam("REGISTRY_CREDENTIALS", "", "ID of the registry credentails that has been stored on the Jenkins server.")
        booleanParam("DELETE_ALL_IMAGES", false, "If set to true, all images in the registry of the environemnt are cleaned up including those of in use images.")
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
            scriptPath('cicd_files/external/jenkins/files/fem5s11_eiffel052/CleanImagesInDockerRegistry.Jenkinsfile')
        }
    }
}