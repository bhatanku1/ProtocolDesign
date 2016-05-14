package pft.frames;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;

public final class DownloadResponse extends Frame {


  private final Status status;
  private final int port;
  private final long size;
  private final byte[] sha1;

  public DownloadResponse(int identifier, Status status, int port, long size, byte[] sha1) {
    super(identifier);
    checkArgument(size > 0);
    this.status = checkNotNull(status);
    this.port = port;
    this.size = size;
    this.sha1 = sha1;
  }

  @Override public byte type() {
    return 2;
  }

  public Status status() {
    return status;
  }

  public int port() {
    return port;
  }

  public long size() {
    return size;
  }

  public byte[] sha1() {
    return sha1;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof DownloadResponse)) {
      return false;
    }

    DownloadResponse that = (DownloadResponse) other;

    return that.identifier() == identifier() &&
        that.status == status
        && that.port == port
        && that.size == size
        && Arrays.equals(that.sha1, sha1);
  }

  @Override
  public int hashCode() {
    return identifier() + 31 * status.hashCode() + 31 * port + 31 * ((int) size) + 31 * Arrays.hashCode(sha1);
  }
}
