### root.hcl
inputs = {
  organization    = "yudapinhas"
  tfc_hostname     = "app.terraform.io"
  project_id      = "netgod-play"
  force_destroy   = true
  gcp_credentials = get_env("GCP_JSON", "")
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