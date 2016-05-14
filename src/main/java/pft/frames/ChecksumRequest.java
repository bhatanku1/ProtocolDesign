package pft.frames;

public class ChecksumRequest extends Frame {

  private final long offset;
  private final long length;

  public ChecksumRequest(int identifier, long offset, long length) {
    super(identifier);
    this.offset = offset;
    this.length = length;
  }

  @Override public byte type() {
    return 7;
  }

  public long offset() {
    return offset;
  }

  public long length() {
    return length;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ChecksumRequest)) {
      return false;
    }

    ChecksumRequest that = (ChecksumRequest) other;

    return that.identifier() == identifier() &&
        that.offset == offset
        && that.length == length;
  }

  @Override
  public int hashCode() {
    return identifier() + ((int) offset) + 31 * ((int) length);
  }
}
