pipelineJob('netgod-terraform-release') {
    definition {
        cpsScm {
            scm {
                git {
                    remote {
                        url('https://github.com/yudapinhas/netgod-terraform.git')
                    }
                    branches('master')
                }
            }
            scriptPath('buildScripts/jenkins/pipeline/release.groovy')
        }
    }
    triggers {
        scm('H/5 * * * *') // every 5 minutes or adjust to your preferred schedule
    }
}
