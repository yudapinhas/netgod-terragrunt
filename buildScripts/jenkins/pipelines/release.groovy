@Library('netgod-jenkins-shared-lib@master') _

pipeline {
    agent any

    parameters {
        string(name: 'GIT_COMMIT', defaultValue: '', description: 'Specific commit SHA to build (optional)')
        choice(name: 'TG_ENV', choices: ['dev', 'prod'], description: 'Terragrunt environment to apply')
    }

    environment {
        CICD = '1'
        TOOL_DIR = '/var/jenkins_home/tools/bin'
        PATH = "${TOOL_DIR}:${env.PATH}"
        REPO_URL = 'git@github.com:yudapinhas/netgod-terragrunt.git'
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
                        script {
                            def commit = params.GIT_COMMIT?.trim()
                            checkout([
                                $class: 'GitSCM',
                                branches: [[name: commit ?: '*/master']],
                                userRemoteConfigs: [[
                                    url: env.REPO_URL,
                                    credentialsId: 'github-ssh-key'
                                ]]
                            ])
                        }
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

        stage('Terragrunt Plan') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        withCredentials([file(credentialsId: 'gcp-sa-json', variable: 'GCP_KEY')]) {
                            sh '''
                                mkdir -p gcp && cp "$GCP_KEY" gcp/credentials.json
                                cd environments/${TG_ENV}
                                terragrunt run-all init
                                terragrunt run-all plan
                            '''
                        }
                    }
                }
            }
        }

        stage('Terragrunt Apply') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        withCredentials([file(credentialsId: 'gcp-sa-json', variable: 'GCP_KEY')]) {
                            sh '''
                                mkdir -p gcp && cp "$GCP_KEY" gcp/credentials.json
                                cd environments/${TG_ENV}
                                terragrunt run-all apply -auto-approve
                            '''
                        }
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
