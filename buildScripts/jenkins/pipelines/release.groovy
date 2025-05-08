@Library('netgod-jenkins-shared-lib@master') _
pipeline {
    agent { label 'netgod-play-pod' }

    options {
        ansiColor('xterm')
        timestamps()
    }

    parameters {
        string(name: 'GIT_COMMIT', defaultValue: '', description: 'Specific commit SHA to build (optional)')
    }

    environment {
        PATH = "$PWD:$WORKSPACE:$HOME/.pulumi/bin:$PATH"
        PULUMI_ACCESS_TOKEN = credentials('pulumi_access_token')
        AWS_ACCESS_KEY_ID = credentials('aws-master-key')
        AWS_SECRET_ACCESS_KEY = credentials('aws-master-secret')
        CICD = 1
    }

    stages {
        stage('Checkout') {
            steps {
                cleanWs()
                script {
                    def commit = params.GIT_COMMIT?.trim()
                    checkout([
                        $class: 'GitSCM',
                        branches: [[name: commit ?: '*/master']],
                        userRemoteConfigs: [[
                            url: 'https://github.com/yudapinhas/netgod-terraform.git'
                        ]]
                    ])
                }
            }
        }

        stage('Run Terraform Plan') {
            steps {
                runTerraform('plan')
            }
        }

        stage('Terraform Apply') {
            steps {
                sh 'terraform apply -auto-approve'
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