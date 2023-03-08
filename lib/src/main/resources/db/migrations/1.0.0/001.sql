CREATE TABLE IF NOT EXISTS compute.meta (
  key VARCHAR(32)
    PRIMARY KEY
    NOT NULL,
  value VARCHAR(256)
    NOT NULL,

  CONSTRAINT compute_meta_key_min_len
    CHECK ( length(key) > 2 )
);