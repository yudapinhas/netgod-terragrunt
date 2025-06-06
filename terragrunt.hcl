locals {
  organization = "yudapinhas"
  region       = "us-east4"
}

generate "provider" {
  path      = "provider.tf"
  if_exists = "overwrite_terragrunt"
  contents  = <<EOF
provider "google" {
  project     = var.project_id
  region      = "${local.region}"
  credentials = file("gcp/credentials.json")
}
EOF
}