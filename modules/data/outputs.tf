output "bucket_name" {
  value       = google_storage_bucket.data_bucket.name
  description = "The name of the created GCS bucket"
}

// environments/dev/provider.tf
terraform {
  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 4.0"
    }
  }
  required_version = ">= 1.0.0"
}

provider "google" {
  project     = var.project_id
  region      = var.region
  credentials = file(var.credentials_file)
}