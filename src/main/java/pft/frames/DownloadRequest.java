package pft.frames;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

public final class DownloadRequest extends Frame {

  private final String filename;
  private final byte[] sha1;

  public DownloadRequest(String filename, byte[] sha1) {
    super(0);
    this.filename = checkNotNull(filename);
    checkArgument(filename.length() < 256);
    this.sha1 = checkNotNull(sha1);
    checkArgument(sha1.length == 20);
  }

  @Override public byte type() {
    return 1;
  }

  public String filename() {
    return filename;
  }

  public byte[] sha1() {
    return sha1;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DownloadRequest)) {
      return false;
    }

    DownloadRequest that = (DownloadRequest) other;
    return identifier() == that.identifier() &&
        filename.equals(that.filename)
        && Arrays.equals(sha1, that.sha1);
  }

  @Override
  public int hashCode() {
    return identifier() + 31 * filename.hashCode() + 31 * Arrays.hashCode(sha1);
  }
}
