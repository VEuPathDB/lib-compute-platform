package org.veupathdb.lib.compute.platform.intern.metrics

import io.prometheus.client.Counter

internal object JobMetrics {

  /**
   * Counter of the number of jobs that have failed since last startup.
   */
  @JvmStatic
  val Failures: Counter = Counter.build()
    .name("job_failures")
    .help("Counter of jobs that failed")
    .labelNames("queue")
    .register()

  /**
   * Counter of the number of jobs that have been retried since last startup.
   */
  @JvmStatic
  val Retries: Counter = Counter.build()
    .name("job_retries")
    .help("Counter of jobs that failed")
    .labelNames("queue")
    .register()


  /**
   * Counter of the number of jobs that completed successfully since last
   * startup.
   */
  @JvmStatic
  val Successes: Counter = Counter.build()
    .name("job_successes")
    .help("Counter of jobs that succeeded")
    .labelNames("queue")
    .register()

}