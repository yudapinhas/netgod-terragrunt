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
  type = string
}

variable "organization" {
  type = string
}