include "root" {
  path   = find_in_parent_folders("root.hcl")
  expose = true
}

terraform {
  source = "../../../../modules/data"
}

locals {
  module_specific_inputs = {
    region = "us-east4"
    force_destroy = true
    bucket_name = "netgod-dev-us-east4-data"
  }
}

inputs = merge(include.root.inputs, local.module_specific_inputs)
