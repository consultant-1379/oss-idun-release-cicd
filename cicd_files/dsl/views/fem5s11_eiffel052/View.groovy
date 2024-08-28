sectionedView('IDUN Thunderbee') {
    description("""<div style="padding:1em;border-radius:1em;text-align:center;background:#fbf6e1;box-shadow:0 0.1em 0.3em #525000">
        <b>The jobs are being migrated to <a href="https://fem7s11-eiffel216.eiffel.gic.ericsson.se:8443/jenkins/">fem7s11-eiffel216</a>.</b><br>
        <b><a href="https://jira-oss.seli.wh.rnd.internal.ericsson.com/browse/IDUN-50197">IDUN-50197: Create dedicated FEM for Product Staging and migrate all relevant pipelines</a>.</b><br>
       CICD Pipelines and Source Control Jobs<br><br>
        Team: <b>Thunderbee &#x26A1</b><br>
    </div>""")
    sections {
        listView() {
            name('OSS Idun Release CICD Jobs')
            jobs {
                name('oss-idun-release-cicd_Files_Transfer_Between_Agents')
                name('oss-idun-release-cicd_Get_Difference_Between_App_Versions')
                name('oss-idun-release-cicd_Set_Path_To_Site_Values')
                name('oss-idun-release-cicd_Create_Branch')
                name('oss-idun-release-cicd_Jenkins_Agent_Cleanup')
                name('oss-idun-release-cicd_Clean_Images_In_Docker_Registry')
            }
            columns setViewColumns()
        }
        listView() {
            name('OSS Idun Release CICD MANA Jobs')
            jobs {
                name('oss-idun-release-cicd_MANA_Deploy')
                name('oss-idun-release-cicd_Unpack_And_Push_Images')
                name('oss-idun-release-cicd_MANA_Deploy_Internal_Testing')
                name('oss-idun-release-cicd_Unpack_And_Push_Images_Internal_Testing')
                name('oss-idun-release-cicd_MANA_Health_Check')
                name('oss-idun-release-cicd_MANA_Health_Check_Internal_Testing')
                name('oss-idun-release-cicd_MANA_Clean_Images_In_Docker_Registry')
                name('oss-idun-release-cicd_MANA_Internal_Testing_Pre_Operations')
                name('oss-idun-release-cicd_MANA_Internal_Testing_Files_Transfer')
            }
            columns setViewColumns()
        }
        listView() {
            name('OSS Idun Release CICD Pipeline Source Control')
            jobs {
                name('oss-idun-release-cicd_Pipeline_Generator')
                name('oss-idun-release-cicd_Pipeline_Updater')
            }
            columns setViewColumns()
        }
        listView() {
            name('OSS Idun Release CICD Other Jobs')
            jobs {
                name('idun-integration-version-step')
                name('idun-integration-site-values-update')
            }
            columns setViewColumns()
        }
        listView() {
            name('OSS Idun Release CICD Auto Apps Jobs')
            jobs {
                name('EIAP-AUTO-APP-CSAR-Builder')
            }
            columns setViewColumns()
        }
    }
}

static Object setViewColumns() {
    return {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}
