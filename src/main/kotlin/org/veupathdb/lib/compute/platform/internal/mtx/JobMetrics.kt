package org.veupathdb.lib.compute.platform.internal.mtx

import io.prometheus.client.Counter

object JobMetrics {

  @JvmStatic
  val Failures: Counter = Counter.build()
    .name("job_failures")
    .help("Counter of jobs that failed")
    .labelNames("queue")
    .register()

  @JvmStatic
  val Successes: Counter = Counter.build()
    .name("job_successes")
    .help("Counter of jobs that succeeded")
    .labelNames("queue")
    .register()

}