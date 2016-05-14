package pft.frames;

import java.util.Arrays;

public class ChecksumResponse extends Frame {

  private final long offset;
  private final int length;
  private final byte[] sha1;

  public ChecksumResponse(int identifier, long offset, int length, byte[] sha1) {
    super(identifier);
    this.offset = offset;
    this.length = length;
    this.sha1 = sha1;
  }

  @Override public byte type() {
    return 8;
  }

  public long offset() {
    return offset;
  }

  public int length() {
    return length;
  }

  public byte[] sha1() {
    return sha1;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ChecksumResponse)) {
      return false;
    }

    ChecksumResponse that = (ChecksumResponse) other;

    return that.identifier() == identifier() &&
        that.offset == offset
        && that.length == length
        && Arrays.equals(that.sha1, sha1);
  }

  @Override
  public int hashCode() {
    return identifier() + 31 * Long.hashCode(offset) + 31 * Long.hashCode(length) + Arrays.hashCode(sha1);
  }
}
