module veupath.compute.platform {
  exports org.veupathdb.lib.compute.platform;
  exports org.veupathdb.lib.compute.platform.config;

  requires kotlin.stdlib;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;

  requires java.sql;

  requires org.slf4j;
  requires com.fasterxml.jackson.databind;
  requires org.postgresql.jdbc;
  requires com.zaxxer.hikari;
  requires simpleclient;

  requires hash.id;
  requires s34k;
  requires rabbit.job.queue;
  requires workspaces;
  requires jackson.singleton;

}