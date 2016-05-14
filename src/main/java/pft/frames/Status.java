package pft.frames;

public enum Status {
  OK((byte) 0),
  ERROR((byte) 1);

  private final byte code;

  Status (byte code) {
    this.code = code;
  }

  public byte code() {
    return code;
  }

  public static Status fromCode(byte code) {
    switch (code) {
      case 0:
        return Status.OK;

      case 1:
        return Status.ERROR;

      default:
        throw new IllegalArgumentException("Status code unknown: " + code);
    }
  }
}
