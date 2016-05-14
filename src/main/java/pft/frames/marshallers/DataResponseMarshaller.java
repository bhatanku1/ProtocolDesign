package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import pft.frames.DataResponse;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class DataResponseMarshaller implements Marshaller<DataResponse> {
  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int OFFSET_LENGTH = 4;
  private static final int LENGTH_LENGTH = 2;

  private static final int LENGTH = OFFSET_LENGTH + LENGTH_LENGTH;

  @Override public DataResponse decode(int identifier, byte[] data) {
    checkArgument(identifier != 0);
    checkNotNull(data);
    // fixed length + variable payload
    checkArgument(data.length > LENGTH);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeBytes(data);

      long offset = buffer.readUnsignedInt();
      int length = buffer.readUnsignedShort();

      checkState(data.length == LENGTH + length);

      byte[] fileData = new byte[length];

      checkState(length == fileData.length);

      buffer.readBytes(fileData);

      return new DataResponse(identifier, offset, length, fileData);
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }

  @Override public byte[] encode(DataResponse frame) {
    checkNotNull(frame);

    ByteBuf buffer = null;
    try {
      int frameLength = LENGTH + (int) frame.length();
      buffer = ALLOCATOR.buffer(frameLength);

      buffer.writeInt((int) frame.offset());
      buffer.writeShort(frame.length());
      buffer.writeBytes(frame.data());

      byte[] data = new byte[frameLength];
      buffer.readBytes(data);

      return data;
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }
}
