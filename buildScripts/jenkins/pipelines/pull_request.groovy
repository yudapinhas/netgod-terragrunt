@Library('netgod-jenkins-shared-lib@master') _

pipeline {
    agent any

    environment {
        CICD = '1'
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

        stage('Run Terraform Plan') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        runTerraform('plan')
                    }
                }
            }
        }

        stage('CI Passed - Notify Yuda') {
            steps {
                script {
                    def subject = "✅ SUCCESS: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
                    def body = "Build passed successfully! Check console output: <a href=\"${env.BUILD_URL}\">${env.BUILD_URL}</a>"
                    notifyYuda(subject: subject, body: body)
                }
            }
        }
    }

    post {
        failure {
            script {
                def subject = "❌ FAILED: ${env.JOB_NAME} [${env.BUILD_NUMBER}]"
                def body = "Build failed! Check console output: <a href=\"${env.BUILD_URL}\">${env.BUILD_URL}</a>"
                notifyYuda(subject: subject, body: body)
            }
        }
        cleanup {
            cleanWs()
        }
    }
}
