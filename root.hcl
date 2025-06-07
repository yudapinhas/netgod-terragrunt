inputs = {
  organization = "yudapinhas"
  regions      = ["us-east4"]

  gcp_credentials = ""
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
