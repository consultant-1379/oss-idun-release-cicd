def pipelineBeingGeneratedName = 'oss-idun-release-cicd_Pipeline_Generator'

pipelineJob(pipelineBeingGeneratedName) {
    description("""
        <div style='width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #005499bd;
            color: #242424;font-family: &quot;Ericsson Hilda&quot;, Helvetica, Arial, sans-serif;'>
            <h1 style='text-align:left;'>Job Description:</h1>
            <p>The oss-idun-release-cicd_Pipeline_Generator job is used to generate initial OSS Idun Release CICD pipelines and a CICD pipeline update job</p>
            <p style='text-align:center'><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href='mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=RE:oss-idun-release-cicd_Pipeline_Generator'>&#128231;
            Send Mail to provide feedback</a></p>
        </div>
    """)

    parameters {
        stringParam('SLAVE', 'RHEL7_GE_Docker_1', 'Label or name of the Jenkins GE where this jenkins job should be executed.')
    }

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
            scriptPath('cicd_files/jenkins/files/pipeline_operations/fem7s11_eiffel216/PipelineGenerator.Jenkinsfile')
        }
    }
}
