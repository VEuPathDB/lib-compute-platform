package org.veupathdb.lib.compute.platform.intern.util

import java.io.InputStream
import java.io.Reader

/**
 * [InputStream] wrapping and reading from a [Reader] instance.
 *
 * @constructor
 *
 * @param reader [Reader] this [InputStream] will wrap and read from.
 *
 * @param chunkSize Number of characters that will be read at a time from the
 * input [Reader].
 *
 * Defaults to `256`
 */
@OptIn(ExperimentalUnsignedTypes::class)
class ReaderInputStream(
  private val reader: Reader,
  private val chunkSize: Int = 256
) : InputStream() {
  /**
   * Reusable character buffer that will be loaded from the input reader then
   * converted to a byte array character by character, then read out of this
   * [InputStream]
   */
  private val readBuf = CharArray(chunkSize)

  /**
   * Number of characters read into the buffer from the input [Reader].
   */
  private var readBufSize = 0

  /**
   * Current character position.
   */
  private var readBufPos = 0

  /**
   * Reusable byte buffer representing the bytes read from a single character.
   *
   * Since Java/Kotlin use UTf-16 encoded characters, each character will be, at
   * most, 2 bytes.
   */
  private val charBuf = UByteArray(2)

  /**
   * Size of the byte buffer for the last read character.
   *
   * This value will either be 1 or 2 after a character read.
   */
  private var charBufSize = 0

  /**
   * Current byte position.
   */
  private var charBufPos = 0

  init {
    // Prepopulate the buffer.  This is required since we use a readBufSize of
    // 0 to indicate that the buffer is now empty.
    refillChunk()
  }

  override fun read(): Int {
    // If we have more bytes in the byte buffer, return the next one.
    if (charBufPos < charBufSize)
      return charBuf[charBufPos++].toInt()

    // If we have more characters in the character buffer, parse the next one
    // and return the first byte from the repopulated byte buffer.
    if (readBufPos < readBufSize) {
      readChar(readBuf[readBufPos++])
      return charBuf[charBufPos++].toInt()
    }

    // If the character buffer has a length of zero, we have reached the end of
    // the reader input and there is nothing more to return.
    if (readBufSize == 0)
      return -1

    // So the byte buffer is empty, the character buffer is empty, and there are
    // potentially more characters to read from the reader.
    // Attempt to read more characters and call this function again.  On the
    // next pass, if there were no more characters, we will bail with a -1
    refillChunk()
    return read()
  }

  /**
   * Reads the next chunk of text into the [readBuf] array.
   */
  private fun refillChunk() {
    // Read more characters from the reader.
    val red = reader.read(readBuf)

    // If the reader returned a -1 then we reached the end on the last read.
    // Set the read buffer size to 0 to indicate that there is nothing more to
    // read.
    if (red == -1) {
      readBufSize = 0
      readBufPos = 0
    }

    // The reader returned a value greater than -1.
    // This means we have at least one character we can read out.
    else {
      readBufSize = red
      readBufPos  = 0
    }
  }

  private fun readChar(c: Char) {
    if (c.code in 1 .. 254) {
      charBuf[0] = c.code.toUByte()
      charBufSize = 1
    } else {
      val one = c.code shr 8
      val two = c.code and 255

      charBuf[0] = one.toUByte()
      charBuf[1] = two.toUByte()
      charBufSize = 2
    }

    charBufPos = 0
  }
}