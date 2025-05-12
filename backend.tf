terraform {
  required_version = ">= 1.0.0"
  backend "gcs" {
    bucket      = "my-terraform-state-bucket"
    prefix      = "netgod-terraform"
    credentials = "../../credentials.json"
  }
}