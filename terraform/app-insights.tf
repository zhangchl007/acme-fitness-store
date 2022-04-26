# Log Analiytics Workspace for App Insights
resource "azurerm_log_analytics_workspace" "asc_workspace" {
  name                = "${var.project_name}-workspace"
  location            = azurerm_resource_group.grp.location
  resource_group_name = azurerm_resource_group.grp.name
  sku                 = "PerGB2018"
  retention_in_days   = 30
}

# Application Insights for ASC Service
resource "azurerm_application_insights" "asc_app_insights" {
  name                = "${var.project_name}-appinsights"
  location            = azurerm_resource_group.grp.location
  resource_group_name = azurerm_resource_group.grp.name
  workspace_id        = azurerm_log_analytics_workspace.asc_workspace.id
  application_type    = "web"
}
