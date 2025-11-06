package org.veupathdb.lib.compute.platform.model

import org.veupathdb.lib.hash_id.HashID

/**
 * Reference to a job with a flag that indicates whether the current platform
 * instance owns the job.
 *
 * @since 1.4.0
 *
 * @constructor Constructs a new JobReference instance.
 *
 * @param jobID ID of the job this reference is referencing.
 *
 * @param owned Whether the current platform instance owns the job.
 */
@ConsistentCopyVisibility
data class JobReference internal constructor(val jobID: HashID, val owned: Boolean)
