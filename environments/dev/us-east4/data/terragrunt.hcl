locals {
  tfc_hostname     = "app.terraform.io"
  tfc_organization = include.root.inputs.organization

  common_inputs = {
    project_id    = "netgod-play"
    bucket_name   = "netgod-dev-us-east4-data"
    force_destroy = true
    organization  = local.tfc_organization
    region        = "us-east4"
    gcp_credentials = getenv("GCP_JSON", "")
  }
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

generate "auto_tfvars" {
  path      = "terragrunt.auto.tfvars"
  if_exists = "overwrite_terragrunt"
  contents  = join("\n", [
    for k, v in local.common_inputs : "${k} = ${jsonencode(v)}"
  ])
}

inputs = local.common_inputs
