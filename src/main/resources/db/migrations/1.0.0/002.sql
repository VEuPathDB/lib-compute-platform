CREATE TABLE IF NOT EXISTS compute.jobs (
  -- Hash ID of the job.
  -- This value is a raw MD5 hash of the job configuration.
  job_id BYTEA(16)
    PRIMARY KEY
    NOT NULL,
  -- Current job status.
  status VARCHAR(8)
    NOT NULL,
  -- Name of the queue the job was submitted to.
  queue VARCHAR(16)
    NOT NULL,
  -- Serialized configuration for this job.
  -- This field should not be used to hold input data for a job, just the
  -- configuration.  Input data should be persisted somewhere else, such as the
  -- filesystem or an object store.
  config TEXT,
  -- Timestamp for when the job was originally created (queued).
  created TIMESTAMP WITH TIME ZONE
    NOT NULL,
  -- Timestamp for when the job was pulled from the queue to be run.
  last_accessed TIMESTAMP WITH TIME ZONE
    NOT NULL,
  grabbed TIMESTAMP WITH TIME ZONE
    NOT NULL,
  -- Timestamp for when the job finished (successfully or not)
  finished TIMESTAMP WITH TIME ZONE
    NOT NULL,

  CONSTRAINT compute_job_id_min_len
    CHECK ( length(job_id) = 16 ),
  CONSTRAINT compute_job_status_enum
    CHECK ( status IN ('queued', 'grabbed', 'complete', 'failed', 'expired') )
);
