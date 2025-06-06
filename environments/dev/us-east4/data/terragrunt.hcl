include "root" {
  path = find_in_parent_folders("root.hcl")
  expose = true
}

terraform {
  source = "../../../../modules/data"
}

remote_state {
  backend = "remote"
  config = {
    hostname = "app.terraform.io"
    organization = include.root.inputs.organization
    workspaces = {
      prefix = "terragrunt-"
    }
  }
}

inputs = {
  project_id = "netgod-play"
  bucket_name  = "netgod-dev-us-east4-data"
  force_destroy = true
  organization = include.root.inputs.organization
  region = "us-east4"
}