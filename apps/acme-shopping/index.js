const express = require("express");
const {SecretClient} = require("@azure/keyvault-secrets");
const {DefaultAzureCredential} = require("@azure/identity");
const appInsights = require("applicationinsights");
const app = express();

if (process.env.APPLICATIONINSIGHTS_CONNECTION_STRING) {
    console.log("APPLICATIONINSIGHTS_CONNECTION_STRING env variable detected, setting up Application Insights");
    let appInsights = require("applicationinsights");
    appInsights.setup().start();
} else {
    console.log("No APPLICATIONINSIGHTS_CONNECTION_STRING provided, skipping Application Insights connection");
}

async function processSecrets() {
    if (process.env.KEYVAULT_URI) {
        console.log("Found Key Vault Connection Info, setting up connection to key vault ")
        const credential = new DefaultAzureCredential();
        const url = process.env.KEYVAULT_URI;
        const client = new SecretClient(url, credential);
        const appInsightsSecretKey = "ApplicationInsights--ConnectionString";

        let containsAppInsightsSecret = false;

        for await (let secretProperties of client.listPropertiesOfSecrets()) {
            if (secretProperties.name === appInsightsSecretKey) {
                containsAppInsightsSecret = true;
            }
        }

        if (!containsAppInsightsSecret) {
            console.log(`Secret key ${appInsightsSecretKey} not found, skipping App Insights setup`)
            return;
        }

        const appInsightsKey = (await client.getSecret(appInsightsSecretKey));

        if (appInsightsKey && appInsightsKey.value) {
            console.log("Found application insights secret, setting up Application Insights")
            appInsights.setup(appInsightsKey.value).start();
        }
    }
}

processSecrets().catch((error) => {
    console.error("An error occurred processing secrets:", error);
    process.exit(1);
});

app.use(express.static("public"));

app.listen(8080, () => {
    console.log("Server started on port 8080");
});