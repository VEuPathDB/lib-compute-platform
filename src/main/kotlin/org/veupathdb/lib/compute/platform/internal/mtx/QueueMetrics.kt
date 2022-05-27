package org.veupathdb.lib.compute.platform.internal.mtx

import io.prometheus.client.Gauge
import io.prometheus.client.Histogram

internal object QueueMetrics {

  @JvmStatic
  val Time: Histogram = Histogram.build()
    .name("queue_time")
    .help("Time spent by jobs in the queue")
    .labelNames("queue")
    .buckets(
      0.1,     // 1/10 Second
      0.5,     // 1/2  Second
      1.0,     // 1    Second
      5.0,     // 5    Seconds
      10.0,    // 10   Seconds
      30.0,    // 30   Seconds
      60.0,    // 1    Minute
      180.0,   // 3    Minutes
      300.0,   // 5    Minutes
      600.0,   // 10   Minutes
      900.0,   // 15   Minutes
      1800.0,  // 30   Minutes
      3600.0,  // 1    Hour
      7200.0,  // 2    Hours
      18000.0, // 5    Hours
    )
    .register()

  @JvmStatic
  val Queued: Gauge = Gauge.build()
    .name("queued_jobs")
    .help("Currently queued jobs")
    .labelNames("queue")
    .register()
}