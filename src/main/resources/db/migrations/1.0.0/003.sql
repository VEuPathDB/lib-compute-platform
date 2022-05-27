-- Extension table for appending arbitrary keys/values to job entries.
CREATE TABLE IF NOT EXISTS compute.jobs_ext (
  job_id BYTEA(16)
    NOT NULL
    REFERENCES compute.jobs (job_id),
  key VARCHAR(32)
    NOT NULL,
  value VARCHAR,

  CONSTRAINT compute_job_ext_id_key_uq
    UNIQUE (job_id, key)
);