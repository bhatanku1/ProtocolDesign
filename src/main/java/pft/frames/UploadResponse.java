package pft.frames;


import static com.google.common.base.Preconditions.checkNotNull;

public class UploadResponse extends Frame {

  Status status;
  int port;

  public UploadResponse(int identifier, Status status, int port) {
    super(identifier);
    this.status = checkNotNull(status);
    this.port = port;
  }

  @Override public byte type() {
    return 4;
  }

  public Status status() {
    return status;
  }

  public int port() {
    return port;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof UploadResponse)) {
      return false;
    }

    UploadResponse that = (UploadResponse) other;

    return that.identifier() == that.identifier() &&
        that.status == status &&
        that.port == port;
  }

  @Override
  public int hashCode() {
    return identifier() + 31 * status.code() + 31 * port;
  }
}
