def pipelineBeingGeneratedName = 'oss-idun-release-cicd_Set_Path_To_Site_Values'

job(pipelineBeingGeneratedName) {
    description("""
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h2> DO NOT UPDATE JOB without Contacting THUNDERBEE</h2>
            <h3 style="text-align:left;">Job Description:</h3>
            <p>Generates an <b>artifact.properties</b> file containing the <b>PATH_TO_SITE_VALUES_FILE</b> variable.<br/>
            <p>Please note this is a temporary solution for
            <a href='https://jira-oss.seli.wh.rnd.internal.ericsson.com/browse/IDUN-14873'>IDUN-14873</a> and will be replaced with
            the deliverable from <a href='https://jira-oss.seli.wh.rnd.internal.ericsson.com/browse/IDUN-14705'>IDUN-14705</a></p>.
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_Set_Path_To_Site_Values%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
    """)

    keepDependencies(false)

    disabled(false)

    concurrentBuild(false)

    label('fem5dockerslave8')

    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    parameters {
        stringParam(['SLAVE', 'fem5dockerslave8', 'Slave to run job on.'])
        stringParam(['INT_CHART_VERSION', '', 'The integration chart version, used to select a site values file.'])
    }

    scm {
        git {
            branch('master')
            remote {
                url('\${GERRIT_MIRROR_HTTPS}/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd')
                credentials('eoadm100-user-credentials')
            }
            extensions {
                cleanBeforeCheckout()
                localBranch 'master'
                choosingStrategy {
                    gerritTrigger()
                }
            }
        }
    }

    steps {
        shell('python3 cicd_files/external/scripts/select_site_values.py')
        shell('cat artifact.properties')
    }

    publishers {
        archiveArtifacts {
            pattern('artifact.properties')
            allowEmpty(false)
            onlyIfSuccessful(true)
            fingerprint(false)
            defaultExcludes(true)
        }
        wsCleanup()
    }
}
