def pipelineBeingGeneratedName = "oss-idun-release-cicd_Get_Difference_Between_App_Versions"

job(pipelineBeingGeneratedName) {
    description("""
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h2> DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
            <h3 style="text-align:left;">Job Description:</h3>
            <p>This job produces difference between two releases.</p>
            <p>Please note this is a temporary solution and will be replaced with the deliverable from <a href="https://jira-oss.seli.wh.rnd.internal.ericsson.com/browse/IDUN-14238">IDUN-14238</a></p>.
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_Get_Difference_Between_App_Versions%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
    """)

    disabled(false)

    concurrentBuild(false)

    keepDependencies(false)

    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    parameters {
        textParam("PREVIOUS_RELEASE_INT_CHART_VERSION", "", "")
        textParam("LATEST_RELEASE_INT_CHART_VERSION", "", "")
        stringParam("INT_CHART_VERSION", "", "")
        stringParam("IS_DM_RELEASED", "", "")
        stringParam("OSS_DEPLOYMENT_MANAGER_VERSION", "", "")
    }

    label("RHEL7_GE_Docker_1")

    scm {
        git {
            branch("master")
            remote {
                url("\${GERRIT_MIRROR}/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd")
                credentials("GERRIT_PASSWORD")
            }
        }
    }

    steps {
        shell("python3 cicd_files/external/scripts/release_diff.py")
        shell("cat artifact.properties")
    }

    publishers {
        archiveArtifacts {
            pattern("artifact.properties")
            allowEmpty(false)
            onlyIfSuccessful(true)
            fingerprint(false)
            defaultExcludes(true)
        }
        wsCleanup()
    }
}
