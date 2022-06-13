package org.veupathdb.lib.compute.platform.intern.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.StringReader

@DisplayName("ReaderInputStream")
internal class ReaderInputStreamTest {

  @Test
  @DisplayName("Multi-byte characters")
  fun t1() {
    val tgt = ReaderInputStream(StringReader("❦"))

    assertEquals(39, tgt.read())
    assertEquals(102, tgt.read())
    assertEquals(-1, tgt.read())
  }

  @Test
  @DisplayName("Strings longer than the set chunk size")
  fun t2() {
    val inp = "hello world, it is I, some code"
    val tgt = ReaderInputStream(StringReader(inp), 10)

    assertEquals(inp, String(tgt.readAllBytes()))
  }

  @Test
  @DisplayName("Empty string")
  fun t3() {
    val tgt = ReaderInputStream(StringReader(""))

    assertEquals(-1, tgt.read())
  }
}
