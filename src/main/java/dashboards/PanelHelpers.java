package dashboards;

import com.grafana.foundation.dashboard.DataSourceRef;
import com.grafana.foundation.dashboard.GridPos;
import com.grafana.foundation.dashboard.Threshold;
import com.grafana.foundation.dashboard.ThresholdsConfigBuilder;
import com.grafana.foundation.dashboard.ThresholdsMode;
import java.util.List;

public class PanelHelpers {
    public static com.grafana.foundation.gauge.PanelBuilder createGaugePanel(String title, GridPos pos, String instance, DataSourceRef prom) {
        return new com.grafana.foundation.gauge.PanelBuilder()
                .title(title)
                .gridPos(pos)
                .min(0.0)
                .max(100.0)
                .thresholds(new ThresholdsConfigBuilder()
                        .mode(ThresholdsMode.PERCENTAGE)
                        .steps(List.of(
                                new Threshold(null, "green"),
                                new Threshold(80.0, "yellow"),
                                new Threshold(90.0, "red")
                        ))
                )
                .withTarget(new com.grafana.foundation.prometheus.DataqueryBuilder()
                        .expr(String.format("avg(cpu_usage{instance=\"%s\"}) by (instance)", instance))
                        .refId("A")
                        .datasource(prom));
    }
}
