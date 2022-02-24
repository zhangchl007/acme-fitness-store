# Demo of ACME Fitness Shop

## Getting Started
These instructions will allow you to run the entire ACME fitness shop on Azure Spring Cloud Enterprise tier. 

## Try it out

### Prerequisites

Before starting this demo, provision an instance of Azure Spring Cloud Enterprise tier provisioned. 

Through the Azure Portal, create a new Builder for Tanzu Build Service with the following configuration:
* Name: all-buildpacks-no-bindings
* OS Stack: io.buildpacks.stacks.bionic-full
* Buildpacks: tanzu-buildpacks/java-azure, tanzu-buildpacks/nodejs, tanzu-buildpacks/dotnet-core, tanzu-buildpacks/go, tanzu-buildpacks/python 
* Bindings: None

> Note: This buildpack is the same as the default one but without any bindings. This is a temporary workaround until new bindings are supported by all buildpacks. 

### 