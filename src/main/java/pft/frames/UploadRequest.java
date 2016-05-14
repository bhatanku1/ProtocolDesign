package pft.frames;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

public class UploadRequest extends Frame {

  String filename;
  long size;
  byte[] sha1;

  public UploadRequest(String filename, long size, byte[] sha1) {
    super(0);
    this.filename = checkNotNull(filename);
    checkArgument(filename.length() < 256);
    this.size = size;
    checkArgument(size > 0);
    this.sha1 = sha1;
  }

  @Override public byte type() {
    return 3;
  }

  public String filename() {
    return filename;
  }

  public long size() {
    return size;
  }

  public byte[] sha1() {
    return sha1;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof UploadRequest)) {
      return false;
    }

    UploadRequest that = (UploadRequest) other;

    return that.identifier() == identifier() &&
        that.filename.equals(filename)
        && that.size == size
        && Arrays.equals(that.sha1, sha1);
  }

  @Override
  public int hashCode() {
    return identifier() + 31 * filename.hashCode() + 31 * ((int) size) + 31 * Arrays.hashCode(sha1);
  }
}
