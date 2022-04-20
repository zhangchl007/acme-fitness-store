variable "project_name" {
  type = string
  default = "acme-fitness"
  description = "Project Name"
}

variable "resource_group_location" {
  type = string
  default = "West Europe"
  description = "Azure Resource Group"
}

variable "asc_cart_service" {
  type = string
  default = "cart-service"
  description = "Cart Service App Name"
}

variable "asc_identity_service" {
  type = string
  default = "identity-service"
  description = "Identity Service App Name"
}

variable "asc_order_service" {
  type = string
  default = "order-service"
  description = "Order Service App Name"
}

variable "asc_payment_service" {
  type = string
  default = "payment-service"
  description = "Payment Service App Name"
}

variable "asc_catalog_service" {
  type = string
  default = "catalog-service"
  description = "Catalog Service App Name"
}

variable "asc_frontend" {
  type = string
  default = "frontend"
  description = "Frontend App Name"
}

variable "dbadmin" {
  type = string
  default = "posgredbadmin"
  description = "Admin User for PosgreSql Server"
}

variable "postgres_db_name" {
  type = list
  default = ["acmefit_catalog","acmefit_order"]
}

# Configure the Microsoft Azure Provider
provider "azurerm"{
    features {}
}

resource "azurerm_resource_group" "grp" {
  name     = "${var.project_name}-resources"
  location = var.resource_group_location
}

# Azure Cache for Redis Instance
resource "azurerm_redis_cache" "redis" {
  name                = "${var.project_name}-redis"
  location            = azurerm_resource_group.grp.location
  resource_group_name = azurerm_resource_group.grp.name
  capacity            = 1
  family              = "C"
  sku_name            = "Basic"
}

# Generate Passworod for Postgresql Server
resource "random_password" "password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Postgresql Flexible Server
resource "azurerm_postgresql_flexible_server" "postgresql_server" {
  name                   = "acmefit-db-server"
  resource_group_name    = azurerm_resource_group.grp.name
  location               = azurerm_resource_group.grp.location
  version                = "13"
  administrator_login    = var.dbadmin
  administrator_password = random_password.password.result
  sku_name               = "GP_Standard_D4s_v3"
  storage_mb             = 32768
  zone                   = "1"
}

# Allow connections from other Azure Services
resource "azurerm_postgresql_flexible_server_firewall_rule" "postgresql_server_fw" {
  name             = "acmefit-db-server-fw"
  server_id        = azurerm_postgresql_flexible_server.postgresql_server.id
  start_ip_address = "0.0.0.0"
  end_ip_address   = "0.0.0.0"
}

# Enable the uuid-ossp extension
resource "azurerm_postgresql_flexible_server_configuration" "postgresql_server_config" {
  name      = "azure.extensions"
  server_id = azurerm_postgresql_flexible_server.postgresql_server.id
  value     = "uuid-ossp"
}

# Acme Fitness Catalog & Order Postgresql DB
resource "azurerm_postgresql_flexible_server_database" "postgre_db" {
  name      = var.postgres_db_name[count.index]
  server_id = azurerm_postgresql_flexible_server.postgresql_server.id
  collation = "en_US.utf8"
  charset   = "utf8"
  count     = length(var.postgres_db_name)
}

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

# Azure Spring Cloud Service (ASC Service)
resource "azurerm_spring_cloud_service" "asc_service" {
  name                = "${var.project_name}-asc"
  resource_group_name = azurerm_resource_group.grp.name
  location            = azurerm_resource_group.grp.location
  sku_name            = "E0"

  trace {
    connection_string = azurerm_application_insights.asc_app_insights.connection_string
  }
}

# Configure Diagnostic Settings for the ASC Service
resource "azurerm_monitor_diagnostic_setting" "asc_diagnostic" {
  name                       = "${var.project_name}-diagnostic"
  target_resource_id         = azurerm_spring_cloud_service.asc_service.id
  log_analytics_workspace_id = azurerm_log_analytics_workspace.asc_workspace.id
  log {
    category = "ApplicationConsole"
    enabled  = true
    retention_policy {
      enabled = false
      days = 0
    }
  }
  log {
    category = "SystemLogs"
    enabled  = true
    retention_policy {
      enabled = false
      days    = 0
    }
  }
  log {
    category = "IngressLogs"
    enabled  = true
    retention_policy {
      enabled = false
      days    = 0
    }
  }
  metric {
    category = "AllMetrics"
    enabled  = true
    retention_policy {
      enabled = false
      days = 0
    }
  }
}

# Create ASC Apps Cart Service
resource "azurerm_spring_cloud_app" "asc_app_cart_service" {
  name                = var.asc_cart_service
  resource_group_name = azurerm_resource_group.grp.name
  service_name        = azurerm_spring_cloud_service.asc_service.name
}

# Create ASC Apps Identity Service
resource "azurerm_spring_cloud_app" "asc_app_indentity_service" {
  name                = var.asc_identity_service
  resource_group_name = azurerm_resource_group.grp.name
  service_name        = azurerm_spring_cloud_service.asc_service.name
}

# Create ASC Apps Order Service
resource "azurerm_spring_cloud_app" "asc_app_order_service" {
  name                = var.asc_order_service
  resource_group_name = azurerm_resource_group.grp.name
  service_name        = azurerm_spring_cloud_service.asc_service.name
}

# Create ASC Apps Catalog Service
resource "azurerm_spring_cloud_app" "asc_app_catalog_service" {
  name                = var.asc_catalog_service
  resource_group_name = azurerm_resource_group.grp.name
  service_name        = azurerm_spring_cloud_service.asc_service.name
}

# Create ASC Apps Payment Service
resource "azurerm_spring_cloud_app" "asc_app_payment_service" {
  name                = var.asc_payment_service
  resource_group_name = azurerm_resource_group.grp.name
  service_name        = azurerm_spring_cloud_service.asc_service.name
}

# Create ASC Apps Frontend
resource "azurerm_spring_cloud_app" "asc_app_frontend" {
  name                = var.asc_frontend
  resource_group_name = azurerm_resource_group.grp.name
  service_name        = azurerm_spring_cloud_service.asc_service.name
}
