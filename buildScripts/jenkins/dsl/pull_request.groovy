pipelineJob('netgod-terraform-pull-request') {
    description("Pull Request CI for netgod-terraform")
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('git@github.com:yudapinhas/netgod-terraform.git')
                        credentials('github-ssh-key')
                        refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                    }
                    branches('*/master')
                }
                scriptPath('buildScripts/jenkins/pipeline/pull_request.groovy')
            }
        }
    }
}
