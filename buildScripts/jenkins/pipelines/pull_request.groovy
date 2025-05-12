@Library('netgod-jenkins-shared-lib@master') _

pipeline {
    agent any

    environment {
        CICD = '1'
        TOOL_DIR = '/var/jenkins_home/tools/bin'
        PATH = "${TOOL_DIR}:${env.PATH}"
        TF_ENV = 'dev'
        REPO_URL = 'git@github.com:yudapinhas/netgod-terraform.git'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        skipDefaultCheckout()
    }

    stages {
        stage('Checkout') {
            steps {
                script {
                    def repoUrl = env.ghprbGhRepository ? "git@github.com:${env.ghprbGhRepository}.git" : env.REPO_URL
                    def commit = env.ghprbActualCommit ?: '*/master'

                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: commit]],
                        userRemoteConfigs: [[
                            url: repoUrl,
                            credentialsId: 'github-ssh-key',
                            refspec: '+refs/pull/*:refs/remotes/origin/pr/*'
                        ]]
                    ])
                }
            }
        }

        stage('Prepare Environment') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        sh '''
                            set -eux
                            echo "terraform workspace selection here"
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
                            cd environments/${TF_ENV}
                            terraform version
                            terraform plan -var-file="${TF_ENV}.tfvars"
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
