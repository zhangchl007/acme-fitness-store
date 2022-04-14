const express = require("express");
const app = express();

if (process.env.APPLICATIONINSIGHTS_CONNECTION_STRING) {
    console.log("APPLICATIONINSIGHTS_CONNECTION_STRING env variable detected, setting up Application Insights");
    let appInsights = require("applicationinsights");
    appInsights.setup().start();
} else {
    console.log("No APPLICATIONINSIGHTS_CONNECTION_STRING provided, skipping Application Insights connection");
}

app.use(express.static("public"));

app.listen(8080, () => {
    console.log("Server started on port 8080");
});