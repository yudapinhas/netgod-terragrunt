pipelineJob('netgod-terraform-release') {

    description('CD release pipeline for netgod-terraform')

    logRotator { numToKeep(30) }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('git@github.com:yudapinhas/netgod-terraform.git')
                        credentials('github-ssh-key')
                    }
                    branches('*/master')
                }
                scriptPath('buildScripts/jenkins/pipeline/release.groovy')
            }
        }
    }

    triggers {
        scm('@daily')
    }
}
