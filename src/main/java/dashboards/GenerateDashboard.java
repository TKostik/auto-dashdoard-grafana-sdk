package dashboards;

import com.grafana.foundation.dashboard.Dashboard;
import com.grafana.foundation.dashboard.DashboardBuilder;
import com.grafana.foundation.dashboard.DashboardDashboardTimeBuilder;
import com.grafana.foundation.dashboard.DataSourceRef;
import com.grafana.foundation.dashboard.GridPos;
import com.grafana.foundation.dashboard.QueryVariableBuilder;
import com.grafana.foundation.dashboard.StringOrMap;
import com.grafana.foundation.dashboard.VariableRefresh;
import com.grafana.foundation.dashboard.VariableSort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GenerateDashboard {
    public static void main(String[] args) throws IOException {
        DataSourceRef prom = new DataSourceRef();
        prom.type = "prometheus";
        prom.uid = "prometheus";
        
        DataSourceRef loki = new DataSourceRef();
        loki.type = "loki";
        loki.uid = "Loki";

        generateCpuUsageDashboard(prom, loki);
        generateCpuUsageByRegionDashboard(prom);
        
        System.out.println("Generated all dashboards successfully!");
    }

    /**
     * Generate the basic CPU Usage Dashboard (without region filtering)
     */
    private static void generateCpuUsageDashboard(DataSourceRef prom, DataSourceRef loki) throws IOException {
        DashboardBuilder db = new DashboardBuilder("CPU Usage Dashboard")
                .uid("cpu-usage")
                .tags(List.of("cpu", "demo"))
                .refresh("5s")
                .time(new DashboardDashboardTimeBuilder().from("now-15m").to("now"));

        GridPos gp1 = new GridPos(); gp1.h = 8; gp1.w = 8; gp1.x = 0; gp1.y = 0;
        GridPos gp2 = new GridPos(); gp2.h = 8; gp2.w = 8; gp2.x = 8; gp2.y = 0;
        GridPos gp3 = new GridPos(); gp3.h = 8; gp3.w = 8; gp3.x = 16; gp3.y = 0;
        GridPos gpTs = new GridPos(); gpTs.h = 8; gpTs.w = 24; gpTs.x = 0; gpTs.y = 8;
        GridPos gpBar = new GridPos(); gpBar.h = 8; gpBar.w = 24; gpBar.x = 0; gpBar.y = 16;
        GridPos gpState = new GridPos(); gpState.h = 8; gpState.w = 24; gpState.x = 0; gpState.y = 24;

        db = db.withPanel(PanelHelpers.createGaugePanel("CPU Usage (server1)", gp1, "server1", prom));
        db = db.withPanel(PanelHelpers.createGaugePanel("CPU Usage (server2)", gp2, "server2", prom));
        db = db.withPanel(PanelHelpers.createGaugePanel("CPU Usage (server3)", gp3, "server3", prom));

        db = db.withPanel(PanelHelpers.createTimeseriesPanel(
                "CPU Usage Over Time (All Servers)",
                gpTs,
                "avg(cpu_usage) by (instance)",
                "{{instance}}",
                prom
        ));

        db = db.withPanel(PanelHelpers.createBarchartPanel(
                "CPU Usage by Instance",
                gpBar,
                "avg(cpu_usage) by (instance)",
                "{{instance}}",
                prom
        ));

        db = db.withPanel(
                new com.grafana.foundation.statetimeline.PanelBuilder()
                        .title("Log Count Over Time by Level and Instance")
                        .gridPos(gpState)
                        .withTarget(new com.grafana.foundation.loki.DataqueryBuilder()
                                .expr("count_over_time({service_name=\"backend\"}[1m])")
                                .legendFormat("{{instance}} - {{detected_level}}")
                                .refId("A")
                                .datasource(loki))
        );

        Dashboard built = db.build();
        String json = built.toJSON();

        Path out = Path.of("json-dashboards/cpu_usage_dashboard.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, json);
        System.out.println("Wrote dashboard to " + out.toAbsolutePath());
    }

    /**
     * Generate the CPU Usage by Region Dashboard (with $region template variable)
     */
    private static void generateCpuUsageByRegionDashboard(DataSourceRef prom) throws IOException {
        DashboardBuilder db = new DashboardBuilder("CPU Usage by Region Dashboard")
                .uid("cpu-usage-by-region")
                .tags(List.of("cpu", "demo", "region"))
                .refresh("5s")
                .time(new DashboardDashboardTimeBuilder().from("now-15m").to("now"));

        db = db.withVariable(new QueryVariableBuilder("region")
                .label("Region")
                .refresh(VariableRefresh.ON_TIME_RANGE_CHANGED)
                .query(StringOrMap.createString("label_values(cpu_usage, region)"))
                .datasource(prom)
                .sort(VariableSort.ALPHABETICAL_ASC)
        );

        GridPos gp1 = new GridPos(); gp1.h = 8; gp1.w = 8; gp1.x = 0; gp1.y = 0;
        GridPos gp2 = new GridPos(); gp2.h = 8; gp2.w = 8; gp2.x = 8; gp2.y = 0;
        GridPos gp3 = new GridPos(); gp3.h = 8; gp3.w = 8; gp3.x = 16; gp3.y = 0;
        GridPos gpTs = new GridPos(); gpTs.h = 8; gpTs.w = 24; gpTs.x = 0; gpTs.y = 8;
        GridPos gpBar = new GridPos(); gpBar.h = 8; gpBar.w = 24; gpBar.x = 0; gpBar.y = 16;

        db = db.withPanel(PanelHelpers.createGaugePanelWithQuery(
                "CPU Usage (server1 - ${region})",
                gp1,
                "cpu_usage{instance=\"server1\", region=\"$region\"}",
                prom
        ));

        db = db.withPanel(PanelHelpers.createGaugePanelWithQuery(
                "CPU Usage (server2 - ${region})",
                gp2,
                "cpu_usage{instance=\"server2\", region=\"$region\"}",
                prom
        ));

        db = db.withPanel(PanelHelpers.createGaugePanelWithQuery(
                "CPU Usage (server3 - ${region})",
                gp3,
                "cpu_usage{instance=\"server3\", region=\"$region\"}",
                prom
        ));

        db = db.withPanel(PanelHelpers.createTimeseriesPanel(
                "CPU Usage Over Time (${region})",
                gpTs,
                "cpu_usage{region=\"$region\"}",
                "{{instance}}",
                prom
        ));

        db = db.withPanel(PanelHelpers.createBarchartPanel(
                "CPU Usage by Instance (${region})",
                gpBar,
                "cpu_usage{region=\"$region\"}",
                "{{instance}}",
                prom
        ));

        Dashboard built = db.build();
        String json = built.toJSON();

        Path out = Path.of("json-dashboards/cpu_usage_by_region_dashboard.json");
        Files.createDirectories(out.getParent());
        Files.writeString(out, json);
        System.out.println("Wrote dashboard to " + out.toAbsolutePath());
    }
}
