#!/bin/bash

set -euo pipefail

readonly CART_SERVICE="acme-cart-service"

az spring-cloud app delete --name $CART_SERVICE
az spring-cloud gateway clear || true
az spring-cloud api-portal clear || true
az spring-cloud gateway update --assign-endpoint false || true
az redis delete --name acme-cart-redis -g paly-acme-fitness --yes
az spring-cloud build-service builder delete --name nodejs-go-python-builder -y || true
