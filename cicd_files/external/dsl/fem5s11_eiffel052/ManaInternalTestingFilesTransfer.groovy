def pipelineBeingGeneratedName = "oss-idun-release-cicd_MANA_Internal_Testing_Files_Transfer"

job(pipelineBeingGeneratedName) {
    description('''
        <div style="width:fit-content;background:#fbf6e1;padding:1em;border-radius:1em;box-shadow: 0 0.1em 0.3em #525000">
            <h3 style="text-align:left;">Job Description:</h3>
            <p>This job is used in CaaP TEST pipeline. This job transfers the required files for install/upgrade of EIAE helmfile.</p>
            <p style="text-align:center"><b>&#x26A1; Job developed and maintained by Thunderbee &#x26A1;</b><br/>
            <a href="mailto:PDLENMCOUN@pdl.internal.ericsson.com?Subject=oss-idun-release-cicd_MANA_Internal_Testing_Files_Transfer%20Job">&#128231; Send Mail to provide feedback</a></p>
        </div>
        ''')

    disabled(false)

    concurrentBuild(false)

    keepDependencies(false)

    logRotator {
        daysToKeep 25
        numToKeep 20
    }

    parameters {
        stringParam('INT_CHART_VERSION', '', 'The version of base platform to install or upgrade')
        stringParam('PATH_TO_CERTIFICATES_FILES', '', 'Path within the Repo to the location of the certificates directory')
        fileParam('SITE_VALUES_UPLOAD', 'Please upload the site_values from your local machine')
        stringParam('DESTINATION_SERVER_HOSTNAME_OR_IP', 'None', 'IP address or hostname for the Destination Server')
        stringParam('DESTINATION_SERVER_PACKAGE_LOCATION', '/tmp', 'Location of the package on the destination server')
    }

    label("fem5dockerslave6")

    scm {
        git {
            branch("master")
            remote {
                url("\${GERRIT_MIRROR}/OSS/com.ericsson.oss.cicd/oss-idun-release-cicd")
            }
            extensions {
                cleanBeforeCheckout()
                localBranch "master"
                choosingStrategy {
                    gerritTrigger()
                }
            }
        }
    }

    wrappers {
        credentialsBinding {
            file('DOCKERCONFIG', 'cloudman-docker-auth-config')
            usernamePassword('SCP_USER', 'SCP_USER_PASSWORD', '72b3037e-a19e-4625-b51f-5573127d1b64')
        }
    }

    steps {
        shell("""
            echo 'Prepare Working Directory'
            git clone --depth 1 \${GERRIT_CENTRAL_HTTP}/OSS/com.ericsson.oss.orchestration.eo/eo-integration-ci
            install -m 600 \${DOCKERCONFIG} \${HOME}/.docker/config.json
        """)

        shell("""
            echo 'Site Values setup'
            cp \${PWD}/SITE_VALUES_UPLOAD \${PWD}/site_values_\${INT_CHART_VERSION}.yaml
            rm -rf \${PWD}/SITE_VALUES_UPLOAD
        """)

        shell("""
            echo 'Certificates files'
            mkdir certificates
            cp -r \${PWD}/\${PATH_TO_CERTIFICATES_FILES}/* \${PWD}/certificates
        """)

        shell("""
            echo 'Create Archive'
            mkdir package
            cp -ar certificates package
            cp site_values_\${INT_CHART_VERSION}.yaml package
            tar -czvf package.tar.gz package
        """)

        shell("""
            echo 'Transfer File to the VM'
            scp package.tar.gz \${SCP_USER}@\${DESTINATION_SERVER_HOSTNAME_OR_IP}:\${DESTINATION_SERVER_PACKAGE_LOCATION}
        """)
    }

    publishers {
        wsCleanup()
    }
}
