include {
  path = find_in_parent_folders()
}

terraform {
  source = "../../../../modules/data"
}

remote_state {
  backend = "remote"
  config = {
    organization = local.organization
    workspaces = {
      prefix = "netgod-data-"
    }
  }
}

inputs = {
  project_id    = "netgod-play"
  bucket_name   = "netgod-data-play-v2"
  force_destroy = true
}
