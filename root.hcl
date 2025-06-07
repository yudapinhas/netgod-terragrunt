locals {
  tfc_hostname     = "app.terraform.io"
  tfc_organization = "yudapinhas"

  common_inputs = {
    organization   = local.tfc_organization
    project_id     = "netgod-play"
    gcp_credentials = get_env("GCP_JSON", "")
  }
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

generate "provider" {
  path      = "provider.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
provider "google" {
  project     = var.project_id
  region      = var.region
  credentials = var.gcp_credentials
}
EOF
}

inputs = local.common_inputs
