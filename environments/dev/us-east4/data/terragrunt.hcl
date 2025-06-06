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
    hostname     = "app.terraform.io"
    organization = include.root.inputs.organization
    workspaces = {
      name = "netgod-data-${path_relative_to_include()}"
    }
  }
}

inputs = {
  project_id    = "netgod-play"
  bucket_name   = "netgod-data-play-v2-${path_relative_to_include()}"
  force_destroy = true
  organization  = include.root.inputs.organization
  region        = "us-east4"
}