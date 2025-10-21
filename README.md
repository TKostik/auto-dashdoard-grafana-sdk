# Auto Dashboard with Grafana Foundation SDK

This project generates a Grafana dashboard JSON mirroring the provided CPU dashboard using the Grafana Foundation SDK for Java.

## Generate the dashboard

### With Gradle

```zsh
gradle build
gradle run
```

The JSON will be written to `json-dashboards/cpu_usage_dashboard.json`.

## Import into Grafana
- Grafana > Dashboards > New > Import
- Upload `cpu_usage_dashboard.json` or paste the JSON
