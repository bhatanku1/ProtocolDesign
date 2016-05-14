package pft.frames.marshallers;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static pft.frames.marshallers.Utils.filenameToString;

import pft.frames.DownloadRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

import java.nio.charset.Charset;

public class DownloadRequestMarshaller implements Marshaller<DownloadRequest> {

  private static final PooledByteBufAllocator ALLOCATOR =
      PooledByteBufAllocator.DEFAULT;

  private static final int FILENAME_LENGTH = 256;
  private static final int SHA1_LENGTH = 20;
  private static final int LENGTH = FILENAME_LENGTH + SHA1_LENGTH;

  @Override public DownloadRequest decode(int identifier, byte[] data) {
    checkArgument(identifier == 0);
    checkNotNull(data);
    checkArgument(data.length == LENGTH);

    String filename = filenameToString(data);
    byte[] sha1 = new byte[SHA1_LENGTH];
    System.arraycopy(data, FILENAME_LENGTH, sha1, 0, SHA1_LENGTH);

    return new DownloadRequest(filename, sha1);
  }

  @Override public byte[] encode(DownloadRequest frame) {
    checkNotNull(frame);
    ByteBuf buffer = null;

    try {
      buffer = ALLOCATOR.buffer(LENGTH);

      byte[] filename = frame.filename().getBytes(Charset.forName("ASCII"));
      buffer.writeBytes(filename);
      // Zero-terminate string
      buffer.writeZero(FILENAME_LENGTH - filename.length);

      checkState(buffer.writerIndex() == FILENAME_LENGTH);

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
