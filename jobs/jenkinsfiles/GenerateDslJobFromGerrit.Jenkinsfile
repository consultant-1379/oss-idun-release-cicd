#!/usr/bin/env groovy
import groovy.json.JsonSlurper
import jenkins.model.*

pipeline {
  agent {
    label SLAVE
  }
  parameters {
    string(name: 'gerrit_link', defaultValue: '', description: 'Link to Gerrit review.')
    string(name: 'class_path', defaultValue: '', description: 'Location of parent directory of where the Jenkinsfiles are.')
    string(name: 'SLAVE', defaultValue: '', description: 'Specify the slave label that you want the job to run on.')
  }
stages {

        stage('Clean Ws') {
            steps {
                cleanWs()
            }
        }

        stage('Perform required rest calls to gerrit review') {
            steps {
                script {
                    env.gerrit_link_for_rest_call = gerrit_link.replace("#/c", "a/changes")
                    env.string_containing_gerrit_info = sh(script: 'set +x; curl -X GET "${gerrit_link_for_rest_call}detail" -H "Authorization: Basic YmJmdW5jdXNlcjpCQi5BcHBsaWNhdGlvbkAxMjM0NQ" -H "Content-Type: application/json" | cut -d "\'" -f2', returnStdout: true).trim()
                    env.string_containing_files_changed = sh(script: 'set +x; curl -X GET "${gerrit_link_for_rest_call}revisions/current/files" -H "Authorization: Basic YmJmdW5jdXNlcjpCQi5BcHBsaWNhdGlvbkAxMjM0NQ" -H "Content-Type: application/json" | cut -d "\'" -f2', returnStdout: true).trim()
                }
            }
        }

        stage ('Get job generation details') {
            steps {
                script {
                    def object_containing_files_changed = new JsonSlurper().parseText(string_containing_files_changed)
                    def object_containing_gerrit_info = new JsonSlurper().parseText(string_containing_gerrit_info)

                    def change_id =  gerrit_link.split("/")[5]
                    def last_two_digits_of_change_id = change_id.substring(change_id.length() - 2)
                    def revision = object_containing_gerrit_info.messages[object_containing_gerrit_info.messages.size() - 1]._revision_number

                    env.project_url = object_containing_gerrit_info.project

                    env.refspec_link = "refs/changes/" + last_two_digits_of_change_id + "/" + change_id + "/" + revision

                    env.underscored_review_jira = object_containing_gerrit_info.subject.split(" ")[0].replace("-", "_")

                    def listOfChangedFiles = object_containing_files_changed.keySet()

                    def changedGroovyFiles = []
                    for (changedFileIndex = 0; changedFileIndex < listOfChangedFiles.size(); changedFileIndex++) {
                        if (listOfChangedFiles[changedFileIndex].contains(".groovy") && listOfChangedFiles[changedFileIndex].contains("DSL") && !listOfChangedFiles[changedFileIndex].contains("common")) {
                            changedGroovyFiles = changedGroovyFiles.plus(listOfChangedFiles[changedFileIndex])
                        }
                    }
                    env.changedGroovyFilesAsString = changedGroovyFiles.join(",")
                    env.userWhoTriggeredJob = getBuildUser()
                }
            }
        }

        stage ('Validate required parameters set') {
            when {
                anyOf {
                    expression {
                        userWhoTriggeredJob == null
                    }
                    expression {
                        changedGroovyFilesAsString == null
                    }
                    expression {
                        underscored_review_jira == null
                    }
                    expression {
                        refspec_link == null
                    }
                }
            }
            steps {
                error('Unable to get some required parameter. Please investigate')
            }
        }

        stage('Checkout repo using gerrit refspec') {
            steps {
                checkout changelog: false,
                    poll: false,
                    scm: [$class: 'GitSCM', branches: [[name: 'master']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'CleanBeforeCheckout'], [$class: 'BuildChooserSetting', buildChooser: [$class: 'GerritTriggerBuildChooser']]],
                    gitTool: 'Default',
                    submoduleCfg: [],
                    userRemoteConfigs: [[name: 'gcn', refspec: refspec_link, url: 'ssh://gerrit-gamma.gic.ericsson.se:29418/' + env.project_url]]]
            }
        }

        stage('Modify DSL files to include users signum and jira') {
            steps {
                script {
                    sh '''
                        for changedGroovyFileAsString in $(echo "${changedGroovyFilesAsString}" | sed "s/,/ /g"); do
                            job_or_pipeline_name=$(cat "${changedGroovyFileAsString}" | grep "pipelineBeingGeneratedName =" || cat "${changedGroovyFileAsString}" | grep "jobBeingGeneratedName =")
                            job_or_pipeline_generated=$(echo "${job_or_pipeline_name}" | xargs | cut -d " " -f4)
                            cat "${changedGroovyFileAsString}" | sed "s/${job_or_pipeline_generated}/${userWhoTriggeredJob}_${underscored_review_jira}_${job_or_pipeline_generated}/g" > tmp_store_dsl
                            rm -f "${changedGroovyFileAsString}"
                            mv tmp_store_dsl "${changedGroovyFileAsString}"
                        done
                    '''
                }
            }
        }

        stage('Generate DSL for changed DSL files') {
            steps {
                script {
                    changed_dsl_files_as_list = env.changedGroovyFilesAsString.split(',')
                    print(changed_dsl_files_as_list.toString())
                }

                jobDsl targets: changed_dsl_files_as_list.join('\n'),
                    additionalParameters: defaultParametersForDsl(),
                    additionalClasspath: class_path
            }
        }

        stage('Update \"TB - DSL Generated Jobs\" View with generated jobs') {
            steps {
                jobDsl scriptText: '''listView(\'TB - DSL Generated Jobs\') {
                    filterBuildQueue()
                    filterExecutors()
                    jobs {
                        regex(/.*IDUN.*/)
                    }
                    columns {
                        status()
                        weather()
                        name()
                        lastSuccess()
                        lastFailure()
                        lastDuration()
                        buildButton()
                    }
                }'''
            }
        }
    }
}

def defaultParametersForDsl() {
    return [REGULAR_SLAVES: 'REGULAR_SLAVES', TE_DOCKER_SLAVES: 'TE_DOCKER_SLAVES',
    PIPELINE_SLAVE: 'PIPELINE_SLAVE', OPENSTACK_SLAVES: 'OPENSTACK_SLAVES', DSL_SLAVES: 'DSL_SLAVES', PERMISSION_GROUPS: 'PERMISSION_GROUPS',
    cluster_id: 'cluster_id', test_phase: 'test_phase', TEAM_EMAIL: 'TEAM_EMAIL',
    mt_utils_version: 'mt_utils_version', drop: 'drop', product_set_version: 'product_set_version',
    deployment_description_xml: 'deployment_description_xml', sed_version: 'sed_version',
    pre_install_workload_cleanup: 'pre_install_workload_cleanup', apt_version: 'apt_version',
    nss_utils_version: 'nss_utils_version', crontab: 'crontab', latest_simdep_release: 'latest_simdep_release']
}

@NonCPS
def getBuildUser() {
    return currentBuild.rawBuild.getCause(Cause.UserIdCause).getUserId()
}
