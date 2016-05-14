package pft.frames.marshallers;


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import pft.frames.Status;
import pft.frames.TerminationRequest;

public class TerminationRequestMarshaller implements Marshaller<TerminationRequest> {
  @Override public TerminationRequest decode(int identifier, byte[] data) {
    checkArgument(identifier != 0);
    checkNotNull(data);
    checkArgument(data.length == 1);

    Status status = Status.fromCode(data[0]);
    return new TerminationRequest(identifier, status);
  }

  @Override public byte[] encode(TerminationRequest frame) {
    checkNotNull(frame);

    byte[] data = new byte[1];
    data[0] = frame.status().code();
    return data;
  }
}
