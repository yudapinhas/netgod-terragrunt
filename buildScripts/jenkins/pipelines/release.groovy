@Library('netgod-jenkins-shared-lib@master') _

pipeline {
    agent any

    parameters {
        string(name: 'GIT_COMMIT', defaultValue: '', description: 'Specific commit SHA to build (optional)')
    }

    environment {
        CICD = '1'
        TOOL_DIR = '/var/jenkins_home/tools/bin'
        PATH = "${TOOL_DIR}:${env.PATH}"
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
                                    url: 'git@github.com:yudapinhas/netgod-terraform.git',
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

        stage('Run Terraform Plan') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        sh 'terraform version'
                        runTerraform('plan')
                    }
                }
            }
        }

        stage('Terraform Apply') {
            steps {
                timestamps {
                    ansiColor('xterm') {
                        sh 'terraform apply -auto-approve'
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
