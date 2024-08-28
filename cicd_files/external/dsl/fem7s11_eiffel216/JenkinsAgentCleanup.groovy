def pipelineBeingGeneratedName = "oss-idun-release-cicd_Jenkins_Agent_Cleanup"

pipelineJob(pipelineBeingGeneratedName) {
description(
"""
<div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
    <h2>DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
    <h3 style="text-align:left;">Job Description:</h3>
    <p>This job cleans the agent it is run on.</p>
    <p>This is a DSL generated job.</p>
    <p> Repository: <a href="https://gerrit-gamma.gic.ericsson.se/#/admin/projects/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd">OSS/com.ericsson.oss.cicd/oss-idun-release-cicd</a></p>
    <p>Groovy file location: cicd_files/external/dsl/fem7s11_eiffel216/JenkinsAgentCleanup.groovy</p>
    <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
    <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_Jenkins_Agent_Cleanup%20Job">&#128231; Send Mail to provide feedback</a></p>
</div>
""")

    parameters {
        stringParam("SLAVE", "", "Specific slave to clean.")
    }
    disabled(false)
    keepDependencies(false)
    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    definition {
        cps {
            script('''pipeline {
    agent {
        label SLAVE
    }
    stages {
        stage('Clean up workspace folders') {
            steps {
                cleanWorkspaces()
            }
        }
        stage('Clean Docker Data') {
            steps {
                cleanDockerData()
            }
        }
        stage('Clean yum Data') {
            steps {
                cleanYumData()
            }
        }
    }
    post {
        always {
            cleanWs()
            dir("${env.WORKSPACE}@tmp") {
                deleteDir()
            }
            dir("${env.WORKSPACE}@script") {
                deleteDir()
            }
            dir("${env.WORKSPACE}@script@tmp") {
                deleteDir()
            }
        }
    }
}

void cleanWorkspaces() {
    script {
        def agentName = params.SLAVE
        def currentWorkspacePath = "${env.WORKSPACE}"
        def workspacePath = sh(returnStdout: true, script: "echo ${currentWorkspacePath} | sed 's/\\\\/[^\\\\/]*\\$//'").trim()
        println("Workspace directory for agent ${agentName}: ${workspacePath}")

        def runningJobFullName = env.JOB_NAME
        echo "Current Job Name: ${runningJobFullName}"

        def workspaceDirs = sh(returnStdout: true, script: "find ${workspacePath} -mindepth 1 -maxdepth 1 -type d").trim().split("\\n")

        workspaceDirs.each { dir ->
            def jobName = new File(dir).getName()
            if (jobName.startsWith(runningJobFullName + "@")) {
                echo "Currently Running and skipped : ${dir}"
            } else if (!jobName.equals(runningJobFullName)) {
                def isJobRunning = Jenkins.instance.getAllItems(Job.class).find {
                    it.fullName == jobName || (jobName.startsWith(it.fullName + "@") && it.isBuilding())
                }?.isBuilding()
                if (isJobRunning == null || !isJobRunning) {
                    sh "rm -rf ${dir} || true"
                    echo "Removed workspace directory ${dir}"
                } else {
                    echo "Currently Running and skipped : ${dir}"
                }
            }
        }
    }
}

void cleanDockerData() {
    script {
        sh \'\'\'
        echo "INFO: Purging Docker Data."
        docker system prune -a --volumes -f
        \'\'\'
    }
}

void cleanYumData() {
    script {
        sh \'\'\'
        yum_path=$(command -v yum)
        if [[ -n ${yum_path} ]]; then
            echo "INFO: Found yum - cleaning up yum data."
            yum clean all
        fi
        \'\'\'
    }
}
''')
            sandbox()
        }
    }
}
