# Resource Group
resource "azurerm_resource_group" "grp" {
  name     = "${var.project_name}-resources"
  location = var.resource_group_location
}
