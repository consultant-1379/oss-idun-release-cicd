package common_classes

class ExternalParameters {

    static List success(String defaultValue='true') {
        return ['SUCCESS', defaultValue, 'Set it to "true" for successful job. Set it to "false" for unsuccessful job.']
    }

}
