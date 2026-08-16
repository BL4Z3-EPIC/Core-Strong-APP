package com.pulsepoint.app.ui.vitality.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec
import com.patrykandpatrick.vico.compose.component.shape.shader.verticalGradient
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import com.pulsepoint.app.core.local.entity.HealthSnapshotEntity
import com.pulsepoint.app.core.util.DateFormatting
import com.pulsepoint.app.ui.vitality.MetricType
import com.pulsepoint.app.ui.vitality.valueFor
import java.time.LocalDate

@Composable
fun MetricChart(
    metric: MetricType,
    snapshots: List<HealthSnapshotEntity>,
    rangeDays: Int,
    modifier: Modifier = Modifier
) {
    val filtered = remember(snapshots, rangeDays) {
        val cutoff = LocalDate.now().minusDays(rangeDays.toLong()).toEpochDay()
        snapshots.filter { it.dateEpochDay >= cutoff }
    }

    if (filtered.size < 2) {
        return
    }

    val model = remember(metric, filtered) {
        entryModelOf(
            filtered.map {
                entryOf(it.dateEpochDay.toFloat(), it.valueFor(metric).toFloat())
            }
        )
    }

    Chart(
        chart = lineChart(
            lines = listOf(
                lineSpec(
                    lineColor = metric.color,
                    lineBackgroundShader = verticalGradient(
                        arrayOf(
                            metric.color.copy(alpha = 0.35f),
                            metric.color.copy(alpha = 0f)
                        )
                    )
                )
            )
        ),
        model = model,
        modifier = modifier,
        startAxis = rememberStartAxis(),
        bottomAxis = rememberBottomAxis(
            valueFormatter = AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
                DateFormatting.formatEpochDay(value.toLong())
            }
        )
    )
}
