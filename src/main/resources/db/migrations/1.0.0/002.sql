CREATE TABLE IF NOT EXISTS compute.jobs (

  -- Hash ID of the job.
  -- This value is a raw MD5 hash of the job configuration.
  job_id BYTEA
    PRIMARY KEY
    NOT NULL,

  -- Current job status.
  status VARCHAR(11)
    NOT NULL,

  -- ID/Name of the queue the job was submitted to.
  queue VARCHAR(32)
    NOT NULL,

  -- Optional serialized configuration for this job.
  -- This field should not be used to hold input data for a job, just the
  -- configuration.  Input data should be persisted somewhere else, such as the
  -- filesystem or an object store.
  config TEXT,

  -- Array of the names of files that were included with the job.
  --
  -- If the job had no input files, this field will be an empty array.
  input_files VARCHAR[]
    NOT NULL,

  -- Timestamp for when the job was originally created (queued).
  --
  -- This should be the timestamp for when the job was submitted and should not
  -- ever change.
  created TIMESTAMP WITH TIME ZONE
    NOT NULL,

  -- Timestamp for when the job was last accessed.
  --
  -- This field must be manually updated for conditions where a job is
  -- considered as have been "accessed".
  --
  -- "Accessed" should not reflect every time the job was returned in a query
  -- result, but should instead reflect every time the job was specifically
  -- requested by ID.
  last_accessed TIMESTAMP WITH TIME ZONE
    NOT NULL,

  -- Timestamp for when the job was pulled from the queue to be run.
  --
  -- This field will be NULL unless the job has been pulled from the job queue.
  grabbed TIMESTAMP WITH TIME ZONE,

  -- Timestamp for when the job finished (successfully or not)
  --
  -- This field will be NULL unless the job has finished.
  finished TIMESTAMP WITH TIME ZONE,

  -- Array of the names of files that were created by the job.
  --
  -- This field will be NULL unless the job completed successfully.
  output_files VARCHAR[]

  CONSTRAINT compute_job_id_min_len
    CHECK ( length(job_id) = 16 ),
  CONSTRAINT compute_job_status_enum
    CHECK ( status IN ('queued', 'in-progress', 'complete', 'failed', 'expired') )
);
