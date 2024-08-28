package common_classes

class CommonParameters {

    static List slave(String defaultValue='GridEngine') {
        return ['SLAVE', defaultValue, 'Slave to run the job against.']
    }

    static String repo() {
        return 'OSS/com.ericsson.oss.cicd/oss-idun-release-cicd'
    }

    static String repoUrl() {
        return '\${GERRIT_MIRROR}/'+repo()
    }
}