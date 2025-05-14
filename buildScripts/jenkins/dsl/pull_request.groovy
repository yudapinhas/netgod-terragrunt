pipelineJob('netgod-terraform-pull-request') {
    displayName('netgod-terraform-pull-request')
    description("Pull Request CI for netgod-terraform")
    properties {
        githubProjectUrl('https://github.com/yudapinhas/netgod-terraform')
    }
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('git@github.com:yudapinhas/netgod-terraform.git')
                        credentials('github-ssh-key')
                        refspec('+refs/pull/*:refs/remotes/origin/pr/*')
                    }
                    branches('${sha1}')
                }
                scriptPath('buildScripts/jenkins/pipeline/pull_request.groovy')
            }
        }
    }
    triggers {
        githubPullRequest {
            useGitHubHooks()
            permitAll()
            extensions {
                commitStatus {
                    context("CI - netgod-terraform PR")
                    triggeredStatus("Triggered by PR or comment")
                    startedStatus("Running CI for PR...")
                    completedStatus("SUCCESS", "Build succeeded")
                    completedStatus("FAILURE", "Build failed")
                }
            }
        }
    }
}
