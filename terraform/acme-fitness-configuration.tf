variable "project_name" {
  type = string
  default = "acme-fitness"
  description = "Project Name"
}

variable "dbadmin" {
  type = string
}

# Configure the Microsoft Azure Provider
provider "azurerm"{
    features {}
}

resource "azurerm_resource_group" "grp" {
  name     = "${var.project_name}-resources"
  location = "East US"
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
  version                = "12"
  administrator_login    = var.dbadmin
  administrator_password = random_password.password.result
  sku_name               = "GP_Standard_D4s_v3"
  storage_mb             = 32768
  zone                   = "1"
}

# Acme Fitness Catalog Postgresql DB
resource "azurerm_postgresql_flexible_server_database" "catalog_db" {
  name      = "acmefit_catalog"
  server_id = azurerm_postgresql_flexible_server.postgresql_server.id
  collation = "en_US.utf8"
  charset   = "utf8"
}

# Acme Fitness Order Postgresql DB
resource "azurerm_postgresql_flexible_server_database" "order_db" {
  name      = "acmefit_order"
  server_id = azurerm_postgresql_flexible_server.postgresql_server.id
  collation = "en_US.utf8"
  charset   = "utf8"
}

output "dbadmin_password" {
  value = azurerm_postgresql_flexible_server.postgresql_server.administrator_password
  sensitive = true
}

