sectionedView('OSS Idun Release CICD') {
    description("""<div style="padding:1em;border-radius:1em;text-align:center;background:#fbf6e1;box-shadow:0 0.1em 0.3em #525000">
        <b>OSS Idun Release CICD</b><br>
       CICD Pipelines and Source Control Jobs<br><br>
        Team: <b>Thunderbee &#x26A1</b><br>
    </div>""")
    sections {
        listView() {
            name('OSS Idun Release CICD Pipelines')
            jobs {
                name('oss-idun-release-cicd_Pre_Code_Review')
                name('oss-idun-release-cicd_Build_And_Publish')
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
            name('OSS Idun Release CICD Thunderbee Pipeline Testing Jobs')
            jobs {
                name('SUCCESS_OR_FAILURE (DO NOT DELETE - FOR TESTING PURPOSES)')
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
