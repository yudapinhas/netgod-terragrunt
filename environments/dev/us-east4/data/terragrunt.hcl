### terragrunt.hcl
include "root" {
  path   = find_in_parent_folders("root.hcl")
  expose = true
}

locals {
  meta_inputs = {
    organization = include.root.inputs.organization
    tfc_hostname = include.root.inputs.tfc_hostname
  }

  specific_inputs = {
    bucket_name = "netgod-dev-us-east4-data"
    region      = "us-east4"
  }

  module_inputs = merge(
    {
      project_id      = include.root.inputs.project_id
      force_destroy   = include.root.inputs.force_destroy
      gcp_credentials = include.root.inputs.gcp_credentials
    },
    local.specific_inputs
  )
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
    hostname     = "${local.meta_inputs.tfc_hostname}"
    organization = "${local.meta_inputs.organization}"
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
  contents  = join("\n", concat(
    [
      "meta = ${jsonencode(local.meta_inputs)}",
    ],
    [
      for k, v in local.module_inputs : "${k} = ${jsonencode(v)}"
    ]
  ))
}

inputs = merge({meta = local.meta_inputs},local.module_inputs)
