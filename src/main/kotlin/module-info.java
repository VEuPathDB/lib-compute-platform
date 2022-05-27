module veupath.compute.platform {
  exports org.veupathdb.lib.compute.platform;

  requires org.slf4j;
  requires com.fasterxml.jackson.databind;
  requires kotlin.stdlib;
  requires kotlin.stdlib.jdk7;
  requires kotlin.stdlib.jdk8;
  requires org.postgresql.jdbc;
  requires com.zaxxer.hikari;

  requires hash.id;
  requires s34k;

}