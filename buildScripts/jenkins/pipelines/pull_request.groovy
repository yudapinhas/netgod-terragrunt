@Library('netgod-jenkins-shared-lib@master') _
pipeline {
    agent any
    environment {
        CICD      = '1'
        TOOL_DIR  = '/var/jenkins_home/tools/bin'
        PATH      = "${TOOL_DIR}:${env.PATH}"
        REPO_NAME = "netgod-terragrunt"
        ORG       = 'yudapinhas'
        REPO_URL  = "git@github.com:${ORG}/${REPO_NAME}.git"
    }
    options {
        ansiColor('xterm')
        skipDefaultCheckout()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    stages {
        stage('Checkout PR branch') {
            steps {
                checkout([$class: 'GitSCM',
                    branches: [[name: "${env.ghprbActualCommit}"]],
                    userRemoteConfigs: [[
                        url: env.REPO_URL,
                        credentialsId: 'github-ssh-key',
                        refspec: "+refs/pull/${env.ghprbPullId}/head:refs/remotes/origin/pr/${env.ghprbPullId}"
                    ]]
                ])
            }
        }
        stage('Determine Terragrunt Targets') {
            steps {
                script {
                    def modules = sh(
                        script: 'find environments -type f -name "terragrunt.hcl" -exec dirname {} \\; | sort -u',
                        returnStdout: true
                    ).trim().split('\n').findAll { it }
                    if (modules.isEmpty()) {
                        echo "No terragrunt.hcl files found in environments/. Proceeding with no targets."
                        env.TG_CHANGED_PATHS = ""
                    } else {
                        env.TG_CHANGED_PATHS = modules.join(',')
                        echo "Detected Terragrunt modules:\n${env.TG_CHANGED_PATHS.replace(',', '\n')}"
                    }
                }
            }
        }
        stage('Terragrunt Plan') {
            when {
                expression { env.TG_CHANGED_PATHS != "" }
            }
            steps {
                withCredentials([
                    file(credentialsId: 'gcp-sa-json', variable: 'GCP_KEY'),
                    string(credentialsId: 'terraform-cloud-token', variable: 'TF_TOKEN_app_terraform_io')
                ]) {
                    script {
                        env.GCP_CREDENTIALS_PATH = "gcp/credentials.json"
                        sh "mkdir -p gcp && cp \$GCP_KEY \$GCP_CREDENTIALS_PATH"
                        
                        def modules = env.TG_CHANGED_PATHS.split(',')
                        modules.each { modulePath ->
                            echo "Running Terragrunt plan in ${modulePath}"
                            dir(modulePath) {
                                sh '''
                                    set -eux
                                    terragrunt init
                                    terragrunt plan
                                '''
                            }
                        }
                    }
                }
            }
        }
    }
    post {
        cleanup {
            cleanWs()
        }
    }
}