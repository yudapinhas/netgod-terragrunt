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
                    def changedFiles = sh(
                        script: 'git diff --name-only origin/master...HEAD',
                        returnStdout: true
                    ).trim().split('\n')
                    def changedEnvs = changedFiles.findAll { it.startsWith('environments/') }
                        .collect { it.split('/')[1] }
                        .unique()
                    if (changedEnvs.isEmpty()) {
                        echo "No changes detected in any environment. Skipping Terragrunt Plan."
                        env.HAS_CHANGES = 'false'
                        env.TG_MODULE_DIRS = ''
                    } else {
                        def allModuleDirs = []
                        changedEnvs.each { env ->
                            def moduleDirs = sh(
                                script: "find environments/${env} -type f -name terragrunt.hcl -exec dirname {} \\;",
                                returnStdout: true
                            ).trim().split('\n')
                            allModuleDirs.addAll(moduleDirs)
                        }
                        env.HAS_CHANGES = 'true'
                        env.TG_MODULE_DIRS = allModuleDirs.join(',')
                        echo "Detected Terragrunt modules to plan:\n${env.TG_MODULE_DIRS}"
                    }
                }
            }
        }

        stage('Terragrunt Plan') {
            when {
                expression { return env.HAS_CHANGES == 'true' }
            }
            steps {
                withCredentials([file(credentialsId: 'gcp-sa-json', variable: 'GCP_KEY')]) {
                    script {
                        env.GCP_CREDENTIALS_PATH = "gcp/credentials.json"
                        sh "mkdir -p gcp && cp $GCP_KEY $GCP_CREDENTIALS_PATH"

                        def moduleDirs = env.TG_MODULE_DIRS.split(',')
                        moduleDirs.each { moduleDir ->
                            echo "Running Terragrunt plan in ${moduleDir}"
                            dir(moduleDir) {
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
