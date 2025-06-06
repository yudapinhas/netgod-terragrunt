locals {
  tfc_hostname     = "app.terraform.io"
  tfc_organization = include.root.inputs.organization
  
  # First define path_parts, then use it
  path_parts = split("/", get_terragrunt_dir())
  workspace  = reverse(path_parts)[0] # This will be "data"
  account    = path_parts[length(path_parts) - 3] # "dev"
  region     = path_parts[length(path_parts) - 2] # "us-east4"
}

include "root" {
  path   = find_in_parent_folders("root.hcl")
  expose = true
}

terraform {
  source = "../../../../modules/data"
}

generate "remote_state" {
  path      = "backend.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
terraform {
  backend "remote" {
    hostname     = "${local.tfc_hostname}"
    organization = "${local.tfc_organization}"
    workspaces {
      prefix = "terragrunt-"
    }
  }
}
EOF
}

inputs = {
  project_id    = "netgod-play"
  bucket_name   = "netgod-dev-us-east4-data"
  force_destroy = true
  organization  = include.root.inputs.organization
  region        = "us-east4"
}