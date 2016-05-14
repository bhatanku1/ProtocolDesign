package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import pft.frames.ChecksumResponse;

public class ChecksumResponseMarshaller implements Marshaller<ChecksumResponse> {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int OFFSET_LENGTH = 4;
  private static final int LENGTH_LENGTH = 2;
  private static final int SHA1_LENGTH = 20;

  private static final int LENGTH = OFFSET_LENGTH + LENGTH_LENGTH + SHA1_LENGTH;

  @Override public ChecksumResponse decode(int identifier, byte[] data) {
    checkArgument(identifier != 0);
    checkNotNull(data);
    checkArgument(data.length == LENGTH);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeBytes(data);

      long offset = buffer.readUnsignedInt();
      int length = buffer.readUnsignedShort();

      byte[] sha1 = new byte[SHA1_LENGTH];
      buffer.readBytes(sha1);

      return new ChecksumResponse(identifier, offset, length, sha1);
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }

  @Override public byte[] encode(ChecksumResponse frame) {
    checkNotNull(frame);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);

      buffer.writeInt((int) frame.offset());
      buffer.writeShort(frame.length());
      buffer.writeBytes(frame.sha1());

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
