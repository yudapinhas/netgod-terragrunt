pipelineJob('netgod-terraform-release') {
    def repo = 'https://github.com/yudapinhas/netgod-terraform.git'

    logRotator {
        numToKeep(10)
    }

    description("Release CD for netgod-terraform")
    properties {
        githubProjectUrl(repo)
    }

    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url(repo)
                    }
                    branches('main')
                }
            }
            scriptPath('buildScripts/jenkins/pipeline/release.groovy')
        }
    }
}