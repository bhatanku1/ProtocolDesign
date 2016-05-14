package pft.frames;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

public final class DataResponse extends Frame {

  private final long offset;
  private final int length;
  private final byte[] data;

  public DataResponse(int identifier, long offset, int length, byte[] data) {
    super(identifier);
    this.offset = offset;
    this.length = length;
    checkArgument(data.length == length);
    this.data = checkNotNull(data);
  }

  @Override public byte type() {
    return 6;
  }

  public long offset() {
    return offset;
  }

  public int length() {
    return length;
  }

  public byte[] data() {
    return data;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DataResponse)) {
      return false;
    }

    DataResponse that = (DataResponse) other;

    return that.identifier() == identifier() &&
        that.offset == offset
        && that.length == length
        && Arrays.equals(that.data, data);
  }

  @Override
  public int hashCode() {
    return identifier() + Long.hashCode(offset) + 31 * Long.hashCode(length) + Arrays.hashCode(data);
  }
}
