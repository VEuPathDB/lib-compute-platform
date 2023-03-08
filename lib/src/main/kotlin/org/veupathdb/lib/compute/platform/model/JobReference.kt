package org.veupathdb.lib.compute.platform.model

import org.veupathdb.lib.hash_id.HashID

data class JobReference(val jobID: HashID, val owned: Boolean)
