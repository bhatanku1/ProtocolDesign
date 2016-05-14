package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import pft.frames.ChecksumRequest;
import pft.frames.DataRequest;

public class ChecksumRequestMarshaller implements Marshaller<ChecksumRequest> {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int OFFSET_LENGTH = 4;
  private static final int LENGTH_LENGTH = 4;

  private static final int LENGTH = OFFSET_LENGTH + LENGTH_LENGTH;

  @Override public ChecksumRequest decode(int identifier, byte[] data) {
    checkArgument(identifier != 0);
    checkNotNull(data);
    checkArgument(data.length == LENGTH);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeBytes(data);

      long offset = buffer.readUnsignedInt();
      long length = buffer.readUnsignedInt();

      return new ChecksumRequest(identifier, offset, length);
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }

  @Override public byte[] encode(ChecksumRequest frame) {
    checkNotNull(frame);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);

      buffer.writeInt((int) frame.offset());
      buffer.writeInt((int) frame.length());

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
