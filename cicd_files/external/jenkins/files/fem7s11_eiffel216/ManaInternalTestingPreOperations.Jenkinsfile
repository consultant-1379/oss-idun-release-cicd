pipeline {
    agent {
        label env.SLAVE_LABEL
    }
    environment {
        package_dir = 'swgw_download_dir'
        csar_list_filename = 'CSAR_LIST'
        artifactory_location = 'https://arm.seli.gic.ericsson.se/artifactory'
    }
    stages {
        stage('Prepare') {
            steps {
                sh "git submodule sync"
                sh "git submodule update --init --recursive --remote"
            }
        }
        stage ('Create CSAR List') {
            steps {
                sh """
                    echo -e "eric-oss-task-automation-ae ${env.ERIC_OSS_TASK_AUTOMATION_AE_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-config-handling ${env.ERIC_OSS_CONFIG_HANDLING_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-app-mgr ${env.ERIC_OSS_APP_MGR_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-ericsson-adaptation ${env.ERIC_OSS_ERICSSON_ADAPTATION_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-topology-handling ${env.ERIC_TOPOLOGY_HANDLING_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-dmm ${env.ERIC_OSS_DMM_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-adc ${env.ERIC_OSS_ADC_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-oran-support ${env.ERIC_OSS_ORAN_SUPPORT_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-oss-common-base ${env.ERIC_OSS_COMMON_BASE_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-cncs-oss-config ${env.ERIC_CNCS_OSS_CONFIG_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-cloud-native-base ${env.ERIC_CLOUD_NATIVE_BASE_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-cnbase-oss-config ${env.ERIC_CNBASE_OSS_CONFIG_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-cloud-native-service-mesh ${env.ERIC_CLOUD_NATIVE_SERVICE_MESH_VERSION}" >> ${env.csar_list_filename}
                    echo -e "eric-eiae-helmfile ${env.EIAE_DEPLOYMENT_HELM_RELEASE_VERSION}" >> ${env.csar_list_filename}
                    echo -e "deployment-manager ${env.OSS_DEPLOYMENT_MANAGER_RELEASE_VERSION}" >> ${env.csar_list_filename}
                """
            }
        }
        stage('Download Packages') {
            steps {
                script {
                    withCredentials([string(credentialsId: env.USER_CREDENTIAL_ID, variable: 'TOKEN')]) {
                        sh """
                            #!/bin/bash

                            while read line
                            do
                                CSAR=`echo \$line | awk '{print \$1}'`
                                VERSION=`echo \$line | awk '{print \$2}'`
                                FILENAME="./\$CSAR-\$VERSION.csar"
                                if [ -f "\$FILENAME" ]
                                then
                                    echo "\$FILENAME already downloaded"
                                else
                                    if [ "\$CSAR" = "eric-eiae-helmfile" ]
                                    then
                                        echo "Removing old tgz version for \$CSAR"
                                        echo "rm -rf ./\$CSAR.*.tgz"
                                        rm -rf ./\$CSAR*.tgz
                                        echo "Downloading EIAE helmfile \$VERSION"
                                        wget ${env.artifactory_location}/proj-eric-oss-drop-helm/eric-eiae-helmfile/eric-eiae-helmfile-\$VERSION.tgz --header 'Authorization: Basic ${env.TOKEN}'
                                    elif [ "\$CSAR" = "deployment-manager" ]
                                    then
                                        echo "Removing old zip version for \$CSAR"
                                        echo "rm -rf ./\$CSAR.*.zip"
                                        rm -rf ./\$CSAR*.zip
                                        echo "Downloading Deployment Manager \$VERSION"
                                        wget ${env.artifactory_location}/proj-eric-oss-drop-generic-local/csars/dm/deployment-manager-\$VERSION.zip --header 'Authorization: Basic ${env.TOKEN}'
                                    else
                                        echo "Removing old CSAR version for \$CSAR"
                                        echo "rm -rf ./\$CSAR.*.csar"
                                        rm -rf ./\$CSAR*.csar
                                        wget ${env.artifactory_location}/proj-eric-oss-drop-generic-local/csars/\$CSAR/\$VERSION/\$CSAR-\$VERSION.csar --header 'Authorization: Basic ${env.TOKEN}'
                                    fi
                                fi
                            done < ${env.csar_list_filename}
                        """
                    }
                }
            }
        }
        stage ('Zip Packages') {
            environment {
                sw_download_path = sh(script: "echo ${params.CSAR_DOWNLOAD_LOCATION_PATH}/${env.package_dir}", returnStdout: true).trim()
            }
            steps {
                sh """
                    mkdir -p ${sw_download_path}
                    rm -rf ${sw_download_path}/*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_TASK_AUTOMATION_AE_PRODUCT_NO} eric-oss-task-automation-ae-*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_CONFIG_HANDLING_PRODUCT_NO} eric-oss-config-handling*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_APP_MGR_PRODUCT_NO} eric-oss-app-mgr*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_ERICSSON_ADAPTATION_PRODUCT_NO} eric-oss-ericsson-adaptation*
                    zip ${env.sw_download_path}/${params.ERIC_TOPOLOGY_HANDLING_PRODUCT_NO} eric-topology-handling*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_DMM_PRODUCT_NO} eric-oss-dmm*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_ADC_PRODUCT_NO} eric-oss-adc*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_ORAN_SUPPORT_PRODUCT_NO} eric-oss-oran-support-*
                    zip ${env.sw_download_path}/${params.ERIC_OSS_COMMON_BASE_PRODUCT_NO} eric-oss-common-base*
                    zip ${env.sw_download_path}/${params.ERIC_CNCS_OSS_CONFIG_PRODUCT_NO} eric-cncs-oss-config*
                    zip ${env.sw_download_path}/${params.ERIC_CLOUD_NATIVE_BASE_PRODUCT_NO} eric-cloud-native-base*
                    zip ${env.sw_download_path}/${params.ERIC_CNBASE_OSS_CONFIG_PRODUCT_NO} eric-cnbase-oss-config*
                    zip ${env.sw_download_path}/${params.ERIC_CLOUD_NATIVE_SERVICE_MESH_PRODUCT_NO} eric-cloud-native-service-mesh*
                    zip ${env.sw_download_path}/${params.EIAE_DEPLOYMENT_HELM_RELEASE_PACKAGE_PRODUCT_NO} eric-eiae-helmfile-*
                    zip ${env.sw_download_path}/${params.OSS_DEPLOYMENT_MANAGER_RELEASE_PACKAGE_PRODUCT_NO} deployment-manager*
                    rm -rf eric-* deployment-manager*
                """
            }
        }
    }
}