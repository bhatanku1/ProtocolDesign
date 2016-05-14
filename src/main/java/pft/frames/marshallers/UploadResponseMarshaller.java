package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import pft.frames.Status;
import pft.frames.UploadResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class UploadResponseMarshaller implements Marshaller<UploadResponse> {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int STATUS_LENGTH = 1;
  private static final int PORT_LENGTH = 2;

  private static final int LENGTH = STATUS_LENGTH + PORT_LENGTH;

  @Override public UploadResponse decode(int identifier, byte[] data) {
    checkNotNull(data);
    checkArgument(identifier != 0);
    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeBytes(data);
      Status status = Status.fromCode(buffer.readByte());
      int port = buffer.readUnsignedShort();

      checkState(buffer.writerIndex() == LENGTH);

      return new UploadResponse(identifier, status, port);
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }

  @Override public byte[] encode(UploadResponse frame) {
    checkNotNull(frame);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeByte(frame.status().code());
      buffer.writeShort(frame.port());

      checkState(buffer.writerIndex() == LENGTH);

      byte[] data = new byte[LENGTH];
      buffer.readBytes(data);

      return data;
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }
}
