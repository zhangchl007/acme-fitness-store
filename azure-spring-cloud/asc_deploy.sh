#!/bin/bash

set -euo pipefail

readonly PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")"/.. && pwd)"
readonly REPO_ROOT="${PROJECT_ROOT}/repos"

readonly REDIS_NAME="acme-fitness-redis"
readonly COSMOS_ACCOUNT="acme-fitness-cosmosdb"
readonly MONGO_CONNECTION="user_service_mongodb"
readonly USER_DB_NAME="users"
readonly CART_SERVICE="cart-service"
readonly USER_SERVICE="user-service"
readonly CUSTOM_BUILDER="nodejs-go-python-builder"

RESOURCE_GROUP=''
SPRING_CLOUD_INSTANCE=''

function configure_defaults() {
  echo "Configure azure defaults resource group: $RESOURCE_GROUP and spring-cloud $SPRING_CLOUD_INSTANCE"
  az configure --defaults group=$RESOURCE_GROUP spring-cloud=$SPRING_CLOUD_INSTANCE
}

function create_dependencies() {
  echo "Creating Azure Cache for Redis Instance $REDIS_NAME in location eastus"
  az redis create --location eastus --name $REDIS_NAME --resource-group $RESOURCE_GROUP --sku Basic --vm-size c0

  echo "Creating CosmosDB Account $COSMOS_ACCOUNT"
  az cosmosdb create --name $COSMOS_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --kind MongoDB --server-version "4.0" \
    --default-consistency-level Eventual \
    --enable-automatic-failover true \
    --locations regionName="East US" failoverPriority=0 isZoneRedundant=False \
    --locations regionName="Central US" failoverPriority=1 isZoneRedundant=False
  az cosmosdb mongodb database create --account-name $COSMOS_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --name $USER_DB_NAME
}

function create_builder() {
  echo "Creating a custom builder with name $CUSTOM_BUILDER and configuration $PROJECT_ROOT/azure-spring-cloud/builder.json"
  az spring-cloud build-service builder create -n $CUSTOM_BUILDER --builder-file "$PROJECT_ROOT/azure-spring-cloud/builder.json"
}

function configure_gateway() {
  az spring-cloud gateway update --assign-endpoint true
  local gateway_url=$(az spring-cloud gateway show | jq -r '.properties.url')

  echo "Configuring Spring Cloud Gateway without SSO enabled"
  az spring-cloud gateway update \
    --api-description "ACME Fitness API" \
    --api-title "ACME Fitness" \
    --api-version "v.01" \
    --server-url "https://$gateway_url" \
    --allowed-origins "*"
}

function create_cart_service() {
  echo "Creating cart-service app"
  az spring-cloud app create --name $CART_SERVICE
  az spring-cloud gateway route-config create --name $CART_SERVICE --app-name $CART_SERVICE --routes-file "$PROJECT_ROOT/azure-spring-cloud/routes/cart-service.json"

  echo "cart-service app successfully created. Please create a service connector to redis before continuing." #TODO: link to instructions
  read -n 1 -s -r -p "Press any key to continue."
}

function create_user_service() {
  echo "Creating user service"
  az spring-cloud app create --name $USER_SERVICE
  az spring-cloud gateway route-config create --name $USER_SERVICE --app-name $USER_SERVICE --routes-file "$PROJECT_ROOT/azure-spring-cloud/routes/user-service.json"

  az spring-cloud connection create cosmos-mongo -g $RESOURCE_GROUP \
    --service $SPRING_CLOUD_INSTANCE \
    --app $USER_SERVICE \
    --deployment default \
    --tg $RESOURCE_GROUP \
    --account $COSMOS_ACCOUNT \
    --database $USER_DB_NAME \
    --secret \
    --client-type java \
    --connection $MONGO_CONNECTION
}

function deploy_cart_service() {
  echo "Deploying cart-service application"
  #  local redis_id=$(az redis show -n acme-cart-redis | jq -r '.id')
  #  git clone git@github.com:pivotal-cf/acme-cart.git "$REPO_ROOT/acme-cart" --branch azure-spring-cloud-deployment
  local redis_conn_name=$(az spring-cloud connection list --app $CART_SERVICE -g $RESOURCE_GROUP --service $SPRING_CLOUD_INSTANCE --deployment default | jq -r '.[0].name')
  local redis_conn_str=$(az spring-cloud connection show -g $RESOURCE_GROUP --service $SPRING_CLOUD_INSTANCE --app $CART_SERVICE --deployment default --connection $redis_conn_name | jq '.configurations[0].value' -r)
  az spring-cloud app deploy --name $CART_SERVICE \
    --builder $CUSTOM_BUILDER \
    --env "CART_PORT=8080" "AZURE_REDIS_CONNECTIONSTRING=$redis_conn_str" \
    --source-path "$REPO_ROOT/acme-cart"
}

function deploy_user_service() {
  local mongo_connection_url=$(az spring-cloud connection show -g $RESOURCE_GROUP --service $SPRING_CLOUD_INSTANCE --deployment default --connection $MONGO_CONNECTION --app $USER_SERVICE | jq '.configurations[0].value' -r)

  az spring-cloud app deploy --name $USER_SERVICE \
    --builder $CUSTOM_BUILDER \
    --env "DB_CONNECTION_URL=$mongo_connection_url"
}

function main() {
  configure_defaults
  create_dependencies
  create_builder
  configure_gateway
  create_cart_service

  deploy_cart_service
}

function usage() {
  echo 1>&2
  echo "Usage: $0 -g <resource_group> -s <spring_cloud_instance>" 1>&2
  echo 1>&2
  echo "Options:" 1>&2
  echo "  -g <namespace>              the Azure resource group to use for the deployment" 1>&2
  echo "  -s <spring_cloud_instance>  the name of the Azure Spring Cloud Instance to use" 1>&2
  echo 1>&2
  exit 1
}

function check_args() {
  if [[ -z $RESOURCE_GROUP ]]; then
    echo "Provide a valid resource group with -g"
    usage
  fi

  if [[ -z $SPRING_CLOUD_INSTANCE ]]; then
    echo "Provide a valid spring cloud instance name with -s"
    usage
  fi
}

while getopts ":g:s:" options; do
  case "$options" in
  g)
    RESOURCE_GROUP="$OPTARG"
    ;;
  s)
    SPRING_CLOUD_INSTANCE="$OPTARG"
    ;;
  *)
    usage
    exit 1
    ;;
  esac

  case $OPTARG in
  -*)
    echo "Option $options needs a valid argument"
    exit 1
    ;;
  esac
done

check_args
main
