
CREATE SCHEMA IF NOT EXISTS compute;

CREATE TABLE IF NOT EXISTS compute.meta (
  key VARCHAR(32)
    PRIMARY KEY
    NOT NULL,
  value VARCHAR(256)
    NOT NULL,

  CONSTRAINT compute_meta_key_min_len
    CHECK ( length(key) > 2 )
);

CREATE TABLE IF NOT EXISTS compute.jobs (
  job_id BYTEA(16)
    PRIMARY KEY
    NOT NULL,
  status VARCHAR(8)
    NOT NULL,
  config TEXT
    NOT NULL,
  created TIMESTAMP WITH TIME ZONE
    NOT NULL,
  grabbed TIMESTAMP WITH TIME ZONE
    NOT NULL,
  finished TIMESTAMP WITH TIME ZONE
    NOT NULL,

  CONSTRAINT compute_job_id_min_len
    CHECK ( length(job_id) = 16 ),
  CONSTRAINT compute_job_status_enum
    CHECK ( status IN ('queued', 'grabbed', 'complete', 'failed', 'expired') )
);

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