package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import pft.frames.DownloadResponse;
import pft.frames.Status;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class DownloadResponseMarshaller implements Marshaller<DownloadResponse> {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int STATUS_LENGTH = 1;
  private static final int PORT_LENGTH = 2;
  private static final int SIZE_LENGTH = 4;
  private static final int SHA1_LENGTH = 20;

  private static final int LENGTH =
      STATUS_LENGTH + PORT_LENGTH + SIZE_LENGTH + SHA1_LENGTH;


  @Override public DownloadResponse decode(int identifier, byte[] data) {
    checkArgument(identifier != 0);
    checkNotNull(data);
    checkArgument(data.length == LENGTH);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeBytes(data);

      Status status = Status.fromCode(buffer.readByte());
      int port = buffer.readUnsignedShort();
      long size = buffer.readUnsignedInt();

      byte[] sha1 = new byte[SHA1_LENGTH];
      buffer.readBytes(sha1);

      checkState(buffer.readerIndex() == LENGTH);

      return new DownloadResponse(identifier, status, port, size, sha1);
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }

  @Override public byte[] encode(DownloadResponse frame) {
    checkNotNull(frame);

    ByteBuf buffer = null;

    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeByte(frame.status().code());
      buffer.writeShort(frame.port());
      buffer.writeInt((int) frame.size());
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
