output "bucket_name" {
  value       = google_storage_bucket.data_bucket.name
  description = "The name of the created GCS bucket"
}