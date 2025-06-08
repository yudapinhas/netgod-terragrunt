variable "bucket_name" {
  type        = string
  description = "Name of the GCS bucket"
}

variable "location" {
  type        = string
  default     = "US"
  description = "Location of the GCS bucket"
}

variable "force_destroy" {
  type        = bool
  default     = false
  description = "Whether to forcibly destroy bucket with objects"
}

variable "region" {
  type        = string
  description = "Region to deploy the resources"
}

variable "project_id" {
  description = "GCP Project ID"
  type        = string
}

variable "gcp_credentials" {
  description = "GCP credentials JSON string"
  type        = string
  sensitive   = true
}

variable "meta" {
  type    = map(any)
  default = {}
}