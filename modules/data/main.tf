resource "google_storage_bucket" "data_bucket" {
  name          = var.bucket_name
  location      = var.location
  force_destroy = var.force_destroy
}