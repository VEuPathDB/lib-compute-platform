module veupath.compute.platform {
  exports org.veupathdb.lib.compute.platform;
  exports org.veupathdb.lib.compute.platform.config;

  requires kotlin.stdlib;

  requires java.sql;

  requires org.slf4j;
  requires com.fasterxml.jackson.databind;
  requires org.postgresql.jdbc;
  requires com.zaxxer.hikari;
  requires simpleclient;

  requires hash.id;
  requires s34k;
  requires rabbit.job.queue;
  requires jackson.singleton;
  requires com.github.benmanes.caffeine;
  requires workspaces.java;

}
