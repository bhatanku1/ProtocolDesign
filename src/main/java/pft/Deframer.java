package pft;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import pft.frames.Frame;
import pft.frames.marshallers.Marshaller;
import pft.frames.marshallers.Marshallers;

public class Deframer {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  public Frame deframe(byte[] data) {
    checkNotNull(data);
    checkArgument(data.length > 7);
    checkArgument(data.length < (2 << 16));

    ByteBuf buffer = null;

    try {
      buffer = ALLOCATOR.buffer(data.length);
      buffer.writeBytes(data);

      int length = buffer.readUnsignedShort();
      checkState(data.length == length + 7);

      byte type = buffer.readByte();
      int identifier = buffer.readInt();

      byte[] payload = new byte[length];
      buffer.readBytes(payload);

      switch (type) {
        case 1:
          return Marshallers.DOWNLOAD_REQUEST.decode(identifier, payload);

        case 2:
          return Marshallers.DOWNLOAD_RESPONSE.decode(identifier, payload);

        case 3:
          return Marshallers.UPLOAD_REQUEST.decode(identifier, payload);

        case 4:
          return Marshallers.UPLOAD_RESPONSE.decode(identifier, payload);

        case 5:
          return Marshallers.DATA_REQUEST.decode(identifier, payload);

        case 6:
          return Marshallers.DATA_RESPONSE.decode(identifier, payload);

        case 7:
          return Marshallers.CHECKSUM_REQUEST.decode(identifier, payload);

        case 8:
          return Marshallers.CHECKSUM_RESPONSE.decode(identifier, payload);

        case 9:
          return Marshallers.TERMINATION_REQUEST.decode(identifier, payload);

        default:
          throw new UnsupportedOperationException("The frame type: '" + type + "' is not supported.");
      }
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }
}
