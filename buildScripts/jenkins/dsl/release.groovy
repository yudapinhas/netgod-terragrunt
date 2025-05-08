pipelineJob('netgod-terraform-release') {
    description("Release CD for netgod-terraform")
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
}
