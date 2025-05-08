pipelineJob('netgod-terraform-pull-request') {
    def repo = 'https://github.com/yudapinhas/netgod-terraform'
    def sshRepo = 'git@github.com:yudapinhas/netgod-terraform.git'

    logRotator {
        numToKeep(30)
    }

    description("Pull Request CI for netgod-terraform")
    properties {
        githubProjectUrl(repo)
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url(sshRepo)
                        credentials('github-ssh-key')
                        refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                    }
                    branches('*/master')
                }
                scriptPath('buildScripts/jenkins/pipeline/pull_request.groovy')
            }
        }
    }

    triggers {
        githubPullRequest {
            useGitHubHooks()
            extensions {
                commitStatus {
                    context("CI - netgod-terraform pull request")
                }
            }
        }
    }
}
