package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static pft.frames.marshallers.Utils.filenameToString;

import pft.frames.UploadRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.charset.Charset;

public class UploadRequestMarshaller implements Marshaller<UploadRequest> {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int FILENAME_LENGTH = 256;
  private static final int SIZE_LENGTH = 4;
  private static final int SHA1_LENGTH = 20;

  private static final int LENGTH = FILENAME_LENGTH + SHA1_LENGTH + SIZE_LENGTH;

  @Override public UploadRequest decode(int identifier, byte[] data) {
    checkArgument(identifier == 0);
    checkNotNull(data);
    checkArgument(data.length == LENGTH);

    ByteBuf buffer = null;

    try {
      buffer = ALLOCATOR.buffer(LENGTH);
      buffer.writeBytes(data);
      byte[] filenameBytes = new byte[FILENAME_LENGTH];
      buffer.readBytes(filenameBytes);
      String filename = filenameToString(filenameBytes);

      checkState(buffer.readerIndex() == FILENAME_LENGTH);

      long size = buffer.readUnsignedInt();

      checkState(buffer.readerIndex() == FILENAME_LENGTH + SIZE_LENGTH);

      byte[] sha1 = new byte[SHA1_LENGTH];
      buffer.readBytes(sha1);

      checkState(buffer.readerIndex() == LENGTH);

      return new UploadRequest(filename, size, sha1);
    } finally {
      if (buffer != null) {
        buffer.release();
      }
    }
  }

  @Override public byte[] encode(UploadRequest frame) {
    checkNotNull(frame);

    ByteBuf buffer = null;
    try {
      buffer = ALLOCATOR.buffer(LENGTH);

      byte[] filename = frame.filename().getBytes(Charset.forName("ASCII"));
      buffer.writeBytes(filename);
      // Zero-terminate string
      buffer.writeZero(FILENAME_LENGTH - filename.length);

      checkState(buffer.writerIndex() == FILENAME_LENGTH);

      buffer.writeInt((int) frame.size());

      checkState(buffer.writerIndex() == FILENAME_LENGTH + SIZE_LENGTH);

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
