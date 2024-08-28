def bob = "bob/bob -r \${WORKSPACE}/cicd_files/external/jenkins/rulesets/CopyEicSiteValuesToOst.yaml"

pipeline {
    agent {
        label params.SLAVE_LABEL
    }
    stages {
        stage('Download the latest site values') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: params.USER_AUTH,
                        usernameVariable: 'USER_NAME',
                        passwordVariable: 'USER_PASSWORD')
                ]) {
                    sh 'mkdir -p output_files'
                    sh'''
                        docker run \
                            --rm \
                            --user "$(id -u):$(id -g)" \
                            --init \
                            --volume ~/.docker:/root/.docker \
                            --volume /var/run/docker.sock:/var/run/docker.sock \
                            --volume ${WORKSPACE}/output_files:/usr/src/app/out \
                                armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/data-store-client:latest \
                                    download-ost-file-by-name \
                                        --verbose \
                                        --auth_user ${USER_NAME} \
                                        --auth_pass ${USER_PASSWORD} \
                                        --bucket_name "eic_site_values_template" \
                                        --datafile_name "site-values-latest" \
                                        --datafile_type "yaml" \
                                        --generate_artifact_properties
                    '''
                }
            }
        }
        stage('Build Copy EIC site values to OST Docker Iamge') {
            steps {
                sh '''
                    docker build -f ${PWD}/cicd_files/external/scripts/copy_eic_site_values_to_ost/Dockerfile-copy_eic_site_values_to_ost -t copy_eic_site_values_to_ost .
                '''
            }
        }
        stage('Run Copy EIC site values to OST Docker Iamge') {
            steps {
                withCredentials([
                    usernamePassword(
                        credentialsId: params.USER_AUTH,
                        usernameVariable: 'USER_NAME',
                        passwordVariable: 'USER_PASSWORD')
                ]) {
                    sh '''
                        cp ${WORKSPACE}/output_files/site-values-latest.yaml ${WORKSPACE}/site-values-${VERSION}.yaml
                        cat site-values-${VERSION}.yaml
                        docker run --user "$(id -u):$(id -g)" \
                            -v ${PWD}:/usr/src/app \
                            --rm copy_eic_site_values_to_ost \
                            site-values-${VERSION}.yaml ${BUCKET_NAME} ${USER_NAME} ${USER_PASSWORD}
                    '''
                }
            }
        }
    }
    post {
        always {
            sh 'docker rmi copy_eic_site_values_to_ost || true'
            sh 'docker rmi -f armdocker.rnd.ericsson.se/proj-eric-oss-dev-test/data-store-client:latest || true'
            cleanWs()
        }
    }
}