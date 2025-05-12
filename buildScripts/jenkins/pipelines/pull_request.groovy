@Library('netgod-jenkins-shared-lib@master') _

pipeline {
    agent any

    environment {
        CICD = '1'
        TOOL_DIR = '/var/jenkins_home/tools/bin'
        PATH = "${TOOL_DIR}:${env.PATH}"
        TF_ENV = 'dev'

    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout()
    }

    stages {
        stage('Checkout') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        cleanWs()
                        checkout scm
                    }
                }
            }
        }

        stage('Prepare Pretools') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        sh '''
                            set -eux
                            for script in /var/jenkins_home/netgod-pretools/*.sh; do
                                echo "Running $script"
                                bash "$script"
                            done
                        '''
                    }
                }
            }
        }

        stage('Run Terraform Plan') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        sh '''
                        terraform version
                        terraform plan -var-file="environments/${TF_ENV}/${TF_ENV}.tfvars"
                        '''
                    }
                }
            }
        }

        stage('CI Passed - Notify Yuda') {
            steps {
                script {
                    def emailInfo = notifyYuda('SUCCESS')
                    emailext(
                        to: emailInfo.to,
                        subject: emailInfo.subject,
                        body: emailInfo.body,
                        mimeType: emailInfo.mimeType
                    )
                }
            }
        }
    }

    post {
        failure {
            script {
                def emailInfo = notifyYuda('FAILURE')
                emailext(
                    to: emailInfo.to,
                    subject: emailInfo.subject,
                    body: emailInfo.body,
                    mimeType: emailInfo.mimeType
                )
            }
        }
        cleanup {
            cleanWs()
        }
    }
}
