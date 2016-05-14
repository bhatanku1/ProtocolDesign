package pft.frames;

import static com.google.common.base.Preconditions.checkNotNull;

public class TerminationRequest extends Frame {

  private final Status status;

  public TerminationRequest(int identifier, Status status) {
    super(identifier);

    this.status = checkNotNull(status);
  }

  @Override public byte type() {
    return 9;
  }

  public Status status() {
    return status;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TerminationRequest)) {
      return false;
    }

    TerminationRequest that = (TerminationRequest) other;

    return that.identifier() == identifier() &&
        that.status() == status();
  }
}
